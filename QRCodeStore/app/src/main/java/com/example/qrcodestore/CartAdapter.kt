package com.example.qrcodestore
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CartAdapter(context: Context, private val productList: MutableList<Product>) :
    ArrayAdapter<Product>(context, 0, productList) {

    private val db = FirebaseFirestore.getInstance()  // Reference to Firestore

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)

        Log.d("CartAdapter", "getView called for position $position")

        val product = getItem(position)
        val productName = view.findViewById<TextView>(R.id.txtproductname)
        val productPrice = view.findViewById<TextView>(R.id.txtproductamount)
        val qtyTextView = view.findViewById<TextView>(R.id.txtQty)
        val increaseQtyButton = view.findViewById<Button>(R.id.btnIncreaseQty)
        val decreaseQtyButton = view.findViewById<Button>(R.id.btnDecreaseQty)
        val removeButton = view.findViewById<Button>(R.id.removeButton)

        product?.let { nonNullableProduct ->
            Log.d("CartAdapter", "Product loaded: ${nonNullableProduct.name}")

            productName.text = nonNullableProduct.name
            productPrice.text = String.format(Locale.getDefault(),"$%.2f", nonNullableProduct.price)
            qtyTextView.text = nonNullableProduct.quantity.toString()

            increaseQtyButton.setOnClickListener {
                Log.d("CartAdapter", "Increase button clicked for ${nonNullableProduct.name}")
                updateQuantity(nonNullableProduct, nonNullableProduct.quantity + 1)
            }

            decreaseQtyButton.setOnClickListener {
                Log.d("CartAdapter", "Decrease button clicked for ${nonNullableProduct.name}")
                if (nonNullableProduct.quantity > 1) {
                    updateQuantity(nonNullableProduct, nonNullableProduct.quantity - 1)
                } else {
                    Toast.makeText(context, "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
                }
            }

            removeButton.setOnClickListener {
                Log.d("CartAdapter", "Remove button clicked for ${nonNullableProduct.name}")
                removeFromCart(nonNullableProduct, position)
            }
        }

        return view
    }


    private fun updateQuantity(product: Product, newQty: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userCartRef = db.collection("carts").document(user.uid).collection("items").document(product.id)
                .update("quantity", newQty)
                .addOnSuccessListener {
                    product.quantity = newQty
                    notifyDataSetChanged()  // Update the UI
                    Log.d("CartAdapter", "Quantity updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("CartAdapter", "Error updating quantity", e)
                }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromCart(product: Product?, position: Int) {
        product?.let {
            // Create an AlertDialog to confirm the removal
            AlertDialog.Builder(context)
                .setTitle("Confirm Removal")
                .setMessage("Are you sure you want to remove this product from your cart?")
                .setPositiveButton("Remove") { dialog, which ->
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()

                        // Remove the product from Firestore first
                        db.collection("carts").document(user.uid).collection("items").document(product.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("CartAdapter", "Product removed successfully")

                                // After successful removal from Firestore, remove it from the list and update the UI
                                productList.removeAt(position)
                                notifyDataSetChanged()  // Notify the adapter to update the list

                                // Update the total price
                                updateTotalAmount()

                                Toast.makeText(context, "Product removed successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("CartAdapter", "Error removing product", e)
                                Toast.makeText(context, "Error removing product: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    // Dismiss the dialog and do not delete the item
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun updateTotalAmount() {
        var grandTotal = 0.0
        for (product in productList) {
            grandTotal += product.price * product.quantity
        }

        // Assuming you have a TextView called totalamount to display the total price
        val totalAmountTextView = (context as? ProductCartListActivity)?.findViewById<TextView>(R.id.totalamount)
        totalAmountTextView?.text = String.format(Locale.getDefault(),"$%.2f", grandTotal)
    }

}
