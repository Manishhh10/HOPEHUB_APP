package com.example.android.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.R
import com.example.android.databinding.ActivityFundraiserDetailBinding
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepositoryImpl
import com.example.android.viewmodel.FundraiserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class FundraiserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFundraiserDetailBinding
    private lateinit var viewModel: FundraiserViewModel
    private lateinit var fundraiser: FundraiserModel
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFundraiserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = FundraiserViewModel(FundraiserRepositoryImpl())
        val fundraiserId = intent.getStringExtra("FUNDRAISER_ID") ?: return finish()

        loadFundraiserDetails(fundraiserId)
        setupButtons()
    }

    private fun loadFundraiserDetails(fundraiserId: String) {
        viewModel.getFundraiserById(fundraiserId)
        viewModel.fundraiser.observe(this) { fundraiser ->
            this.fundraiser = fundraiser ?: return@observe
            bindDataToViews()
        }
    }

    private fun bindDataToViews() {
        with(binding) {
            Picasso.get().load(fundraiser.imageUrl).into(ivFundraiser)
            tvTitle.text = fundraiser.title
            tvCategory.text = fundraiser.category
            tvReason.text = fundraiser.reason
            tvLocation.text = fundraiser.location
            tvTargetAmount.text = "Target: ${formatCurrency(fundraiser.targetAmount)}"
            tvCurrentAmount.text = "Raised: ${formatCurrency(fundraiser.currentAmount)}"
            tvDonationCount.text = "Donations: ${fundraiser.donationCount}"
            tvDates.text = "${fundraiser.startDate} - ${fundraiser.endDate}"
            progressBar.progress = (fundraiser.currentAmount / fundraiser.targetAmount * 100).toInt()

            val isCreator = currentUser?.uid == fundraiser.creatorId
            val isCompleted = fundraiser.currentAmount >= fundraiser.targetAmount

            when {
                isCompleted -> {
                    tvStatus.visibility = View.VISIBLE
                    btnDonate.visibility = View.GONE
                    layoutCreatorActions.visibility = View.GONE
                }
                isCreator -> {
                    layoutCreatorActions.visibility = View.VISIBLE
                    btnDonate.visibility = View.GONE
                    tvStatus.visibility = View.GONE
                }
                else -> {
                    btnDonate.visibility = View.VISIBLE
                    layoutCreatorActions.visibility = View.GONE
                    tvStatus.visibility = View.GONE
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnDonate.setOnClickListener { showDonationDialog() }
        binding.btnEdit.setOnClickListener { openEditActivity() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun showDonationDialog() {
        val remaining = fundraiser.targetAmount - fundraiser.currentAmount
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_donation, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etDonationAmount)
        val tvMax = dialogView.findViewById<TextView>(R.id.tvMaxAmount)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        tvMax.text = getString(R.string.max_donation, currencyFormat.format(remaining))

        AlertDialog.Builder(this)
            .setTitle("Make a Donation")
            .setView(dialogView)
            .setPositiveButton("Donate") { dialog, _ ->
                handleDonationInput(etAmount.text.toString(), remaining)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun handleDonationInput(input: String, remaining: Double) {
        if (input.isEmpty()) {
            showError("Please enter donation amount")
            return
        }

        try {
            val amount = input.toDouble()
            when {
                amount <= 0 -> showError("Amount must be greater than zero")
                amount > remaining -> showError("Amount exceeds remaining target")
                else -> processDonation(amount)
            }
        } catch (e: NumberFormatException) {
            showError("Invalid amount format")
        }
    }

    private fun processDonation(amount: Double) {
        viewModel.addDonation(fundraiser.fundraiserId, amount) { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) loadFundraiserDetails(fundraiser.fundraiserId)
        }
    }

    private fun openEditActivity() {
        Intent(this, UpdateFundraiserActivity::class.java).apply {
            putExtra("FUNDRAISER_ID", fundraiser.fundraiserId)
            startActivity(this)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Fundraiser")
            .setMessage("Are you sure you want to delete this fundraiser?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFundraiser(fundraiser.fundraiserId) { success, message ->
                    if (success) {
                        Toast.makeText(this, "Fundraiser deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}