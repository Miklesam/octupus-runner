package com.gloves.game.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.onelinegaming.squidrunner.R
import kotlinx.android.synthetic.main.onboarding_fragment.*

class SecondStep : BaseOnboardingDialog() {
    companion object {
        fun open(fragmentManager: FragmentManager) {
            val fragment = SecondStep()
            fragment.show(fragmentManager, "SecondStep")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        first.text = getString(R.string.onboarding_second_title)
        next_bttn.setOnClickListener {
            dismiss()
            ThirdStep.open(
                requireFragmentManager()
            )
        }
        image_id.setImageResource(R.drawable.step)
    }
}