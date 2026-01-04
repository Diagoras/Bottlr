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
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.util.NavDrawerHelper
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BottleDetailsFragment - Display full bottle information
 *
 * Uses DetailsViewModel for:
 * - Reactive bottle data (auto-updates!)
 * - Delete functionality with Firestore sync
 */
@AndroidEntryPoint
class BottleDetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModels()
    private var navDrawer: NavDrawerHelper? = null

    // UI elements - matching description_screen.xml IDs
    private lateinit var bottleImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var distilleryText: TextView
    private lateinit var detailsText: TextView  // Combined type, region, age, ABV
    private lateinit var ratingText: TextView
    private lateinit var notesText: TextView
    private lateinit var keywordsText: TextView
    private lateinit var editButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var nfcButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.description_screen, container, false)

        // Initialize UI elements with correct IDs from description_screen.xml
        bottleImage = view.findViewById(R.id.detailImageView)
        nameText = view.findViewById(R.id.tvBottleName)
        distilleryText = view.findViewById(R.id.tvDistillery)
        detailsText = view.findViewById(R.id.tvBottleDetails)
        ratingText = view.findViewById(R.id.tvRating)
        notesText = view.findViewById(R.id.tvNotes)
        keywordsText = view.findViewById(R.id.tvKeywords)
        editButton = view.findViewById(R.id.editButton)
        backButton = view.findViewById(R.id.backButton)
        nfcButton = view.findViewById(R.id.nfcButton)

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
        // Observe bottle data (reactive!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottle.collectLatest { bottle ->
                bottle?.let { displayBottle(it) }
            }
        }

        // Observe delete status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteStatus.collectLatest { status ->
                when (status) {
                    is DeleteStatus.Deleting -> {
                        editButton.isEnabled = false
                    }
                    is DeleteStatus.Success -> {
                        Snackbar.make(requireView(), "Bottle deleted", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is DeleteStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        editButton.isEnabled = true
                    }
                    is DeleteStatus.Idle -> {
                        editButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        editButton.setOnClickListener {
            viewModel.bottle.value?.let { bottle ->
                findNavController().navigate(
                    R.id.action_details_to_editor,
                    bundleOf("bottleId" to bottle.id)
                )
            }
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        nfcButton.setOnClickListener {
            Snackbar.make(requireView(), "NFC sharing coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun displayBottle(bottle: BottleEntity) {
        nameText.text = bottle.name.ifEmpty { "Unknown" }
        distilleryText.text = bottle.distillery.ifEmpty { "No distillery" }

        // Build combined details string matching the original layout format
        val type = bottle.type.ifEmpty { "No Type" }
        val region = bottle.region.ifEmpty { "No Region" }
        val age = bottle.age?.let { "$it Year" } ?: "No"
        val abv = bottle.abv?.let { "$it%" } ?: "N/A"
        detailsText.text = "$type, $region, $age, $abv ABV"

        ratingText.text = bottle.rating?.let { "$it / 10" } ?: "Not rated"
        notesText.text = bottle.notes.ifEmpty { "No notes" }
        keywordsText.text = "Keywords: ${bottle.keywords.ifEmpty { "None" }}"

        // Load photo
        bottle.photoUri?.let { uri ->
            Glide.with(this)
                .load(Uri.parse(uri))
                .placeholder(R.drawable.nodrinkimg)
                .error(R.drawable.nodrinkimg)
                .into(bottleImage)
        } ?: bottleImage.setImageResource(R.drawable.nodrinkimg)
    }
}
