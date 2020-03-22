package com.liempo.letran.auth

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.util.concurrent.atomic.AtomicBoolean
class BarcodeAnalyzer: ImageAnalysis.Analyzer {

    // For thread concurrency
    private val isProcessing = AtomicBoolean(false)

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().visionBarcodeDetector
    }

    private var onBarcodeDetectedListener:
            ((FirebaseVisionBarcode) -> Unit)? = null
    private var onFailureListener: (() -> Unit)? = null

    internal fun setOnBarcodeDetectedListener(
        unit: ((FirebaseVisionBarcode) -> Unit)) {
        onBarcodeDetectedListener = unit
    }

    internal fun setOnFailureListener(
        unit: (() -> Unit)) {
        onFailureListener = unit
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    override fun analyze(imageProxy: ImageProxy?, rotationDegrees: Int) {
        // Skip function if still processing
        if (isProcessing.get())
            return
        isProcessing.set(true)

        // Get media.Image object, return if null
        val mediaImage = imageProxy?.image ?: return
        // Convert degrees to firebase readable
        val imageRotation = degreesToFirebaseRotation(rotationDegrees)

        // Create firebase image readable object
        val image = FirebaseVisionImage
            .fromMediaImage(mediaImage, imageRotation)

        // Start firebase detection
        detector.detectInImage(image)
            .addOnSuccessListener { results ->
                isProcessing.set(false)
                results.forEach {
                    onBarcodeDetectedListener?.invoke(it)
                }
            }
            .addOnFailureListener {
                isProcessing.set(false)
                onFailureListener?.invoke()
            }
    }
}

