/**
 * MoviesForever backend Worker.
 *
 * Endpoints (base = https://moviesforever.workers.dev):
 *   GET  /banners     -> all banners, sorted by order (public, for landing page)
 *   GET  /trending    -> trending movies joined with movie details (public)
 *   GET  /app-link    -> newest APK share link from app-sharing (public)
 *   GET  /stats       -> { movieCount, installCount } (public)
 *   GET  /contact     -> { whatsappNumber, groupTitle, groupLink } (public)
 *   POST /redeem      { id, username }  -> burn a redemption code, create/update the user
 *   POST /signed-url  { movieId }       -> return public streaming URL for a movie
 *   GET  /health      -> "ok"
 *
 * Scheduled (cron, every minute -- see wrangler.toml):
 *   Sends push notifications for any `notifications` doc with pushStatus == "pending"
 *   (written by the admin panel) through FCM topics, then marks it "sent".
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
    scope: "https://www.googleapis.com/auth/datastore https://www.googleapis.com/auth/firebase.messaging",
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

function singleValue(value) {
  if (!value) return undefined;
  if ("stringValue" in value) return value.stringValue;
  if ("integerValue" in value) return parseInt(value.integerValue, 10);
  if ("doubleValue" in value) return parseFloat(value.doubleValue);
  if ("booleanValue" in value) return value.booleanValue;
  if ("timestampValue" in value) return value.timestampValue;
  if ("nullValue" in value) return null;
  if ("arrayValue" in value) return (value.arrayValue.values || []).map(singleValue);
  if ("mapValue" in value) return fieldsToObject(value.mapValue.fields);
  return undefined;
}

function fieldsToObject(fields) {
  const out = {};
  if (!fields) return out;
  for (const [key, value] of Object.entries(fields)) {
    out[key] = singleValue(value);
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

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
};

function jsonResponse(code, obj) {
  return new Response(JSON.stringify(obj), {
    status: code,
    headers: { "Content-Type": "application/json", ...corsHeaders },
  });
}

async function firestoreQuery(queryBody) {
  const token = await getAccessToken();
  const url = `${FIRESTORE_BASE}/projects/${FIREBASE_PROJECT_ID}/databases/(default)/documents:runQuery`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(queryBody),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Firestore runQuery -> ${res.status}: ${text}`);
  }
  return JSON.parse(text);
}

async function firestoreAggregationQuery(queryBody) {
  const token = await getAccessToken();
  const url = `${FIRESTORE_BASE}/projects/${FIREBASE_PROJECT_ID}/databases/(default)/documents:runAggregationQuery`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(queryBody),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Firestore runAggregationQuery -> ${res.status}: ${text}`);
  }
  return JSON.parse(text);
}

function documentIdFromName(name) {
  return name.split("/").pop();
}

async function listCollection(collectionId, orderByField, direction = "ASCENDING") {
  const structuredQuery = { from: [{ collectionId }] };
  if (orderByField) {
    structuredQuery.orderBy = [
      { field: { fieldPath: orderByField }, direction },
    ];
  }
  const results = await firestoreQuery({ structuredQuery });
  return (results || [])
    .filter((r) => r.document)
    .map((r) => ({
      id: documentIdFromName(r.document.name),
      ...fieldsToObject(r.document.fields),
    }));
}

// Firestore REST batchGet is capped at 10 documents per call, so chunk.
async function batchGetDocuments(collectionId, ids) {
  const token = await getAccessToken();
  const out = [];
  for (let i = 0; i < ids.length; i += 10) {
    const chunk = ids.slice(i, i + 10);
const documents = chunk.map((id) =>
    `projects/${FIREBASE_PROJECT_ID}/databases/(default)/documents/${collectionId}/${encodeURIComponent(id)}`
  );
    const res = await fetch(
      `${FIRESTORE_BASE}/projects/${FIREBASE_PROJECT_ID}/databases/(default)/documents:batchGet`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ documents }),
      }
    );
    const text = await res.text();
    if (!res.ok) {
      throw new Error(`Firestore batchGet -> ${res.status}: ${text}`);
    }
    const results = JSON.parse(text);
    for (const result of results || []) {
      if (result.found) {
        out.push({
          id: documentIdFromName(result.found.name),
          ...fieldsToObject(result.found.fields),
        });
      }
    }
  }
  return out;
}

async function handleBanners() {
  const banners = await listCollection("banners", "order");
  return jsonResponse(200, banners);
}

async function handleTrending() {
  const trending = await listCollection("trending", "order");
  const movieIds = trending.map((t) => t.movieId).filter(Boolean);
  const movies = movieIds.length
    ? await batchGetDocuments("movies", movieIds)
    : [];
  const byId = new Map(movies.map((m) => [m.id, m]));

  const items = trending
    .filter((t) => t.movieId && byId.has(t.movieId))
    .filter((t) => {
      const movie = byId.get(t.movieId);
      return movie && !movie.paused && movie.thumbnailUrl;
    })
    .map((t) => {
      const movie = byId.get(t.movieId);
      return {
        id: t.movieId,
        order: t.order,
        title: movie.title || "Untitled",
        thumbnailUrl: movie.thumbnailUrl || null,
        year: movie.year || null,
        badge: movie.badge || null,
        isFree: Boolean(movie.isFree),
        imdbRating: movie.imdbRating || null,
      };
    });

  return jsonResponse(200, items);
}

async function handleAppLink() {
  const links = await listCollection("app-sharing", "createdAt", "DESCENDING");
  const latest = links[0] || null;
  // Landing page prefers an uploaded APK file (direct download) over the
  // Google Drive link the mobile app uses, so look for the most recent doc
  // that actually has a file.
  const latestApk = links.find((l) => l.apkFileUrl) || null;
  const source = latestApk || latest;
  return jsonResponse(200, source
    ? {
        apkUrl: (latestApk ? latestApk.apkFileUrl : source.apkUrl) || null,
        title: source.title || "MoviesForever",
        version: source.version || null,
        apkFileName: latestApk ? latestApk.apkFileName || null : null,
        apkFileSize: latestApk ? latestApk.apkFileSize || null : null,
      }
    : { apkUrl: null, title: "MoviesForever", version: null });
}

async function handleContact() {
  try {
    const doc = await getDocument("settings", "contact");
    const contact = doc && doc.fields ? fieldsToObject(doc.fields) : {};
    return jsonResponse(200, {
      whatsappNumber: contact.whatsappNumber || null,
      groupTitle: contact.groupTitle || null,
      groupLink: contact.groupLink || null,
    });
  } catch (_) {
    return jsonResponse(200, {
      whatsappNumber: null,
      groupTitle: null,
      groupLink: null,
    });
  }
}

async function handleStats() {
  let movieCount = 0;
  let installCount = 0;

  try {
    const agg = await firestoreAggregationQuery({
      structuredAggregationQuery: {
        structuredQuery: { from: [{ collectionId: "movies" }] },
        aggregations: [{ alias: "count", count: {} }],
      },
    });
    const aggResult = (agg || []).find((r) => r.result)?.result || {};
    const countVal = aggResult.aggregateFields?.count;
    movieCount = countVal ? parseInt(countVal.integerValue || countVal.doubleValue || "0", 10) : 0;
  } catch (_) {}

  try {
    const doc = await getDocument("installs", "counter");
    const counter = doc && doc.fields ? fieldsToObject(doc.fields) : {};
    installCount = typeof counter.installCount === "number"
      ? counter.installCount
      : parseInt(counter.installCount || "0", 10);
  } catch (_) {}

  return jsonResponse(200, { movieCount, installCount });
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

  if (id.length > 100 || username.length > 100) {
    return jsonResponse(200, { success: false, message: "This code is invalid." });
  }

  if (!id || !username) {
    return jsonResponse(400, {
      success: false,
      message: "Please enter both your Code ID and Username.",
    });
  }

  let doc;
  try {
    doc = await getDocument("codes", id);
  } catch (e) {
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

  // Atomically burn the code. The write only succeeds if the doc has not changed since we
  // read it (updateTime precondition), so two simultaneous requests can never both redeem it.
  try {
    await firestoreRequest(
      "PATCH",
      `codes/${encodeURIComponent(id)}` +
        `?updateMask.fieldPaths=status&updateMask.fieldPaths=usedAt` +
        `&currentDocument.updateTime=${encodeURIComponent(doc.updateTime)}`,
      { fields: objectToFields({ status: "used", usedAt: new Date().toISOString() }) }
    );
  } catch (e) {
    const msg = String(e && e.message ? e.message : e);
    if (msg.includes("FAILED_PRECONDITION") || msg.includes("-> 409") || msg.includes("-> 400")) {
      return jsonResponse(200, { success: false, message: "This code is already used." });
    }
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

// ---------------------------------------------------------------------------
// Push notifications (FCM)
// ---------------------------------------------------------------------------

// Devices subscribe to exactly one of these topics (see PushTopics.kt in the app).
const TOPIC_PREFIX = "mf_";
const VALID_TARGETS = ["free", "paid", "paused"];
const PUSH_CHANNEL_ID = "moviesforever_push"; // must match PushNotifier.CHANNEL_ID
const PUSH_MAX_AGE_MS = 6 * 60 * 60 * 1000;   // don't blast a stale notification late
const PUSH_MAX_ATTEMPTS = 3;
const PUSH_BATCH = 5;                          // docs per cron run (keeps us under subrequest limits)

function pushTarget(targets) {
  const valid = VALID_TARGETS.filter((t) => (targets || []).includes(t));
  if (valid.length === 0) return null;
  // One topic -> `topic`. Several -> a condition, so ONE request reaches all groups.
  if (valid.length === 1) return { topic: TOPIC_PREFIX + valid[0] };
  return {
    condition: valid.map((t) => `'${TOPIC_PREFIX}${t}' in topics`).join(" || "),
  };
}

async function sendFcm(notificationId, text, targets) {
  const target = pushTarget(targets);
  if (!target) return { ok: false, retry: false, error: "no valid targets" };

  const body = text.length > 200 ? text.slice(0, 197) + "..." : text;
  const token = await getAccessToken();
  const res = await fetch(
    `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          ...target,
          notification: { title: "MoviesForever", body },
          data: { notificationId: String(notificationId) },
          android: {
            priority: "HIGH",
            ttl: "86400s", // drop it if a device is offline for more than a day
            notification: { channel_id: PUSH_CHANNEL_ID },
          },
        },
      }),
    }
  );
  const resText = await res.text();
  if (res.ok) {
    let name = "";
    try { name = JSON.parse(resText).name || ""; } catch (_) {}
    return { ok: true, name };
  }
  return {
    ok: false,
    retry: res.status === 429 || res.status >= 500,
    error: `FCM ${res.status}: ${resText}`.slice(0, 500),
  };
}

async function processPendingNotifications() {
  const rows = await firestoreQuery({
    structuredQuery: {
      from: [{ collectionId: "notifications" }],
      where: {
        fieldFilter: {
          field: { fieldPath: "pushStatus" },
          op: "EQUAL",
          value: { stringValue: "pending" },
        },
      },
      limit: PUSH_BATCH,
    },
  });

  for (const row of rows || []) {
    if (!row.document) continue;
    const docId = documentIdFromName(row.document.name);
    const n = fieldsToObject(row.document.fields);

    // Too old (e.g. the cron was down for hours): skip the push, the in-app feed still has it.
    const age = Date.now() - new Date(n.createdAt || 0).getTime();
    if (age > PUSH_MAX_AGE_MS) {
      await updateDocument("notifications", docId, ["pushStatus"], { pushStatus: "expired" });
      continue;
    }

    // Claim the doc so two overlapping runs can never send the same notification twice.
    try {
      await firestoreRequest(
        "PATCH",
        `notifications/${encodeURIComponent(docId)}` +
          `?updateMask.fieldPaths=pushStatus` +
          `&currentDocument.updateTime=${encodeURIComponent(row.document.updateTime)}`,
        { fields: objectToFields({ pushStatus: "sending" }) }
      );
    } catch (_) {
      continue; // someone else got it
    }

    const attempts = (n.pushAttempts || 0) + 1;
    try {
      const r = await sendFcm(docId, String(n.text || ""), n.targets);
      if (r.ok) {
        await updateDocument(
          "notifications", docId,
          ["pushStatus", "pushedAt", "pushMessageName", "pushAttempts"],
          { pushStatus: "sent", pushedAt: new Date().toISOString(), pushMessageName: r.name, pushAttempts: attempts }
        );
      } else {
        const giveUp = !r.retry || attempts >= PUSH_MAX_ATTEMPTS;
        await updateDocument(
          "notifications", docId,
          ["pushStatus", "pushError", "pushAttempts"],
          { pushStatus: giveUp ? "failed" : "pending", pushError: r.error, pushAttempts: attempts }
        );
      }
    } catch (e) {
      await updateDocument(
        "notifications", docId,
        ["pushStatus", "pushError", "pushAttempts"],
        {
          pushStatus: attempts >= PUSH_MAX_ATTEMPTS ? "failed" : "pending",
          pushError: String(e.message || e).slice(0, 500),
          pushAttempts: attempts,
        }
      ).catch(() => {});
    }
  }
}

export default {
  async fetch(request, env, ctx) {
    // Expose secrets to global constants for simpler code in this file.
    globalThis.FIREBASE_PROJECT_ID = env.FIREBASE_PROJECT_ID;
    globalThis.FIREBASE_SERVICE_ACCOUNT = env.FIREBASE_SERVICE_ACCOUNT;

    const url = new URL(request.url);
    const path = url.pathname;

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders });
    }

    if (request.method === "GET" && path === "/health") {
      return new Response("ok", { status: 200 });
    }

    if (request.method === "GET" && path === "/banners") {
      try {
        return await handleBanners();
      } catch (e) {
        return jsonResponse(500, { success: false, message: String(e.message || e) });
      }
    }

    if (request.method === "GET" && path === "/trending") {
      try {
        return await handleTrending();
      } catch (e) {
        return jsonResponse(500, { success: false, message: String(e.message || e) });
      }
    }

    if (request.method === "GET" && path === "/app-link") {
      try {
        return await handleAppLink();
      } catch (e) {
        return jsonResponse(500, { success: false, message: String(e.message || e) });
      }
    }

    if (request.method === "GET" && path === "/stats") {
      try {
        return await handleStats();
      } catch (e) {
        return jsonResponse(500, { success: false, message: String(e.message || e) });
      }
    }

    if (request.method === "GET" && path === "/contact") {
      try {
        return await handleContact();
      } catch (e) {
        return jsonResponse(500, { success: false, message: String(e.message || e) });
      }
    }

    if (request.method === "POST" && path === "/redeem") {
      return handleRedeem(request);
    }

    if (request.method === "POST" && path === "/signed-url") {
      return handleSignedUrl(request);
    }

    return jsonResponse(404, { success: false, message: "Not found." });
  },

  // Cron trigger (every minute): deliver any notifications the admin has queued.
  async scheduled(event, env, ctx) {
    globalThis.FIREBASE_PROJECT_ID = env.FIREBASE_PROJECT_ID;
    globalThis.FIREBASE_SERVICE_ACCOUNT = env.FIREBASE_SERVICE_ACCOUNT;
    ctx.waitUntil(
      processPendingNotifications().catch((e) =>
        console.error("push cron failed", String(e && e.message ? e.message : e))
      )
    );
  },
};
