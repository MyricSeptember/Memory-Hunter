package com.myricseptember.memoryhunter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myricseptember.memoryhunter.databinding.FragmentMemoryHunterBinding
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
}