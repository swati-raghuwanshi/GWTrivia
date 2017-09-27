package edu.gwu.gwtrivia.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import edu.gwu.gwtrivia.R
import edu.gwu.gwtrivia.async.BingImageSearchManager
import edu.gwu.gwtrivia.model.GameData
import edu.gwu.gwtrivia.model.Question
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.toast
import java.util.*

class GameActivity : AppCompatActivity(), BingImageSearchManager.ImageSearchCompletionListener {
    private var questions: List<Question> = emptyList()
    private var triviaCategory: String = ""

    private val buttons = ArrayList<Button>()

    private var score: Int = 0
    private var numWrong: Int = 0
    private var currentQuestionIndex: Int = 0

    private lateinit var bingImageSearchManager: BingImageSearchManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //obtain gameData from intent
        val gameData = intent.getParcelableExtra<GameData>("gameData")

        //set member variables
        buttons.apply {
            add(top_left_button)
            add(top_right_button)
            add(bottom_left_button)
            add(bottom_right_button)
        }

        questions = gameData.questions
        Collections.shuffle(questions)
        triviaCategory = gameData.triviaCategory

        bingImageSearchManager = BingImageSearchManager(this, image_background)
        bingImageSearchManager.imageSearchCompletionListener = this

        nextTurn()
    }

    private fun nextTurn() {
        buttons.forEach {
            it.isEnabled = false
            it.text = ""
        }

        image_background.setImageBitmap(null)

        val correctAnswer = questions[currentQuestionIndex].correctAnswer.answer

        bingImageSearchManager.search("$correctAnswer $triviaCategory")

    }

    private fun displayAnswers() {
        val answers = questions[currentQuestionIndex].wrongAnswers + questions[currentQuestionIndex].correctAnswer
        Collections.shuffle(answers)

        for(i in buttons.indices) {
            val answer = answers[i]
            val button = buttons[i]

            button.apply {
                text = answer.answer
                tag = answer.correct
            }
        }
    }

    override fun imageLoaded() {
        displayAnswers()

        buttons.forEach {
            it.isEnabled = true
        }
    }

    override fun imageNotLoaded() {
        toast("Image didn't load :(")
    }
}
