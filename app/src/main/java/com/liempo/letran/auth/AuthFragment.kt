package com.liempo.letran.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector

import com.liempo.letran.databinding.FragmentAuthBinding

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
                // TODO Start CameraX
            }
        } else requestPermissions(arrayOf(
            Manifest.permission.CAMERA), RC_CAMERA)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if camera permissions was granted
        if (requestCode == RC_CAMERA && grantResults.all {
                it == PackageManager.PERMISSION_GRANTED }) {
            binding.preview.post {
                // TODO Start CameraX
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RC_CAMERA = 420
    }
}
