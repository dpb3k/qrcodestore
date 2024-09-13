# QR Code Store

### Project Overview
This project is a **QR Code-Based Customer Billing System** designed for supermarkets, where users can:

- Scan QR codes to add products to their cart
- View the contents of their cart
- Proceed to purchase products

The system is built with **Firebase Firestore** as the database backend, used to store product information and user-specific cart data. The app also integrates **Firebase Authentication** for secure user registration and login, making the shopping experience user-friendly and secure.

**Note:** No real purchases or currency transactions are involved through this app.

---

### Features
- **User Registration & Login:** Secure authentication using email and password.
- **QR Code Product Scanning:** Add products to the cart by scanning QR codes.
- **Cart Management:** View and manage the shopping cart, including the ability to:
  - Update product quantities
  - Clear the cart
  - Proceed to checkout (simulated purchase)
- **Firebase Integration:** Real-time database management with Firestore for products and cart data storage.

---

### Installation & Usage

1. **Download the App:**
   - Download the `QRCodeStore.apk` and install it on your Android device.
   - You may need to search how to install from unknown sources if needed.

2. **Register & Login:**
   - Open the app and click the **Register** button to create a new account using your email and password.
   - Log in with your credentials.

3. **Scan QR Codes:**
   - After logging in, go to the GitHub repository and navigate to the **QRCodeGenerator > CODES** directory.
   - Use the **Scan Products** option in the app to scan the QR codes from the CODES directory. Successful scans will display the product details in the app.

4. **Manage Your Cart:**
   - Enter the quantity for each product.
   - Click **Show Cart** in the upper right corner to view the cart and the total price.
   - From the cart, you can:
     - **Buy Now:** Simulate a purchase (no real currency involved).
     - **Add More Products:** Scan more products to add to your cart.
     - **Clear Cart:** Remove all items from your cart.

---

Enjoy a seamless, secure, and fun shopping experience with QR Code Store!
