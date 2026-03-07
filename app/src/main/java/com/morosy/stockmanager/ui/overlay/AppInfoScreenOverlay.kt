package com.morosy.stockmanager.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.morosy.stockmanager.R

enum class AppInfoScreenType {
    HOW_TO_USE,
    ABOUT,
    OSS_LICENSES,
    PRIVACY_POLICY
}

@Composable
fun AppInfoScreenOverlay(
    type: AppInfoScreenType,
    onClose: () -> Unit,
    appVersion: String,
    textLoader: (Int) -> String
) {
    val title = when (type) {
        AppInfoScreenType.HOW_TO_USE -> "使い方"
        AppInfoScreenType.ABOUT -> "About"
        AppInfoScreenType.OSS_LICENSES -> "OSSライセンス"
        AppInfoScreenType.PRIVACY_POLICY -> "プライバシーポリシー"
    }

    val bodyText = when (type) {
        AppInfoScreenType.HOW_TO_USE -> null
        AppInfoScreenType.ABOUT -> null
        AppInfoScreenType.OSS_LICENSES -> textLoader(R.raw.oss_licenses)
        AppInfoScreenType.PRIVACY_POLICY -> textLoader(R.raw.privacy_policy)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1200f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, top = 10.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                when (type) {
                    AppInfoScreenType.HOW_TO_USE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "基本操作",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "・右下の「＋」でアイテムを追加")
                            Text(text = "・カードをタップして在庫/欠品を切り替え")
                            Text(text = "・左下の編集ボタンで削除モードを切り替え")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ボード操作",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "・左上メニューからボードを追加・編集")
                            Text(text = "・三点リーダーからエクスポート/インポート")
                            Text(text = "・「ツールからボードを作成」で外部テンプレートツールへ移動")
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    AppInfoScreenType.ABOUT -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "StockManager",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Version $appVersion")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Copyright (c) 2026 Shunsuke Morozumi")
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    else -> {
                        val scrollState = rememberScrollState()
                        Text(
                            text = bodyText.orEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .verticalScroll(scrollState)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

