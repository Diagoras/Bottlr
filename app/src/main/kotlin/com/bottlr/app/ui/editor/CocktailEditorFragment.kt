package com.bottlr.app.ui.editor

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
class CocktailEditorFragment : Fragment() {

    private val viewModel: CocktailEditorViewModel by viewModels()
    private var navDrawer: NavDrawerHelper? = null

    private lateinit var nameField: EditText
    private lateinit var baseField: EditText
    private lateinit var mixerField: EditText
    private lateinit var juiceField: EditText
    private lateinit var liqueurField: EditText
    private lateinit var garnishField: EditText
    private lateinit var extraField: EditText
    private lateinit var notesField: EditText
    private lateinit var keywordsField: EditText
    private lateinit var ratingField: EditText
    private lateinit var saveButton: Button
    private lateinit var addPhotoButton: Button
    private lateinit var deleteButton: ImageButton

    private var tempPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setPhotoUri(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.setPhotoUri(tempPhotoUri!!)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraIntent()
        } else {
            Snackbar.make(requireView(), "Camera permission required", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.addcocktailwindow, container, false)

        nameField = view.findViewById(R.id.cocktailNameField)
        baseField = view.findViewById(R.id.baseField)
        mixerField = view.findViewById(R.id.mixerField)
        juiceField = view.findViewById(R.id.juiceField)
        liqueurField = view.findViewById(R.id.liqueurField)
        garnishField = view.findViewById(R.id.garnishField)
        extraField = view.findViewById(R.id.extraField)
        notesField = view.findViewById(R.id.tastingNotesField)
        keywordsField = view.findViewById(R.id.keywordsField)
        ratingField = view.findViewById(R.id.ratingField)
        saveButton = view.findViewById(R.id.saveButtonCocktail)
        addPhotoButton = view.findViewById(R.id.addPhotoButtonCocktail)
        deleteButton = view.findViewById(R.id.deleteButton)

        // Show delete button only when editing existing cocktail
        if (viewModel.isEditMode) {
            deleteButton.visibility = View.VISIBLE
        }

        setupNavWindow(view)
        setupObservers()
        setupClickListeners()

        return view
    }

    private fun setupNavWindow(view: View) {
        view.findViewById<View>(R.id.nav_window)?.let {
            navDrawer = NavDrawerHelper(it, resources = resources)
        }

        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            navDrawer?.toggle()
        }

        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            navDrawer?.close()
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

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cocktail.collectLatest { cocktail ->
                cocktail?.let { populateFields(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveStatus.collectLatest { status ->
                when (status) {
                    is SaveStatus.Saving -> {
                        saveButton.isEnabled = false
                        saveButton.text = "Saving..."
                    }
                    is SaveStatus.Success -> {
                        Snackbar.make(requireView(), "Cocktail saved!", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is SaveStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                        saveButton.text = "Save Cocktail"
                    }
                    is SaveStatus.Idle -> {
                        saveButton.isEnabled = true
                        saveButton.text = "Save Cocktail"
                    }
                }
            }
        }

        // Observe delete status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteStatus.collectLatest { status ->
                when (status) {
                    is DeleteStatus.Deleting -> {
                        deleteButton.isEnabled = false
                    }
                    is DeleteStatus.Success -> {
                        Snackbar.make(requireView(), "Cocktail deleted", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is DeleteStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        deleteButton.isEnabled = true
                    }
                    is DeleteStatus.Idle -> {
                        deleteButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveCocktail()
        }

        addPhotoButton.setOnClickListener {
            showPhotoDialog()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Cocktail")
            .setMessage("Are you sure you want to delete this cocktail? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCocktail()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCocktail() {
        val name = nameField.text.toString().trim()
        if (name.isEmpty()) {
            Snackbar.make(requireView(), "Name is required", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.saveCocktail(
            name = name,
            base = baseField.text.toString(),
            mixer = mixerField.text.toString(),
            juice = juiceField.text.toString(),
            liqueur = liqueurField.text.toString(),
            garnish = garnishField.text.toString(),
            extra = extraField.text.toString(),
            notes = notesField.text.toString(),
            keywords = keywordsField.text.toString(),
            rating = ratingField.text.toString().toFloatOrNull()
        )
    }

    private fun showPhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery", "Cancel")) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCameraIntent()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCameraIntent() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "cocktail_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        tempPhotoUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        tempPhotoUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun populateFields(cocktail: CocktailEntity) {
        nameField.setText(cocktail.name)
        baseField.setText(cocktail.base)
        mixerField.setText(cocktail.mixer)
        juiceField.setText(cocktail.juice)
        liqueurField.setText(cocktail.liqueur)
        garnishField.setText(cocktail.garnish)
        extraField.setText(cocktail.extra)
        notesField.setText(cocktail.notes)
        keywordsField.setText(cocktail.keywords)
        ratingField.setText(cocktail.rating?.toString() ?: "")
    }
}
