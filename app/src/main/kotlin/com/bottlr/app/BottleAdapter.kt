package com.bottlr.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.util.toShortDateString
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class BottleAdapter(
    private val onBottleClick: (BottleEntity) -> Unit
) : ListAdapter<BottleEntity, BottleAdapter.BottleViewHolder>(BottleDiffCallback()) {

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
        val bottle = getItem(position)
        holder.textName.text = bottle.name
        holder.textDistillery.text = bottle.distillery
        holder.textType.text = bottle.type.ifEmpty { "Spirit" }
        holder.textDate.text = bottle.createdAt.toShortDateString()

        bottle.photoUri?.let { uriString ->
            if (uriString.isNotEmpty() && uriString != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(uriString))
                    .centerCrop()
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageBottle)
            } else {
                holder.imageBottle.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageBottle.setImageResource(R.drawable.nodrinkimg)

        holder.cardBottle.setOnClickListener {
            onBottleClick(bottle)
        }
    }

    private class BottleDiffCallback : DiffUtil.ItemCallback<BottleEntity>() {
        override fun areItemsTheSame(oldItem: BottleEntity, newItem: BottleEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BottleEntity, newItem: BottleEntity): Boolean {
            return oldItem == newItem
        }
    }
}
