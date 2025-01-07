package com.example.supabasedemo.compose.views

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors

private const val CAMERA_PERMISSION_REQUEST_CODE = 101

@Composable
fun QRCodeScanner(
    onScanSuccess: (String, String, String) -> Unit,
    onScanError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scanner = BarcodeScanning.getClient()

    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!permissionGranted) {
        LaunchedEffect(context) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    } else {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = androidx.camera.core.Preview.Builder()
                        .build()
                        .apply {
                            surfaceProvider = previewView.surfaceProvider
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImageProxy(scanner, imageProxy, onScanSuccess, onScanError)
                    })

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QRCodeScanner", "Camera binding failed: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onScanSuccess: (String, String, String) -> Unit,
    onScanError: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { qrCode ->
                        try {
                            val qrData = JSONObject(qrCode)
                            val gameUuid = qrData.optString("game_uuid", "")
                            val deviceAddress = qrData.optString("device_address", "")
                            val devicePreamble = qrData.optString("device_preamble", "")

                            if (gameUuid.isNotEmpty() && deviceAddress.isNotEmpty() && devicePreamble.isNotEmpty()) {
                                onScanSuccess(gameUuid, deviceAddress, devicePreamble)
                            } else {
                                Log.e("QRCodeScanner", "QR code missing required fields")
                                onScanError()
                            }
                        } catch (e: JSONException) {
                            Log.e("QRCodeScanner", "Failed to parse QR code JSON: ${e.message}")
                            onScanError()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QRCodeScanner", "QR code scanning failed: ${it.message}")
                onScanError()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}