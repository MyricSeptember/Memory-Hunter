package com.myricseptember.memoryhunter.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myricseptember.memoryhunter.model.BoardSize
import com.myricseptember.memoryhunter.model.MemoryCard
import com.myricseptember.memoryhunter.util.DEFAULT_ICONS

class MemoryHunterViewModel : ViewModel() {

    var boardSize = MutableLiveData(BoardSize.EASY)

    private var _cards = MutableLiveData<List<MemoryCard>>()
    val cards: LiveData<List<MemoryCard>> = _cards

    private val _numPairsFound = MutableLiveData<Int>(0)
    val numPairsFound: LiveData<Int> = _numPairsFound

    private var _numCardFlips = MutableLiveData<Int>(0)
    val numCardFlips: LiveData<Int> = _numCardFlips

    private var _indexOfSingleSelectedCard = MutableLiveData<Int?>(0)
    val indexOfSingleSelectedCard: MutableLiveData<Int?> = _indexOfSingleSelectedCard


    init {
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.value?.getNumPairs()!!)
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        _cards.value = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean {
        _numCardFlips.value?.plus(1)
        val card = cards.value?.get(position)
        var foundMatch = false
        // Three cases
        // 0 cards previously flipped over => restore cards + flip over the selected card
        // 1 card previously flipped over => flip over the selected card + check if the images match
        // 2 cards previously flipped over => restore cards + flip over the selected card
        if (_indexOfSingleSelectedCard.value == null) {
            // 0 or 2 selected cards previously
            restoreCards()
            _indexOfSingleSelectedCard.value = position
        } else {
            // exactly 1 card was selected previously
            foundMatch = checkForMatch(_indexOfSingleSelectedCard.value!!, position)
            _indexOfSingleSelectedCard.value = null
        }
        card?.isFaceUp = !card?.isFaceUp!!
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards.value?.get(position1)?.identifier != cards.value?.get(position2)?.identifier) {
            return false
        }
        cards.value?.get(position1)?.isMatched = true
        cards.value?.get(position2)?.isMatched = true
        numPairsFound.value?.plus(1)
        return true
    }

    // Turn all unmatched cards face down
    private fun restoreCards() {
        for (card in cards.value!!) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.value?.getNumPairs() ?: false
    }

    fun isCardFaceUp(position: Int): Boolean? {
        return cards.value?.get(position)?.isFaceUp
    }

    fun getNumMoves(): Int? {
        return numCardFlips.value?.div(2)
    }
}