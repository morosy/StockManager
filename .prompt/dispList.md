# 買い物リスト表示機能 実装仕様書

## 1. 目的

- 画面下部の操作エリアに `欠品リストを表示` FAB を追加する。
- FAB 押下後、オーバーレイ上で複数ボードを選択し、選択されたボード内の黄色・赤色アイテムだけを一覧表示する。
- 一覧表示はボードごとに区切り、色表現は通常のアイテム表示と同じ見た目に寄せる。
- 一覧オーバーレイ内でアイテムをタップしても状態変更はできない。

## 2. 現状確認

### 2.1 画面構成

- メイン画面は [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt) に集約されている。
- 画面下部の主操作は現在 2 つ。
  - 左下: 編集 FAB
  - 右下: 追加 FAB
- 現行UIには「TAB」コンポーネントは存在しないため、本要件の「TAB, 編集と追加の間」は、下部固定操作エリアの中央に横長ボタンを置く解釈で実装するのが自然。

### 2.2 データ取得

- `StockManagerViewModel` の `uiState` は `boards: List<BoardWithItems>` を保持しており、全ボードと全アイテムをすでに画面側へ渡している。
- `BoardWithItems` はボードとその配下アイテムをまとめて持つため、この機能のために新しい DAO / Repository API を増やさなくても実装可能。
- ボード順は `StockDao.observeBoardsWithItems()` の `ORDER BY sort_order ASC, id ASC` に従う。

### 2.3 ステータス定義

- [`app/src/main/java/com/morosy/stockmanager/data/db/StockItemStatus.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/data/db/StockItemStatus.kt)
  - `IN_STOCK = 0` 白
  - `HIGHLIGHTED = 1` 黄色
  - `OUT_OF_STOCK = 2` 赤
- 今回の抽出対象は `HIGHLIGHTED` と `OUT_OF_STOCK`。

### 2.4 既存UIの再利用ポイント

- 通常アイテム表示は [`app/src/main/java/com/morosy/stockmanager/ui/components/MagnetCard.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/components/MagnetCard.kt)。
- 既存オーバーレイは `Dialog` ベースのモーダルと、全画面オーバーレイの 2 系統がある。
- 本機能は「選択」→「結果表示」の 2 段階なので、新規 `Dialog` ベースの専用オーバーレイを 1 つ追加し、その中で表示モードを切り替える構成が扱いやすい。

## 3. 要件解釈と実装前提

### 3.1 FAB配置

- `欠品リストを表示` FAB は、編集 FAB と追加 FAB の間に置く。
- 3つの下部操作はすべて FAB 系コンポーネントで統一する。
- 中央 FAB は横長の `ExtendedFloatingActionButton` とする。
- 3つの FAB はボード上に浮いて見える配置とし、下部に専用の背景帯は作らない。
- 3つの FAB の背景色は紫系 (`colorScheme.primary`) とする。
- 中央 FAB は左右 FAB との間隔が常に `16dp` になる幅で表示する。
- ボード未作成時は、既存 FAB と同様に非表示でよい。

### 3.2 ボード選択オーバーレイ

- ボード一覧を複数選択可能にする。
- 初期状態は全ボード選択とする。
- 選択中の行は背景色を変える。
- 行の右端にチェックマークを表示する。
- 下部に `表示` ボタンを配置する。
- `表示` は 1 件以上選択されるまで無効化する。
- `表示するボードを選択` ステップでは、オーバーレイ外タップで閉じられる。

### 3.3 一覧表示オーバーレイ

- 選択済みボードから、黄色・赤色のみを抽出して表示する。
- 表示単位はボードごとのセクションにする。
- ボード名見出しの下に、そのボードの対象アイテムを並べる。
- 各アイテムは通常表示と同じ配色ルールで表示する。
- read only。タップしても状態変更しない。
- 結果表示ステップでは、オーバーレイ外タップで閉じない。

### 3.4 空状態

