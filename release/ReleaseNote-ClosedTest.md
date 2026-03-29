```update template
v0.0.0

- 内容1
- 内容2

```

---


```1.0.0
<ja-JP>
StockManager をリリースしました。

ホワイトボードにマグネットを貼る感覚で、
家庭の在庫や欠品状況を簡単に管理できるアプリです。

- マグネットを裏返して在庫 / 欠品を切り替え
- 複数のボードを作成してカテゴリごとに管理
- アイテムの追加 / 編集 / 削除
- データのインポート / エクスポート機能
</ja-JP>

<en-US>
Initial release of StockManager.

StockManager allows you to manage household inventory
like magnets on a whiteboard.

Features:
- Flip magnets to switch between In Stock / Out of Stock
- Create and manage multiple boards
- Add, edit, and delete items
- Import / Export data
</en-US>
```


```1.1.0
<ja-JP>
v1.1.0

- アイテムのステータスを「赤色」「黄色」「白色」の3種類に変更
- 「在庫あり・要確認・在庫なし」や「予備あり・予備なし・在庫なし」など、用途に合わせてステータスを使い分けられるようになりました。
</ja-JP>

<en-US>
v1.1.0
- Changed item status to three types: "Red", "Yellow", and "White"
- You can now use the statuses according to your needs, such as "In Stock / Needs Confirmation / Out of Stock" or "Spare Available / No Spare / Out of Stock".
</en-US>
```

```1.1.1
<ja-JP>
v1.1.1
【新機能】
- アイテムのステータスを「赤色」「黄色」「白色」の3種類に変更
- 「在庫あり・要確認・在庫なし」や「予備あり・予備なし・在庫なし」など、用途に合わせてステータスを使い分けられるようになりました。

【修正点】
- フィルターボタンのテキストを修正
- データベースの不整合を修正
</ja-JP>

<en-US>
v1.1.1
[New Features]
- Changed item status to three types: "Red", "Yellow", and "White"
- You can now use the statuses according to your needs, such as "In Stock / Needs Confirmation / Out of Stock" or "Spare Available / No Spare / Out of Stock".

[Fixes]
- Fixed filter button text
- Fixed database inconsistency
</en-US>
```


```1.1.2
<ja-JP>
v1.1.2
【修正点】
- "ボードを追加・編集"ボタンがナビゲーションボタンと被る問題を修正
- ボード名を変更の際，文字数と上限文字数の両方を表示するように修正
- すべてのテキストボックスで，オーバーレイが表示された時点でテキストボックスをアクティブにするように修正
</ja-JP>

<en-US>
v1.1.2
[Fixes]
- Fixed issue where "Add/Edit Board" button overlaps with navigation buttons
- When changing board name, both current character count and maximum character limit are now displayed
- For all text boxes, the text box will now become active as soon as the overlay is displayed
</en-US>
```

```1.1.3
<ja-JP>
v1.1.3
【修正点】
- ユーザーインターフェースを修正
- ステータスバーの視認性向上
- 検索バーのレイアウト修正
</ja-JP>

<en-US>
v1.1.3
[Fixes]
- Updated user interface
- Improved visibility of status bar
- Adjusted layout of search bar
</en-US>
```

```1.1.4
<ja-JP>
v1.1.4
【修正点】
- アニメーションの修正
- 在庫→欠品の切り替えの際、切り替えアニメーションの視認性向上
- ボードの並び替えの際、ドラッグアンドドロップのアニメーションを追加
</ja-JP>

<en-US>
v1.1.4
[Fixes]
- Updated animations
- Improved visibility of switch animation when toggling between In Stock and Out of Stock
- Added drag-and-drop animation when reordering boards
</en-US>
```

```1.1.5
<ja-JP>
v1.1.5
【新機能】
- アイテム名変更機能を実装
【修正点】
- ボード並び替えのアニメーションを修正
- 編集画面のレイアウトを修正
- アイテム削除ボタンの位置を修正
- 検索機能の不具合を修正
</ja-JP>

<en-US>
v1.1.5
[New Features]
- Implemented item name change feature
[Fixes]
- Updated animation for board reordering
- Updated layout of edit screen
- Adjusted position of item delete button
- Fixed bug in search functionality
</en-US>
```

```1.1.6
<ja-JP>
v1.1.6
【新機能】
- チュートリアル機能を実装
- ソートの昇順・降順の切り替えを実装
【修正点】
- ボードが作成されていない時のUIを修正
- ソート順の修正
</ja-JP>

<en-US>
v1.1.6
[New Features]
- Implemented tutorial feature
- Implemented ascending/descending sort toggle
[Fixes]
- Fixed UI when no boards are created
- Fixed sort order
</en-US>
```

```1.1.7
<ja-JP>
v1.1.7
【新機能】
- チュートリアル機能に進捗を追加
【修正点】
- 検索機能を修正
</ja-JP>

<en-US>
v1.1.7
[New Features]
- Added progress tracking to tutorial feature
[Fixes]
- Fixed search functionality
</en-US>
```


```1.1.8
<ja-JP>
v1.1.8
【新機能】
- アイテム名の重複を防ぐ機能を実装
- 利用規約を追加
</ja-JP>

<en-US>
v1.1.8
[New Features]
- Implemented feature to prevent duplicate item names
- Added terms of use
</en-US>
```

```1.1.9
<ja-JP>
v1.1.9
【新機能】
- ボードをまたいだ欠品リスト出力機能を試験的に実装
- データの削除機能を実装

【修正点】
- 外部ツールへの遷移ボタンのテキストを変更
- 初期状態のボード1へアイテム1を追加
</ja-JP>

<en-US>
v1.1.9
[New Features]
- Trial implementation of out-of-stock list output function across boards
- Implemented data deletion feature

[Fixes]
- Changed text of button for transitioning to external tools
- Added Item 1 to Board 1 in initial state
</en-US>
```

```1.2.0
<ja-JP>
v1.2.0
【修正点】
- 一部アイテムをタップしずらい問題を解決
- アイテム編集モードのレイアウトを修正
- チュートリアルの一部修正
- ナビゲーションバーに依存したレイアウトを修正
</ja-JP>

<en-US>
v1.2.0
[Fixes]
- Fixed issue where some items were difficult to tap
- Updated layout of item edit mode
- Fixed some issues in the tutorial
- Fixed layout dependent on navigation bar
</en-US>
```
