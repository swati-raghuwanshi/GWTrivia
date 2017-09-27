package edu.gwu.gwtrivia.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gwu.trivia.Utilities
import edu.gwu.gwtrivia.R
import kotlinx.android.synthetic.main.activity_menu.*
import org.jetbrains.anko.activityUiThread
import org.jetbrains.anko.doAsync

class MenuActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        play_button.setOnClickListener {
            loadGameData()
        }

        high_scores_button.setOnClickListener {
            //TODO
        }
    }

    fun loadGameData() {
        doAsync {
            val gameData = Utilities.loadGameData("presidents.csv",this@MenuActivity)
            if(gameData != null && gameData.questions.isNotEmpty()) {

                activityUiThread {
                    val intent = Intent(this@MenuActivity, GameActivity::class.java)

                    intent.putExtra("gameData", gameData)
                    startActivity(intent)
                }

                Log.d(TAG, "Number of questions ${gameData.questions.count().toString()}")
            }
            else {
                Log.d(TAG, "problem")
            }
        }
    }
}
