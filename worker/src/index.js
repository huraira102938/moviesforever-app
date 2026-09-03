/**
 * MoviesForever backend Worker.
 *
 * Endpoints (base = https://moviesforever.workers.dev):
 *   POST /redeem       { id, username }  -> burn a redemption code, create/update the user
 *   POST /signed-url   { movieId, id, username } -> future: return signed streaming URL
 *   GET  /health                          -> "ok"
 *
 * Uses Firebase Firestore REST API with a service-account JWT signed via WebCrypto.
 *
 * Required Worker secrets:
 *   FIREBASE_PROJECT_ID        e.g. moviesforever-da21d
 *   FIREBASE_SERVICE_ACCOUNT   full service-account JSON (client_email + private_key)
 */

const FIRESTORE_BASE = "https://firestore.googleapis.com/v1";
const TOKEN_URL = "https://oauth2.googleapis.com/token";

let cachedToken = null;
let cachedTokenExpiry = 0;

function b64urlFromBuffer(buf) {
  let bin = "";
  const bytes = new Uint8Array(buf);
  for (let i = 0; i < bytes.byteLength; i++) bin += String.fromCharCode(bytes[i]);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

async function base64UrlDecode(str) {
  const b64 = str.replace(/-/g, "+").replace(/_/g, "/");
  const bin = atob(b64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes;
}

async function signRsaSha256(data, privateKeyPem) {
  const pem = privateKeyPem
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s+/g, "");
  const binaryDer = await base64UrlDecode(pem);
  const key = await crypto.subtle.importKey(
    "pkcs8",
    binaryDer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, data);
  return b64urlFromBuffer(signature);
}

async function getAccessToken() {
  const now = Math.floor(Date.now() / 1000);
  if (cachedToken && now < cachedTokenExpiry) return cachedToken;

  const sa = JSON.parse(FIREBASE_SERVICE_ACCOUNT);
  const projectId = FIREBASE_PROJECT_ID;
  const iat = now;
  const exp = now + 3600;
  const header = { alg: "RS256", typ: "JWT" };
  const claim = {
    iss: sa.client_email,
    scope: "https://www.googleapis.com/auth/datastore",
    aud: TOKEN_URL,
    iat,
    exp,
  };
  const signingInput = b64urlFromBuffer(new TextEncoder().encode(JSON.stringify(header))) +
    "." + b64urlFromBuffer(new TextEncoder().encode(JSON.stringify(claim)));
  const signature = await signRsaSha256(
    new TextEncoder().encode(signingInput),
    sa.private_key
  );
  const jwt = `${signingInput}.${signature}`;

  const res = await fetch(TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });
  if (!res.ok) {
    throw new Error("token exchange failed: " + (await res.text()));
  }
  const json = await res.json();
  cachedToken = json.access_token;
  cachedTokenExpiry = now + (json.expires_in || 3600) - 300;
  return cachedToken;
}

