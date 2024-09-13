package com.example.qrcodestore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var progressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.splashProgressBar)

        // Handler to update the progress bar every 100ms
        val handler = Handler(Looper.getMainLooper())

        // Create a thread to simulate the loading process
        Thread {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                }
                Thread.sleep(30)  // Simulate loading delay (e.g., 30ms per increment)
            }

            // After loading is done, start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.start()
    }
}
