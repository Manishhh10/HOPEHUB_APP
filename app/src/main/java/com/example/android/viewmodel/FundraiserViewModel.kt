package com.example.android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.android.model.FundraiserModel
import com.example.android.repository.FundraiserRepository

class FundraiserViewModel(private val repository: FundraiserRepository) {

    fun addFundraiser(fundraiser: FundraiserModel, callback: (Boolean, String) -> Unit) {
        repository.addFundraiser(fundraiser, callback)
    }

    fun updateFundraiser(fundraiserId: String, data: MutableMap<String, Any>, callback: (Boolean, String) -> Unit) {
        repository.updateFundraiser(fundraiserId, data, callback)
    }

    fun deleteFundraiser(fundraiserId: String, callback: (Boolean, String) -> Unit) {
        repository.deleteFundraiser(fundraiserId, callback)
    }

    fun addDonation(fundraiserId: String, amount: Double, callback: (Boolean, String) -> Unit) {
        repository.addDonation(fundraiserId, amount, callback)
    }

    fun refreshFundraisers() {
        getAllFundraisers()
    }

    // LiveData for single fundraiser
    private val _fundraiser = MutableLiveData<FundraiserModel?>()
    val fundraiser: MutableLiveData<FundraiserModel?> get() = _fundraiser

    // LiveData for all fundraisers
    private val _allFundraisers = MutableLiveData<List<FundraiserModel>?>()
    val allFundraisers: MutableLiveData<List<FundraiserModel>?> get() = _allFundraisers

    // Loading state
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: MutableLiveData<Boolean> get() = _loadingState

    fun getFundraiserById(fundraiserId: String) {
        repository.getFundraiserById(fundraiserId) { fundraiser, success, message ->
            if (success) {
                _fundraiser.value = fundraiser
            }
        }
    }

    fun getAllFundraisers() {
        _loadingState.value = true
        repository.getAllFundraisers { fundraisers, success, message ->
            if (success) {
                _allFundraisers.value = fundraisers
            }
            _loadingState.value = false
        }
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repository.uploadImage(context, imageUri, callback)
    }
}