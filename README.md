# StockManager

StockManager は、ボード単位で在庫管理できる Android アプリです。  
Jetpack Compose で UI を構築し、Room でローカル永続化しています。

## 主な機能

- ボード管理
  - ボードの追加・削除・並び替え・名前変更
- マグネット（アイテム）管理
  - 追加・削除・在庫/欠品の切り替え
  - 名前入力の最大文字数制限（24文字）
- 一覧機能
  - 在庫/欠品フィルタ
  - ソート
  - 検索
- データ連携
  - JSON / CSV エクスポート
  - JSON / CSV インポート
- 情報画面
  - About（バージョン/コピーライト）
  - OSSライセンス
  - プライバシーポリシー

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
./gradlew.bat :app:assembleDebug
```

## ライセンス

MIT License
