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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.clip

// Animation用
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

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
    var addModalOpen by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // StatusBar分 + 16dp を確保（TopAppBar自体の「上部マージン」）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            // テキストを上下左右中央寄せ
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "在庫管理",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        navigationIcon = {
                            // 三点リーダー：左
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.MoreVert, contentDescription = null)
                            }
                        },
                        actions = {
                            // 検索：右
                            IconButton(onClick = { searchOpen = !searchOpen }) {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFFF3EDF7),
                            titleContentColor = Color(0xFF1C1B1F),
                            navigationIconContentColor = Color(0xFF1C1B1F),
                            actionIconContentColor = Color(0xFF1C1B1F)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterSegmentedRow(
                        modifier = Modifier.weight(1.3f),
                        showStock = showStock,
                        showOut = showOut,
                        onStockClick = ::toggleStock,
                        onOutClick = ::toggleOut
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
                onClick = { addModalOpen = true },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color(0xFF6750A4)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->

        if (addModalOpen) {
            AddModal(
                onDismiss = { addModalOpen = false },
                onSave = { name ->
                    val now = System.currentTimeMillis()
                    val nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1L
                    items = items + Seasoning(
                        id = nextId,
                        name = name,
                        inStock = true,      // ← 状態：在庫 で追加
                        createdAt = now
                    )
                    addModalOpen = false
                }
            )
        }

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
            shape = RoundedCornerShape(
                topStart = 20.dp,
                bottomStart = 20.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )
        )
        SegmentedLikeButton(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            text = "欠品",
            selected = showOut,
            onClick = onOutClick,
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp
            )
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
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp
                ),
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
                shape = RoundedCornerShape(
                    topEnd = 20.dp,
                    bottomEnd = 20.dp
                ),
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
    val scope = rememberCoroutineScope()
    // 0f = 表 / 180f = 裏
    val rotation = remember(seasoning.id) { Animatable(0f) }
    var showingFront by remember(seasoning.id) { mutableStateOf(seasoning.inStock) }
    // 外部の状態更新（items更新）に追従
    LaunchedEffect(seasoning.inStock) {
        showingFront = seasoning.inStock
        // 状態に応じて角度も合わせる（急に切り替わらないように）
        val target = if (seasoning.inStock) 0f else 180f
        if (kotlin.math.abs(rotation.value - target) > 1f) {
            rotation.snapTo(target)
        }
    }
    // 「今どちら面を描画するか」は角度で決める
    val drawFront = rotation.value <= 90f
    val bg = if (drawFront) stockBg else outBg
    val textColor = if (drawFront) stockText else outText
    val border = if (drawFront) BorderStroke(1.dp, stockBorder) else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 16f * density
            }
            .clickable {
                scope.launch {
                    // 1) 0->180 or 180->0 へ回す
                    val goingToBack = rotation.value < 90f
                    val target = if (goingToBack) 180f else 0f

                    rotation.animateTo(
                        targetValue = target,
                        // 回転速度
                        animationSpec = tween(durationMillis = 700)
                    )

                    // 2) アニメーション完了後に状態反転（DB/状態を更新）
                    //    表裏の「意味」＝在庫/欠品なので、回った後に確定で切り替える
                    onToggle()
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = border
    ) {
        // 90°超えると鏡文字になるので、裏面はさらに180°回して正しい向きにする
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (!drawFront) {
                        rotationY = 180f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = seasoning.name,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModal(
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
                // Header: close + title
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
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { attemptSave() }
                    )
                )

                Button(
                    onClick = { attemptSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = text.trim().isNotEmpty(), // 未入力のとき「保存」押せない
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
