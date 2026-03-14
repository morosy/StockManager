package com.morosy.stockmanager.ui.tutorial

import androidx.compose.ui.geometry.Rect

enum class TutorialTarget {
    NAV_MENU,
    BOARD_EDIT,
    BOARD_ADD,
    ITEM_ADD_FAB,
    ITEM_EDIT_FAB,
    BOARD_TITLE,
    FILTER_ROW,
    SORT_BUTTON
}

enum class TutorialStep(
    val target: TutorialTarget,
    val title: String,
    val message: String,
    val requiresTargetTap: Boolean,
    val actionLabel: String
) {
    OPEN_BOARD_LIST(
        target = TutorialTarget.NAV_MENU,
        title = "ボード一覧を開く",
        message = "ここからボード一覧とメニューを開きます。",
        requiresTargetTap = true,
        actionLabel = "タップしてください"
    ),
    OPEN_BOARD_EDIT(
        target = TutorialTarget.BOARD_EDIT,
        title = "ボード編集を開く",
        message = "ボードの追加や並び替えはここから行います。",
        requiresTargetTap = true,
        actionLabel = "タップしてください"
    ),
    ADD_BOARD(
        target = TutorialTarget.BOARD_ADD,
        title = "ボードを追加する",
        message = "最初にボードを1つ追加してください。名前は自由に入力できます。",
        requiresTargetTap = true,
        actionLabel = "ボードを追加"
    ),
    ADD_ITEM(
        target = TutorialTarget.ITEM_ADD_FAB,
        title = "アイテムを追加する",
        message = "このボタンからアイテムを追加します。名前は自由に入力できます。",
        requiresTargetTap = true,
        actionLabel = "タップしてください"
    ),
    EDIT_ITEM(
        target = TutorialTarget.ITEM_EDIT_FAB,
        title = "アイテムを編集する",
        message = "編集モードに切り替えると、名前変更や削除を行えます。",
        requiresTargetTap = true,
        actionLabel = "タップしてください"
    ),
    RENAME_BOARD(
        target = TutorialTarget.BOARD_TITLE,
        title = "ボード名を編集する",
        message = "ボード名をタップすると、名前を変更できます。",
        requiresTargetTap = true,
        actionLabel = "タップしてください"
    ),
    FILTER_ITEMS(
        target = TutorialTarget.FILTER_ROW,
        title = "絞り込み",
        message = "在庫 / 欠品の表示を切り替えて、見たいアイテムだけを表示できます。",
        requiresTargetTap = false,
        actionLabel = "次へ"
    ),
    SORT_ITEMS(
        target = TutorialTarget.SORT_BUTTON,
        title = "並び替え",
        message = "ソートボタンから、アイテムの並び順を切り替えられます。",
        requiresTargetTap = false,
        actionLabel = "完了"
    );

    fun next(): TutorialStep? {
        return values().getOrNull(ordinal + 1)
    }
}

data class TutorialUiState(
    val step: TutorialStep,
    val targetRect: Rect?
)
