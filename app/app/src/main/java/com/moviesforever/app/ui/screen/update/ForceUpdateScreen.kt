package com.moviesforever.app.ui.screen.update

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.AppUpdateConfig
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.components.GoldOutlinedButton
import com.moviesforever.app.ui.theme.*

private const val DEFAULT_TITLE = "ایپ کا نیا ورژن ڈاؤن لوڈ کریں"
private const val DEFAULT_MESSAGE =
    "آپ کی ایپ کا یہ ورژن پرانا ہے اور اب کام نہیں کرے گا۔ براہ کرم نیا ورژن ڈاؤن لوڈ کریں یا ہم سے رابطہ کریں۔"

/**
 * Full-screen block shown to builds that are older than the minimum allowed version
 * (set in the admin panel > Force Update). There is no way past it: the back button closes the app.
 * Title, message, contact number and download link all come from the admin panel.
 */
@Composable
fun ForceUpdateScreen(config: AppUpdateConfig, currentVersionCode: Long) {
    val context = LocalContext.current
    BackHandler(enabled = true) { context.findActivity()?.finish() }

    val title = config.title.ifBlank { DEFAULT_TITLE }
    val message = config.message.ifBlank { DEFAULT_MESSAGE }
    val number = config.contactNumber.trim()
    val url = config.downloadUrl.trim()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(DarkSurface, CircleShape)
                    .border(1.5.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = message,
                color = TextSecondary,
                fontSize = 16.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            if (url.isNotEmpty()) {
                GoldButton(
                    text = "نیا ورژن ڈاؤن لوڈ کریں",
                    onClick = { context.openLink(url) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            if (number.isNotEmpty()) {
                GoldOutlinedButton(
                    text = "واٹس ایپ پر رابطہ کریں",
                    onClick = { context.openLink("https://wa.me/" + number.filter { it.isDigit() }) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                // Phone numbers read left-to-right even on an Urdu page.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = number,
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "موجودہ ورژن: $currentVersionCode",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

private fun Context.openLink(link: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // no app can open it: nothing else to do
    }
}

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
