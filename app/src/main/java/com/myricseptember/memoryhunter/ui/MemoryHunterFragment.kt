package com.myricseptember.memoryhunter.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.myricseptember.memoryhunter.R
import com.myricseptember.memoryhunter.databinding.FragmentMemoryHunterBinding
import com.myricseptember.memoryhunter.model.BoardSize
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemoryHunterFragment : Fragment() {

    private lateinit var binding: FragmentMemoryHunterBinding

    private val memoryHunterViewModel: MemoryHunterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentMemoryHunterBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
            }
        subscribeToObservers()
        return binding.root
    }


    private fun subscribeToObservers() {
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryHunterViewModel.getNumMoves()!! > 0 && !memoryHunterViewModel.haveWonGame()) {

                } else {
                }
                return true
            }
            R.id.mi_new_size -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}