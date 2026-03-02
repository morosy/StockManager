# GenerateAI-NextPhase.md
## StockManager (Jetpack Compose) – MVVM / Room / Refactor Blueprint / Clean Architecture Design
最終更新: 2026-03-02 | 状況: Phase 1 (MVVM) 実装済み、Phase 2 (Room) 実装済み

---

# 0. このドキュメントの目的

このドキュメントは **「build が通った現状」** を正としつつ、次フェーズとして

- MVVM 版の設計・ファイル構造
- Room 導入版の設計書（Entity/DAO/DB/Repository）
- リファクタ設計図（段階的に壊さず改善）
- Clean Architecture 版の設計（層分離と依存方向）
- 私（AI）が提案する次の開発フェーズ計画

を **コピー＆ペースト可能** な形でまとめたものである。

対象読者は **新しく参加した開発者（デベロッパー）** であり、アプリ利用者ではない。

## 0.1 前提知識
#### 背景
このプロジェクトは、プロジェクトマネージャー・マスターが、自宅の調味料や日用品の在庫品/欠品状況を、両面タイプの単語が記載されたマグネット(表：黒字->在庫, 裏：赤字->欠品)を張り付けたホワイトボード用いて管理していた状況をアプリケーション化するものである。
アプリケーションにおいても、裏返すことのできる(アニメーションで疑似的に再現)マグネットを用いて現在の状況を管理する。

#### 実装フロー
実装は、Figmaで作成されたUIの画像の提示および、自然言語での指示となる。

#### タスク
1. UI設計補助
2. コーディング
3. エラーハンドリング

#### プロジェクトにて登場する単語
- `Magnet/マグネット`　: 単語が記載された1つのブロック。`タイル` など、類似語で呼ぶ場合もあり、固定的な名詞は無い
```Magnetの状態遷移
在庫 (white)
  ↓ toggle
欠品 (red)
  ↓ toggle
在庫
```
- `Board/ボード`：`Magnet`の集合するスクロール可能な1ページ。
  ユースケースとして、Boardのタイトルを`調味料`, `風呂場`など、カテゴリにして管理する。
- `在庫`：物品管理のユースケースにおいて、手元に物品がある状態。`背景：白, 文字：黒色`で統一する。
- `欠品`：物品管理のユースケースにおいて、手元に物品がない状態。`背景：薄い赤, 文字：赤`で統一する。

- `menu`：ベースUI左上の`三点リーダー`。タップすることで`menu`(=`ManageBoard`)が開く
- `TopAppBar`：上部の`menu`, `Title/Label`, `Search`が内包されているコンポーネント。
- `SegmentedButton`, `SplitButton`：`TopAppBar`下の **絞り込み・並べ替え** 機能を持つボタン  
**important**：`SegmentedButton`の状態
  1. 在庫のみ表示
  2. 欠品のみ表示
  3. 両方表示  
  の **3状態のみ** となる(両方非表示は無い)  
  -> 1. または 2. の時、本来両方非表示となる操作が行われた場合、現在の状態と逆の表示となる。  
    **Ⅰ.** `1. 在庫のみ表示` の時に`在庫`がタップされる -> `2. 欠品のみ表示` へ遷移  
    **Ⅱ.** `2. 欠品のみ表示` の時に`欠品`がタップされる -> `1. 在庫のみ表示` へ遷移  

- `PlusButton`, `EditButton`：Magnetの追加、編集をおこなうボタン
- `AddModal/オーバーレイ`：`PlusButton`がタップされた際にポップアップする。


---

# 1. 現状 (build が通った状態) の整理

## 1.1 技術スタック・ビルド設定

- Kotlin / JVM 11
- Android Gradle Plugin: 8.10.1
- Jetpack Compose: BOM 2025.02.00
- Compose foundation-layout: 1.7.8
- Material3: 1.3.1
- minSdk 24, targetSdk 35, compileSdk 36

### build.gradle.kts（要点）
- `buildFeatures { compose = true }`
- Compose のバージョンは BOM を使用して揃えている
- 依存関係に `androidx.compose.foundation:foundation-layout` が含まれている
- Material3 が 1.3.1

## 1.2 永続化（Room + MVVM）

#### 1.2.1 概要
現在は **Room データベース + Repository + ViewModel** が稼働し、
アプリ再起動後もボード・アイテム・設定がそのまま保持される。

- 保存対象:
  - BoardEntity / StockItemEntity / SettingsEntity
