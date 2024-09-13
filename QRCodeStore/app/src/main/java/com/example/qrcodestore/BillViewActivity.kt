package com.example.qrcodestore

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BillViewActivity : AppCompatActivity() {

    private lateinit var billNoTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var billDateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_view)

        billNoTextView = findViewById(R.id.billno)
        totalTextView = findViewById(R.id.total)
        billDateTextView = findViewById(R.id.billdate)

        loadBillDetails()
    }

    private fun loadBillDetails() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Fetch the most recent purchase
            FirebaseFirestore.getInstance().collection("carts").document(user.uid)
                .collection("bills")
                .orderBy("purchasedOn", com.google.firebase.firestore.Query.Direction.DESCENDING) // To get the latest bill
                .limit(1) // Only fetch the most recent one
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.first()

                        // Using document ID as the bill number
                        val billNo = document.id
                        val totalAmount = document.getDouble("totalAmount")
                        val purchaseDate = document.getDate("purchasedOn")

                        // Format the purchase date to a more readable format
                        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                        val formattedDate = purchaseDate?.let { dateFormat.format(it) } ?: ""

                        // Set the values in the TextViews
                        billNoTextView.text = billNo
                        totalTextView.text = String.format(Locale.getDefault(), "%.2f", totalAmount ?: 0.0)
                        billDateTextView.text = formattedDate
                    } else {
                        Toast.makeText(this, "No recent bills found.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading bill: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show()
        }
    }

    fun exitButtonClicked(view: View) {
        startActivity(Intent(this, UserHomeActivity::class.java))
    }
}
