package com.liempo.letran.verify

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector

import com.liempo.letran.databinding.FragmentAuthBinding
import com.liempo.letran.R

class AuthFragment : Fragment() {

    // View binding attributes
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    // Firebase object for barcode detection
    private lateinit var detector: FirebaseVisionBarcodeDetector

    // Firebase auth providers
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build())

    // Barcode detected will be saved here
    private lateinit var barcode: String

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH && resultCode == RESULT_OK) {
            // Create data to be uploaded
            val entry = hashMapOf("student_number" to barcode)

            // Update the database with uid as document id
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                Firebase.firestore.collection("profile")
                    .document(uid).set(entry).addOnSuccessListener {
                        Toast.makeText(context,
                            "Updated database",
                            Toast.LENGTH_LONG).show()
                    }
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
                // Disregard if verification failed
                if (!VerificationUtils.verifyAll(it.rawValue))
                    return@setOnBarcodeDetectedListener

                // I added !! because verifyAll will return false if it.rawValue is null
                // So using it.rawValue at this point is null-safe and will not crash
                barcode = it.rawValue!!
                binding.check.check(); stop()

                // Check database if barcode exists
                Firebase.firestore.collection("profile")
                    .whereEqualTo("student_number", barcode)
                    .get().addOnSuccessListener {  query ->
                        if (query.isEmpty)
                            // Launch sign-in intent
                            startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.drawable.banner)
                                .setTheme(R.style.AppTheme)
                                .build(), RC_AUTH)
                        else findNavController()
                            .navigate(R.id.action_auth_to_home)
                    }


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
        private const val RC_AUTH = 69
    }
}
