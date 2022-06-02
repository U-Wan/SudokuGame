package com.tsu.sudokugame.model.game.listener

import com.tsu.sudokugame.model.game.GameCell

interface IModelChangedListener {
    fun onModelChange(c: GameCell?)
}
interface IHintListener {
    fun onHintUsed()
}
interface ITimerListener {
    fun onTick(time: Int)
}
interface IHighlightChangedListener {
    fun onHighlightChanged()
}
interface IGameSolvedListener {
    fun onSolved()
}