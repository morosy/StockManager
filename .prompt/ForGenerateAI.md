# ForGenerateAI.md

## StockManager コンテキストガイド
最終更新: 2026-03-22

このファイルは、StockManager リポジトリを AI が安全かつ一貫して扱うための要約です。実装変更・レビュー・不具合修正・リリースノート更新の前に、ここに書かれた前提と重要ファイルを確認してください。

---

## 1. プロダクト概要

StockManager は、家庭内在庫をホワイトボード上のマグネット感覚で管理する Android アプリです。

主要機能:
- Jetpack Compose ベースのシングルアクティビティ構成
- Room によるローカル永続化
- 複数ボード管理
- アイテムの追加・削除・状態切り替え
- ボード順の並び替え
- 複数ボード横断の欠品リスト表示
- JSON / CSV のインポート・エクスポート
- アプリ内ヘルプ、About、OSS ライセンス、プライバシーポリシー表示

---

## 2. 現在のバージョン

- アプリ versionName: `1.1.8`
- アプリ versionCode: `10`
- Room DB version: `7`
- パッケージ名: `com.morosy.stockmanager`

定義場所:
- `app/build.gradle.kts`
- `app/src/main/java/com/morosy/stockmanager/data/db/AppDatabase.kt`

---

## 3. 在庫状態モデル

StockManager の在庫状態は `Boolean` ではなく `status: Int` で表現します。

定義:
- `0 = IN_STOCK`
- `1 = HIGHLIGHTED`
- `2 = OUT_OF_STOCK`

意味:
- `0`: 白, 在庫あり
- `1`: 黄, 要確認または中間状態
- `2`: 赤, 欠品

トグル順:
- `白 -> 黄 -> 赤 -> 白 -> ...`

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/data/db/StockItemStatus.kt`
- `app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/components/MagnetCard.kt`

互換性:
- 旧データや旧フォーマットでは `inStock: Boolean` が残ることがあります
- 変換ルールは以下です
  - `inStock = true` -> `status = 0`
  - `inStock = false` -> `status = 2`

---

## 4. フィルター UI の現在仕様

フィルターは 2 ボタン構成です。

- `在庫`: 白・黄を含む「在庫あり側」
- `欠品`: 赤のみ

重要:
- 表示文言は現在 `在庫 / 欠品`
- 以前の `在庫 / 出庫` は最新仕様ではない
- `在庫` と `欠品` の間には小さなギャップがある
- セグメントの押下色は角丸形状に沿って描画される

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/components/FilterSegmentedRow.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`

---

## 5. JSON / CSV 仕様

### 5.1 JSON

JSON には主に 2 系統あります。

1. テンプレート JSON
- `format = "stockmanager-board-template"`
- 主に `board.name` と `items[].name` を扱う
- 状態値は省略可能

2. エクスポート JSON
- `format = "stockmanager-board-export"`
- `exportId`, `createdAt`, `updatedAt`, `status` を含む

現行の基本フィールド:
- `items[].status`

互換性:
- インポート時は旧 `items[].inStock` も受け入れる
- 現在の export JSON は `status` と `inStock` を両方出力する
- import 時は `status` を優先し、無い場合は `inStock` から復元する

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/data/BoardTransferCodec.kt`
- `example/example.json`
- `example/exampleMin.json`
- `.idea/sync/example_template.json`

### 5.2 CSV

CSV は旧互換を維持しつつ、現行では `status` 列も扱います。

主な列:
- `type`
- `exportId`
- `name`
- `status`
- `inStock`
- `createdAt`
- `updatedAt`

注意:
- 新形式 CSV は `status` により 3 状態を保持できる
- 旧形式 CSV は `inStock` のみでも引き続き import 可能
- import 時は `status` を優先し、無い場合は `inStock` から復元する

---

## 6. ソート仕様

`SortMode` は以下を持ちます。

- `OLDEST`
- `NEWEST`
- `NAME`
- `NAME_DESC`
- `STOCK_FIRST`
- `OUT_FIRST`

重要:
- `在庫順 (STOCK_FIRST)` は `白 -> 黄 -> 赤`
- `欠品順 (OUT_FIRST)` は `赤 -> 黄 -> 白`
- 同順位内は `name` 昇順
- 旧実装のような「在庫側 / 欠品側」の2値比較ではない
- ソートボタン右側の矢印で、次のペアを相互切替する
  - `古い順` <-> `新しい順`
  - `名前順` <-> `名前逆順`
  - `在庫順` <-> `欠品順`
- 矢印の向きは現在の並び方向に応じて変化する

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/model/SortMode.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/components/SortSplitButton.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`

---

## 7. ホーム画面の空状態表示

ホーム画面には空状態用テキスト表示があります。

優先順位:
- ボードが 1 つもない場合:
  - `ボートがありません`
  - `ボードを追加してください`
