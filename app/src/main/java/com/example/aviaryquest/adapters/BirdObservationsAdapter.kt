package com.example.aviaryquest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aviaryquest.R
import com.example.aviaryquest.models.SaveBird

class BirdObservationsAdapter : ListAdapter<SaveBird, BirdObservationsAdapter.BirdObservationViewHolder>(SaveBirdDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdObservationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.observation_item, parent, false)
        return BirdObservationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BirdObservationViewHolder, position: Int) {
        val observation = getItem(position)
        holder.bind(observation)
    }

    inner class BirdObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSpecies: TextView = itemView.findViewById(R.id.tvSpecies)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)

        fun bind(observation: SaveBird) {
            tvSpecies.text = "Species-> ${observation.species}"
            tvLocation.text = "Location-> ${observation.location}"
            tvDate.text = "Date-> ${observation.date}"
            tvTime.text = "Time-> ${observation.time}"
            tvNotes.text = "Notes-> ${observation.notes}"
        }
    }

    class SaveBirdDiffCallback : DiffUtil.ItemCallback<SaveBird>() {
        override fun areItemsTheSame(oldItem: SaveBird, newItem: SaveBird): Boolean {
            return oldItem.species == newItem.species // Compare items based on their unique ID
        }

        override fun areContentsTheSame(oldItem: SaveBird, newItem: SaveBird): Boolean {
            return oldItem == newItem
        }
    }

}
