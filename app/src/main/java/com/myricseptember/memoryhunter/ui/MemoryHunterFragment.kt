package com.myricseptember.memoryhunter.ui

import android.animation.ArgbEvaluator
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.myricseptember.memoryhunter.R
import com.myricseptember.memoryhunter.databinding.FragmentMemoryHunterBinding
import com.myricseptember.memoryhunter.model.BoardSize
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemoryHunterFragment : Fragment() {

    private lateinit var binding: FragmentMemoryHunterBinding

    private val memoryHunterViewModel: MemoryHunterViewModel by viewModels()
    private lateinit var adapter: MemoryBoardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentMemoryHunterBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = memoryHunterViewModel
            }
        subscribeToObservers()
        setupBoard()
        return binding.root
    }

    private fun subscribeToObservers() {
    }

    private fun showNewSizeDialog() {
        val boardSizeView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroupSize)
        when (memoryHunterViewModel.boardSize.value) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog(getString(R.string.choose_size), boardSizeView, View.OnClickListener {
            memoryHunterViewModel.boardSize.value = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun setupBoard() {
        when (memoryHunterViewModel.boardSize.value) {
            BoardSize.EASY -> {
                binding.tvNumMoves.text = "Easy: 4 x 2"
                binding.tvNumPairs.text = "Pairs: 0/4"
                memoryHunterViewModel.newSize()
            }
            BoardSize.MEDIUM -> {
                binding.tvNumMoves.text = "Medium: 6 x 3"
                binding.tvNumPairs.text = "Pairs: 0/9"
                memoryHunterViewModel.newSize()
            }
            BoardSize.HARD -> {
                binding.tvNumMoves.text = "Hard: 6 x 4"
                binding.tvNumPairs.text = "Pairs: 0/12"
                memoryHunterViewModel.newSize()
            }
        }
        binding.tvNumPairs.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_progress_none
            )
        )
        memoryHunterViewModel.resetScore()
        adapter = MemoryBoardAdapter(
            requireContext(),
            memoryHunterViewModel.boardSize.value!!,
            memoryHunterViewModel.cards.value!!,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }
            })
        binding.rvBoard.adapter = adapter
        binding.rvBoard.setHasFixedSize(true)
        binding.rvBoard.layoutManager =
            GridLayoutManager(requireContext(), memoryHunterViewModel.boardSize.value!!.getWidth())
    }

    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryHunterViewModel.getNumMoves()!! > 0 && !memoryHunterViewModel.haveWonGame()) {
                    showAlertDialog(
                        getString(R.string.quit_game_message),
                        null,
                        View.OnClickListener {
                            setupBoard()
                        })
                } else {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateGameWithFlip(position: Int) {
        // Error handling:
        if (memoryHunterViewModel.haveWonGame()) {
            Snackbar.make(
                binding.clRoot,
                getString(R.string.already_won_message),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        if (memoryHunterViewModel.isCardFaceUp(position)!!) {
            Snackbar.make(
                binding.clRoot,
                getString(R.string.invalid_entry_message),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        // Actually flip the card
        if (memoryHunterViewModel.flipCard(position)) {
            Log.i(
                TAG,
                "Found a match! Num pairs found: ${memoryHunterViewModel.numPairsFound.value}"
            )
            val color = ArgbEvaluator().evaluate(
                memoryHunterViewModel.numPairsFound.value?.toFloat()!! / memoryHunterViewModel.boardSize.value?.getNumPairs()!!,
                ContextCompat.getColor(requireContext(), R.color.color_progress_none),
                ContextCompat.getColor(requireContext(), R.color.color_progress_full)
            ) as Int
            binding.tvNumPairs.setTextColor(color)
            binding.tvNumPairs.text =
                "Pairs: ${memoryHunterViewModel.numPairsFound.value} / ${memoryHunterViewModel.boardSize.value!!.getNumPairs()}"
            if (memoryHunterViewModel.haveWonGame()) {
                Snackbar.make(
                    binding.clRoot,
                    getString(R.string.winning_message),
                    Snackbar.LENGTH_LONG
                )
                    .show()
                CommonConfetti.rainingConfetti(
                    binding.clRoot,
                    intArrayOf(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                ).oneShot()
            }
        }
        binding.tvNumMoves.text = "Moves: ${memoryHunterViewModel.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}