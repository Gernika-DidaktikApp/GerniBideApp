package es.didaktikapp.gernikapp.picasso

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class ViewInterpretActivity : BaseMenuActivity() {

    private lateinit var progressText: TextView
    private lateinit var elementImage: ImageView
    private lateinit var questionText: TextView
    private lateinit var option1: Button
    private lateinit var option2: Button
    private lateinit var option3: Button
    private lateinit var option4: Button
    private lateinit var feedbackText: TextView
    private lateinit var nextButton: Button

    private var currentQuestionIndex = 0
    private var correctAnswers = 0

    companion object {
        private const val KEY_QUESTION_INDEX = "question_index"
        private const val KEY_CORRECT_ANSWERS = "correct_answers"
        private const val PREFS_NAME = "view_interpret_progress"
        private const val KEY_HAS_SAVED_PROGRESS = "has_saved_progress"
        private const val KEY_TEST_COMPLETED = "test_completed"

        fun hasSavedProgress(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)
        }

        fun isTestCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_TEST_COMPLETED, false)
        }

        fun loadProgress(context: Context): Pair<Int, Int>? {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)) return null
            return Pair(prefs.getInt(KEY_QUESTION_INDEX, 0), prefs.getInt(KEY_CORRECT_ANSWERS, 0))
        }

        fun clearProgress(context: Context) {
            context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
        }
    }

    private data class Question(
        val imageName: String,
        val questionKey: String,
        val optionsKeys: List<String>,
        val correctAnswerIndex: Int,
        val feedbackKey: String
    )

    private val questions = listOf(
        Question("picasso_zaldia", "view_interpret_q1", listOf("view_interpret_q1_opt1", "view_interpret_q1_opt2", "view_interpret_q1_opt3", "view_interpret_q1_opt4"), 1, "view_interpret_q1_feedback"),
        Question("picasso_ama_haurrarekin", "view_interpret_q2", listOf("view_interpret_q2_opt1", "view_interpret_q2_opt2", "view_interpret_q2_opt3", "view_interpret_q2_opt4"), 1, "view_interpret_q2_feedback"),
        Question("picasso_argia", "view_interpret_q3", listOf("view_interpret_q3_opt1", "view_interpret_q3_opt2", "view_interpret_q3_opt3", "view_interpret_q3_opt4"), 1, "view_interpret_q3_feedback"),
        Question("picasso_zezena", "view_interpret_q4", listOf("view_interpret_q4_opt1", "view_interpret_q4_opt2", "view_interpret_q4_opt3", "view_interpret_q4_opt4"), 2, "view_interpret_q4_feedback"),
        Question("picasso_oihua", "view_interpret_q5", listOf("view_interpret_q5_opt1", "view_interpret_q5_opt2", "view_interpret_q5_opt3", "view_interpret_q5_opt4"), 2, "view_interpret_q5_feedback"),
        Question("picasso_gerlaria", "view_interpret_q6", listOf("view_interpret_q6_opt1", "view_interpret_q6_opt2", "view_interpret_q6_opt3", "view_interpret_q6_opt4"), 1, "view_interpret_q6_feedback")
    )

    override fun getContentLayoutId(): Int = R.layout.picasso_view_interpret

    override fun onContentInflated() {
        initViews()
        setupOptionListeners()
        nextButton.setOnClickListener { loadNextQuestion() }
        checkForSavedProgress()
    }

    private fun initViews() {
        progressText = contentContainer.findViewById(R.id.progressText)
        elementImage = contentContainer.findViewById(R.id.elementImage)
        questionText = contentContainer.findViewById(R.id.questionText)
        option1 = contentContainer.findViewById(R.id.option1)
        option2 = contentContainer.findViewById(R.id.option2)
        option3 = contentContainer.findViewById(R.id.option3)
        option4 = contentContainer.findViewById(R.id.option4)
        feedbackText = contentContainer.findViewById(R.id.feedbackText)
        nextButton = contentContainer.findViewById(R.id.nextButton)
    }

    private fun checkForSavedProgress() {
        if (hasSavedProgress(this)) {
            if (isTestCompleted(this)) {
                val progress = loadProgress(this)
                val previousScore = progress?.second ?: 0
                val stars = when (previousScore) {
                    6 -> "⭐⭐⭐⭐⭐"
                    5 -> "⭐⭐⭐⭐"
                    4 -> "⭐⭐⭐"
                    3 -> "⭐⭐"
                    else -> "⭐"
                }

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_interpret_previous_result_title))
                    .setMessage(getString(R.string.view_interpret_previous_result_message, previousScore, questions.size, stars))
                    .setPositiveButton(getString(R.string.view_interpret_redo_test)) { dialog, _ ->
                        dialog.dismiss()
                        clearProgress(this)
                        currentQuestionIndex = 0
                        correctAnswers = 0
                        loadQuestion()
                    }
                    .setNegativeButton(getString(R.string.view_interpret_exit)) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_interpret_load_title))
                    .setMessage(getString(R.string.view_interpret_load_message))
                    .setPositiveButton(getString(R.string.view_interpret_load_continue)) { dialog, _ ->
                        dialog.dismiss()
                        loadSavedProgress()
                    }
                    .setNegativeButton(getString(R.string.view_interpret_load_start_new)) { dialog, _ ->
                        dialog.dismiss()
                        clearProgress(this)
                        loadQuestion()
                    }
                    .setCancelable(false)
                    .show()
            }
        } else {
            loadQuestion()
        }
    }

    private fun loadSavedProgress() {
        val progress = loadProgress(this)
        if (progress != null) {
            currentQuestionIndex = progress.first
            correctAnswers = progress.second
        }
        loadQuestion()
    }

    private fun saveProgress(testCompleted: Boolean = false) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(KEY_HAS_SAVED_PROGRESS, true)
            putBoolean(KEY_TEST_COMPLETED, testCompleted)
            putInt(KEY_QUESTION_INDEX, currentQuestionIndex)
            putInt(KEY_CORRECT_ANSWERS, correctAnswers)
            apply()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_QUESTION_INDEX, currentQuestionIndex)
        outState.putInt(KEY_CORRECT_ANSWERS, correctAnswers)
    }

    private fun setupOptionListeners() {
        val options = listOf(option1, option2, option3, option4)
        options.forEachIndexed { index, button ->
            button.setOnClickListener { checkAnswer(index, button) }
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]

            progressText.text = "${currentQuestionIndex + 1}/${questions.size}"

            val imageResId = resources.getIdentifier(question.imageName, "drawable", packageName)
            if (imageResId != 0) elementImage.setImageResource(imageResId)

            questionText.text = getStringByKey(question.questionKey)
            option1.text = getStringByKey(question.optionsKeys[0])
            option2.text = getStringByKey(question.optionsKeys[1])
            option3.text = getStringByKey(question.optionsKeys[2])
            option4.text = getStringByKey(question.optionsKeys[3])

            resetButtons()
            feedbackText.visibility = View.GONE
            nextButton.visibility = View.GONE
        }
    }

    private fun getStringByKey(key: String): String {
        val resId = resources.getIdentifier(key, "string", packageName)
        return if (resId != 0) getString(resId) else key
    }

    private fun resetButtons() {
        val options = listOf(option1, option2, option3, option4)
        options.forEach { button ->
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.bg_opcion_normal)
            button.setTextColor(Color.parseColor("#39455F"))
        }
    }

    private fun checkAnswer(selectedIndex: Int, selectedButton: Button) {
        val question = questions[currentQuestionIndex]
        val options = listOf(option1, option2, option3, option4)

        options.forEach { it.isEnabled = false }

        if (selectedIndex == question.correctAnswerIndex) {
            selectedButton.setBackgroundResource(R.drawable.bg_opcion_correcta)
            selectedButton.setTextColor(Color.parseColor("#2E7D32"))
            feedbackText.text = getStringByKey(question.feedbackKey)
            feedbackText.setTextColor(Color.parseColor("#2E7D32"))
            correctAnswers++
        } else {
            selectedButton.setBackgroundResource(R.drawable.bg_opcion_incorrecta)
            selectedButton.setTextColor(Color.parseColor("#D84315"))

            options[question.correctAnswerIndex].setBackgroundResource(R.drawable.bg_opcion_correcta)
            options[question.correctAnswerIndex].setTextColor(Color.parseColor("#2E7D32"))

            val correctOption = getStringByKey(question.optionsKeys[question.correctAnswerIndex])
            val feedback = getStringByKey(question.feedbackKey)
            feedbackText.text = getString(R.string.view_interpret_wrong_answer, correctOption, feedback)
            feedbackText.setTextColor(Color.parseColor("#C62828"))
        }

        feedbackText.visibility = View.VISIBLE
        nextButton.visibility = View.VISIBLE
    }

    private fun loadNextQuestion() {
        currentQuestionIndex++
        saveProgress()

        if (currentQuestionIndex < questions.size) {
            loadQuestion()
        } else {
            showFinalResults()
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentQuestionIndex < questions.size) saveProgress()
    }

    private fun showFinalResults() {
        saveProgress(testCompleted = true)

        val stars = when (correctAnswers) {
            6 -> "⭐⭐⭐⭐⭐"
            5 -> "⭐⭐⭐⭐"
            4 -> "⭐⭐⭐"
            3 -> "⭐⭐"
            else -> "⭐"
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.view_interpret_results_title))
            .setMessage(getString(R.string.view_interpret_results_message, correctAnswers, questions.size, stars))
            .setPositiveButton(getString(R.string.view_interpret_results_finish)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}