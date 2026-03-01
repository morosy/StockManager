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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
    val inStock: Boolean,
    val createdAt: Long,
)

enum class SortMode(val label: String) {
    OLDEST("古い順"),
    NEWEST("新しい順"),
    NAME("名前順"),
    STOCK_FIRST("在庫順"),
    OUT_FIRST("欠品順"),
}

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

    val appBg = Color(0xFFF5F5F5)
    val stockBg = Color.White
    val stockText = Color(0xFF1C1B1F)
    val stockBorder = Color(0xFFE7E0EC)
    val outBg = Color(0xFFF9DEDC)
    val outText = Color(0xFFB3261E)

    var items by remember {
        mutableStateOf(
            listOf(
                Seasoning(1, "しょうゆ", true, 1_000L),
                Seasoning(2, "塩", true, 2_000L),
                Seasoning(3, "こしょう", false, 3_000L),
                Seasoning(4, "みりん", true, 4_000L),
            )
        )
    }

    var showStock by remember { mutableStateOf(true) }
    var showOut by remember { mutableStateOf(true) }
    var sortMode by remember { mutableStateOf(SortMode.OLDEST) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    fun toggleStock() {
        when {
            showStock && !showOut -> { showStock = false; showOut = true }
            showStock && showOut -> showStock = false
            else -> showStock = true
        }
    }

    fun toggleOut() {
        when {
            !showStock && showOut -> { showOut = false; showStock = true }
            showStock && showOut -> showOut = false
            else -> showOut = true
        }
    }

    val filteredSortedItems = remember(items, showStock, showOut, sortMode, query) {
        val q = query.trim()
        val filtered = items.filter {
            val passStock = (it.inStock && showStock) || (!it.inStock && showOut)
            val passQuery = q.isEmpty() || it.name.contains(q, true)
            passStock && passQuery
        }

        when (sortMode) {
            SortMode.OLDEST -> filtered.sortedBy { it.createdAt }
            SortMode.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortMode.NAME -> filtered.sortedBy { it.name }
            SortMode.STOCK_FIRST -> filtered.sortedWith(compareBy({ !it.inStock }, { it.name }))
            SortMode.OUT_FIRST -> filtered.sortedWith(compareBy({ it.inStock }, { it.name }))
        }
    }

    Scaffold(
        containerColor = appBg,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("調味料管理") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.MoreVert, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchOpen = !searchOpen }) {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    FilterSegmentedRow(
                        modifier = Modifier.weight(1.3f),
                        showStock,
                        showOut,
                        ::toggleStock,
                        ::toggleOut
                    )

                    SortSplitButton(
                        modifier = Modifier.weight(1f),
                        label = sortMode.label,
                        menuOpen = sortMenuOpen,
                        onMenuOpenChange = { sortMenuOpen = it },
                        onSelect = {
                            sortMode = it
                            sortMenuOpen = false
                        }
                    )
                }

                if (searchOpen) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("調味料名で検索") },
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color.White,
                contentColor = Color(0xFF6750A4)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredSortedItems, key = { it.id }) { seasoning ->
                MagnetCard(
                    seasoning,
                    stockBg,
                    stockText,
                    stockBorder,
                    outBg,
                    outText
                ) {
                    items = items.map {
                        if (it.id == seasoning.id)
                            it.copy(inStock = !it.inStock)
                        else it
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSegmentedRow(
    modifier: Modifier,
    showStock: Boolean,
    showOut: Boolean,
    onStockClick: () -> Unit,
    onOutClick: () -> Unit
) {
    Row(
        modifier = modifier.height(40.dp)
    ) {
        SegmentedLikeButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            text = "在庫",
            selected = showStock,
            onClick = onStockClick,
            shape = RoundedCornerShape(12.dp, 0.dp, 0.dp, 12.dp)
        )
        SegmentedLikeButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            text = "欠品",
            selected = showOut,
            onClick = onOutClick,
            shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp)
        )
    }
}

@Composable
private fun SegmentedLikeButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    val bg = if (selected)
        MaterialTheme.colorScheme.secondaryContainer
    else Color.White

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text)
        }
    }
}

@Composable
private fun SortSplitButton(
    modifier: Modifier = Modifier,
    label: String,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onSelect: (SortMode) -> Unit
) {
    Box(modifier = modifier) {
        val containerColor = MaterialTheme.colorScheme.secondaryContainer
        val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        val dividerColor = MaterialTheme.colorScheme.outlineVariant

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            // 左：テキスト（最小幅）
            FilledTonalButton(
                onClick = { onMenuOpenChange(true) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(label, maxLines = 1)
            }

            // 真ん中の区切り線（Split感を出す）
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp)
                        .fillMaxWidth()
                )
            }
            // ※ Spacerの色はRowの背景に依存するので、下のSurface案がより綺麗です
            // まずは「▼を出す」目的で簡易のままでもOK

            // 右：ドロップダウン（必ず表示させるため固定幅）
            FilledTonalButton(
                onClick = { onMenuOpenChange(true) },
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "並び替え"
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { onMenuOpenChange(false) }
        ) {
            SortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = { onSelect(mode) }
                )
            }
        }
    }
}

@Composable
private fun MagnetCard(
    seasoning: Seasoning,
    stockBg: Color,
    stockText: Color,
    stockBorder: Color,
    outBg: Color,
    outText: Color,
    onToggle: () -> Unit
) {
    val bg = if (seasoning.inStock) stockBg else outBg
    val textColor = if (seasoning.inStock) stockText else outText
    val border = if (seasoning.inStock)
        BorderStroke(1.dp, stockBorder)
    else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = border
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(seasoning.name, color = textColor)
        }
    }
}
