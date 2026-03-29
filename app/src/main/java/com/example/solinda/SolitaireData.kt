package com.example.solinda

data class SolitaireData(
    val stock: List<PileState>,
    val waste: List<PileState>,
    val foundations: List<PileState>,
    val tableau: List<PileState>,
    val freeCells: List<PileState>
)
