# StockManager

StockManager は、家庭内の在庫をホワイトボード上のマグネット感覚で管理する Android アプリです。
Jetpack Compose で UI を構築し、Room でローカル永続化を行います。

初期状態では `ボード1` と `アイテム1` が作成され、初回起動時にはチュートリアルを自動で開始します。

現在のアプリバージョンは `1.2.0` です。

## 主な機能

- ボードの追加・削除・並び替え・名称変更
- アイテムの追加・削除・名称変更
- アイテム状態の 3 段階管理
  - 在庫 / 要確認 / 欠品
- 検索
- フィルター
  - 在庫 / 欠品
- ソート
  - 古い順 / 新しい順 / 名前順 / 名前逆順 / 在庫順 / 欠品順
- 複数ボード横断の欠品リスト表示
- JSON / CSV のエクスポート
- JSON / CSV のインポート
- チュートリアル、About、OSS ライセンス、利用規約、プライバシーポリシー表示
- メニューからの全データ削除

## 現在の仕様メモ

- 在庫状態は `白 -> 黄 -> 赤` の順で切り替わります。
- フィルターは `在庫 / 欠品` の 2 ボタン構成です。
- ホーム画面下部には `編集 / 欠品リストを表示 / 追加` の 3 つの FAB を表示します。
- チュートリアルは静的画面ではなく、対象 UI をハイライトするオーバーレイ方式です。
- ホーム画面下部は edge-to-edge 描画で、FAB 直下の余白は維持しつつ、そのさらに下の system navigation inset 領域だけを端末設定に応じて可変にしています。

## 技術スタック

- Kotlin
- Jetpack Compose / Material 3
- AndroidX Activity / Lifecycle
- Room
- Coroutines
- SplashScreen API
- KSP
- Gradle Version Catalog

## 開発環境

- Android Studio
- JDK 17
- Android SDK

## ビルド

```bash
./gradlew.bat :app:assembleDebug
```

## テスト

```bash
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:testDebugUnitTest
```

## 関連ドキュメント

- AI 向けコンテキスト: `.prompt/ForGenerateAI.md`
- リリースノート: `release/release-note.md`
- OSS ライセンス一覧: `app/src/main/res/raw/oss_licenses.txt`

## ライセンス

- アプリ本体: MIT License
- 利用ライブラリ: アプリ内の `OSS ライセンス` 画面、および `app/src/main/res/raw/oss_licenses.txt` を参照してください。

`1.2.0` 時点では、`app/build.gradle.kts` と `gradle/libs.versions.toml` を確認した結果、ランタイム依存の追加はありませんでした。
そのため、OSS ライセンス一覧の更新は不要と判断しています。
