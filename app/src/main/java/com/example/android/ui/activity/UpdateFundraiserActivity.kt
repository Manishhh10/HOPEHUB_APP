package com.example.android.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityUpdateFundraiserBinding
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepositoryImpl
import com.example.android.viewmodel.FundraiserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateFundraiserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateFundraiserBinding
    private lateinit var viewModel: FundraiserViewModel
    private var fundraiserId: String? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateFundraiserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupDatePickers()
        loadFundraiserData()
        setupListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeComponents() {
        viewModel = FundraiserViewModel(FundraiserRepositoryImpl())
        fundraiserId = intent.getStringExtra("FUNDRAISER_ID")
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.etStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.etStartDate.setText("%02d/%02d/%d".format(day, month + 1, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.etEndDate.setText("%02d/%02d/%d".format(day, month + 1, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadFundraiserData() {
        fundraiserId?.let { id ->
            viewModel.getFundraiserById(id)
            viewModel.fundraiser.observe(this) { fundraiser ->
                fundraiser?.let {
                    binding.apply {
                        etTitle.setText(it.title)
                        etReason.setText(it.reason)
                        etLocation.setText(it.location)
                        etTargetAmount.setText(it.targetAmount.toString())
                        etStartDate.setText(it.startDate)
                        etEndDate.setText(it.endDate)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnUpdateFundraiser.setOnClickListener {
            if (validateForm()) {
                updateFundraiser()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Reset errors
        arrayOf(binding.etTitle, binding.etReason, binding.etLocation,
            binding.etTargetAmount, binding.etStartDate, binding.etEndDate).forEach {
            it.error = null
        }

        // Title validation
        if (binding.etTitle.text.isNullOrEmpty()) {
            binding.etTitle.error = "Title is required"
            isValid = false
        }

        // Reason validation
        if (binding.etReason.text.isNullOrEmpty()) {
            binding.etReason.error = "Reason is required"
            isValid = false
        }

        // Location validation
        if (binding.etLocation.text.isNullOrEmpty()) {
            binding.etLocation.error = "Location is required"
            isValid = false
        }

        // Target amount validation
        try {
            val amount = binding.etTargetAmount.text.toString().toDouble()
            if (amount <= 0) {
                binding.etTargetAmount.error = "Invalid target amount"
                isValid = false
            }
        } catch (e: NumberFormatException) {
            binding.etTargetAmount.error = "Invalid number format"
            isValid = false
        }

        // Date validation
        val startDate = binding.etStartDate.text.toString()
        val endDate = binding.etEndDate.text.toString()

        if (startDate.isEmpty()) {
            binding.etStartDate.error = "Start date required"
            isValid = false
        }

        if (endDate.isEmpty()) {
            binding.etEndDate.error = "End date required"
            isValid = false
        }

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            try {
                val start = dateFormat.parse(startDate)
                val end = dateFormat.parse(endDate)

                if (start != null && end != null && end.before(start)) {
                    binding.etEndDate.error = "End date must be after start date"
                    isValid = false
                }
            } catch (e: Exception) {
                binding.etStartDate.error = "Invalid date format (dd/MM/yyyy)"
                binding.etEndDate.error = "Invalid date format (dd/MM/yyyy)"
                isValid = false
            }
        }

        return isValid
    }

    private fun updateFundraiser() {
        val updatedData = mutableMapOf<String, Any>().apply {
            put("title", binding.etTitle.text.toString())
            put("reason", binding.etReason.text.toString())
            put("location", binding.etLocation.text.toString())
            put("targetAmount", binding.etTargetAmount.text.toString().toDouble())
            put("startDate", binding.etStartDate.text.toString())
            put("endDate", binding.etEndDate.text.toString())
        }
        fundraiserId?.let { id ->
            viewModel.updateFundraiser(id, updatedData) { success, message ->
                val toastMessage = message ?: if (success) "Update successful" else "Update failed"
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
                if (success) {
                    // Refresh data
                    viewModel.getAllFundraisers()
                    finish()
                }
            }
        }
    }
}