- 保存方式:
  - Android Room（version 2.6.1）
  - `AppDatabase` に `boardDao` / `stockDao` / `settingsDao` を配置
- データアクセス:
  - `StockRepository` が DB 操作をラップ
  - `StockManagerViewModel` は Repository を使って StateFlow を公開
  - UI は `collectAsStateWithLifecycle()` で ViewModel の状態を監視

#### 1.2.2 ライフサイクルと設定
- 起動時に ViewModel が `repo.ensureSeeded()` を呼びデフォルトデータを注入
- 設定（currentBoardId, showStock/showOut, sortMode, query）は
  `SettingsEntity` に格納し、変更は `StockRepository.updateSettings` 経由で
  Room に upsert される。
- ボード順序・名前変更・アイテム追加削除・在庫トグル等も
  Repository の suspend メソッドを介して行う。

#### 1.2.3 現在のデータ層構造
```
app/src/main/java/com/example/stockmanager/data/
├── StockRepository.kt          # DB への窓口
└── db/
    ├── AppDatabase.kt          # RoomDatabase
    ├── BoardDao.kt
    ├── StockDao.kt
    ├── SettingsDao.kt
    ├── Entities.kt            # BoardEntity, StockItemEntity
    ├── SettingsEntity.kt
    └── Relations.kt           # BoardWithItems
```

#### 1.2.4 現状のアーキテクチャ
- UI (Compose) ← ViewModel ← Repository ← Room(DB)
- ViewModel は `StateFlow<StockManagerUiState>` を公開し、
  Screen はそれを受け取って描画
- Repository には `observeBoardsWithItems()` など Flow を提供し、
  ViewModel が combine して UI 状態を構築する

#### 1.2.5 次の改善余地
- ViewModel 内のフィルタ／ソートロジックはドメイン層に移す
- Repository インタフェースの分離とモック実装追加
- Clean Architecture へ向けたレイヤ分離（現状は data と presentation が
  多少混在）

(旧 DataStore 実装は過去ログとして歴史に残るが、現在は使用されていない。)

将来的には:
```
UI
  ↓
ViewModel
  ↓
Repository
  ↓
DataStore / Room
```

---

# 2. 現状のファイル構造（実際のプロジェクト）

以下は現在のソースディレクトリで確認できる主要ファイル群。
MVVM + Room + Compose UI が混在した構造になっている。
```
app/src/main/java/com/example/stockmanager/
├── MainActivity.kt
├── model/
│   └── Models.kt            # ドメインモデル (Board, StockItem, SortMode)
├── ui/
│   ├── StockManagerScreen.kt
│   ├── StockManagerViewModel.kt
│   ├── components/
│   │   ├── FilterSegmentedRow.kt
│   │   ├── MagnetCard.kt
│   │   └── SortSplitButton.kt
│   ├── modal/
│   │   └── AddItemModal.kt
│   ├── overlay/
│   │   ├── BoardAddModal.kt
│   │   ├── BoardDrawerOverlay.kt
│   │   ├── ConfirmBoardDeleteDialog.kt
│   │   └── RenameBoardOverlay.kt
│   └── theme/                # Compose テーマ関連
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── data/
│   ├── StockRepository.kt    # DB への窓口
│   ├── StockManagerLocalStore.kt  # レガシー DataStore（現在は未使用）
│   └── db/
│       ├── AppDatabase.kt
│       ├── BoardDao.kt
│       ├── StockDao.kt
│       ├── SettingsDao.kt
│       ├── Entities.kt      # BoardEntity, StockItemEntity
│       ├── SettingsEntity.kt
│       └── Relations.kt     # BoardWithItems
```


---

# 3. 現状の依存関係（レイヤの依存方向）

現在は MVVM + Repository + Room を採用しており、責務が比較的明確に分離されている。
依存の流れは次のとおり。

- `ui/StockManagerScreen.kt` は ViewModel (`StockManagerViewModel`) の `UiState` を受け取り
  イベントを ViewModel に渡すだけ。
- ViewModel は `model` のデータクラスと `StockRepository` のインタフェースに依存。
- `StockRepository` は Room DAO を利用してデータベースへアクセスし、
  Flow を返す。
- Compose コンポーネント（components/modal/overlay）は基本的に model を参照するが、
  ViewModel や Repository には直接触れない。

依存図（現状）:
```
StockManagerScreen -> StockManagerViewModel -> StockRepository -> Room(DB)
                             ↑
                             model (Board, StockItem, SortMode)
```

レガシーだった `StockManagerLocalStore` はコードベースに残るが、
現在は参照されていない。


