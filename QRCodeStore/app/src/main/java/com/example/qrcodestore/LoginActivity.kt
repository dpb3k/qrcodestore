package com.example.qrcodestore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        supportActionBar?.title = "Login"

        // Set focus change listener for emailEditText
        emailEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(view)
            }
        }

        // Set focus change listener for passwordEditText
        passwordEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(view)
            }
        }
    }

    fun login(view: View) {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_LONG).show()

                        // Store user email using UserPrefs
                        UserPrefs.storeUserEmail(this, email)

                        val intent = Intent(this, UserHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_LONG).show()
        }
    }

    // Hide the keyboard programmatically
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    object UserPrefs {
        fun storeUserEmail(context: Context, email: String) {
            val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("UserEmail", email).apply()
        }

        fun getUserEmail(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("UserEmail", null)
        }
    }

    fun registerNow(view: View) {
        // Navigate to Registration Activity
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    fun onForgotPasswordClicked(view: View) {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }
}
