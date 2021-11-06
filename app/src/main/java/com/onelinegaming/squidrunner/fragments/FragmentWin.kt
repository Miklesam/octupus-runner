package com.onelinegaming.squidrunner.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.onelinegaming.squidrunner.R
import kotlinx.android.synthetic.main.fragment_won.*


class FragmentWin : Fragment(R.layout.fragment_won) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        again_won.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentWin_to_gameOneFragment)
        }
        to_menu_won.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentWin_to_menuFragment)
        }
    }
}