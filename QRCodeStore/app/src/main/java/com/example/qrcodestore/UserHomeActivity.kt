package com.example.qrcodestore
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.appcompat.app.ActionBar

class UserHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        supportActionBar?.title = "QR-Code Store - Home"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.user_home, menu)
        return true
    }

    fun scanQr(view: View) {
        // Start the scanning activity
        try {
            val intent = Intent(this, ScanBarcodeActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open scanner: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun viewCartDetails(view: View) {
        try {
            val intent = Intent(this, ProductCartListActivity::class.java)
            startActivity(intent)
        } catch (e : Exception) {
            Toast.makeText(this, "Failed to open cart details: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun logout(view: View) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Optionally clear any other stored data

        // Redirect back to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
