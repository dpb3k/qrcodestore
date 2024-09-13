import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import qrcode
from PIL import Image

# Use the service account
cred = credentials.Certificate('qrcodestore-f155c-firebase-adminsdk-kugjx-daadd79473.json')
firebase_admin.initialize_app(cred)

db = firestore.client()


def generate_qr_code(data, filename):
    # Generate QR code
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(data)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    # Save the image to a file
    img.save(filename)
    print(f"QR code saved as {filename}")


def generate_qr_for_all_products():
    products_ref = db.collection('products')
    docs = products_ref.stream()  # Fetch all documents from the products collection

    for doc in docs:
        product_id = doc.id
        product_data = doc.to_dict()
        print(f"Product Data for {product_id}: {product_data}")

        # Generate QR code and save with the format "product_id_qr_code.png"
        filename = f"{product_id}_qr_code.png"
        generate_qr_code(product_id, filename)


# Example usage
generate_qr_for_all_products()