---

# 4. 現状の各ファイル詳細（できるだけ詳しく）

## 4.1 ui/StockManagerScreen.kt

### 4.1.1 役割
メイン画面の stateless Composable。
画面の UI 描画と、ユーザー操作イベントを `StockManagerViewModel` に渡す。
状態本体は ViewModel が持ち、Screen 側には
「ダイアログやローカルUI（renameOpen/検索欄など）の開閉」や
「アニメーション制御用の一時的な state」だけが残る。

### 4.1.2 画面構成（UI）
- `Scaffold`
  - `topBar`:
    - `CenterAlignedTopAppBar`
      - 左: menu（MoreVert）→ BoardDrawerOverlay を開閉
      - 右: search → 検索欄を開閉
    - フィルタ行:
      - `FilterSegmentedRow`（在庫/出庫の表示切替）
      - `SortSplitButton`（ソート条件）
    - 検索欄:
      - `OutlinedTextField`（`searchOpen` 時のみ表示）
  - `content`:
    - `LazyVerticalGrid`（2列）
      - ViewModel `uiState.visibleItems` の各要素で `MagnetCard` を描画
  - 画面下:
    - 左下: 編集トグル FAB（Edit）
    - 右下: 追加 FAB（Add）※ editMode 時は非表示
- モーダル類:
  - `AddItemModal`（アイテム追加）
  - `BoardAddModal`（ボード追加）
  - `ConfirmBoardDeleteDialog`（ボード削除確認）
  - `BoardDrawerOverlay`（ボード一覧/編集）

### 4.1.3 ViewModel との連携
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val currentBoardName = uiState.boards
    .firstOrNull { it.board.id == uiState.currentBoardId }
    ?.board?.name.orEmpty()

