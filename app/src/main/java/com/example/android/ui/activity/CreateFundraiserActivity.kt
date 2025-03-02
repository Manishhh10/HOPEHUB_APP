package com.example.android.ui.activity

import android.R
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.databinding.ActivityCreateFundraiserBinding
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepositoryImpl
import com.example.android.utils.ImageUtils
import com.example.android.utils.LoadingUtils
import com.example.android.viewmodel.FundraiserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateFundraiserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateFundraiserBinding
    private lateinit var viewModel: FundraiserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var imageUtils: ImageUtils
    private var imageUri: Uri? = null
    private val categories = arrayOf("Medical", "Education", "Animals", "Environment", "Business", "Community", "Sports", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFundraiserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()
        setupDatePickers()
        initializeComponents()
        setupClickListeners()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.editStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.editStartDate.setText("%02d/%02d/%d".format(day, month + 1, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.editEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.editEndDate.setText("%02d/%02d/%d".format(day, month + 1, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun initializeComponents() {
        loadingUtils = LoadingUtils(this)
        imageUtils = ImageUtils(this)
        viewModel = FundraiserViewModel(FundraiserRepositoryImpl())

        imageUtils.registerActivity { uri ->
            imageUri = uri
            binding.ivFundraiserImage.setImageURI(uri)
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            imageUtils.launchGallery(this)
        }

        binding.btnCreateHope.setOnClickListener {
            if (validateForm()) {
                uploadImageAndCreateFundraiser()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val resources = resources

        // Reset errors
        binding.editTitle.error = null
        binding.editReason.error = null
        binding.editLocation.error = null
        binding.editTargetAmount.error = null
        binding.editStartDate.error = null
        binding.editEndDate.error = null

        // Title validation
        if (binding.editTitle.text.toString().trim().isEmpty()) {
            binding.editTitle.error = "Title is required"
            isValid = false
        }

        // Reason validation
        if (binding.editReason.text.toString().trim().isEmpty()) {
            binding.editReason.error = "Reason is required"
            isValid = false
        }

        // Location validation
        if (binding.editLocation.text.toString().trim().isEmpty()) {
            binding.editLocation.error = "Location is required"
            isValid = false
        }

        // Target amount validation
        val targetAmountStr = binding.editTargetAmount.text.toString()
        if (targetAmountStr.isEmpty()) {
            binding.editTargetAmount.error = "Target amount is required"
            isValid = false
        } else {
            try {
                val amount = targetAmountStr.toDouble()
                if (amount <= 0) {
                    binding.editTargetAmount.error = "Amount must be greater than 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.editTargetAmount.error = "Invalid amount format"
                isValid = false
            }
        }

        // Date validation
        val startDateStr = binding.editStartDate.text.toString()
        val endDateStr = binding.editEndDate.text.toString()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (startDateStr.isEmpty()) {
            binding.editStartDate.error = "Start date is required"
            isValid = false
        }

        if (endDateStr.isEmpty()) {
            binding.editEndDate.error = "End date is required"
            isValid = false
        }

        if (startDateStr.isNotEmpty() && endDateStr.isNotEmpty()) {
            try {
                val startDate = dateFormat.parse(startDateStr)
                val endDate = dateFormat.parse(endDateStr)

                if (startDate != null && endDate != null) {
                    if (endDate.before(startDate)) {
                        binding.editEndDate.error = "End date must be after start date"
                        isValid = false
                    }
                }
            } catch (e: ParseException) {
                binding.editStartDate.error = "Invalid date format (dd/MM/yyyy)"
                binding.editEndDate.error = "Invalid date format (dd/MM/yyyy)"
                isValid = false
            }
        }

        // Image validation
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun uploadImageAndCreateFundraiser() {
        loadingUtils.show()
        imageUri?.let { uri ->
            viewModel.uploadImage(this, uri) { imageUrl ->
                if (imageUrl != null) {
                    createFundraiser(imageUrl)
                } else {
                    loadingUtils.dismiss()
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            createFundraiser("")
        }
    }

    private fun createFundraiser(imageUrl: String) {
        val fundraiser = FundraiserModel(
            title = binding.editTitle.text.toString(),
            category = binding.spinnerCategory.selectedItem.toString(),
            reason = binding.editReason.text.toString(),
            location = binding.editLocation.text.toString(),
            targetAmount = binding.editTargetAmount.text.toString().toDouble(),
            startDate = binding.editStartDate.text.toString(),
            endDate = binding.editEndDate.text.toString(),
            imageUrl = imageUrl,
            creatorId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            currentAmount = 0.0,
            donationCount = 0
        )

        viewModel.addFundraiser(fundraiser) { success, message ->
            loadingUtils.dismiss()
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) finish()
        }
    }
}