package com.example.android.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.android.model.UserModel
import com.example.android.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(val repo: UserRepository) {
    fun login(email:String,password:String,callback:(Boolean,String) -> Unit){
        repo.login(email,password, callback)
    }

    fun signup(email: String,password: String,callback: (Boolean,String,String) -> Unit){
        repo.signup(email, password, callback)
    }

    fun forgetPassword(email: String,callback: (Boolean, String) -> Unit){
        repo.forgetPassword(email, callback)
    }

    fun addUserToDatabase(userId:String, userModel: UserModel,
                          callback: (Boolean, String) -> Unit){
        repo.addUserToDatabase(userId, userModel, callback)
    }

    fun getCurrentUser() : FirebaseUser?{
        return repo.getCurrentUser()
    }

    var _userData = MutableLiveData<UserModel?>()
    var userData = MutableLiveData<UserModel?>()
        get() = _userData


    fun getUserFromDatabase(userId:String){
        repo.getUserFromDatabase(userId){
                userModel,sucess,message->
            if(sucess){
                _userData.value = userModel
            }else{
                _userData.value = null
            }
        }
    }

    fun logout(callback: (Boolean, String) -> Unit){
        repo.logout(callback)
    }

    fun editProfile(userId: String,data:MutableMap<String,Any>,
                    callback: (Boolean, String) -> Unit){
        repo.editProfile(userId, data, callback)
    }
}