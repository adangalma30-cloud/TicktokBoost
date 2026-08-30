package com.tiktokboost.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.Quests
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.SecondaryButton

/** Referral screen: code, link, copy, native share + mock qualification (demo). */
@Composable
fun ReferralScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    var qualifiedNote by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Invite a friend", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Invite creators you know. When a friend joins with your code, the referral quest unlocks — you still claim the reward yourself.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Your referral code", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Session.referralCode(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = cs.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Your link", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Session.referralLink(),
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        SecondaryButton("Copy link", modifier = Modifier.weight(1f), onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("TickTokBoost referral", Session.referralLink()))
                            copied = true
                        })
                        Spacer(Modifier.width(8.dp))
                        BrandButton("Share", modifier = Modifier.weight(1f), onClick = {
                            openShareSheet(context)
                        })
                    }
                    if (copied) {
                        Spacer(Modifier.height(8.dp))
                        Text("Link copied ✓", style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Waiting for your friend", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (Session.referralQualified)
                            "Your friend joined — head back to Earn and claim your reward."
                        else
                            "Rewards unlock when a referred friend actually joins. Opening the share sheet alone doesn't complete this quest.",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                    if (!Session.referralQualified) {
                        Spacer(Modifier.height(10.dp))
                        SecondaryButton("Simulate friend joining (demo)", modifier = Modifier.fillMaxWidth(), onClick = {
                            Quests.onReferralQualified()
                            qualifiedNote = "Referral qualified — claim your reward in Earn ✨"
                        })
                    }
                    qualifiedNote?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
