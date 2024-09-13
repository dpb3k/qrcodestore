package com.example.qrcodestore
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.widget.Toast
import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView
import androidx.camera.core.ExperimentalGetImage
import android.Manifest
import android.view.MenuItem
import androidx.annotation.OptIn

@OptIn(ExperimentalGetImage::class)
class ScanBarcodeActivity : BaseActivity() {
    companion object {
        private val TAG = ScanBarcodeActivity::class.java.simpleName
        private const val REQUEST_CAMERA_PERMISSION = 101

    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = findViewById(R.id.viewFinder)

        // Check camera permissions before starting the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        else {
            startCamera()  // CameraX
        }

        supportActionBar?.title = "QR Scanner"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // enable back button

    }

    // Implement onRequestPermissionsResult to handle the permission request response
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start the camera
                startCamera()
            } else {
                // Permission was denied, handle the failure
                Toast.makeText(this, "Camera permission is required to use the scanner", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy -> analyzeImage(imageProxy) }
                }

            try {
                cameraProvider.unbindAll() // Unbind use cases before rebinding
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first()  // Assuming you are interested in the first barcode only
                        val intent = Intent(this, ProductViewActivity::class.java)
                        intent.putExtra("qrData", barcode.rawValue)
                        Log.e(TAG, "RAW VALUE IS ${barcode.rawValue}")
                        startActivity(intent)
                        finish()  // Close the scanner activity
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ScanBarcodeActivity", "Barcode scanning failed", exception)
                    Toast.makeText(this, "Scanning failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    imageProxy.close()  // Close the current camera frame
                    it.result?.let {  // Check if any barcode was processed
                        if (it.isNotEmpty()) {
                            // Stop the camera if we processed at least one barcode
                            cameraExecutor.shutdownNow()
                        }
                    }
                }
        } else {
            imageProxy.close()
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
