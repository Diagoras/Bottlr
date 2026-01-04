package com.bottlr.app

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.util.toShortDateString
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class CocktailAdapter(
    private var cocktails: MutableList<CocktailEntity> = mutableListOf(),
    private val onCocktailClick: (CocktailEntity) -> Unit
) : RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder>() {

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
        holder.textDate.text = cocktail.createdAt.toShortDateString()

        // Build ingredients summary
        val ingredients = listOf(cocktail.base, cocktail.mixer, cocktail.juice)
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(", ")
        holder.textIngredients.text = ingredients.ifEmpty { cocktail.base }

        cocktail.photoUri?.let { uriString ->
            if (uriString.isNotEmpty() && uriString != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(uriString))
                    .centerCrop()
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageCocktail)
            } else {
                holder.imageCocktail.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageCocktail.setImageResource(R.drawable.nodrinkimg)

        holder.cardCocktail.setOnClickListener {
            onCocktailClick(cocktail)
        }
    }

    override fun getItemCount(): Int = cocktails.size

    fun updateData(newCocktails: List<CocktailEntity>) {
        Log.d("Search", "Updating cocktail adapter with ${newCocktails.size} items")
        this.cocktails = newCocktails.toMutableList()
        notifyDataSetChanged()
    }

}
