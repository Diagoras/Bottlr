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
import com.bottlr.app.CocktailAdapter
import com.bottlr.app.R
import com.bottlr.app.util.NavDrawerHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CocktailGalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: CocktailAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
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
        adapter = CocktailAdapter(mutableListOf()) { cocktail ->
            findNavController().navigate(
                R.id.action_cocktails_to_details,
                bundleOf("cocktailId" to cocktail.id)
            )
        }
        recyclerView.adapter = adapter

        // Setup FAB to add new cocktail
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            findNavController().navigate(
                R.id.action_cocktails_to_editor,
                bundleOf("cocktailId" to -1L)
            )
        }

        // Setup search field
        view.findViewById<TextInputEditText>(R.id.search_edit_text)?.addTextChangedListener(object : TextWatcher {
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
            findNavController().navigate(R.id.bottleGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            navDrawer?.close() // Already on this screen
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        // Observe cocktails from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cocktails.collectLatest { cocktails ->
                adapter.updateData(cocktails)
            }
        }

        return view
    }
}