- ボードは存在するが、選択中ボード内にアイテムがない場合:
  - `アイテムがありません`
  - `アイテムを追加してください`

補足:
- この表示は実データの有無で判定する
- 検索やフィルター結果が 0 件でも、空状態メッセージには切り替えない

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`

---

## 8. 初期状態

新規インストール直後は、初期シードとして `ボード1` という名前の空ボードを1つだけ作成します。

重要:
- 初回起動時にだけ `ensureSeeded()` が走る
- 既存ユーザーが全ボードを削除した状態では、自動で `ボード1` を再生成しない
- 初期状態でアイテムは0件

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt`

---

## 9. チュートリアル仕様

チュートリアルは静的説明画面ではなく、UI上の対象をハイライトしながら進むオーバーレイです。

起動条件:
- 新規インストール直後の初回起動時
- ドロワーメニューの`使い方`タップ時

進行ルール:
- `次へ`で進行できる
- `戻る`で1ステップ戻れる
- `スキップ`で即終了できる
- オーバーレイ上部に現在位置と進捗バーを表示する
- 初期状態では `ボード1` があるため、そのまま進行できる
- チュートリアル開始時に、`ボードを追加`ステップを含む 12 枚構成か、除外した 11 枚構成かを決定する
- ただし、ボードが0件の状態でチュートリアルを起動した場合は、`ボードを追加`ステップで実際にボードを作成するまで先へ進めない
- 最終ステップでは「このチュートリアルは、メニューの『使い方』よりいつでも見ることができます」と案内する
- 最終ステップでは `スキップ` を表示しない

現在の流れ:
- ボード一覧を開く
- ボード編集を開く
- ボードを追加
- アイテムを追加
- アイテムを編集
- ボード一覧
- 表示切り替え
- ボード名を編集
- 絞り込み
- 並び替え
- 欠品リストを表示
- チュートリアルについて

重要:
- `AppInfoScreenType.HOW_TO_USE`の静的画面は実質使っていない
- チュートリアルオーバーレイは`StockManagerScreen.kt`と`TutorialOverlay.kt`で制御している
- ターゲット座標は同一Compose root上で扱う
- ハイライトは円形で、最後の`チュートリアルについて`ステップにはハイライトがない
- 説明文は中央揃え
- `次へ`で実際のボタンを押さなくても必要な画面遷移が進む
- 既存ボードがある状態で開始した場合は、`ボードを追加`ステップを飛ばした 11 枚構成になる
- `アイテムを追加`、`ボード名を編集`では実際の入力オーバーレイを開かず、説明のみで進行する
- `使い方の場所`ステップは現在存在しない
- `欠品リストを表示` ステップは中央の Extended FAB を対象にする

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/overlay/TutorialOverlay.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/tutorial/TutorialModels.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/overlay/BoardDrawerOverlay.kt`

---

## 9.1 検索バー仕様

検索バーはトップバーの検索アイコンから開閉する。

重要:
- 入力値は `SettingsEntity.query` に保存される
- 検索バーの `閉じる` を押した場合、UI を閉じるだけでなく `query` も空文字に戻して絞り込みを解除する

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt`

---

## 9.2 欠品リスト表示仕様

`欠品リストを表示` は、複数ボードを横断して黄色・赤色アイテムだけを集約表示する機能です。

重要:
- 画面下部には 3 つの FAB が並ぶ
  - 左: 編集
  - 中央: `欠品リストを表示`
  - 右: 追加