// UI 側にはローカルフラグだけを保持
var renameOpen by remember { mutableStateOf(false) }
var sortMenuOpen by remember { mutableStateOf(false) }
// ...その他 UI ダイアログ用 state
```

ViewModel への操作は関数呼び出しで行う:
```kotlin
IconButton(onClick = { viewModel.toggleSearch() })
// ...
viewModel.addItem(boardId, name)
viewModel.toggleItem(item)
viewModel.renameBoard(id, newName)
```

### 4.1.4 ロジックの位置づけ
Screen 内に残るロジックはごく少量:
- クリックリスナーやモーダルの開閉
- 画面ローカルな `deletingIds` アニメ用 state の管理
- `filteredSortedItems` の計算は ViewModel 側で行い、
  `uiState.visibleItems` として提供されるようになった。

（以前はここにフィルタ/ソート/検索ロジックが複数あり、肥大化の原因だった）

---

### 4.1.5 今後の改修ポイント
UI はほぼ Stateless になったものの、
フィルタ・ソート・検索を ViewModel ではなく外部ユースケースに移すと
テストがさらに容易になる。

```kotlin
// 例: ViewModel が UseCase を呼ぶように
viewModelScope.launch { addItemUseCase(boardId, name) }
```

また `renameOpen` などのフラグは `UiState` に入れてしまっても構わない。

### 4.1.6 ViewModel / Repository の構成

- **StockManagerViewModel.kt**
  - `uiState` を StateFlow で公開
  - リポジトリの Flow を combine し、フィルタ・ソート済みリストや
    UI 設定を保持
  - ユーザー操作に応じたメソッド (`addItem`, `toggleItem`, `renameBoard` など)
  - 副作用（データベース更新）は `viewModelScope.launch` 内で実行

- **StockRepository.kt**
  - `AppDatabase` を受け取り DAO を操作
  - **Flow** でボードと設定を監視
  - Suspend メソッドで CRUD 操作を提供
  - 画面ロジックに依存しない純粋なデータ層

- データベース (`data/db` 以下) は Room で定義されており、
  Entity・Dao・Database・Relation が揃っている。
  ViewModel や UI は Entity ではなく Model クラスを使うことで
 依存を低減している（Mapper が ViewModel に混在）。

### 4.1.5 filteredSortedItems の計算
```
val filteredSortedItems = remember(boards, currentBoardId, showStock, showOut, sortMode, query) {
    items.filter { inStock/Out && query match } -> sortMode に応じて並び替え
}
```


注意:
- `remember` の key に `boards` が入っているので、items 更新で再計算される
- `query` の trim を都度実行している（小さいが最適化余地）

### 4.1.6 注意点
- **状態とロジックが一箇所に集約**されているため、機能追加が進むほど肥大化する
- 「永続化」「テスト」「状態復元（process death）」に弱い
- `delay(220)` は UI アニメーションと同期していない（将来の不具合要因）

---

## 4.2 ui/components/FilterSegmentedRow.kt（現状の build 通過版）

### 4.2.1 役割
セグメント（タブ）風 UI。複数項目の中から 1つを選択する。

### 4.2.2 API（関数シグネチャ）
現在の修正後は以下形が正。
```
@Composable
fun FilterSegmentedRow(
modifier: Modifier,
items: List<String>,
selectedIndex: Int,
onSelect: (Int) -> Unit,
)
```


StockManagerScreen から呼ぶ場合の例:
- items = listOf("在庫", "出庫") など
- selectedIndex は 0/1
- onSelect 内で showStock/showOut のトグルを呼ぶ、または state 更新

### 4.2.3 実装の要点
- `EqualWidthRow` を自作して `Layout` で等幅を保証
- `weight()` を利用しない（内部 API 問題回避）
- 選択中は primary color、それ以外は透明背景

### 4.2.4 過去に起きた問題と対策
- `RowColumnParentData?.weight` が internal で参照できないエラーが発生した
- `Modifier.weight` や、間接的に weight に触れるようなコードが原因になり得る
- 今回は custom Layout（EqualWidthRow）で根本回避した

---

## 4.3 ui/components/MagnetCard.kt（実装済み）

### 4.3.1 役割
在庫アイテム 1件の表示。
- アイテム名
- 在庫/出庫の色分け
- 編集モード時の削除ボタン
- 在庫状態のトグル

### 4.3.2 想定 props（StockManagerScreen 側の呼び出しから推定）
```
MagnetCard(
item: StockItem,
stockBg: Color,
stockText: Color,
stockBorder: Color,
outBg: Color,
outText: Color,
editMode: Boolean,
isDeleting: Boolean,
onToggle: () -> Unit,
onDelete: () -> Unit,
)
```


### 4.3.3 注意点
- `editMode` のときは onToggle を抑止する設計が StockManagerScreen 側で入っている
- `isDeleting` は削除アニメーション表示（例えば alpha/scale）に使う想定

---

## 4.4 ui/components/SortSplitButton.kt（実装済み）

### 4.4.1 役割
ソート条件を変更する UI（ボタン + メニュー）。

### 4.4.2 想定 props（StockManagerScreen から推定）
```
SortSplitButton(
modifier: Modifier,
label: String,
menuOpen: Boolean,
onMenuOpenChange: (Boolean) -> Unit,
onSelect: (SortMode) -> Unit
)
```


### 4.4.3 注意点
- Material3 の experimental API を使っている場合がある（過去に ExperimentalMaterial3Api の unresolved が出た）
- 依存のバージョンが上がると API 差分で壊れやすい

---

## 4.5 ui/modal/AddItemModal.kt（実装済み）

### 4.5.1 役割
アイテム名入力 → 保存/キャンセルするモーダル。

### 4.5.2 想定 props
```
AddItemModal(
onDismiss: () -> Unit,
onSave: (String) -> Unit,
)
```


---

## 4.6 ui/overlay/BoardDrawerOverlay.kt（実装済み）

### 4.6.1 役割
ボードの一覧表示と切替。
- open/close
- boardEditMode（編集モード）
- ボード追加
- ボード削除要求（確認ダイアログに流す）

### 4.6.2 想定 props（StockManagerScreen から確定）
```
BoardDrawerOverlay(
open: Boolean,
boards: List<Board>,
currentBoardId: Long,
editMode: Boolean,
onSelectBoard: (Long) -> Unit,
onClose: () -> Unit,
onEnterEdit: () -> Unit,
onExitEdit: () -> Unit,
onAddBoard: () -> Unit,
onRequestDeleteBoard: (Board) -> Unit
)
```


---

## 4.7 ui/overlay/BoardAddModal.kt（実装済み）
- ボード名入力
- 保存 → addBoard(name)

props:
```
BoardAddModal(
onDismiss: () -> Unit,
onSave: (String) -> Unit
)
```


---

## 4.8 ui/overlay/ConfirmBoardDeleteDialog.kt（実装済み）
- 文言に boardName を表示
- confirm → deleteBoard(boardId)

props:
```
ConfirmBoardDeleteDialog(
boardName: String,
onConfirm: () -> Unit,
onCancel: () -> Unit
)
```


---

## 4.9 model/Board.kt / StockItem.kt / SortMode.kt（実装済み）
- UI の状態とロジックはこれらのモデルに依存
- `StockItem.createdAt` は Long（Unix ms）

---

# 5. 次フェーズ提案（AI 提案）

本プロジェクトでは **Phase 1 (MVVM)** と **Phase 2 (Room 永続化)**が
すでに実装済みであり、UI は ViewModel を介して Repository / Room に依存する
構造になっている。

そのため、今後の開発は「既存機能を守りつつ内部構造をより堅牢にする」
方向へシフトするのが自然だ。

現時点で着手すべきフェーズは以下の通りで、いずれも現在のコードベース
を壊さずに進められる。

## Phase 3: リファクタ（UseCase / Repository インタフェース化）
- 目的: 画面追加やユースケース追加に耐える柔軟な構造
- 作業例:
  - Repository を interface に分離し DI で注入
  - ビジネスロジックを UseCase/Interactor に切り出す
  - ViewModel は UseCase を呼ぶだけに
- 期待効果:
  - 単体テストの容易化
  - リポジトリをモック／フェイクに差し替えやすくなる

## Phase 4: Clean Architecture 化（層を明確化）
- 目的: 長期運用・チーム拡大時の保守性向上
- 作業例:
  - presentation / domain / data パッケージに分離
  - domain 層にモデル・Repository interface・UseCase を置く
  - data 層は domain にのみ依存
  - UI 層は domain にのみ依存
- 期待効果:
  - 依存関係が一方向に統一され、変更影響範囲が限定される
---

# 6. MVVM 版設計（GenerateAI – MVVM）

※以下の設計は既にコードに反映済みであり、現行の ViewModel/UiState
実装はこのパターンに概ね従っている。

## 6.1 目標
- UI は state を受け取り描画し、イベントを ViewModel に送る
- ViewModel が state を生成し、Repository を介して data を読み書きする

## 6.2 MVVM の新ファイル構造（提案）
```
ui/
├── screen/
│ ├── StockManagerScreen.kt (UI: stateless を目指す)
│ ├── StockManagerRoute.kt (ViewModel 取得 + collectAsState)
│ ├── StockManagerUiState.kt
│ ├── StockManagerUiEvent.kt (任意: UIイベント定義)
│ └── StockManagerViewModel.kt
├── components/...
├── modal/...
└── overlay/...

