package com.gloves.game.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.onelinegaming.squidrunner.R
import kotlinx.android.synthetic.main.onboarding_fragment.*

class FirstStep : BaseOnboardingDialog() {


    companion object {
        fun open(fragmentManager: FragmentManager) {
            val fragment = FirstStep()
            fragment.show(fragmentManager, "OnboardingFragment")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        first.text = getString(R.string.onboarding_first_title)
        image_id.setImageResource(R.drawable.squid_setup)
        next_bttn.setOnClickListener {
            dismiss()
            SecondStep.open(
                requireFragmentManager()
            )
        }
    }

}