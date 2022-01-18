package com.penguinstech.contactsync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    companion object {
        var appId:String = "com.penguinstech.contactsync"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val database = FirebaseDatabase.getInstance()

        val userName:EditText = findViewById(R.id.user_name)
        val phoneEt:EditText = findViewById(R.id.phone_no)
        val submitBtn:Button = findViewById(R.id.login)

        submitBtn.setOnClickListener {
            if(userName.text.toString().equals("")) {
                userName.error = "All fields are required"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    userName.focusable
                }
            }else if(phoneEt.text.toString().equals("")) {
                phoneEt.error = "All fields are required"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    phoneEt.focusable
                }
            }else {

                val ref = database.getReference("users")
                ref.child(phoneEt.text.toString()).get()
                    .addOnSuccessListener {
                    if (it.exists()) {
                        //sign in
                        val user:User = it.getValue(User::class.java) as User
                        val sharedPref = this@LoginActivity.getSharedPreferences(
                            appId, Context.MODE_PRIVATE
                        )
                        val editor = sharedPref.edit()
                        editor.putString("userId", user.userId)
                        editor.putString("userName", user.userName)
                        editor.putString("phoneNo", user.phoneNo)
                        editor.commit()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        this@LoginActivity.finish()


                    }else {
                        //sign up

                        val userId = ref.push().key
                        ref.child(phoneEt.text.toString())
                            .setValue(
                                User(
                                    userId.toString(),
                                    userName.text.toString(),
                                    phoneEt.text.toString()
                                )
                            )
                            .addOnCompleteListener {

                                val sharedPref = this@LoginActivity.getSharedPreferences(
                                    appId, Context.MODE_PRIVATE
                                )
                                val editor = sharedPref.edit()
                                editor.putString("userId", userId)
                                editor.putString("userName", userName.text.toString())
                                editor.putString("phoneNo", phoneEt.text.toString())
                                editor.commit()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                this.finish()
                            }
                            .addOnFailureListener{
                                Toast.makeText(this@LoginActivity, "Could not sign up try again later", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                    .addOnFailureListener{
                        Toast.makeText(this@LoginActivity, "Could not sign up try again later", Toast.LENGTH_LONG).show()
                    }


            }
        }


    }


}