package com.gloves.game.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.onelinegaming.squidrunner.R
import kotlinx.android.synthetic.main.onboarding_fragment.*

class ForthStep : BaseOnboardingDialog() {
    companion object {
        fun open(fragmentManager: FragmentManager) {
            val fragment = ForthStep()
            fragment.show(fragmentManager, "ForthStep")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        first.text = getString(R.string.onboarding_forth_title)
        next_bttn.setOnClickListener {
            dismiss()
        }
        image_id.setImageResource(R.drawable.screen_3)
    }
}