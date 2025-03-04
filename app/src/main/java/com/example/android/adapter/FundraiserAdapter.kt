package com.example.android.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.android.R
import com.example.android.model.FundraiserModel
import com.example.android.ui.activity.FundraiserDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*

class FundraiserAdapter(
    private val context: Context,
    private var data: List<FundraiserModel>,
    private val currentUserId: String?,
    private val onDonate: (fundraiserId: String, amount: Double) -> Unit,
    private val onEdit: (fundraiserId: String) -> Unit,  // Add this
    private val onDelete: (fundraiserId: String) -> Unit
) : RecyclerView.Adapter<FundraiserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val btnDonate: Button = itemView.findViewById(R.id.btnDonate)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivFundraiser: ImageView = itemView.findViewById(R.id.ivFundraiser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_fundraiser, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fundraiser = data[position]
        Picasso.get()
            .load(fundraiser.imageUrl)
            .placeholder(R.drawable.error_image)
            .error(R.drawable.error_image)
            .into(holder.ivFundraiser)
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        holder.tvTitle.text = fundraiser.title
        holder.tvProgress.text = "Rs.${"%.2f".format(fundraiser.currentAmount)} raised of Rs.${"%.2f".format(fundraiser.targetAmount)}"

        val progress = (fundraiser.currentAmount / fundraiser.targetAmount * 100).toInt()
        holder.progressBar.progress = progress

        val isCreator = currentUserId == fundraiser.creatorId
        val isCompleted = fundraiser.currentAmount >= fundraiser.targetAmount

        if (isCompleted) {
            holder.tvStatus.visibility = View.VISIBLE
            holder.btnDonate.visibility = View.GONE
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.tvStatus.visibility = View.GONE
            holder.btnDonate.visibility = if (!isCreator) View.VISIBLE else View.GONE
            holder.btnEdit.visibility = if (isCreator) View.VISIBLE else View.GONE
            holder.btnDelete.visibility = if (isCreator) View.VISIBLE else View.GONE
        }

        holder.btnEdit.setOnClickListener {
            if (!isCompleted) {
                onEdit(fundraiser.fundraiserId)
            }
        }

        holder.btnDelete.setOnClickListener {
            if (!isCompleted) {
                showDeleteConfirmation(fundraiser.fundraiserId)
            }
        }

        holder.btnDonate.setOnClickListener {
            showDonationDialog(fundraiser.fundraiserId, fundraiser.targetAmount - fundraiser.currentAmount)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, FundraiserDetailActivity::class.java).apply {
                putExtra("FUNDRAISER_ID", fundraiser.fundraiserId)
            }
            context.startActivity(intent)
        }
    }

    private fun showDeleteConfirmation(fundraiserId: String) {
        AlertDialog.Builder(context)
            .setTitle("Delete Fundraiser")
            .setMessage("Are you sure you want to delete this fundraiser?")
            .setPositiveButton("Delete") { _, _ ->
                onDelete(fundraiserId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDonationDialog(fundraiserId: String, remainingAmount: Double) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_donation, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etDonationAmount)
        val tvMax = dialogView.findViewById<TextView>(R.id.tvMaxAmount)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        tvMax.text = context.getString(R.string.max_donation, "Rs.${"%.2f".format(remainingAmount)}")
        AlertDialog.Builder(context)
            .setTitle("Make a Donation")
            .setView(dialogView)
            .setPositiveButton("Donate") { dialog, _ ->
                val input = etAmount.text.toString()
                if (input.isNotEmpty()) {
                    try {
                        val amount = input.toDouble()
                        when {
                            amount <= 0 -> showError("Amount must be greater than zero")
                            amount > remainingAmount -> showError("Amount exceeds remaining target")
                            else -> {
                                onDonate(fundraiserId, amount)
                                dialog.dismiss()
                            }
                        }
                    } catch (e: NumberFormatException) {
                        showError("Invalid amount format")
                    }
                } else {
                    showError("Please enter donation amount")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<FundraiserModel>) {
        data = newData
        notifyDataSetChanged()
    }
}