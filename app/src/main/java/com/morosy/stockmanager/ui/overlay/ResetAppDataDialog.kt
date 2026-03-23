package com.morosy.stockmanager.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ResetAppDataDialogStage {
    FirstWarning,
    FinalConfirmation
}

@Composable
fun ResetAppDataDialog(
    stage: ResetAppDataDialogStage,
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember(stage) { mutableStateOf("") }

    LaunchedEffect(stage) {
        if (stage == ResetAppDataDialogStage.FirstWarning) {
            confirmText = ""
        }
    }

    val isFinalStage = stage == ResetAppDataDialogStage.FinalConfirmation
    val canConfirm = !inProgress && confirmText.trim() == "delete"
    val title = if (isFinalStage) "\u6700\u7d42\u78ba\u8a8d" else "\u30c7\u30fc\u30bf\u3092\u524a\u9664"
    val message = if (isFinalStage) {
        "\u524a\u9664\u3092\u5b9f\u884c\u3059\u308b\u3068\u3001\u30dc\u30fc\u30c9\u30fb\u30a2\u30a4\u30c6\u30e0\u30fb\u8a2d\u5b9a\u3092\u3059\u3079\u3066\u5931\u3044\u307e\u3059\u3002\n`delete` \u3068\u5165\u529b\u3059\u308b\u3068\u524a\u9664\u3057\u3066\u30a2\u30d7\u30ea\u3092\u7d42\u4e86\u3057\u307e\u3059\u3002"
    } else {
        "\u4fdd\u5b58\u3055\u308c\u3066\u3044\u308b\u30dc\u30fc\u30c9\u3001\u30a2\u30a4\u30c6\u30e0\u3001\u8a2d\u5b9a\u3092\u3059\u3079\u3066\u524a\u9664\u3057\u307e\u3059\u3002\n\u3053\u306e\u64cd\u4f5c\u306f\u5143\u306b\u623b\u305b\u307e\u305b\u3093\u3002"
    }

    Dialog(
        onDismissRequest = {
            if (!inProgress) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !inProgress,
            dismissOnClickOutside = !inProgress
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(text = message)

                if (isFinalStage) {
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = {
                            if (!inProgress) {
                                confirmText = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("\u78ba\u8a8d\u6587\u5b57\u5217") },
                        placeholder = { Text("delete") },
                        supportingText = {
                            Text("`delete` \u3068\u5b8c\u5168\u4e00\u81f4\u3067\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044")
                        },
                        enabled = !inProgress
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !inProgress
                    ) {
                        Text("\u30ad\u30e3\u30f3\u30bb\u30eb")
                    }

                    Button(
                        onClick = {
                            if (isFinalStage) {
                                onConfirm()
                            } else {
                                onContinue()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = if (isFinalStage) canConfirm else !inProgress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB3261E),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB3261E).copy(alpha = 0.40f),
                            disabledContentColor = Color.White.copy(alpha = 0.80f)
                        )
                    ) {
                        Text(if (isFinalStage) "\u524a\u9664\u3057\u3066\u7d42\u4e86" else "\u6b21\u3078")
                    }
                }
            }
        }
    }
}