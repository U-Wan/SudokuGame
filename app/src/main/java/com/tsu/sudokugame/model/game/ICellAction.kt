package com.tsu.sudokugame.model.game

interface ICellAction<T> {
    fun action(gc: GameCell?, existing: T): T
}