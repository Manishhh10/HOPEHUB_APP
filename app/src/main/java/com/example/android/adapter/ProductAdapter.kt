package com.example.android.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.R
import com.example.android.model.ProductModel
import com.example.android.ui.activity.UpdateProductActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.ArrayList

class ProductAdapter(
    private val context: Context,
    private var data: ArrayList<ProductModel>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.getImage)
        val loading: ProgressBar = itemView.findViewById(R.id.progressBar2)
        val pName: TextView = itemView.findViewById(R.id.displayName)
        val pPrice: TextView = itemView.findViewById(R.id.displayPrice)
        val pDesc: TextView = itemView.findViewById(R.id.displayDesc)
        val editButton: TextView = itemView.findViewById(R.id.edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView: View = LayoutInflater.from(context)
            .inflate(R.layout.sample_products, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = data[position]
        holder.pName.text = product.productName
        holder.pPrice.text = product.price.toString()
        holder.pDesc.text = product.productDesc

        // Check if the image URL is not null or empty
        if (!product.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(product.imageUrl).into(holder.imageView, object : Callback {
                override fun onSuccess() {
                    holder.loading.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    holder.loading.visibility = View.GONE
                    holder.imageView.setImageResource(R.drawable.error_image) // Set an error image
                }
            })
        } else {
            holder.loading.visibility = View.GONE
            holder.imageView.setImageResource(R.drawable.error_image) // Set an error image if the URL is empty
        }

        holder.editButton.setOnClickListener {
            val intent = Intent(context, UpdateProductActivity::class.java)
            intent.putExtra("productId", product.productId)
            context.startActivity(intent)
        }
    }

    fun updateData(products: List<ProductModel>) {
        data.clear()
        data.addAll(products)
        notifyDataSetChanged()
    }

    fun getProductId(position: Int): String {
        return data[position].productId
    }
}