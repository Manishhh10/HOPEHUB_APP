package com.example.android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.databinding.ActivityFundraiserDetailBinding
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepositoryImpl
import com.example.android.viewmodel.FundraiserViewModel
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class FundraiserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFundraiserDetailBinding
    private lateinit var viewModel: FundraiserViewModel
    private lateinit var fundraiser: FundraiserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFundraiserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = FundraiserViewModel(FundraiserRepositoryImpl())
        val fundraiserId = intent.getStringExtra("FUNDRAISER_ID") ?: return finish()

        loadFundraiserDetails(fundraiserId)
        setupDonateButton()
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
        }
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
    }

    private fun setupDonateButton() {
        binding.btnDonate.setOnClickListener {
            // Implement donation logic
        }
    }
}