- 選択ボードのすべてに対象アイテムがない場合は、結果オーバーレイ内に「対象なし」の空状態メッセージを表示する。
- 一部のボードに対象アイテムがない場合、そのボードは結果一覧からは省略する。

## 4. 実装方針

### 4.1 責務の切り方

- DB / DAO / Repository / migration の変更は行わない。
- 画面表示用の選択状態は `StockManagerScreen` ローカル state で持つ。
- 「選択されたボードから黄色・赤色を抽出して、表示用のセクションに変換する処理」は pure な helper に切り出す。
  - 理由: `StockManagerScreen` の肥大化を抑え、テストしやすくするため。

### 4.2 追加する状態

`StockManagerScreen` に以下の state を追加する。

- `shoppingListOverlayOpen: Boolean`
- `shoppingListStep: ShoppingListOverlayStep`
  - `BoardSelection`
  - `Result`
- `selectedShoppingBoardIds: SnapshotStateList<Long>` もしくは `mutableStateListOf<Long>()`

初期選択は全ボード選択とする。

### 4.3 表示用モデル

新規ファイル候補:

- `app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt`

想定データ:

```kotlin
data class ShoppingListBoardSection(
    val boardId: Long,
    val boardName: String,
    val items: List<StockItemEntity>
)
```

想定 helper:

```kotlin
fun buildShoppingListSections(
    boards: List<BoardWithItems>,
    selectedBoardIds: Set<Long>,
    sortMode: SortMode
): List<ShoppingListBoardSection>
```

処理内容:

1. `boards` を選択されたボードIDで絞る
2. 各ボード内で `status == HIGHLIGHTED || status == OUT_OF_STOCK` のみ抽出
3. 既存の並び順ロジックに合わせてアイテムをソート
4. 対象アイテムが 1 件以上あるボードだけ `ShoppingListBoardSection` に変換

### 4.4 ソート方針

- ボード順は `ui.boards` の順序をそのまま使う。
- ボード内アイテム順は、通常一覧と整合するよう `ui.sortMode` を再利用する。
- 現在 `StockManagerScreen` にソート処理がインラインで書かれているため、共通 comparator / sort helper に切り出して再利用する。

## 5. UI設計

### 5.1 下部FAB

対象:

- [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt)

実装方針:

- `hasBoard` のときのみ表示
- 画面下部中央寄せ
- 左右 FAB と干渉しないよう、同じ `navigationBarsPadding()` を使って配置
- 背景帯は設けず、ボード上に直接浮かせる
- 3つの FAB はすべて紫背景・白文字/白アイコンで統一する
- 中央 FAB は左右に `96dp` ずつ余白を持たせ、左右 FAB との間隔を `16dp` にする

推奨レイアウト:

- 左: 編集 FAB
- 中央: `欠品リストを表示` Extended FAB
- 右: 追加 FAB

### 5.2 新規オーバーレイ

新規ファイル候補:

- `app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt`

1 つの composable の中で 2 ステップを切り替える。

- `BoardSelection` 表示
- `Result` 表示

追加仕様:

- ボード選択ステップでは `dismissOnClickOutside = true`
- 結果表示ステップでは `dismissOnClickOutside = false`

### 5.3 ボード選択ビュー

見た目:

- 上部: タイトル `表示するボードを選択`
- 中央: ボード一覧 `LazyColumn`
- 下部: `表示` ボタン

各行の仕様:

- ボード名を表示
- 選択時は `primaryContainer` 系の背景色に変更
- 右端に `check` アイコン
- タップで選択/解除

### 5.4 結果ビュー

見た目:

- 上部: タイトル `欠品リスト`
- 中央: ボードごとのセクション一覧
- 下部: `閉じる`

セクション仕様:

- セクション先頭にボード名
- ボードごとに divider または余白で区切る
- アイテムは 2 列のカード状表示にする

2 列表示の実装案:

- `LazyColumn` の各セクション内で `items.chunked(2)` を使って `Row` 単位に並べる
- 各カードは `weight(1f)` で幅を揃える
- 奇数個の行は末尾に空の `Spacer` か `Box(Modifier.weight(1f))` を入れて見た目を整える

