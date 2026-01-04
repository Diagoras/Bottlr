package com.bottlr.app.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bottlr.app.Bottle
import com.bottlr.app.BottleAdapter
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.BottleEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BottleGalleryFragment - Displays the liquor cabinet
 *
 * Uses GalleryViewModel for reactive data
 * Shows bottles in a grid layout
 * Navigates to details when bottle clicked
 * Navigates to editor when FAB clicked
 */
@AndroidEntryPoint
class BottleGalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: BottleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var searchEditText: TextInputEditText? = null

    private var navWindow: View? = null
    private var isNavOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.liquorRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup adapter with click listener
        adapter = BottleAdapter(
            mutableListOf()
        ) { bottleName, _, _, _, _, _, _, _, _, _, _ ->
            // Navigate to details with bottle name
            navigateToDetails(bottleName)
        }
        recyclerView.adapter = adapter

        // Setup FAB to add new bottle
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            findNavController().navigate(
                R.id.action_gallery_to_editor,
                bundleOf("bottleId" to -1L) // -1 = new bottle
            )
        }

        // Setup search field
        searchEditText = view.findViewById(R.id.search_edit_text)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // Nav window setup
        navWindow = view.findViewById(R.id.nav_window)

        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            toggleNavWindow()
        }

        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            closeNavWindow()
        }

        // Nav menu buttons
        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            closeNavWindow() // Already on this screen
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            findNavController().navigate(R.id.cocktailGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        // Observe bottles from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottles.collectLatest { bottleEntities ->
                // Convert BottleEntity to legacy Bottle for adapter
                val bottles = bottleEntities.map { convertToLegacyBottle(it) }
                adapter.updateData(bottles)
            }
        }

        return view
    }

    private fun navigateToDetails(bottleName: String) {
        // For now, navigate with name - ideally we'd pass the ID
        // This is a temporary solution until we update the adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottles.value.find { it.name == bottleName }?.let { bottle ->
                findNavController().navigate(
                    R.id.action_gallery_to_details,
                    bundleOf("bottleId" to bottle.id)
                )
            }
        }
    }

    /**
     * Convert new BottleEntity to legacy Bottle for existing adapter
     * TODO: Update adapter to work directly with BottleEntity
     */
    private fun convertToLegacyBottle(entity: BottleEntity): Bottle {
        return Bottle(
            entity.name,
            entity.distillery,
            entity.type,
            entity.abv?.toString() ?: "",
            entity.age?.toString() ?: "",
            entity.photoUri?.let { Uri.parse(it) },
            entity.notes,
            entity.region,
            entity.keywords,
            entity.rating?.toString() ?: ""
        ).apply {
            bottleID = entity.id.toString()
            createdAt = entity.createdAt
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
            ?.translationX(-280f * resources.displayMetrics.density)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = false
    }
}
