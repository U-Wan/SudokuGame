package com.tsu.sudokugame.controler


interface IHintDialogFragmentListener {
    fun onHintDialogPositiveClick()
    fun onDialogNegativeClick()
}
interface IFinalizeDialogFragmentListener {
    fun onFinalizeDialogPositiveClick()
    fun onDialogNegativeClick()
}
interface IDeleteDialogFragmentListener {
    fun onDialogPositiveClick(position: Int)
    fun onDialogNegativeClick(position: Int)
}
interface IResetDialogFragmentListener {
    fun onResetDialogPositiveClick()
    fun onDialogNegativeClick()
}
interface IShareDialogFragmentListener {
    fun onShareDialogPositiveClick(input: String?)
    fun onDialogNegativeClick()
}