package com.example.qrcodestore
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.qrcodestore.LoginActivity.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.Locale
import androidx.appcompat.app.AlertDialog


class ProductCartListActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CartAdapter
    private lateinit var clearCartButton: Button
    private lateinit var buyNowButton: Button  // Declare the button here
    private var grandTotal = 0.0
    private var products = mutableListOf<Product>()
    private val context = this
    private val db = FirebaseFirestore.getInstance()  // Reference to Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_cart_list)
        listView = findViewById(R.id.listView1)
        buyNowButton = findViewById<Button>(R.id.buyNowButton)
        clearCartButton = findViewById(R.id.clearCartButton)
        adapter = CartAdapter(this, products)
        listView.adapter = adapter
        supportActionBar?.title = "Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // enable back button

        loadCartItems()

        clearCartButton.setOnClickListener {
            clearCart()
            Toast.makeText(this, "Cart cleared successfully.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadCartItems() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("carts").document(user.uid).collection("items")
                .get()
                .addOnSuccessListener { documents ->
                    products.clear()  // Clear previous data
                    grandTotal = 0.0
                    for (document in documents) {
                        val productId = document.id
                        val productName = document.getString("productName") ?: "Unknown"
                        val price = document.getDouble("price") ?: 0.0
                        val quantity = document.getLong("quantity")?.toInt() ?: 0

                        val product = Product(
                            id = productId,
                            name = productName,  // Manually map productName to name
                            price = price,
                            quantity = quantity
                        )

                        Log.d("ProductCartListActivity", "Product fetched: ${product.name}")
                        products.add(product)
                        grandTotal += product.price * product.quantity
                    }
                    if (products.isEmpty()) {
                        Toast.makeText(context, "No items in the cart", Toast.LENGTH_LONG).show()
                    }

                    // Update total
                    val totalAmountTextView = findViewById<TextView>(R.id.totalamount)
                    totalAmountTextView.text = String.format(Locale.getDefault(), "Total: $%.2f", grandTotal)
                    adapter.notifyDataSetChanged()  // Notify adapter about data changes
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading cart: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ProductCartListActivity", "Error fetching cart items: ${e.message}")
                }
        } else {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_LONG).show()
            Log.e("ProductCartListActivity", "User is not authenticated")
        }
    }



    fun addMoreProducts(view: View) {
        // This could be used to navigate back to a product selection or home screen where the user can add more items
        val intent = Intent(applicationContext, UserHomeActivity::class.java)
        startActivity(intent)
    }

    fun buyNow(view: View) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && grandTotal > 0) {
            // Disable the button to prevent multiple clicks
            buyNowButton.isEnabled = false

            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Are you sure you want to buy these items for a total of $${String.format(Locale.getDefault(), "%.2f", grandTotal)}?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Proceed with purchase
                    val cartRef = FirebaseFirestore.getInstance().collection("carts").document(user.uid)
                    val purchaseInfo = mapOf(
                        "totalAmount" to grandTotal,
                        "items" to products, // Ensure productList is formatted to be Firestore compatible
                        "purchasedOn" to FieldValue.serverTimestamp(),
                        "status" to "Pending"
                    )

                    // Create a new purchase document
                    cartRef.collection("bills").add(purchaseInfo)
                        .addOnSuccessListener { purchaseDocument ->
                            // Purchase completed, now update the status to "Complete"
                            cartRef.collection("bills").document(purchaseDocument.id)
                                .update("status", "Complete")
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Purchase completed successfully!", Toast.LENGTH_LONG).show()
                                    clearCart()  // Clear the cart after successful purchase
                                    Toast.makeText(this, "Cart cleared after purchase.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, BillViewActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_LONG).show()
                                    buyNowButton.isEnabled = true // Re-enable button if status update fails
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Purchase failed: ${e.message}", Toast.LENGTH_LONG).show()
                            buyNowButton.isEnabled = true // Re-enable button if purchase fails
                        }
                }
                .setNegativeButton("No") { dialog, which ->
                    // Re-enable the button if the user cancels the purchase
                    buyNowButton.isEnabled = true
                }
                .show()
        } else {
            Toast.makeText(this, "No items in the cart or not logged in", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearCart() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("carts").document(user.uid).collection("items")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                    }
                    // After clearing the cart, reload the cart items
                    loadCartItems()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to clear cart: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    var quantity: Int = 0  // Assuming Firestore uses "quantity"
)

