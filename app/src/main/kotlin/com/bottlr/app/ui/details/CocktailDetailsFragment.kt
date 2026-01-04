package com.bottlr.app.ui.details

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.util.NavDrawerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CocktailDetailsFragment : Fragment() {

    private val viewModel: CocktailDetailsViewModel by viewModels()
    private var navDrawer: NavDrawerHelper? = null

    private lateinit var cocktailImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var baseText: TextView
    private lateinit var detailsText: TextView
    private lateinit var ratingText: TextView
    private lateinit var notesText: TextView
    private lateinit var keywordsText: TextView
    private lateinit var editButton: ImageButton
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
        backButton = view.findViewById(R.id.backButton)

        setupNavWindow(view)
        setupObservers()
        setupClickListeners()

        return view
    }

    private fun setupNavWindow(view: View) {
        val navWindow = view.findViewById<View>(R.id.nav_window)
        val navScrim = view.findViewById<View>(R.id.nav_scrim)

        if (navWindow != null) {
            navDrawer = NavDrawerHelper(navWindow, navScrim, resources)
        }

        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            navDrawer?.toggle()
        }

        // Tapping scrim closes drawer
        navScrim?.setOnClickListener {
            navDrawer?.close()
        }

        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            navDrawer?.close()
        }

        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            navDrawer?.close()
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            navDrawer?.close()
            findNavController().navigate(R.id.bottleGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            navDrawer?.close()
            findNavController().navigate(R.id.cocktailGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            navDrawer?.close()
            findNavController().navigate(R.id.settingsFragment)
        }

        // Back press closes drawer instead of navigating back
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navDrawer?.isDrawerOpen == true) {
                        navDrawer?.close()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
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
}