- 3つの FAB は紫背景で統一する
- 中央 FAB は `ExtendedFloatingActionButton`
- 中央 FAB は左右 FAB との間隔が `16dp` になる幅で配置する
- FAB 用の背景帯は作らず、ボード上に浮いて見える前提にする
- 選択画面では全ボードが初期選択される
- 選択対象はボード単位で複数選択可能
- 選択済みボードだけから `HIGHLIGHTED` と `OUT_OF_STOCK` を抽出する
- 一覧はボードごとのセクションで表示する
- 結果カードは read only で、タップしても状態変更しない
- ボード選択ステップは外タップで閉じられる
- 結果表示ステップは外タップで閉じない

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/overlay/ShoppingListOverlay.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/shopping/ShoppingListModels.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/components/ShoppingListItemCard.kt`

---

## 9.3 OSS ライセンス表示仕様

OSS ライセンス画面は、静的テキストをアプリ内オーバーレイで表示する方式です。

重要:
- 表示内容は `app/src/main/res/raw/oss_licenses.txt` をそのまま読み込む
- 表示対象は主にアプリ本体で利用するランタイム依存
- テスト専用依存やビルドプラグインは原則として表示対象から除外する
- 現在の主要ランタイム依存は Apache License 2.0 で統一されている
- 依存バージョン更新時は、`gradle/libs.versions.toml` と `app/build.gradle.kts` を確認して `oss_licenses.txt` も同期する

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/ui/overlay/AppInfoScreenOverlay.kt`
- `app/src/main/res/raw/oss_licenses.txt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

---

## 10. アーキテクチャ概要

基本構成:
- UI: Compose
- 状態管理: ViewModel
- 永続化とビジネスロジック: Repository
- DB: Room

依存関係の流れ:
- `MainActivity`
  - `StockManagerScreen` を表示
- `StockManagerScreen`
  - `StockManagerViewModel` を利用
- `StockManagerViewModel`
  - `StockRepository` を利用
- `StockRepository`
  - Room DAO と `BoardTransferCodec` を利用

現状メモ:
- 専用 DI フレームワークは未導入
- `AppDatabase.getInstance(app)` を直接利用する構成

---

## 11. DB 構造と migration

DB:
- 名前: `stockmanager.db`
- Room Database: `AppDatabase`
- 現行 version: `7`

主なテーブル:
- `boards`
- `stock_items`
- `settings`

### 10.1 `stock_items` の現行スキーマ

主なカラム:
- `id`
- `board_id`
- `name`
- `status`
- `created_at`
- `updated_at`
- `export_id`

存在しない前提の旧カラム:
- `in_stock`

### 10.2 migration 履歴

- `2 -> 3`
  - `boards.sort_order` を追加
- `3 -> 4`
  - `boards.created_at`
  - `boards.export_id`
  - `stock_items.updated_at`
  - `stock_items.export_id`
- `4 -> 5`
  - 旧 `in_stock` ベースの `stock_items` から、新 `status` ベースのテーブルへ再作成移行
- `5 -> 6`
  - 壊れた `v5` スキーマを救済
  - `in_stock` が残っている端末でも `stock_items` を再構築して整合を取る
- `6 -> 7`
  - `settings.tutorial_seen` を追加

### 10.3 重要な背景

`v1.1.0` 配信時、`4 -> 5` migration が不完全で `in_stock` が残り、Room 検証でクラッシュする不具合がありました。

`v1.1.1` では以下を実施済みです。
- DB version を `6` に引き上げ
- `4 -> 5` を正しい再構築 migration に修正
- `5 -> 6` を追加し、壊れた `v5` スキーマも救済

重要方針:
- 旧ユーザーデータは保持する前提
- データ削除や destructive fallback は既定手段にしない

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/data/db/AppDatabase.kt`
- `app/src/main/java/com/morosy/stockmanager/data/db/Entities.kt`
- `app/src/main/java/com/morosy/stockmanager/data/db/SettingsEntity.kt`

---

## 11. 主なソース配置

```text
app/src/main/java/com/morosy/stockmanager/
|- MainActivity.kt
|- AppLimits.kt
|- model/
|  |- SortMode.kt
|- data/
|  |- StockRepository.kt
|  |- BoardTransferCodec.kt
|  |- db/
|     |- AppDatabase.kt
|     |- BoardDao.kt
|     |- StockDao.kt
|     |- SettingsDao.kt
|     |- Entities.kt
|     |- SettingsEntity.kt
|     |- Relations.kt
|     |- StockItemStatus.kt
|- ui/
|  |- StockManagerScreen.kt
|  |- StockManagerViewModel.kt
|  |- components/
|  |  |- FilterSegmentedRow.kt
|  |  |- MagnetCard.kt
|  |  |- ShoppingListItemCard.kt
|  |  |- SortSplitButton.kt
|  |- modal/
|  |  |- AddItemModal.kt
|  |- overlay/
|     |- AppInfoScreenOverlay.kt
|     |- BoardAddModal.kt
|     |- BoardDrawerOverlay.kt
|     |- ConfirmBoardDeleteDialog.kt
|     |- RenameBoardOverlay.kt
|     |- ShoppingListOverlay.kt
|     |- TutorialOverlay.kt
|  |- shopping/
|     |- ShoppingListModels.kt
|  |- tutorial/
|     |- TutorialModels.kt
|- app/src/test/java/com/morosy/stockmanager/
|  |- data/
|  |  |- BoardTransferCodecTest.kt
|  |  |- db/
|  |     |- StockItemStatusTest.kt
|  |- model/
|     |- SortModeTest.kt
|  |- ui/
|  |  |- shopping/
|  |  |  |- ShoppingListModelsTest.kt
```

---

## 12. 制約と定数

主な制約:
- `MAX_ITEM_NAME_LENGTH = 24`
- `MAX_BOARD_NAME_LENGTH = 10`

その他:
- インポート最大アイテム数: `500`
- `minSdk = 24`
- `targetSdk = 35`
- `compileSdk = 36`
- JDK: `17`

関連ファイル:
- `app/src/main/java/com/morosy/stockmanager/AppLimits.kt`
- `app/build.gradle.kts`

