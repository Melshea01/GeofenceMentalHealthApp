package com.example.firstapp

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.firstapp.databinding.ActivityMainBinding



class QuestionnaireActivity : FragmentActivity() {


    private lateinit var binding: ActivityMainBinding
    val questionnaire = QuestionnaireGAD7()
    var indexQuestionnaire = 0
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayQuestion(indexQuestionnaire)
        binding.nextButton.setOnClickListener {
            indexQuestionnaire++
            if(indexQuestionnaire<=questionnaire.gad7Questions.size-1){
                displayQuestion(indexQuestionnaire)
                if(indexQuestionnaire==questionnaire.gad7Questions.size-1){
                    binding.nextButton.text = "Finish"
                    }
                }else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
    }


    private fun displayQuestion(questionId: Int){
        binding.QuestionText.text = questionnaire.getQuestionByIndex(questionId)
        binding.radioButtonOption1.text = questionnaire.getResponseByIndex(0)
        binding.radioButtonOption2.text = questionnaire.getResponseByIndex(1)
        binding.radioButtonOption3.text = questionnaire.getResponseByIndex(2)
        binding.radioButtonOption4.text = questionnaire.getResponseByIndex(3)
    }

}