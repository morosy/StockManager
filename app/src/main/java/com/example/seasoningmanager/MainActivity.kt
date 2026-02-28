package com.example.seasoningmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Seasoning(
    val id: Long,
    val name: String,
    val inStock: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SeasoningManagerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasoningManagerApp() {
    // --- UI colors (Figmaに合わせた値) ---
    val appBg = Color(0xFFF5F5F5)

    val stockBg = Color(0xFFFFFFFF)
    val stockText = Color(0xFF1C1B1F)
    val stockBorder = Color(0xFFE7E0EC)

    val outBg = Color(0xFFF9DEDC)
    val outText = Color(0xFFB3261E)

    // --- State ---
    var items by remember {
        mutableStateOf(
            listOf(
                Seasoning(1, "しょうゆ", true),
                Seasoning(2, "塩", true),
                Seasoning(3, "こしょう", false),
                Seasoning(4, "みりん", true),
            )
        )
    }

    var sheetOpen by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = appBg,
        topBar = {
            TopAppBar(
                title = { Text("調味料管理") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputName = ""
                    showError = false
                    sheetOpen = true
                },
                containerColor = Color.White,
                contentColor = Color(0xFF6750A4) // plus色（紫）
            ) {
                Icon(Icons.Filled.Add, contentDescription = "追加")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { seasoning ->
                MagnetCard(
                    name = seasoning.name,
                    inStock = seasoning.inStock,
                    stockBg = stockBg,
                    stockText = stockText,
                    stockBorder = stockBorder,
                    outBg = outBg,
                    outText = outText,
                    onToggle = {
                        items = items.map {
                            if (it.id == seasoning.id) {
                                it.copy(inStock = !it.inStock)
                            } else {
                                it
                            }
                        }
                    }
                )
            }
        }

        // ---- BottomSheet（追加モーダル） ----
        if (sheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    sheetOpen = false
                    showError = false
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 32.dp)
                ) {
                    // ハンドルっぽい見た目（任意）
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "追加",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = stockText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                            if (showError && it.isNotBlank()) {
                                showError = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("調味料名") },
                        isError = showError,
                        supportingText = {
                            if (showError) {
                                Text("未入力の場合は保存できません")
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val trimmed = inputName.trim()
                            if (trimmed.isEmpty()) {
                                showError = true
                                return@Button
                            }
                            val nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1L
                            items = items + Seasoning(nextId, trimmed, true)
                            sheetOpen = false
                            showError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("保存")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun MagnetCard(
    name: String,
    inStock: Boolean,
    stockBg: Color,
    stockText: Color,
    stockBorder: Color,
    outBg: Color,
    outText: Color,
    onToggle: () -> Unit
) {
    val bg = if (inStock) stockBg else outBg
    val textColor = if (inStock) stockText else outText
    val border = if (inStock) BorderStroke(1.dp, stockBorder) else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onToggle() },
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
