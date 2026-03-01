# StockManager (Android / Jetpack Compose)

在庫（Stock）/ 出庫（Out）をボード単位で管理する、Jetpack Compose 製のシンプルな管理アプリです。  
UI 先行で作っており、現状は **ローカル状態（in-memory）** で動作します。

---

## Features

- **Board 管理**
  - ボードの追加 / 切り替え / 削除（確認ダイアログあり）
  - ドロワー（オーバーレイ）でボード一覧を表示

- **Item 管理**
  - アイテム追加（モーダル）
  - 在庫 / 出庫の切替
  - 編集モードで削除（簡易アニメーションあり）

- **表示**
  - 在庫 / 出庫フィルタ（セグメント UI）
  - ソート（SplitButton）
  - 検索（テキスト入力）

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Version Catalog（`libs.versions.toml`）
- JVM target: 11

---

## Project Status

- ✅ build OK
- 現状は **永続化なし（Room 未導入）**
- 次フェーズ候補:
  - MVVM 化
  - Room 導入
  - Clean Architecture 化

（詳細は `GenerateAI-NextPhase.md` を参照）

---

## Requirements

- Android Studio (最新安定版推奨)
- JDK 11
- Android SDK

---

## Build / Run

```bash
# Windows (PowerShell)
./gradlew.bat :app:assembleDebug
```

Android Studio で app を Run してください。

---

## Notes

- FilterSegmentedRow は Compose の weight に起因するトラブル回避のため、独自 Layout で等幅配置しています。
- UI 主導の構成のため、StockManagerScreen に状態とロジックが集約されています（今後 ViewModel に移行予定）。

## LISENCE

MIT License

---
