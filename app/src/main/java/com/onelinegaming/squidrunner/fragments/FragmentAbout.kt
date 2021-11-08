package com.onelinegaming.squidrunner.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.onelinegaming.squidrunner.R
import kotlinx.android.synthetic.main.fragment_about.*
import android.content.Intent
import android.net.Uri


class FragmentAbout : Fragment(R.layout.fragment_about) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discord_bttn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/twKyPZDn"))
            startActivity(browserIntent)
        }
    }
}