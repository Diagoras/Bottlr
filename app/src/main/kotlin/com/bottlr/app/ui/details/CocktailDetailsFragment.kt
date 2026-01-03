package com.bottlr.app.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.CocktailEntity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CocktailDetailsFragment : Fragment() {

    private val viewModel: CocktailDetailsViewModel by viewModels()

    private var navWindow: View? = null
    private var isNavOpen = false

    private lateinit var cocktailImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var baseText: TextView
    private lateinit var detailsText: TextView
    private lateinit var ratingText: TextView
    private lateinit var notesText: TextView
    private lateinit var keywordsText: TextView
    private lateinit var editButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.description_cocktails, container, false)

        cocktailImage = view.findViewById(R.id.detailImageView)
        nameText = view.findViewById(R.id.cvCocktailName)
        baseText = view.findViewById(R.id.cvBase)
        detailsText = view.findViewById(R.id.cvCocktailDetails)
        ratingText = view.findViewById(R.id.cvRating)
        notesText = view.findViewById(R.id.cvNotes)
        keywordsText = view.findViewById(R.id.cvKeywords)
        editButton = view.findViewById(R.id.editButton)
        shareButton = view.findViewById(R.id.shareButton)
        backButton = view.findViewById(R.id.backButton)

        setupNavWindow(view)
        setupObservers()
        setupClickListeners()

        return view
    }

    private fun setupNavWindow(view: View) {
        navWindow = view.findViewById(R.id.nav_window)

        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            toggleNavWindow()
        }

        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            closeNavWindow()
        }

        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            findNavController().navigate(R.id.bottleGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            findNavController().navigate(R.id.cocktailGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    private fun toggleNavWindow() {
        if (isNavOpen) closeNavWindow() else openNavWindow()
    }

    private fun openNavWindow() {
        navWindow?.animate()
            ?.translationX(0f)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = true
    }

    private fun closeNavWindow() {
        navWindow?.animate()
            ?.translationX(-420f * resources.displayMetrics.density)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = false
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cocktail.collectLatest { cocktail ->
                cocktail?.let { displayCocktail(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteStatus.collectLatest { status ->
                when (status) {
                    is DeleteStatus.Success -> {
                        Snackbar.make(requireView(), "Cocktail deleted", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is DeleteStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        shareButton.setOnClickListener {
            shareCocktail()
        }

        editButton.setOnClickListener {
            Snackbar.make(requireView(), "Edit coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun displayCocktail(cocktail: CocktailEntity) {
        nameText.text = cocktail.name.ifEmpty { "Unknown" }
        baseText.text = "Base: ${cocktail.base.ifEmpty { "None" }}"

        val details = listOfNotNull(
            cocktail.mixer.takeIf { it.isNotEmpty() }?.let { "Mixer: $it" },
            cocktail.juice.takeIf { it.isNotEmpty() }?.let { "Juice: $it" },
            cocktail.liqueur.takeIf { it.isNotEmpty() }?.let { "Liqueur: $it" },
            cocktail.garnish.takeIf { it.isNotEmpty() }?.let { "Garnish: $it" }
        ).joinToString("\n")
        detailsText.text = details.ifEmpty { "No ingredients" }

        ratingText.text = cocktail.rating?.let { "Rating: $it / 10" } ?: "Not rated"
        notesText.text = cocktail.notes.ifEmpty { "No notes" }
        keywordsText.text = "Keywords: ${cocktail.keywords.ifEmpty { "None" }}"

        cocktail.photoUri?.let { uri ->
            Glide.with(this)
                .load(Uri.parse(uri))
                .placeholder(R.drawable.nodrinkimg)
                .error(R.drawable.nodrinkimg)
                .into(cocktailImage)
        } ?: cocktailImage.setImageResource(R.drawable.nodrinkimg)
    }

    private fun shareCocktail() {
        viewModel.cocktail.value?.let { cocktail ->
            val text = buildString {
                append("Check out this cocktail!\n\n")
                append("${cocktail.name}\n")
                append("Base: ${cocktail.base}\n")
                if (cocktail.mixer.isNotEmpty()) append("Mixer: ${cocktail.mixer}\n")
                if (cocktail.juice.isNotEmpty()) append("Juice: ${cocktail.juice}\n")
                if (cocktail.liqueur.isNotEmpty()) append("Liqueur: ${cocktail.liqueur}\n")
                if (cocktail.garnish.isNotEmpty()) append("Garnish: ${cocktail.garnish}\n")
                cocktail.rating?.let { append("Rating: $it/10\n") }
                if (cocktail.notes.isNotEmpty()) append("\nNotes: ${cocktail.notes}")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, "Share Cocktail"))
        }
    }
}
