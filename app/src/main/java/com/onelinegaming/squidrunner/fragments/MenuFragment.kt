package com.onelinegaming.squidrunner.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gloves.game.onboarding.FirstStep
import com.onelinegaming.squidrunner.R
import com.onelinegaming.squidrunner.StartActivity
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game_1_bttn.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_gameOneFragment)
        }
        about_bttn.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_fragmentAbout)
        }
        how_to_play.setOnClickListener {
            FirstStep.open(requireActivity().supportFragmentManager)
        }
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                StartActivity.REQUIRED_PERMISSIONS,
                StartActivity.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = StartActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }
}