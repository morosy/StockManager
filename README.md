# StockManager

StockManager は、ボード単位で在庫管理できる Android アプリです。  
Jetpack Compose で UI を構築し、Room でローカル永続化しています。

初回起動時は空の状態から始まり、アプリ内チュートリアルで基本操作を案内します。

## 主な機能

- ボード管理
  - ボードの追加・削除・並び替え・名前変更
  - ボードがない場合の空状態表示
- マグネット（アイテム）管理
  - 追加・削除・3状態の切り替え（白 / 黄 / 赤）
  - 名前入力の最大文字数制限（24文字）
- 一覧機能
  - 在庫/欠品フィルタ
  - ソート（古い順 / 新しい順 / 名前順 / 在庫順 / 欠品順）
  - 検索
- データ連携
  - JSON / CSV エクスポート
  - JSON / CSV インポート
- ガイド
  - 初回チュートリアル
  - メニューの`使い方`からチュートリアルを再表示
- 情報画面
  - About（バージョン/コピーライト）
  - OSSライセンス
  - プライバシーポリシー

## 現在の仕様メモ

- 初期状態ではボードもアイテムも存在しません
- `在庫順`は `白 -> 黄 -> 赤`
- `欠品順`は `赤 -> 黄 -> 白`
- CSV は旧形式との互換を保ちながら `status` 列にも対応しています

## 技術スタック

- Kotlin
- Jetpack Compose / Material 3
- AndroidX Room
- Coroutines
- Gradle Version Catalog (`gradle/libs.versions.toml`)

## 開発環境

- Android Studio (最新安定版推奨)
- JDK 17
- Android SDK

## ビルド

```bash
bash ./gradlew :app:assembleDebug
```

## テスト

```bash
bash ./gradlew testDebugUnitTest
```

## ライセンス

MIT License
