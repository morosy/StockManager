package com.morosy.stockmanager.ui.tutorial

import androidx.compose.ui.geometry.Rect

enum class TutorialTarget {
    NONE,
    NAV_MENU,
    BOARD_EDIT,
    BOARD_ADD,
    ITEM_ADD_FAB,
    ITEM_EDIT_FAB,
    BOARD_LIST,
    CURRENT_BOARD_ITEM,
    CURRENT_ITEM,
    BOARD_TITLE,
    FILTER_ROW,
    SORT_BUTTON,
    SHOPPING_LIST_FAB
}

enum class TutorialStep(
    val target: TutorialTarget,
    val title: String,
    val message: String
) {
    OPEN_BOARD_LIST(
        target = TutorialTarget.NAV_MENU,
        title = "ボード一覧を開く",
        message = "ここからボード一覧とメニューを開きます。"
    ),
    OPEN_BOARD_EDIT(
        target = TutorialTarget.BOARD_EDIT,
        title = "ボード編集を開く",
        message = "ボードの追加や並び替えはここから行います。"
    ),
    EXPLAIN_ITEM(
        target = TutorialTarget.CURRENT_ITEM,
        title = "アイテムについて",
        message = "これがアイテムです。在庫状態は色で表示されます：白=在庫あり、黄=要確認、赤=欠品。"
    ),
    ADD_BOARD(
        target = TutorialTarget.BOARD_ADD,
        title = "ボードを追加する",
        message = "必要に応じて、ここからボードを追加できます。名前は自由に入力できます。"
    ),
    ADD_ITEM(
        target = TutorialTarget.ITEM_ADD_FAB,
        title = "アイテムを追加する",
        message = "このボタンからアイテムを追加します。名前は自由に入力できます。"
    ),
    EDIT_ITEM(
        target = TutorialTarget.ITEM_EDIT_FAB,
        title = "アイテムを編集する",
        message = "編集モードに切り替えると、名前変更や削除を行えます。"
    ),
    BOARD_LIST_OVERVIEW(
        target = TutorialTarget.BOARD_LIST,
        title = "ボード一覧",
        message = "サイドバーには作成したボードの一覧が表示されます。"
    ),
    BOARD_DISPLAY_SWITCH(
        target = TutorialTarget.CURRENT_BOARD_ITEM,
        title = "表示を切り替える",
        message = "ここをタップすると、表示するボードを切り替えられます。"
    ),
    RENAME_BOARD(
        target = TutorialTarget.BOARD_TITLE,
        title = "ボード名を編集する",
        message = "ボード名をタップすると、名前を変更できます。"
    ),
    FILTER_ITEMS(
        target = TutorialTarget.FILTER_ROW,
        title = "絞り込み",
        message = "在庫 / 欠品の表示を切り替えて、見たいアイテムだけを表示できます。"
    ),
    SORT_ITEMS(
        target = TutorialTarget.SORT_BUTTON,
        title = "並び替え",
        message = "ソートボタンから、アイテムの並び順を切り替えられます。"
    ),
    SHOW_SHOPPING_LIST(
        target = TutorialTarget.SHOPPING_LIST_FAB,
        title = "欠品リストを表示する",
        message = "このボタンから、複数ボードを横断した欠品リストを表示できます。"
    ),
    TUTORIAL_REMINDER(
        target = TutorialTarget.NONE,
        title = "チュートリアルについて",
        message = "このチュートリアルは、メニューの「使い方」よりいつでも見ることができます"
    );

    companion object {
        fun flow(includeAddBoardStep: Boolean): List<TutorialStep> {
            return buildList {
                add(OPEN_BOARD_LIST)
                add(OPEN_BOARD_EDIT)
                add(EXPLAIN_ITEM)
                if (includeAddBoardStep) {
                    add(ADD_BOARD)
                }
                add(ADD_ITEM)
                add(EDIT_ITEM)
                add(BOARD_LIST_OVERVIEW)
                add(BOARD_DISPLAY_SWITCH)
                add(RENAME_BOARD)
                add(FILTER_ITEMS)
                add(SORT_ITEMS)
                add(SHOW_SHOPPING_LIST)
                add(TUTORIAL_REMINDER)
            }
        }
    }
}

data class TutorialUiState(
    val step: TutorialStep,
    val targetRect: Rect?
)