これにより、通常画面の `MagnetCard` に近い表示をオーバーレイ内でも再現しやすい。

## 6. 既存コンポーネントの再利用方針

### 6.1 `MagnetCard` の扱い

結果一覧のアイテムを通常表示と同じ見た目に寄せるため、以下のいずれかで対応する。

案A: `MagnetCard` に read only モードを追加

- `readOnly: Boolean = false` を追加
- `readOnly == true` のときはタップしても `onToggle()` を呼ばない
- 編集UIも表示しない

案B: 見た目ロジックだけ共通化して、結果用カードを別 composable で作る

- 状態ごとの背景色・文字色・枠線決定処理を helper 化
- 結果オーバーレイ用に `ShoppingListItemCard` を作る

採用: 案B

- `MagnetCard` はアニメーションや編集モード制御を多く抱えている
- read only 要件だけのために責務を増やしすぎない方が安全
- 色と形の共通化だけ抽出した方が差分影響が小さい

## 7. 変更対象ファイル

### 7.1 変更

- [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt)
  - 下部 FAB 追加
  - オーバーレイ表示 state 追加
  - オーバーレイ呼び出し追加
  - ソート helper の切り出し

### 7.2 新規

- [`app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt)
  - ボード選択と結果表示を持つ専用オーバーレイ

- [`app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt)
  - 表示用モデル
  - section 構築 helper

- [`app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt)
  - 結果表示専用の read only カード

- [`app/src/test/java/com/morosy/stockmanager/ui/shopping/ShoppingListModelsTest.kt`](/d:/dev/StockManager/app/src/test/java/com/morosy/stockmanager/ui/shopping/ShoppingListModelsTest.kt)
  - 抽出ロジックの unit test

## 8. 文言方針

- 現状のプロジェクトでは Compose 内に日本語リテラルを直接持つ箇所が多い。
- この機能もまずは既存方針に合わせて UI 内リテラル追加でよい。
- ただし、将来的な多言語化や文言整理を考えるなら `strings.xml` への集約余地は残る。

今回追加する主な文言:

- `欠品リストを表示`
- `表示するボードを選択`
- `表示`
- `欠品リスト`
- `対象のアイテムはありません`
- `閉じる`

## 9. テスト観点

### 9.1 ロジック

- 1 ボード選択時に黄色・赤だけ抽出される
- 複数ボード選択時にボード順が維持される
- ソートモード変更時に結果一覧の並びが期待通り
- 対象アイテム 0 件のボードが除外される
- 全ボード 0 件なら空状態になる

### 9.2 UI

- FAB が編集 FAB と追加 FAB の間に表示される
- 3つの FAB が紫背景で統一される
- 中央 FAB と左右 FAB の間隔が `16dp` になる
- ボード複数選択時に背景色とチェックが切り替わる
- 初期状態で全ボードが選択されている
- 未選択時は `表示` が押せない
- ボード選択ステップでは外タップで閉じられる
- 結果表示ステップでは外タップで閉じない
- 結果オーバーレイ内でアイテムをタップしても状態変更されない
- FAB がボード上に浮いて見える

## 10. 実装順

1. `StockManagerScreen` からソート処理を helper 化
2. `ShoppingListBoardSection` と section 構築 helper を追加
3. `ShoppingListOverlay` を新規作成
4. 画面下部中央ボタンを追加
5. `StockManagerScreen` に選択 state とオーバーレイ遷移を組み込む
6. read only カードの見た目を通常カードに合わせる
7. 抽出ロジックの unit test を追加

## 11. 結論

- この機能は既存の `uiState.boards` をそのまま活用できるため、DB 層の変更なしで実装可能。
- 差分の中心は `StockManagerScreen` と新規オーバーレイ。
- 表示用データ生成を helper に切り出し、結果カードは read only 専用コンポーネントとして分ける方針が、影響範囲と保守性のバランスがよい。
