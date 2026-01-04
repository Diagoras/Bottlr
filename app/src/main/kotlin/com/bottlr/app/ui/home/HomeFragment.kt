package com.bottlr.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bottlr.app.R
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * HomeFragment - App's main dashboard screen
 *
 * Shows collection stats and provides quick navigation to:
 * - Liquor Cabinet (Bottle Gallery)
 * - Cocktail Maker (Cocktail Gallery)
 * - Settings
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var navWindow: View? = null
    private var navScrim: View? = null
    private var isNavOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.homescreen, container, false)

        // Setup nav drawer
        navWindow = view.findViewById(R.id.nav_window)
        navScrim = view.findViewById(R.id.nav_scrim)

        // Menu button opens nav drawer
        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            toggleNavWindow()
        }

        // Scrim closes nav drawer
        navScrim?.setOnClickListener {
            closeNavWindow()
        }

        // Account button navigates to settings
        view.findViewById<View>(R.id.account_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // Stats card - Liquor Cabinet
        view.findViewById<MaterialCardView>(R.id.card_liquor)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gallery)
        }

        // Stats card - Cocktail Maker
        view.findViewById<MaterialCardView>(R.id.card_cocktails)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cocktails)
        }

        // Quick action buttons
        view.findViewById<View>(R.id.btn_add_bottle)?.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_gallery,
                bundleOf("openEditor" to true)
            )
        }

        view.findViewById<View>(R.id.btn_add_cocktail)?.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_cocktails,
                bundleOf("openEditor" to true)
            )
        }

        // Nav drawer buttons
        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            closeNavWindow()
        }

        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            closeNavWindow()
            findNavController().navigate(R.id.action_home_to_gallery)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            closeNavWindow()
            findNavController().navigate(R.id.action_home_to_cocktails)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            closeNavWindow()
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // Observe counts from ViewModel
        val bottleCountText = view.findViewById<TextView>(R.id.bottle_count)
        val cocktailCountText = view.findViewById<TextView>(R.id.cocktail_count)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottleCount.collectLatest { count ->
                bottleCountText?.text = if (count == 1) "1 bottle" else "$count bottles"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cocktailCount.collectLatest { count ->
                cocktailCountText?.text = if (count == 1) "1 cocktail" else "$count cocktails"
            }
        }

        return view
    }

    private fun toggleNavWindow() {
        if (isNavOpen) closeNavWindow() else openNavWindow()
    }

    private fun openNavWindow() {
        navScrim?.visibility = View.VISIBLE
        navScrim?.animate()
            ?.alpha(1f)
            ?.setDuration(300)
            ?.start()

        navWindow?.animate()
            ?.translationX(0f)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = true
    }

    private fun closeNavWindow() {
        navScrim?.animate()
            ?.alpha(0f)
            ?.setDuration(300)
            ?.withEndAction { navScrim?.visibility = View.GONE }
            ?.start()

        navWindow?.animate()
            ?.translationX(-280f * resources.displayMetrics.density)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = false
    }
}
