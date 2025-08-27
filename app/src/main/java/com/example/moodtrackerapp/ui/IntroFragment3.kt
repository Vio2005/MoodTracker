package com.example.moodtrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.moodtrackerapp.R
import com.example.moodtrackerapp.ui.RegisterActivity

class IntroFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro3, container, false)

        val btnGetStarted: Button = view.findViewById(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}