async function firestoreRequest(method, resourcePath, body) {
  const token = await getAccessToken();
  const url = `${FIRESTORE_BASE}/projects/${FIREBASE_PROJECT_ID}/databases/(default)/documents/${resourcePath}`;
  const res = await fetch(url, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json = null;
  try {
    json = text ? JSON.parse(text) : null;
  } catch (_) {}
  if (!res.ok) {
    throw new Error(`Firestore ${method} ${resourcePath} -> ${res.status}: ${text}`);
  }
  return json;
}

function fieldsToObject(fields) {
  const out = {};
  if (!fields) return out;
  for (const [key, value] of Object.entries(fields)) {
    if ("stringValue" in value) out[key] = value.stringValue;
    else if ("integerValue" in value) out[key] = parseInt(value.integerValue, 10);
    else if ("doubleValue" in value) out[key] = parseFloat(value.doubleValue);
    else if ("booleanValue" in value) out[key] = value.booleanValue;
    else if ("timestampValue" in value) out[key] = value.timestampValue;
    else if ("nullValue" in value) out[key] = null;
    else if ("arrayValue" in value) out[key] = (value.arrayValue.values || []).map(fieldsToObject);
    else if ("mapValue" in value) out[key] = fieldsToObject(value.mapValue.fields);
  }
  return out;
}

function objectToFields(obj) {
  const fields = {};
  for (const [key, value] of Object.entries(obj)) {
    if (value === null || value === undefined) {
      fields[key] = { nullValue: null };
    } else if (typeof value === "boolean") {
      fields[key] = { booleanValue: value };
    } else if (typeof value === "number") {
      fields[key] = Number.isInteger(value)
        ? { integerValue: String(value) }
        : { doubleValue: value };
    } else {
      fields[key] = { stringValue: String(value) };
    }
  }
  return fields;
}

async function getDocument(collection, docId) {
  return firestoreRequest("GET", `${collection}/${encodeURIComponent(docId)}`);
}

async function updateDocument(collection, docId, updateMaskFields, object) {
  const mask = updateMaskFields
    .map((f) => `updateMask.fieldPaths=${encodeURIComponent(f)}`)
    .join("&");
  const fields = objectToFields(object);
  return firestoreRequest(
    "PATCH",
    `${collection}/${encodeURIComponent(docId)}?${mask}`,
    { fields }
  );
}

async function createDocument(collection, docId, object) {
  return firestoreRequest(
    "POST",
    `${collection}?documentId=${encodeURIComponent(docId)}`,
    { fields: objectToFields(object) }
  );
}

function jsonResponse(code, obj) {
  return new Response(JSON.stringify(obj), {
    status: code,
    headers: { "Content-Type": "application/json" },
  });
}

async function handleRedeem(request) {
  let body;
  try {
    body = await request.json();
  } catch (_) {
    return jsonResponse(400, { success: false, message: "Invalid JSON body." });
  }
  const id = (body.id || "").trim();
  const username = (body.username || "").trim();

  if (!id || !username) {
    return jsonResponse(400, {
      success: false,
      message: "Please enter both your Code ID and Username.",
    });
  }

  let doc;
  try {
    doc = await getDocument("codes", id);
    console.error("DEBUG redeem doc", id, JSON.stringify(doc));
  } catch (e) {
    console.error("DEBUG redeem getDocument error", id, String(e));
    return jsonResponse(200, { success: false, message: "This code is invalid." });
  }
  if (!doc || !doc.fields) {
    return jsonResponse(200, { success: false, message: "This code is invalid." });
  }

  const code = fieldsToObject(doc.fields);
  if (code.status === "used") {
    return jsonResponse(200, { success: false, message: "This code is already used." });
  }
  if (String(code.username) !== username) {
    return jsonResponse(200, {
      success: false,
      message: "The username does not match this code.",
    });
  }

  // Atomically burn the code
  try {
    await updateDocument(
      "codes",
      id,
      ["status", "usedAt"],
      { status: "used", usedAt: new Date().toISOString() }
    );
  } catch (e) {
    return jsonResponse(500, { success: false, message: "Server error. Please try again." });
  }

  // Upsert the user record so the username maps back to this code
  try {
    const existing = await getDocument("users", id).catch(() => null);
    const now = new Date().toISOString();
    if (existing && existing.fields) {
      const user = fieldsToObject(existing.fields);
      await updateDocument(
        "users",
        id,
        ["lastUnlockedAt"],
        { lastUnlockedAt: now }
      );
    } else {
      await createDocument("users", id, {
        id,
        username,
        referralCount: 0,
        createdAt: now,
      });
    }
  } catch (_) {
    // Non-fatal: the code is already burned and unlock is valid.
  }

  return jsonResponse(200, {
    success: true,
    message: "Unlocked! Enjoy lifetime access.",
    username,
  });
}

async function handleSignedUrl(request) {
  let body;
  try {
    body = await request.json();
  } catch (_) {
    return jsonResponse(400, { success: false, message: "Invalid JSON body." });
  }
  const { movieId } = body;
  if (!movieId) {
    return jsonResponse(400, { success: false, message: "movieId is required." });
  }

  let movie;
  try {
    const doc = await getDocument("movies", movieId);
    movie = doc && doc.fields ? fieldsToObject(doc.fields) : null;
  } catch (_) {
    movie = null;
  }

  if (!movie) {
    return jsonResponse(200, { success: false, message: "Movie not found." });
  }

  // For now videoUrl is a public R2 URL; later protect paid content here.
  const url = movie.videoUrl || movie.thumbnailUrl || null;
  return jsonResponse(200, {
    url,
    allowed: Boolean(url),
    message: url ? "OK" : "This movie has no playable source.",
  });
}

export default {
  async fetch(request, env, ctx) {
    // Expose secrets to global constants for simpler code in this file.
    globalThis.FIREBASE_PROJECT_ID = env.FIREBASE_PROJECT_ID;
    globalThis.FIREBASE_SERVICE_ACCOUNT = env.FIREBASE_SERVICE_ACCOUNT;

    const url = new URL(request.url);
    const path = url.pathname;

    if (request.method === "GET" && path === "/health") {
      return new Response("ok", { status: 200 });
    }

    if (request.method === "POST" && path === "/redeem") {
      return handleRedeem(request);
    }

    if (request.method === "POST" && path === "/signed-url") {
      return handleSignedUrl(request);
    }

    return jsonResponse(404, { success: false, message: "Not found." });
  },
};
