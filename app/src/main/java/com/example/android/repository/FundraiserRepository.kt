package com.example.android.repository

import android.content.Context
import android.net.Uri
import com.example.android.model.FundraiserModel

interface FundraiserRepository {
    fun addFundraiser(fundraiser: FundraiserModel, callback: (Boolean, String) -> Unit)
    fun updateFundraiser(fundraiserId: String, data: MutableMap<String, Any>, callback: (Boolean, String) -> Unit)
    fun deleteFundraiser(fundraiserId: String, callback: (Boolean, String) -> Unit)
    fun getFundraiserById(fundraiserId: String, callback: (FundraiserModel?, Boolean, String) -> Unit)
    fun getAllFundraisers(callback: (List<FundraiserModel>?, Boolean, String) -> Unit)
    fun uploadImage(context: Context, imageUri: Uri, callback: (String) -> Unit)
    fun getFileNameFromUrl(context: Context, uri: Uri): String?
    fun addDonation(fundraiserId: String, amount: Double, callback: (Boolean, String) -> Unit)
}