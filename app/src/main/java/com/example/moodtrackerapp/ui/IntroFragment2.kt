package com.example.moodtrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.moodtrackerapp.R

class IntroFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro2, container, false)

        val radioGroupGender: RadioGroup = view.findViewById(R.id.radioGroupGender)
        val btnNext2: Button = view.findViewById(R.id.btnNext2)

        btnNext2.setOnClickListener {
            // Check if a gender is selected
            if (radioGroupGender.checkedRadioButtonId == -1) {
                Toast.makeText(requireContext(), "Please select your gender", Toast.LENGTH_SHORT).show()
            } else {
                // Navigate to IntroFragment3
                findNavController().navigate(R.id.action_intro2_to_intro3)
            }
        }

        return view
    }
}
