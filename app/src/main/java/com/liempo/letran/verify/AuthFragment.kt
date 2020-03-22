package com.liempo.letran.verify

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector

import com.liempo.letran.databinding.FragmentAuthBinding
import timber.log.Timber

class AuthFragment : Fragment() {

    // View binding attributes
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    // Firebase object for barcode detection
    private lateinit var detector: FirebaseVisionBarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize barcode detector
        detector = FirebaseVision.getInstance()
            .visionBarcodeDetector
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(
            inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check camera permissions
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            binding.preview.post {
                startCameraX()
            }
        } else requestPermissions(arrayOf(
            Manifest.permission.CAMERA), RC_CAMERA)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode,
            permissions, grantResults)

        // Check if camera permissions was granted
        if (requestCode == RC_CAMERA && grantResults.all {
                it == PackageManager.PERMISSION_GRANTED }) {
            binding.preview.post {
                startCameraX()
            }
        }
    }

    private fun startCameraX() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = binding.preview.parent as ViewGroup
            parent.removeView(binding.preview)
            parent.addView(binding.preview, 0)

            binding.preview.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the analysis use case
        val analysisConfig = ImageAnalysisConfig.Builder()
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()
        val analyzer = BarcodeAnalyzer().apply {
            setOnBarcodeDetectedListener {
                Timber.d("BarcodeValue: ${it.rawValue}")
                binding.check.check(); stop()
            }
        }
        val analysis = ImageAnalysis(analysisConfig).apply {
            setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
        }

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview, analysis)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = binding.preview.width / 2f
        val centerY = binding.preview.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(binding.preview.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(
            (-rotationDegrees).toFloat(),
            centerX, centerY)

        // Finally, apply transformations to our TextureView
        binding.preview.setTransform(matrix)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RC_CAMERA = 420
    }
}
