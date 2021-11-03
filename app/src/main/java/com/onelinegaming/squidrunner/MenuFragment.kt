package com.onelinegaming.squidrunner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game_1_bttn.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_gameOneFragment)
        }
    }
}