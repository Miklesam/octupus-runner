package com.onelinegaming.squidrunner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_lose.*

class FragmentLose : Fragment(R.layout.fragment_lose) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        again.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentLose_to_gameOneFragment)
        }
        to_menu.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentLose_to_menuFragment)
        }
    }
}