package com.example.android.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityUpdateProductBinding
import com.example.android.repository.ProductRepositoryImpl
import com.example.android.viewmodel.ProductViewModel

class UpdateProductActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateProductBinding
    lateinit var productViewModel: ProductViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = ProductRepositoryImpl()
        productViewModel = ProductViewModel(repo)

        //if intent bata ako model ko value get garnu paryo bhane
//        var products: ProductModel? = intent.getParcelableExtra("products")
//
//        products.let {
//            binding.updateProductDesc.setText(it?.productName.toString())
//            binding.updateProductprice.setText(it?.price.toString())
//            binding.updateProductName.setText(it?.productName.toString())
//        }


        var productId : String? = intent.getStringExtra("productId")

        productViewModel.getProductById(productId.toString())

        productViewModel.products.observe(this){
            binding.updateProductName.setText(it?.productName.toString())
            binding.updateProductPrice.setText(it?.price.toString())
            binding.updateProductDesc.setText(it?.productDesc.toString())
        }

        binding.btnUpdateProduct.setOnClickListener {
            val productName = binding.updateProductName.text.toString()
            val productDesc = binding.updateProductDesc.text.toString()
            val productPrice = binding.updateProductPrice.text.toString().toInt()

            var updatedMap = mutableMapOf<String, Any>()
            updatedMap["productName"] = productName
            updatedMap["productDesc"] = productDesc
            updatedMap["price"] = productPrice


            productViewModel.updateProduct(
                productId.toString(),
                updatedMap
            ){ success, message ->
                if(success){
                    Toast.makeText(this@UpdateProductActivity,
                        message, Toast.LENGTH_LONG).show()
                    finish()
                }else {
                    Toast.makeText(this@UpdateProductActivity,
                        message, Toast.LENGTH_LONG).show()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}