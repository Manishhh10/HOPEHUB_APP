package com.example.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityRegisterBinding
import com.example.android.model.UserModel
import com.example.android.repository.UserRepositoryImpl
import com.example.android.utils.LoadingUtils
import com.example.android.viewmodel.UserViewModel

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    lateinit var userViewModel: UserViewModel
    lateinit var loadingUtils: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingUtils = LoadingUtils(this)
        val userRepository = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepository)

        binding.signUp.setOnClickListener {
            loadingUtils.show()
            val email: String = binding.registerEmail.text.toString()
            val password: String = binding.registerPassword.text.toString()
            val Fname: String = binding.firstName.text.toString()
            val Lname: String = binding.lastName.text.toString()

            userViewModel.signup(email, password) { success, message, userId ->
                if (success) {
                    // Create UserModel with correct attribute assignments
                    val userModel = UserModel(
                        userId = userId, // userId should be assigned from the signup response
                        firstname = Fname, // First Name
                        lastname = Lname, // Last Name
                        email = email // Email
                    )
                    addUser(userModel)
                } else {
                    loadingUtils.dismiss()
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Add OnClickListener for the Login button
        binding.btnLoginn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun addUser(userModel: UserModel) {
        userViewModel.addUserToDatabase(userModel.userId, userModel) { success, message ->
            if (success) {
                Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
            }
            loadingUtils.dismiss()
        }
    }
}