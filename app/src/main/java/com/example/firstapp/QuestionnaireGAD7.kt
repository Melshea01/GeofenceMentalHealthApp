package com.example.firstapp

class QuestionnaireGAD7 {
    private var questionIndex = 0

    private val gad7Questions = arrayOf(
        "Over the last 2 weeks, how often have you been bothered by symptoms such as feeling nervous, anxious, or on edge?",
        "Over the last 2 weeks, how often have you been unable to stop or control worrying?",
        "Over the last 2 weeks, how often have you worried about having trouble falling or staying asleep?",
        "Over the last 2 weeks, how often have you worried about feeling restless or fidgety?",
        "Over the last 2 weeks, how often have you had trouble relaxing?",
        "Over the last 2 weeks, how often have you become easily annoyed or irritable?",
        "Over the last 2 weeks, how often have you felt afraid as if something awful might happen?"
    )

    val gad7Responses = arrayOf("Not at all", "Several days", "More than half the days", "Nearly every day")

    fun getNextQuestion(): String? {
        if (questionIndex < gad7Questions.size) {
            val nextQuestion = gad7Questions[questionIndex]
            questionIndex++
            return nextQuestion
        }
        return null // Toutes les questions ont été posées
    }

    fun getResponseByIndex(index: Int): String? {
        if (index < gad7Responses.size) {
            return gad7Responses[index]
        }
        return null // Index en dehors de la plage des réponses possibles
    }

}