package com.example.android.model

import android.os.Parcel
import android.os.Parcelable

data class FundraiserModel(
    var fundraiserId: String = "",
    var title: String = "",
    var category: String = "",
    var reason: String = "",
    var location: String = "",
    var targetAmount: Double = 0.0,
    var currentAmount: Double = 0.0,
    var startDate: String = "",
    var endDate: String = "",
    var imageUrl: String = "",
    var creatorId: String = "",
    var donationCount: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fundraiserId)
        parcel.writeString(title)
        parcel.writeString(category)
        parcel.writeString(reason)
        parcel.writeString(location)
        parcel.writeDouble(targetAmount)
        parcel.writeDouble(currentAmount)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(imageUrl)
        parcel.writeString(creatorId)
        parcel.writeInt(donationCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FundraiserModel> {
        override fun createFromParcel(parcel: Parcel): FundraiserModel {
            return FundraiserModel(parcel)
        }

        override fun newArray(size: Int): Array<FundraiserModel?> {
            return arrayOfNulls(size)
        }
    }
}