domain/ (Phase3 以降で導入でもOK)
data/ (Room導入で導入)
```


## 6.3 UI state 設計

### StockManagerUiState（案）
```
data class StockManagerUiState(
val boards: List<Board> = emptyList(),
val currentBoardId: Long? = null,
val showStock: Boolean = true,
val showOut: Boolean = true,

val sortMode: SortMode = SortMode.OLDEST,
val sortMenuOpen: Boolean = false,

val searchOpen: Boolean = false,
val query: String = "",

val addItemModalOpen: Boolean = false,
val editMode: Boolean = false,
val deletingIds: Set<Long> = emptySet(),

val drawerOpen: Boolean = false,
val boardEditMode: Boolean = false,
val boardAddModalOpen: Boolean = false,
val pendingDeleteBoardId: Long? = null,
)
```


注意:
- Compose の SnapshotStateList は ViewModel に持ち込まず、通常の immutable な Set などに寄せる
- `pendingDeleteBoardId` にして Board 全体を持たない（DB導入後に参照が安定する）

## 6.4 UI event 設計（任意だが推奨）
```
sealed interface StockManagerUiEvent {
data object ToggleSearch : StockManagerUiEvent
data class UpdateQuery(val value: String) : StockManagerUiEvent

data object ToggleEditMode : StockManagerUiEvent
data object OpenAddItemModal : StockManagerUiEvent
data object CloseAddItemModal : StockManagerUiEvent
data class AddItem(val name: String) : StockManagerUiEvent

data object ToggleDrawer : StockManagerUiEvent
data class SelectBoard(val boardId: Long) : StockManagerUiEvent

data class RequestDeleteItem(val itemId: Long) : StockManagerUiEvent

data class SetSortMode(val mode: SortMode) : StockManagerUiEvent
data class SetShowStock(val value: Boolean) : StockManagerUiEvent
data class SetShowOut(val value: Boolean) : StockManagerUiEvent

data object OpenBoardAddModal : StockManagerUiEvent
data class AddBoard(val name: String) : StockManagerUiEvent

data class RequestDeleteBoard(val boardId: Long) : StockManagerUiEvent
data object CancelDeleteBoard : StockManagerUiEvent
data object ConfirmDeleteBoard : StockManagerUiEvent
}
```


## 6.5 ViewModel の責務

- UiState を `StateFlow` として公開
- フィルタ・ソート済み items は:
  - (A) UiState に `visibleItems` を持つ
  - (B) UI 側で derivedStateOf 的に計算する
  - (C) ViewModel が `visibleItems` を計算し UI に渡す

推奨は (C):
- 一貫したロジック
- テストしやすい

ただし items 数が極端に多い場合は Paging 検討。

---

# 7. Room 導入版設計書（Entity / DAO / DB / Repository）

ここで説明する Room の設計パターンは既に
app/src/main/java/com/example/stockmanager/data/db 設下に実装されており、
以下では実提供されているファイルを基恤に施描している。

## 7.1 目標
- boards / items を永続化
- 現状の in-memory データ構造をそのまま DB にマッピング
- UI は Repository 経由で読み書き

## 7.2 テーブル設計（案）

### boards テーブル
- boardId (PK)
- name
- createdAt

### stock_items テーブル
- itemId (PK)
- boardId (FK: boards.boardId)
- name
- inStock
- createdAt
- updatedAt（将来用）

注意:
- `boardId` で items をグルーピングするため、Board.items を直接保持しない
- 参照は `@Relation` で組み立てるか、2クエリで取る

## 7.3 Entity 設計（案）

```kotlin
@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    val boardId: Long = 0L,
    val name: String,
    val createdAt: Long,
)
@Entity(
    tableName = "stock_items",
    indices = [
        Index(value = ["boardId"]),
        Index(value = ["createdAt"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["boardId"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0L,
    val boardId: Long,
    val name: String,
    val inStock: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
```

## Relation（BoardWithItems）
```Kitlin
data class BoardWithItems(
    @Embedded
    val board: BoardEntity,

    @Relation(
        parentColumn = "boardId",
        entityColumn = "boardId"
    )
    val items: List<StockItemEntity>,
)
```

## 7.5 DAO 設計（案）

#### BoardDao
- observe boards
- insert/update/delete board

```Kotlin
@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY createdAt ASC")
    fun observeBoards(): Flow<List<BoardEntity>>

    @Insert
    suspend fun insertBoard(entity: BoardEntity): Long

    @Update
    suspend fun updateBoard(entity: BoardEntity)

    @Query("DELETE FROM boards WHERE boardId = :boardId")
    suspend fun deleteBoard(boardId: Long)
}
```

#### StockItemDao
- boardId ごとの items
- insert/update/delete item

```Kotlin
@Dao
interface StockItemDao {
    @Query("SELECT * FROM stock_items WHERE boardId = :boardId ORDER BY createdAt ASC")
    fun observeItems(boardId: Long): Flow<List<StockItemEntity>>

    @Insert
    suspend fun insertItem(entity: StockItemEntity): Long

    @Update
    suspend fun updateItem(entity: StockItemEntity)

    @Query("DELETE FROM stock_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM stock_items WHERE boardId = :boardId")
    suspend fun deleteItemsByBoard(boardId: Long)
}
```

## 7.6 Database 設計
```Kotlin
@Database(
    entities = [
        BoardEntity::class,
        StockItemEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun stockItemDao(): StockItemDao
}
```

## 7.7 Repository 設計（案）
目的:
- UI は DB を直接触らない
- 将来、Network/Cloud を足しても UI 変更が最小

#### Interface（domain へ移すのが最終形）

```Kotlin
interface StockRepository {
    fun observeBoards(): Flow<List<Board>>
    fun observeItems(boardId: Long): Flow<List<StockItem>>

    suspend fun addBoard(name: String): Long
    suspend fun deleteBoard(boardId: Long)

    suspend fun addItem(boardId: Long, name: String): Long
    suspend fun toggleItemStock(itemId: Long, inStock: Boolean)
    suspend fun deleteItem(itemId: Long)
}
```

#### 実装（data 層）
- Entity <-> Domain model を mapper で変換する

# 8. リファクタ設計図（壊さず段階的に改善）

## 8.1 目的

- 「大工事」ではなく、コンパイルを壊さない単位で改善
- 今の UI を保ちながら内部を整理

## 8.2 推奨ステップ
#### Step A: UI state の外出し（まだ ViewModel 無し）
- StockManagerUiState.kt を作り、Screen 内 state をそこへ移す
- Screen は state を 1つ持つだけにする

#### Step B: ViewModel 導入（in-memory のまま）
- Repository なしで ViewModel が in-memory を保持
- UI は ViewModel の state を表示するだけにする

#### Step C: Repository 層追加（まだ Room 無し）
- in-memory Repository 実装を作る（Fake でも良い）
- ViewModel は Repository を通して読み書き

#### Step D: Room 実装を追加して差し替え
- Repository interface はそのまま
- 実装が Fake -> Room に切り替わる

#### Step E: UseCase 導入（Domain）
- AddItemUseCase, DeleteItemUseCase など
- ViewModel は UseCase を呼ぶだけにする

# 9. Clean Architecture 版設計（最終形）
## 9.1 層と責務

#### presentation
- Compose UI
- ViewModel
- UiState / UiEvent

#### domain
- Entity（ドメインモデル: Board, StockItem）
- Repository interface
- UseCase（Interactor）
- ルール（フィルタ/ソートロジックの中核）

#### data
- Room（DAO/DB/Entity）
- Repository implementation
- Mapper

#### 依存方向（必須）:
```
presentation -> domain
data -> domain
domain -> (何にも依存しない)
```

## 9.2 Clean Architecture ファイル構造（提案）
```
com.example.stockmanager/
├── presentation/
│   ├── screen/
│   │   ├── StockManagerRoute.kt
│   │   ├── StockManagerScreen.kt
│   │   ├── StockManagerViewModel.kt
│   │   ├── StockManagerUiState.kt
│   │   └── StockManagerUiEvent.kt
│   ├── components/...
│   ├── modal/...
│   └── overlay/...
├── domain/
│   ├── model/
│   │   ├── Board.kt
│   │   ├── StockItem.kt
│   │   └── SortMode.kt
│   ├── repository/
│   │   └── StockRepository.kt
│   └── usecase/
│       ├── ObserveBoardsUseCase.kt
│       ├── ObserveItemsUseCase.kt
│       ├── AddBoardUseCase.kt
│       ├── DeleteBoardUseCase.kt
│       ├── AddItemUseCase.kt
│       ├── ToggleItemUseCase.kt
│       └── DeleteItemUseCase.kt
└── data/
    ├── db/
    │   ├── AppDatabase.kt
    │   ├── BoardDao.kt
    │   ├── StockItemDao.kt
    │   ├── entity/
    │   │   ├── BoardEntity.kt
    │   │   └── StockItemEntity.kt
    │   └── relation/
    │       └── BoardWithItems.kt
    ├── mapper/
    │   ├── BoardMapper.kt
    │   └── StockItemMapper.kt
    └── repository/
        └── StockRepositoryImpl.kt
```

# 10. 重要な設計論点（今後の事故を減らす）
## 10.1 フィルタ UI の設計

#### 現状:
- showStock/showOut の2フラグで排他制御している

#### 今後の推奨:
- Domain として FilterMode を導入し、3状態にする
    - ALL / STOCK_ONLY / OUT_ONLY
- UI は selectedIndex 1つで扱える

例:
```
enum class FilterMode {
    ALL,
    STOCK,
    OUT,
}
```

## 10.2 ソートの責務
- SortMode は domain
- 実際の並び替えロジックは domain 側（UseCase or helper）へ寄せるとテスト可能

#### 10.3 deletingIds の扱い
- Domain には “削除アニメ” の概念は不要
- presentation 層でのみ持つのが自然
- DB で削除したあと、UI は animation のための state を一定時間保持してから消すなどの制御が必要


# 11. “次にやる作業” の具体タスク（提案）
## 11.1 最小の次フェーズ（おすすめ）

「MVVM + Room を最短で入れる」

#### Task 1: presentation/screen を作る
- StockManagerRoute
- StockManagerViewModel
- StockManagerUiState

受入条件:
- 画面が ViewModel の state で動く
- build 成功


#### Task 2: Repository interface を domain に作る
受入条件:
- ViewModel は interface にしか依存しない


#### Task 3: data に Room を導入
- Entity/DAO/DB/RepositoryImpl

受入条件:
再起動しても boards/items が残る

#### Task 4: Mapper を導入
受入条件:
- UI/Domain は Entity を参照しない

# 12. 引継ぎメモ（運用上の注意）
- Compose BOM は更新時に Material3 / Foundation の破壊的変更が出る可能性がある
- FilterSegmentedRow は weight 問題回避のため custom Layout を使っている。ここを安易に Row+weight に戻すと再燃する可能性がある
- 現状の Screen は UI が完成しているので、内部設計を変えるときは「UIを変えずに裏を変える」方針が安全
- “編集モード” 周りは UI/UX の要件が変わることが多いので、domain へ持ち込みすぎない

# 13. 付録: MVVM + Room 導入時の実装メモ（超短縮）
- ViewModel は StateFlow<UiState> を公開
- Repository は Flow で boards/items を expose
- UI は collectAsStateWithLifecycle() を使用（lifecycle-runtime-compose 追加推奨）
- DI は最初は手動でも良いが、将来的には Hilt を推奨

# 14. 終わりに

このドキュメントは
- “今動いている UI” を維持しつつ

- “データ永続化” と “保守可能な設計” に移行するための設計図
である。

次に着手する場合は Phase 1（MVVM）→ Phase 2（Room） の順が最短で安定する。


---

## ここから先、すぐ作業できるようにするために（こちらから提案）

次の返答で、あなたの作業をさらに加速するために **「実際に追加/置換するファイル一式（雛形コード）」** まで一気に出せます。

- `presentation/screen/*`（Route/ViewModel/UiState/UiEvent）
- `domain/*`（Repository interface / UseCase）
- `data/db/*`（Entity/DAO/Database/RepositoryImpl/Mapper）
- 既存 `StockManagerScreen` の “stateless 化” 版

必要なら、今の UI 仕様（ボード・編集・削除・検索・ソート）を崩さずに移植します。
その場合、**現状のコードと互換になるように**、段階導入（MVVMだけ先に導入→次にRoom）で出します。

---

# 15. 2026-03-03 追加機能: データインポート / データエクスポート

## 15.1 概要
- 追加日: 2026-03-03
- 対象: `Board` とその配下 `Magnet(StockItem)`
- 単位: `Board` 単位で Export / Import
- ユースケース:
  - バックアップ
  - 機種変更時の移行
  - テンプレート配布/取り込み
  - JSON直接編集による一括管理

## 15.2 UI導線
- 位置: ボード管理オーバーレイ上部の三点リーダー (`BoardDrawerOverlay`)
- 追加メニュー:
  - `このボードをエクスポート`（JSON）
  - `このボードをエクスポート(CSV)`
  - `ボードをインポート`

## 15.3 エクスポート仕様

### JSON
- ファイル名: `[Board名].json`
- 形式:
  - `schemaVersion = 1`
  - `format = stockmanager-board-export`
  - `exportedAt` を付与
  - `board.exportId` / `item.exportId` を付与（ID衝突回避）
- 互換:
  - テンプレート形式 `format = stockmanager-board-template` も Import時に受理

### CSV
- ファイル名: `[Board名].csv`
- 形式:
  - `schemaVersion = 1`
  - `format = stockmanager-board-export-csv`
  - メタ情報 + item行の2ブロック構成

## 15.4 インポート仕様
- `Merge` ではなく `Add` のみ
- 同名ボードは許容（IDが異なる別ボードとして追加）
- 同一ボード判定は行わない
- `schemaVersion != 1` は拒否
- `board.name` が空なら拒否
- `item.name` が空ならその項目のみスキップ
- item上限: `500`（暫定）

## 15.5 欠損許容と補完ルール
- 欠損許容: `exportId`, `inStock`, `createdAt`, `updatedAt`
- 補完（Import時）:
  - Board:
    - DB-ID: 新規採番
    - `createdAt = now`
    - `exportId` がなければ生成
  - Item:
    - DB-ID: 新規採番
    - `inStock = true`（欠損時）
    - `createdAt = now`（欠損時）
    - `updatedAt = now`（欠損時）
    - `exportId` がなければ生成

## 15.6 実装メモ（2026-03-03時点）
- 変換ロジック:
  - `data/BoardTransferCodec.kt`
- Repository API:
  - `StockRepository.buildBoardExport(...)`
  - `StockRepository.importBoard(...)`
- ViewModel API:
  - `StockManagerViewModel.exportCurrentBoard(...)`
  - `StockManagerViewModel.importBoard(...)`
- UI連携:
  - `ui/overlay/BoardDrawerOverlay.kt`
  - `ui/StockManagerScreen.kt` (SAF: CreateDocument/OpenDocument)

## 15.7 DBスキーマ更新
- `AppDatabase` version: `3 -> 4`
- 追加カラム:
  - `boards.created_at`
  - `boards.export_id`
  - `stock_items.updated_at`
  - `stock_items.export_id`
- Migration:
  - `MIGRATION_3_4`
