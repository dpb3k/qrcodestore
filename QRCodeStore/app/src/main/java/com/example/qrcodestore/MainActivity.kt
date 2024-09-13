package com.example.qrcodestore

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity :  AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)  // Ensure this matches the name of your XML layout file

        // Check if the user is already logged in
        checkLogin()

        // Handling the back press with a custom callback
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Intent to go back to MainActivity with flags to clear the top of the stack
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish() // Ensure the current activity is finished
            }
        }
        // Register the callback with the OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun login(v: View?) {
        val i = Intent(applicationContext, LoginActivity::class.java)
        startActivity(i)
    }

    private fun checkLogin() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_LONG).show()
            // Navigate to the UserHome activity
            val intent = Intent(this, UserHomeActivity::class.java)
            startActivity(intent)
            finish() // Close the login activity
        } else {
            // No user is signed in
            Toast.makeText(this, "Please log in", Toast.LENGTH_LONG).show()
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Ensure MainActivity is not accessible without logging in
        }
    }

}
