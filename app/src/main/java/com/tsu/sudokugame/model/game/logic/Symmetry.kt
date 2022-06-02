package com.tsu.sudokugame.model.game.logic

enum class Symmetry {
    NONE, ROTATE90, ROTATE180, MIRROR, FLIP, RANDOM;

    open operator fun get(s: String?): Symmetry? {
        var s = s ?: return null
        return try {
            s = s.uppercase()
            valueOf(s)
        } catch (aix: java.lang.IllegalArgumentException) {
            null
        }
    }
    open fun getName(): String? {
        val name = toString()
        return name.substring(0, 1) + name.substring(1).lowercase()
    }
}