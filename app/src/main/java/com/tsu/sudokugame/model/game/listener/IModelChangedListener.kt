
package com.tsu.sudokugame.model.game.listener

import com.tsu.sudokugame.model.game.GameCell

interface IModelChangedListener {
    fun onModelChange(c: GameCell?)
}