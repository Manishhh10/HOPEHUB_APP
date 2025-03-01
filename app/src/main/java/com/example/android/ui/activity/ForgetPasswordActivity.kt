package com.example.android.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityForgetPasswordBinding
import com.example.android.repository.UserRepositoryImpl
import com.example.android.viewmodel.UserViewModel
import com.example.android.utils.LoadingUtils

class ForgetPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgetPasswordBinding
    lateinit var userViewModel: UserViewModel
    lateinit var loadingUtils: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingUtils = LoadingUtils(this)
        userViewModel = UserViewModel(UserRepositoryImpl())

        binding.btnResetPassword.setOnClickListener {
            loadingUtils.show()
            val email: String = binding.emailInput.text.toString()

            userViewModel.forgetPassword(email) { success, message ->
                loadingUtils.dismiss()
                Toast.makeText(this@ForgetPasswordActivity, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    finish() // Close this activity after successful request
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