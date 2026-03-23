# 買い物リスト表示機能 実装仕様書

## 1. 目的

- 画面下部中央に `欠品リストを表示` FAB を配置する。
- FAB 押下後、オーバーレイ上で複数ボードを選択し、選択されたボード内の黄色・赤色アイテムを一覧表示する。
- 欠品リスト内ではカードをタップしてステータスをドラフト変更でき、`保存して閉じる` で初めて実データへ反映する。
- 保存前に白へ変わったアイテムも、そのままリスト内へ残して比較確認できるようにする。

## 2. 実装済み仕様の要点

### 2.1 画面構成

- メイン画面は [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt) に集約されている。
- 下部には 3 つの FAB を配置する。
  - 左下: 編集
  - 中央: `欠品リストを表示`
  - 右下: 追加
- 3 つの FAB は紫背景で統一する。
- 中央 FAB は `ExtendedFloatingActionButton` で、左右 FAB との間隔が `16dp` になるように幅を確保する。
- FAB 用の背景帯は作らず、ボード上に浮いて見えるレイアウトとする。

### 2.2 データ取得と保存

- `StockManagerViewModel` の `uiState.boards` から全ボード・全アイテムを取得する。
- 欠品リスト用の表示セクション生成は [`ShoppingListModels.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt) で行う。
- 結果画面でのステータス変更はオーバーレイ内のドラフト state に対して行う。
- `保存して閉じる` 押下時にだけ [`StockManagerViewModel.saveShoppingListChanges()`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt) から [`StockRepository.updateItemStatuses()`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt) を呼び、DB へ反映する。
- DB スキーマ変更や migration 追加は不要。

### 2.3 ステータス定義

- [`StockItemStatus.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/data/db/StockItemStatus.kt)
  - `IN_STOCK = 0` 白
  - `HIGHLIGHTED = 1` 黄色
  - `OUT_OF_STOCK = 2` 赤
- 欠品リストの抽出対象は `HIGHLIGHTED` と `OUT_OF_STOCK`。
- 結果画面内のタップでも通常と同じ `白 -> 黄 -> 赤 -> 白` で遷移する。

## 3. オーバーレイ仕様

### 3.1 ボード選択ステップ

- タイトルは `表示するボードを選択`。
- 補助文 `複数選択できます` は左右中央揃え。
- ボード一覧は複数選択可能。
- 初期状態は全ボード選択。
- 各選択肢は、選択時に背景色が変わり、右側にチェックマークを出す。
- 各選択肢のボード名は左右中央揃え。
- `表示` は 1 件以上選択されるまで有効。
- オーバーレイ外タップで閉じられる。

### 3.2 結果表示ステップ

- タイトルは `欠品リスト`。
- ボードごとにセクションを分けて表示する。
- 各セクションのボード名は左右中央揃え。
- オーバーレイ外タップでは閉じない。
- 結果一覧の並び順は `黄色 -> 赤色` を最優先にし、その中で現在のソートモードを適用する。
- 抽出対象がないボードは結果一覧から省略する。
- 全選択ボードに対象がない場合は `対象のアイテムはありません` を表示する。

### 3.3 ドラフト編集と保存

- 結果カードは [`ShoppingListItemCard.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt) で表示する。
- カードタップでドラフト状態の `status` を切り替える。
- ステータス変更で白になっても、そのオーバーレイ表示中は非表示にしない。
- 変更がなければ下部ボタンは `閉じる`。
- 変更があると下部ボタンは `保存して閉じる` に変わる。
- `保存して閉じる` を押したときのみ変更を DB へ反映する。

### 3.4 破棄確認

- 変更がない場合、`X` ボタンや戻る操作でそのまま閉じる。
- 変更がある場合、`X` ボタンや戻る操作で警告ダイアログを表示する。
- ダイアログの選択肢は `閉じる` と `キャンセル`。
- `閉じる` は変更を破棄してオーバーレイを閉じる。
- `キャンセル` は欠品リストへ戻る。

## 4. 実装構成

### 4.1 主要ファイル

- [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt)
  - FAB 表示
  - オーバーレイ open/close
  - ボード選択 state
  - 保存処理の接続

- [`app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt)
  - ボード選択 UI
  - 結果表示 UI
  - ドラフト state 管理
  - 破棄確認ダイアログ

- [`app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt)
  - セクション生成
  - 欠品リスト専用ソート

- [`app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt)
  - 欠品リスト結果カード
  - クリック可能な Surface 対応

- [`app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt)
  - `saveShoppingListChanges()` の公開

- [`app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt)
  - `updateItemStatuses()` の追加

### 4.2 表示用モデル

[`ShoppingListModels.kt`](/d:/dev/StockManager/app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt) では、以下のモデルを使う。

```kotlin
data class ShoppingListBoardSection(
    val boardId: Long,
    val boardName: String,
    val items: List<StockItemEntity>
)
```

## 5. 現在の設計判断

- 欠品リストの抽出元は既存の `uiState.boards` を使う。
- ボード順は既存ボード順を維持する。
- ボード内アイテム順は、欠品リスト専用に `黄色 -> 赤色` を優先しつつ、各グループ内で現在のソートモードを反映する。
- 保存前の変更は DB に書かず、オーバーレイ内ローカル state に閉じ込める。
- `MagnetCard` は本体画面専用の責務が大きいため、欠品リスト結果には専用の `ShoppingListItemCard` を使う。

## 6. テスト観点

### 6.1 ロジック

- 黄色・赤色だけが抽出される
- ボード順が維持される
- 欠品リストでは `黄色 -> 赤色` が優先される
- 同色内ではソートモードに応じた順序になる
- 対象アイテム 0 件のボードは除外される
- 全ボード 0 件なら空状態になる

### 6.2 UI

- 下部 3 FAB の配置と色が仕様通り
- ボード選択ステップは全選択で開く
- ボード選択の補助文とボード名が中央揃え
- ボード選択ステップは外タップで閉じる
- 結果ステップは外タップで閉じない
- 結果見出しのボード名が中央揃え
- 結果カードをタップすると見た目の状態が変わる
- 白に変わったアイテムも保存前はリストに残る
- 変更時は `保存して閉じる` へ変わる
- 変更時に `X` を押すと破棄確認が出る

## 7. 結論

- 欠品リスト機能は、単なる read only 一覧ではなく、複数ボード横断のドラフト編集付き確認画面として実装済み。
- 永続化は保存時のみ行うため、一覧の視認性と誤操作防止の両立ができている。
- 差分の中心は `ShoppingListOverlay` と保存経路の追加であり、DB スキーマ変更は不要。
