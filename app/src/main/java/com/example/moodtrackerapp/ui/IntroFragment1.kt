package com.example.moodtrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.moodtrackerapp.R

class IntroFragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro1, container, false)

        val btnNext1: Button = view.findViewById(R.id.btnNext1)
        btnNext1.setOnClickListener {
            findNavController().navigate(R.id.action_intro1_to_intro2)
        }

        return view
    }
}
