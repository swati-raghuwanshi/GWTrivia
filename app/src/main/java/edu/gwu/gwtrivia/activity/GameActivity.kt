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

    //initialize game state
    private var questions: List<Question> = emptyList()
    private var triviaCategory: String = ""
    private var score: Int = 0
    private var numWrong: Int = 0
    private var currentQuestionIndex: Int = 0

    //convenience array to hold references to our 4 answer buttons
    private val buttons = ArrayList<Button>()

    //helper declarations
    private lateinit var bingImageSearchManager: BingImageSearchManager
    private lateinit var persistanceManager: PersistanceManager
    private lateinit var locationDetector: LocationDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //setup toolbar
        setSupportActionBar(game_toolbar)

        //set member variables
        buttons.apply {
            add(top_left_button)
            add(top_right_button)
            add(bottom_left_button)
            add(bottom_right_button)
        }

        //obtain gameData from intent
        val gameData = intent.getParcelableExtra<GameData>("gameData")
        questions = gameData.questions
        Collections.shuffle(questions)
        triviaCategory = gameData.triviaCategory

        //init our bing image search manager
        bingImageSearchManager = BingImageSearchManager(this, image_background)
        bingImageSearchManager.imageSearchCompletionListener = this

        //init our persistance manager
        persistanceManager = PersistanceManager(this)

        //init our location detector
        locationDetector = LocationDetector(this)
        locationDetector.locationListener = this

        //start the game
        nextTurn()
    }

    private fun nextTurn() {
        showLoading(true)

        //disable buttons
        buttons.forEach {
            it.isEnabled = false
            it.text = ""
        }

        //blank out image
        image_background.setImageBitmap(null)

        //update score
        supportActionBar?.title = "${getString(R.string.score)}: $score"

        //determine whether game should continue or game over
        if(numWrong == 3) { //game over
            locationDetector.detectLocation()
        }
        else { //continue playing
            //update question pointer, loop back to beginning if at end
            currentQuestionIndex++
            currentQuestionIndex %= questions.size

            //obtain correct answer, and make image search query
            val correctAnswer = questions[currentQuestionIndex].correctAnswer.answer
            bingImageSearchManager.search("$correctAnswer $triviaCategory")
        }
    }

    private fun displayAnswers() {
        //obtain answers for current trivia question and shuffle em
        val answers = questions[currentQuestionIndex].wrongAnswers + questions[currentQuestionIndex].correctAnswer
        Collections.shuffle(answers)

        //set 1 answer per button, and utilize the button's tag to store a boolean - true if answer is correct, false otherwise
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

        //disable skip button once used
        item.isEnabled = false

        nextTurn()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game, menu)

        return true
    }

    fun buttonPressed(v: View) {
        //check tag of button, increment score or numWrong depending on correctness
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

        //enable buttons once image is displayed
        buttons.forEach {
            it.isEnabled = true
        }
    }

    override fun imageNotLoaded() {
        //in case of an error loading image, jump user to next question
        nextTurn()
    }

    override fun locationFound(location: Location) {
        showLoading(false)

        //persist score
        val score = Score(score, Date(), location.latitude, location.longitude)
        persistanceManager.saveScore(score)

        finish()
    }

    override fun locationNotFound(reason: LocationDetector.FailureReason) {
        showLoading(false)

        //log issue with location (a toast might be better here so user knows what's up - or they may not care!)
        when(reason){
            LocationDetector.FailureReason.TIMEOUT -> Log.d(TAG, "Location timed out")
            LocationDetector.FailureReason.NO_PERMISSION -> Log.d(TAG, "No location permission")
        }

        //persist score
        val score = Score(score, Date(), null, null)
        persistanceManager.saveScore(score)

        finish()
    }
}
