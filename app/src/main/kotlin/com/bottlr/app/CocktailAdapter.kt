package com.bottlr.app

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

class CocktailAdapter(
    private var cocktails: MutableList<Cocktail> = mutableListOf(),
    private val onCocktailClick: OnCocktailCheckListener
) : RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder>() {

    fun interface OnCocktailCheckListener {
        fun onButtonClick(cocktail: Cocktail)
    }

    class CocktailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCocktail: MaterialCardView = itemView.findViewById(R.id.cardCocktail)
        val imageCocktail: ImageView = itemView.findViewById(R.id.imageCocktail)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textIngredients: TextView = itemView.findViewById(R.id.textIngredients)
        val textBase: TextView = itemView.findViewById(R.id.textBase)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocktailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cocktail_compact, parent, false)
        return CocktailViewHolder(view)
    }

    override fun onBindViewHolder(holder: CocktailViewHolder, position: Int) {
        val cocktail = cocktails[position]
        holder.textName.text = cocktail.name
        holder.textBase.text = cocktail.base.ifEmpty { "Spirit" }
        holder.textDate.text = formatDate(cocktail.createdAt)

        // Build ingredients summary
        val ingredients = listOf(cocktail.base, cocktail.mixer, cocktail.juice)
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(", ")
        holder.textIngredients.text = ingredients.ifEmpty { cocktail.base }

        cocktail.photoUri?.let {
            if (it.toString().isNotEmpty() && it.toString() != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(it)
                    .centerCrop()
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageCocktail)
            } else {
                holder.imageCocktail.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageCocktail.setImageResource(R.drawable.nodrinkimg)

        holder.cardCocktail.setOnClickListener { onCocktailClick.onButtonClick(cocktail) }
    }

    private fun formatDate(instant: Instant?): String {
        if (instant == null) return ""
        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    override fun getItemCount(): Int = cocktails.size

    fun updateData(newCocktails: List<Cocktail>) {
        Log.d("Search", "Updating cocktail adapter with ${newCocktails.size} items")
        this.cocktails = newCocktails.toMutableList()
        notifyDataSetChanged()
    }

}
