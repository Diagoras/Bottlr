package com.bottlr.app

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class BottleAdapter(
    private var bottles: MutableList<Bottle> = mutableListOf(),
    private val onBottleClick: OnBottleCheckListener
) : RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {

    fun interface OnBottleCheckListener {
        fun onButtonClick(
            bottleName: String,
            bottleId: String?,
            bottleDistillery: String,
            bottleType: String,
            bottleABV: String,
            bottleAge: String,
            bottlePhoto: Uri?,
            bottleNotes: String,
            bottleRegion: String,
            bottleRating: String,
            bottleKeywords: String
        )
    }

    class BottleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBottle: MaterialCardView = itemView.findViewById(R.id.cardBottle)
        val imageBottle: ImageView = itemView.findViewById(R.id.imageBottle)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textDistillery: TextView = itemView.findViewById(R.id.textDistillery)
        val textType: TextView = itemView.findViewById(R.id.textType)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bottle_compact, parent, false)
        return BottleViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        val bottle = bottles[position]
        holder.textName.text = bottle.name
        holder.textDistillery.text = bottle.distillery
        holder.textType.text = bottle.type.ifEmpty { "Spirit" }
        holder.textDate.text = formatDate(bottle.createdAt)

        bottle.photoUri?.let {
            if (it.toString().isNotEmpty() && it.toString() != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(it)
                    .centerCrop()
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageBottle)
            } else {
                holder.imageBottle.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageBottle.setImageResource(R.drawable.nodrinkimg)

        holder.cardBottle.setOnClickListener {
            onBottleClick.onButtonClick(
                bottle.name,
                bottle.bottleID,
                bottle.distillery,
                bottle.type,
                bottle.abv,
                bottle.age,
                bottle.photoUri,
                bottle.notes,
                bottle.region,
                bottle.rating,
                bottle.keywords
            )
        }
    }

    private fun formatDate(instant: Instant?): String {
        if (instant == null) return ""
        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    override fun getItemCount(): Int = bottles.size

    fun updateData(newBottles: List<Bottle>) {
        Log.d("Search", "Updating bottle adapter with ${newBottles.size} items")
        this.bottles = newBottles.toMutableList()
        notifyDataSetChanged()
    }

}
