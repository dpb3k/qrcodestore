package com.example.qrcodestore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ProductViewActivity : BaseActivity() {
    private lateinit var productName: TextView
    private lateinit var manufactureDate: TextView
    private lateinit var expiryDate: TextView
    private lateinit var price: TextView
    private lateinit var manufacturer: TextView
    private lateinit var description: TextView
    private lateinit var quantityEditText: EditText
    private lateinit var cartButton: Button
    private var productId: String? = null
    private var availableQuantity = 0
    companion object {
        private val TAG = ProductViewActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)
        setupUI(findViewById(R.id.mainLayout))

        productName = findViewById(R.id.pname)
        manufactureDate = findViewById(R.id.mandate)
        expiryDate = findViewById(R.id.expdate)
        price = findViewById(R.id.price)
        manufacturer = findViewById(R.id.manuf)
        manufacturer = findViewById(R.id.manuf)
        description = findViewById(R.id.desc)
        quantityEditText = findViewById(R.id.cartitem)
        cartButton = findViewById(R.id.cartbtn)
        supportActionBar?.title = "Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // enable back button
        cartButton.setOnClickListener {
            Log.d("AddToCart", "Add to Cart button clicked")

            addToCart()
        }

        // Retrieve the QR data from the intent
        val qrData = intent.getStringExtra("qrData") ?: ""
        Log.d(TAG, "Received QR data: $qrData") // Log the QR data

        if (qrData.isEmpty()) {
            Log.e(TAG, "QR data is empty")
            Toast.makeText(this, "Invalid QR data", Toast.LENGTH_SHORT).show()
            finish() // Close this activity if the data is invalid
            return
        }

        getProductDetails(qrData)
    }

    private fun getProductDetails(qrData: String) {
        FirebaseFirestore.getInstance().collection("products").document(qrData)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    productName.text = document.getString("name") ?: "No name"
                    val manufacturingTimestamp = document.getTimestamp("manufacturingDate")
                    val expiryTimestamp = document.getTimestamp("expiryDate")

                    val manufacturingDate = manufacturingTimestamp?.toDate()
                    val expiryDateDate = expiryTimestamp?.toDate()
                    manufactureDate.text = formatDate(manufacturingDate) ?: "No manufacturing date"
                    expiryDate.text = formatDate(expiryDateDate) ?: "No expiry date"

                    price.text = document.getDouble("price")?.toString() ?: "No price"
                    manufacturer.text = document.getString("manufacturer") ?: "No manufacturer"
                    description.text = document.getString("description") ?: "No description"
                    productId = document.id
                    availableQuantity = document.getLong("quantity")?.toInt() ?: 0
                } else {
                    Toast.makeText(this, "No such product found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting product details: ${exception.message}")
                Toast.makeText(this, "Error getting product details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToCart() {
        val quantityString = quantityEditText.text.toString()
        if (quantityString.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show()
            return
        }
        val quantity = quantityString.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            quantityEditText.error = "Please enter a valid quantity"
            return
        }
        if (quantity > availableQuantity) {
            quantityEditText.error = "Not enough items in stock"
            return
        }

        cartButton.isEnabled = false  // Disable the button

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show()
            return
        }

        val userCartRef = FirebaseFirestore.getInstance().collection("carts").document(user.uid)
        val productRef = FirebaseFirestore.getInstance().collection("products").document(productId ?: "")

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            // Read product data
            val productSnapshot = transaction.get(productRef)
            val currentStock = productSnapshot.getLong("quantity") ?: 0
            Log.d("AddToCart", "Current stock for product: $currentStock")

            if (currentStock < quantity) {
                Log.e("AddToCart", "Not enough stock for productId: $productId")
                throw FirebaseFirestoreException("Not enough stock", FirebaseFirestoreException.Code.ABORTED)
            }

            // Read cart item data (if it exists)
            val cartItemSnapshot = transaction.get(userCartRef.collection("items").document(productId!!))

            // Update product's stock quantity
            transaction.update(productRef, "quantity", currentStock - quantity)
            Log.d("AddToCart", "Updated product stock for productId: $productId. New stock: ${currentStock - quantity}")

            // Handle cart item updates
            if (cartItemSnapshot.exists()) {
                val existingQuantity = cartItemSnapshot.getLong("quantity") ?: 0
                val updatedQuantity = existingQuantity + quantity
                transaction.update(userCartRef.collection("items").document(productId!!), "quantity", updatedQuantity)
                Log.d("AddToCart", "Updated cart item quantity for productId: $productId. New quantity: $updatedQuantity")
            } else {
                transaction.set(
                    userCartRef.collection("items").document(productId!!),
                    mapOf(
                        "productName" to productName.text.toString(),
                        "price" to price.text.toString().toDouble(),
                        "quantity" to quantity  // Directly set the quantity
                    )
                )
                Log.d("AddToCart", "Added new cart item for productId: $productId with quantity: $quantity")
            }

            // Return null because this is a Void transaction
            null
        }.addOnSuccessListener {
            Log.d("AddToCart", "Transaction successful")
            Toast.makeText(this, "Added to cart successfully!", Toast.LENGTH_LONG).show()
            quantityEditText.text.clear()  // Clear the quantity input box
        }.addOnFailureListener { e ->
            Log.e("AddToCart", "Transaction failed: ${e.message}")
            Toast.makeText(this, "Failed to add to cart: ${e.message}", Toast.LENGTH_LONG).show()
        }.addOnCompleteListener {
            cartButton.isEnabled = true  // Re-enable the button
        }
    }

    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, _ ->
                hideSoftKeyboard()
                v.performClick() // Ensure that this is considered as an accessibility click
                false
            }
        }

        // If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    // Helper function to format the date
    private fun formatDate(date: Date?): String? {
        return if (date != null) {
            val sdf = SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()) // E.g., "January 01 2024"
            sdf.format(date)
        } else {
            null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}



