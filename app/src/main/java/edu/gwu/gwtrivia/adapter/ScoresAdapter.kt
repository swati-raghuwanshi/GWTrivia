package edu.gwu.gwtrivia.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import edu.gwu.gwtrivia.R
import edu.gwu.gwtrivia.model.Score

/**
 * Created by jared on 10/2/17.
 */
class ScoresAdapter(private val scores: List<Score>) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        //obtain score at position
        val score = scores?.get(position)

        //bind score to view holder
        score?.let {
            (holder as ViewHolder).bind(score)
        }
    }

    override fun getItemCount(): Int {
        return scores.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)

        //inflate our score row layout
        return ViewHolder(layoutInflater.inflate(R.layout.row_score, parent, false))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val scoreTextView: TextView = view.findViewById(R.id.score)
        private val dateTextView: TextView = view.findViewById(R.id.date)

        //update score row ui with score and date
        fun bind(score: Score) {
            scoreTextView.text = score.score.toString()
            dateTextView.text = score.date.toString()
        }
    }
}