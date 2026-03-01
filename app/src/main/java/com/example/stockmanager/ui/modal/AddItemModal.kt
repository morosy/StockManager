package com.example.stockmanager.ui.modal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemModal(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun attemptSave() {
        val name = text.trim()
        if (name.isEmpty()) {
            showError = true
            return
        }
        onSave(name)
        text = ""
        showError = false
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "閉じる")
                    }

                    Text(
                        text = "追加",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (showError) {
                            showError = it.trim().isEmpty()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名前") },
                    placeholder = { Text("名前を入力") },
                    singleLine = true,
                    isError = showError,
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    text = ""
                                    showError = true
                                }
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "クリア")
                            }
                        }
                    },
                    supportingText = {
                        if (showError) {
                            Text("入力してください")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { attemptSave() })
                )

                Button(
                    onClick = { attemptSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = text.trim().isNotEmpty(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF6750A4).copy(alpha = 0.40f),
                        disabledContentColor = Color.White.copy(alpha = 0.80f)
                    )
                ) {
                    Text("保存")
                }
            }
        }
    }
}
