package com.example.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.adapter.FundraiserAdapter
import com.example.android.databinding.ActivityFundraiserDashboardBinding
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepositoryImpl
import com.example.android.viewmodel.FundraiserViewModel
import com.google.firebase.auth.FirebaseAuth

class FundraiserDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFundraiserDashboardBinding
    private lateinit var viewModel: FundraiserViewModel
    private lateinit var adapter: FundraiserAdapter
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFundraiserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFAB()
        initializeComponents()
        setupRecyclerView()
        observeViewModel()
        loadFundraisers()
    }

    private fun setupFAB() {
        binding.fabAddFundraiser.setOnClickListener {
            startActivity(Intent(this, CreateFundraiserActivity::class.java))
        }
    }

    private fun initializeComponents() {
        viewModel = FundraiserViewModel(FundraiserRepositoryImpl())
        adapter = FundraiserAdapter(
            this,
            emptyList(),
            currentUser?.uid,
            { fundraiserId, amount ->
                viewModel.addDonation(fundraiserId, amount) { success, message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        // Refresh data after successful donation
                        viewModel.getAllFundraisers()
                    }
                }
            },
            { fundraiserId ->
                val intent = Intent(this, UpdateFundraiserActivity::class.java).apply {
                    putExtra("FUNDRAISER_ID", fundraiserId)
                }
                startActivity(intent)
            },
            { fundraiserId ->
                viewModel.deleteFundraiser(fundraiserId) { success, message ->
                    if (success) {
                        viewModel.getAllFundraisers() // Refresh list
                        Toast.makeText(this, "Fundraiser deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerFundraisers.layoutManager = LinearLayoutManager(this)
        binding.recyclerFundraisers.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.allFundraisers.observe(this) { fundraisers ->
            fundraisers?.let { adapter.updateData(it) }
        }

        viewModel.loadingState.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadFundraisers() {
        viewModel.getAllFundraisers()
    }
}