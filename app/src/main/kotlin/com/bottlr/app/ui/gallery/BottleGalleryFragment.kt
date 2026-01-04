package com.bottlr.app.ui.gallery

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bottlr.app.BottleAdapter
import com.bottlr.app.R
import com.bottlr.app.util.NavDrawerHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottleGalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: BottleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var searchEditText: TextInputEditText? = null
    private var navDrawer: NavDrawerHelper? = null

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
        adapter = BottleAdapter { bottle ->
            findNavController().navigate(
                R.id.action_gallery_to_details,
                bundleOf("bottleId" to bottle.id)
            )
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

        // Nav drawer setup
        view.findViewById<View>(R.id.nav_window)?.let {
            navDrawer = NavDrawerHelper(it, resources = resources)
        }

        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            navDrawer?.toggle()
        }

        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            navDrawer?.close()
        }

        // Nav menu buttons
        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            navDrawer?.close() // Already on this screen
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            findNavController().navigate(R.id.cocktailGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        // Observe bottles from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottles.collectLatest { bottles ->
                adapter.submitList(bottles)
            }
        }

        return view
    }
}
