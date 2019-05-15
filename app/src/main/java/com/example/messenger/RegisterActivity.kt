package com.example.messenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity(), MainActivityPresenter {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_btn.setOnClickListener {
            onPerformRegister()
        }

        register_text.setOnClickListener {
            onSignin()
        }

        dp.setOnClickListener {
            Log.d("RegisterActivity", "SELECT IMAGE clicked")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo is selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            dp.setBackgroundDrawable(bitmapDrawable)
            dp.setImageBitmap(bitmap)
            Log.d("Display picture", "$bitmap || $selectedPhotoUri")
        }
    }

    private fun onPerformRegister() {
        val email = email.text.toString()
        val password = password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "email and password cannot be empty", Toast.LENGTH_LONG).show()
        } else {
            Log.d("RegisterActivity", "email = $email")
            Log.d("RegisterActivity", "password = $password")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(this, "Enter text in email & password", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }
                    Log.d("RegisterActivity", "Successfully created user with ${it.result.user.uid}")
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_LONG).show()

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to create user ${it.message}")
                    Toast.makeText(this, "Failed!!", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Image uploaded successsfully ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                // failed
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user data to firebase Database")

                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("RegisterActivity","saving user to firebase failed : ${it.message}")
            }
    }

    private fun onSignin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String)
