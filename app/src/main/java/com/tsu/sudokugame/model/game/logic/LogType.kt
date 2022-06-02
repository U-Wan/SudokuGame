package com.tsu.sudokugame.model.game.logic

enum class LogType(val description: String) {
    GIVEN("Mark given"), SINGLE("Mark only possibility for cell"), HIDDEN_SINGLE_ROW("Mark single possibility for value in row"), HIDDEN_SINGLE_COLUMN(
        "Mark single possibility for value in column"
    ),
    HIDDEN_SINGLE_SECTION("Mark single possibility for value in section"), GUESS("Mark guess (start round)"), ROLLBACK(
        "Roll back round"
    ),
    NAKED_PAIR_ROW("Remove possibilities for naked pair in row"), NAKED_PAIR_COLUMN("Remove possibilities for naked pair in column"), NAKED_PAIR_SECTION(
        "Remove possibilities for naked pair in section"
    ),
    POINTING_PAIR_TRIPLE_ROW("Remove possibilities for row because all values are in one section"), POINTING_PAIR_TRIPLE_COLUMN(
        "Remove possibilities for column because all values are in one section"
    ),
    ROW_BOX("Remove possibilities for section because all values are in one row"), COLUMN_BOX("Remove possibilities for section because all values are in one column"), HIDDEN_PAIR_ROW(
        "Remove possibilities from hidden pair in row"
    ),
    HIDDEN_PAIR_COLUMN("Remove possibilities from hidden pair in column"), HIDDEN_PAIR_SECTION("Remove possibilities from hidden pair in section");

}