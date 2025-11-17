package com.example.solinda

enum class PileType { STOCK, WASTE, FOUNDATION, TABLEAU }

class Pile(
    val type: PileType,
    val cards: MutableList<Card> = mutableListOf()
) {
    fun topCard(): Card? = cards.lastOrNull()
    fun addCard(card: Card) = cards.add(card)
    fun removeTopCard(): Card? = if (cards.isNotEmpty()) cards.removeAt(cards.lastIndex) else null
    fun isEmpty() = cards.isEmpty()

    fun removeStack(stack: MutableList<Card>) {
        cards.removeAll(stack)
    }

    fun removeFrom(card: Card): MutableList<Card> {
        val index = cards.indexOf(card)
        return if (index != -1) {
            val moved = cards.subList(index, cards.size).toMutableList()
            cards.subList(index, cards.size).clear()
            moved
        } else mutableListOf()
    }

    fun addStack(stack: List<Card>) {
        cards.addAll(stack)
    }
}
