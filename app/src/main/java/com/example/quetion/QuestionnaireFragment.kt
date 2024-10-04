package com.example.quetion

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class QuestionnaireFragment : Fragment() {

    companion object {
        const val SUBMIT_REQUEST_KEY = "submit_request_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_questionnaire, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the submit button from the layout
        val submitButton = view.findViewById<Button>(R.id.submit_button)

        // Set the onClickListener to trigger result listener
        submitButton.setOnClickListener {
            // Send fragment result to the parent activity when submit is clicked
            parentFragmentManager.setFragmentResult(SUBMIT_REQUEST_KEY, Bundle())
        }
    }
}