---

## 10. 使用技術

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Activity / Lifecycle
- Room
- Coroutines
- SplashScreen API
- KSP
- Gradle Version Catalog

### 10.1 UI テーマ補足

- `MainActivity` では `StockManagerTheme` を通して UI を描画する
- `MainActivity` で `enableEdgeToEdge()` を有効化する
- ステータスバーは Compose テーマ側で制御する
- ノーマルモードではダークアイコン、ダークモードではライトアイコンを使う
- `WindowCompat.getInsetsController(...)` で system bar icon appearance を切り替える
- `statusBarColor` のような非推奨 API には依存しない
- 動的カラーは使わず、紫系の固定カラースキームを優先する
- 基準色は `#6750A4` と `#E7E0EC`
- 主要 UI は `MaterialTheme.colorScheme` を基準にし、ライト/ダーク両モードで破綻しないようにする
- ボード一覧では、アクティブなボードを濃い紫系背景で強調し、非アクティブでも背景境界が見える薄い面を持たせる
- ホーム画面下部の 3 つの FAB は紫背景で統一し、中央の欠品リスト FAB は横長にする
- アイテムカードは 12 文字 x 2 行でも違和感が出にくいよう、やや小さめの文字サイズと少し強めの角丸を使う
- 検索バーはフィルター列から 16dp 程度の間隔を空け、角丸を付け、右側に `閉じる` 操作用のボタンを置く
- アイテムの裏返しアニメーションは視認性を優先し、完了後に状態更新が反映されるようにする
- ボード並び替え中は、長押ししたタイル全体が外側の影付きで強く持ち上がり、指を置いた位置を保ったまま追従することを優先する
- 並び替え判定は隣接タイルの中央ラインを越えた時点で行い、越された側のタイルはスプリングで弾かれるように移動する
- 複数段を跨ぐドラッグでも、隣接 swap を順次積み上げる形で並び替えられる前提にする
- 長押し継続中は保持タイル自体に並び替えアニメーションをかけず、リリース後にだけ正しい位置へゆっくり戻す
- 越された側のタイル移動は保持タイルより遅めに見えるよう、配置アニメーション時間を長く取る

---

## 11. AI が変更前に確認すべき重要ファイル

実装変更前に優先して読む:
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerScreen.kt`
- `app/src/main/java/com/morosy/stockmanager/ui/StockManagerViewModel.kt`
- `app/src/main/java/com/morosy/stockmanager/data/StockRepository.kt`
- `app/src/main/java/com/morosy/stockmanager/data/BoardTransferCodec.kt`
- `app/src/main/java/com/morosy/stockmanager/data/db/AppDatabase.kt`
- `app/src/main/java/com/morosy/stockmanager/data/db/Entities.kt`
- `app/src/main/java/com/morosy/stockmanager/data/db/StockItemStatus.kt`

---

## 12. AI が特に気をつける点

- `status` と `inStock` の互換性を壊さない
- CSV は 3 状態を完全保持できない前提を守る
- `showStock` / `showOut` のフィルター意味を変えない
- 欠品リスト表示は黄色・赤のみを対象とする
- ボード順は `sort_order` で保持される
- `currentBoardId` は `settings` に保存される
- migration 変更時は既存ユーザーデータ保持を優先する
- 文言変更時は UI の現在仕様 `在庫 / 欠品` を基準にする

避けるべきこと:
- DB 不整合を無視した schema 変更
- import/export 互換性を考慮しないフィールド削除
- 既存 JSON / CSV サンプルを根拠なく更新すること
- 制約値を `AppLimits.kt` を見ずに変更すること

---

## 13. 推奨テスト

基本:

```bash
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:testDebugUnitTest
```

変更内容に応じて見るポイント:
- 旧 JSON をインポートできるか
- 旧 `inStock` データが `status` に正しく変換されるか
- CSV エクスポート / インポートで既存仕様を壊していないか
- フィルターとソートが 3 状態対応のまま動くか
- ボード順変更が保持されるか
- `v1.0.0` 相当 DB から `v1.1.1` へ更新して起動できるか

---

## 14. 今後の改善候補

- Room schema export を有効化して migration テストを整備
- CSV の 3 状態表現対応を検討
- UI 文言の `strings.xml` 集約
- Repository / DB 生成の DI 導入
- `StockManagerScreen.kt` の責務分割
- README とリリース関連ドキュメントの UTF-8 正規化

---

## 15. 変更時の原則

このプロジェクトで AI が提案・実装する変更は、次の原則を守ること。

- 既存ユーザーの保存データを優先して守る
- UI / DB / import-export の 3 領域の整合を見る
- 互換性に関わる変更は、コードだけでなく migration とサンプルも確認する
- 1 ファイルだけ見て判断せず、関連層を最低限たどる
- 変更後はコンパイル確認を行う
