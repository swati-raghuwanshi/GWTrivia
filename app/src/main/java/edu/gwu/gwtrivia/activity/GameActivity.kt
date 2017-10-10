package edu.gwu.gwtrivia.activity

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import edu.gwu.gwtrivia.utils.PersistanceManager
import edu.gwu.gwtrivia.R
import edu.gwu.gwtrivia.utils.BingImageSearchManager
import edu.gwu.gwtrivia.model.GameData
import edu.gwu.gwtrivia.model.Question
import edu.gwu.gwtrivia.model.Score
import edu.gwu.gwtrivia.utils.LocationDetector
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.toast
import java.util.*

class GameActivity : AppCompatActivity(), BingImageSearchManager.ImageSearchCompletionListener, LocationDetector.LocationListener {
    private val TAG = "GameActivity"

    private var questions: List<Question> = emptyList()
    private var triviaCategory: String = ""

    private val buttons = ArrayList<Button>()

    private var score: Int = 0
    private var numWrong: Int = 0
    private var currentQuestionIndex: Int = 0

    private lateinit var bingImageSearchManager: BingImageSearchManager
    private lateinit var persistanceManager: PersistanceManager
    private lateinit var locationDetector: LocationDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //setup toolbar
        setSupportActionBar(game_toolbar)

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

        persistanceManager = PersistanceManager(this)

        locationDetector = LocationDetector(this)
        locationDetector.locationListener = this

        nextTurn()
    }

    private fun nextTurn() {
        showLoading(true)

        buttons.forEach {
            it.isEnabled = false
            it.text = ""
        }

        supportActionBar?.title = "${getString(R.string.score)}: $score"

        image_background.setImageBitmap(null)

        if(numWrong == 3) { //game over
            locationDetector.detectLocation()
        }
        else { //continue playing
            //update pointer
            currentQuestionIndex++
            currentQuestionIndex %= questions.size

            val correctAnswer = questions[currentQuestionIndex].correctAnswer.answer
            bingImageSearchManager.search("$correctAnswer $triviaCategory")
        }
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

    fun skipPressed(item: MenuItem) {
        toast(R.string.skip_pressed)

        item.isEnabled = false

        nextTurn()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game, menu)

        return true
    }

    fun buttonPressed(v: View) {
        val correct = v.tag as Boolean
        if(correct) {
            score++
        }
        else{
            numWrong++
        }

        nextTurn()
    }

    private fun showLoading(show: Boolean) {
        if(show) {
            progressBar.visibility = ProgressBar.VISIBLE
        }
        else {
            progressBar.visibility = ProgressBar.INVISIBLE
        }
    }

    override fun imageLoaded() {
        showLoading(false)

        displayAnswers()

        buttons.forEach {
            it.isEnabled = true
        }
    }

    override fun imageNotLoaded() {
        nextTurn()
    }

    override fun locationFound(location: Location) {
        showLoading(false)

        val score = Score(score, Date(), location.latitude, location.longitude)
        persistanceManager.saveScore(score)

        finish()
    }

    override fun locationNotFound(reason: LocationDetector.FailureReason) {
        showLoading(false)

        when(reason){
            LocationDetector.FailureReason.TIMEOUT -> Log.d(TAG, "Location timed out")
            LocationDetector.FailureReason.NO_PERMISSION -> Log.d(TAG, "No location permission")
        }

        val score = Score(score, Date(), null, null)
        persistanceManager.saveScore(score)

        finish()
    }
}
