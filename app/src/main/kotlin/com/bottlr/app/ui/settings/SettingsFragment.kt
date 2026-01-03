package com.bottlr.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bottlr.app.R
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.data.repository.CocktailRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * SettingsFragment - Firebase authentication and cloud sync
 *
 * Features:
 * - Google Sign-In
 * - Sign out
 * - Erase cloud storage
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var bottleRepository: BottleRepository

    @Inject
    lateinit var cocktailRepository: CocktailRepository

    private lateinit var userTextView: TextView
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button
    private lateinit var eraseButton: Button
    private lateinit var syncButton: Button

    private var navWindow: View? = null
    private var isNavOpen = false

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            if (isAdded && view != null) {
                Snackbar.make(requireView(), "Sign-in failed: ${e.statusCode}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_settings, container, false)

        // Initialize views with correct IDs from activity_settings.xml
        userTextView = view.findViewById(R.id.signed_in_user)
        loginButton = view.findViewById(R.id.login_Button)
        logoutButton = view.findViewById(R.id.logout_Button)
        eraseButton = view.findViewById(R.id.erase_Button)
        syncButton = view.findViewById(R.id.sync_Button)

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
            findNavController().navigate(R.id.bottleGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            findNavController().navigate(R.id.cocktailGalleryFragment)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            closeNavWindow()
        }

        setupUI()
        setupClickListeners()

        return view
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

    private fun setupUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userTextView.text = currentUser.email ?: "Signed in"
            loginButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
            eraseButton.visibility = View.VISIBLE
            syncButton.visibility = View.VISIBLE
        } else {
            userTextView.text = getString(R.string.not_signed_in)
            loginButton.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
            eraseButton.visibility = View.GONE
            syncButton.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            signIn()
        }

        logoutButton.setOnClickListener {
            signOut()
        }

        eraseButton.setOnClickListener {
            eraseCloudStorage()
        }

        syncButton.setOnClickListener {
            syncNow()
        }
    }

    private fun syncNow() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showSnackbar("Syncing...")
                // Upload local changes
                bottleRepository.syncAllToFirestore()
                cocktailRepository.syncAllToFirestore()
                // Pull remote changes
                bottleRepository.syncFromFirestore()
                cocktailRepository.syncFromFirestore()
                showSnackbar("Sync complete")
            } catch (e: Exception) {
                showSnackbar("Sync failed: ${e.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        if (isAdded && view != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.bottlr_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                setupUI()
                showSnackbar("Signed in successfully")
            } catch (e: Exception) {
                showSnackbar("Authentication failed: ${e.message}")
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        setupUI()
        showSnackbar("Signed out")
    }

    private fun eraseCloudStorage() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showSnackbar("Erasing cloud storage...")
                bottleRepository.eraseAllFromFirestore()
                cocktailRepository.eraseAllFromFirestore()
                showSnackbar("Cloud storage erased")
            } catch (e: Exception) {
                showSnackbar("Failed: ${e.message}")
            }
        }
    }
}
