package com.example.quetion

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire

class MainActivity : AppCompatActivity() {

    var questionnaireJsonString: String? = null
    var currentQuestionnaireFile: String = "questionnaire.json" // Default questionnaire file

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Load the default questionnaire JSON from assets
        loadQuestionnaire(currentQuestionnaireFile)

        // If there is no saved instance state, commit the fragment to the container
        if (savedInstanceState == null) {
            loadQuestionnaireFragment()
        }
    }

    // Load the questionnaire JSON from the assets and set it to the current string
    private fun loadQuestionnaire(fileName: String) {
        currentQuestionnaireFile = fileName
        questionnaireJsonString = getStringFromAssets(fileName)
    }

    // Function to replace the QuestionnaireFragment based on the current JSON file
    private fun loadQuestionnaireFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragment = QuestionnaireFragment.builder()
                .setQuestionnaire(questionnaireJsonString!!)
                .build()
            fragment.setHasOptionsMenu(false) // Ensure the fragment does not handle the menu
            replace(R.id.fragment_container_view, fragment)
        }
    }

    // Inflate the menu with the submit button and questionnaire options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.submit_menu, menu)
        return true
    }

    // Handle menu item clicks for selecting questionnaires and submitting
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.submit -> {
                submitQuestionnaire()
                true
            }
            R.id.questionnaire_default -> {
                loadQuestionnaire("questionnaire.json")
                loadQuestionnaireFragment() // Reload fragment with selected questionnaire
                true
            }
            R.id.questionnaire_covid -> {
                loadQuestionnaire("covid.json")
                loadQuestionnaireFragment()
                true
            }
            R.id.questionnaire_register -> {
                loadQuestionnaire("register.json")
                loadQuestionnaireFragment()
                true
            }
            R.id.questionnaire_start -> {
                loadQuestionnaire("start.json")
                loadQuestionnaireFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Function to submit the questionnaire and extract FHIR resources
    private fun submitQuestionnaire() =
        lifecycleScope.launch {
            try {
                // Get the QuestionnaireFragment
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                        as QuestionnaireFragment

                // Get the questionnaire response
                val questionnaireResponse = fragment.getQuestionnaireResponse()

                // Log the questionnaire response in JSON format
                val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                val questionnaireResponseString =
                    jsonParser.encodeResourceToString(questionnaireResponse)
                Log.d("response", questionnaireResponseString)

                // Parse the questionnaire JSON string into a Questionnaire object
                val questionnaire = jsonParser.parseResource(questionnaireJsonString) as Questionnaire

                // Use ResourceMapper to extract FHIR resources from the Questionnaire and QuestionnaireResponse
                val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse)

                // Log the extraction result in JSON format
                Log.d("extraction result", jsonParser.encodeResourceToString(bundle))

            } catch (e: Exception) {
                Log.e("MainActivity", "Error during FHIR resource extraction", e)
            }
        }

    // Function to load the questionnaire JSON from the assets folder
    private fun getStringFromAssets(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
