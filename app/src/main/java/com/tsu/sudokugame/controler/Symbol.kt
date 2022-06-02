package com.tsu.sudokugame.controler

enum class Symbol(private val map: Array<String>) {
    SaveFormat(
        arrayOf(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P"
        )
    ),
    Default(
        arrayOf(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "J",
            "K",
            "L",
            "M",
            "N"
        )
    );

    companion object {
        @JvmStatic
        fun getSymbol(type: Symbol, value: Int): String {
            return type.map[value]
        }

        @JvmStatic
        fun getValue(type: Symbol, c: String): Int {
            for (i in type.map.indices) {
                if (type.map[i] == c) return i
            }
            return -1
        }
    }
}