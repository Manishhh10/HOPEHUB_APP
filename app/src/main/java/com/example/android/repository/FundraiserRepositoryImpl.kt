package com.example.android.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.android.model.FundraiserModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.io.InputStream
import java.util.concurrent.Executors

class FundraiserRepositoryImpl : FundraiserRepository {

    private val database: FirebaseDatabase = Firebase.database
    private val reference: DatabaseReference = database.reference.child("fundraisers")

    override fun addFundraiser(fundraiser: FundraiserModel, callback: (Boolean, String) -> Unit) {
        val id = reference.push().key ?: return callback(false, "Error generating ID")
        fundraiser.fundraiserId = id
        reference.child(id).setValue(fundraiser)
            .addOnCompleteListener {
                callback(it.isSuccessful, it.exception?.message ?: "Fundraiser created successfully")
            }
    }

    override fun updateFundraiser(fundraiserId: String, data: MutableMap<String, Any>, callback: (Boolean, String) -> Unit) {
        reference.child(fundraiserId).updateChildren(data)
            .addOnCompleteListener {
                callback(it.isSuccessful, it.exception?.message ?: "Fundraiser updated successfully")
            }
    }

    override fun deleteFundraiser(fundraiserId: String, callback: (Boolean, String) -> Unit) {
        reference.child(fundraiserId).removeValue()
            .addOnCompleteListener {
                callback(it.isSuccessful, it.exception?.message ?: "Fundraiser deleted")
            }
    }


    override fun getFundraiserById(fundraiserId: String, callback: (FundraiserModel?, Boolean, String) -> Unit) {
        reference.child(fundraiserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fundraiser = snapshot.getValue(FundraiserModel::class.java)
                callback(fundraiser, true, "Success")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun getAllFundraisers(callback: (List<FundraiserModel>?, Boolean, String) -> Unit) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fundraisers = mutableListOf<FundraiserModel>()
                snapshot.children.forEach { child ->
                    child.getValue(FundraiserModel::class.java)?.let { fundraisers.add(it) }
                }
                callback(fundraisers, true, "Success")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun addDonation(fundraiserId: String, amount: Double, callback: (Boolean, String) -> Unit) {
        val fundraiserRef = reference.child(fundraiserId)
        fundraiserRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val fundraiser = currentData.getValue(FundraiserModel::class.java)
                    ?: return Transaction.abort()

                // Update donation values
                fundraiser.currentAmount += amount
                fundraiser.donationCount += 1
                currentData.value = fundraiser
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    callback(false, "Donation failed: ${error.message}")
                } else {
                    callback(committed, if (committed) "Donation successful!" else "Donation failed")
                }
            }
        })
    }


    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dqnuuxzqt",
            "api_key" to "393255512166569",
            "api_secret" to "QcXyxln4besrLaykAs8EWxpDyNc"
        )
    )

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUrl(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    if (imageUrl != null) {
                        callback(imageUrl)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null.toString())
                }
            }
        }
    }

    override fun getFileNameFromUrl(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}