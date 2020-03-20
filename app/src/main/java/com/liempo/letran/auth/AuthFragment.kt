package com.liempo.letran.auth

import android.graphics.ImageFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.media.Image
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

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

        // Setup the camera object
        binding.camera.setLifecycleOwner(this)
        binding.camera.addFrameProcessor { frame ->
            // First, identify if frame data
            // is from camera1 or camera2 api
            if (frame.dataClass == Image::class.java) {
                Timber.v("CameraView is using camera2")
                // TODO Parse media.Image (it.getData())
            } else if (frame.dataClass == ByteArray::class.java) {
                // Create frame metadata
                val metadata = FirebaseVisionImageMetadata.Builder()
                    .setWidth(frame.size.width)
                    .setHeight(frame.size.height)
                    .setRotation(frame.rotationToUser)
                    .setFormat(ImageFormat.NV21)
                    .build()

                // Create firebase compatible object
                val image = FirebaseVisionImage.fromByteArray(
                    frame.getData(), metadata)

                // Try to detect the current frame
                detector.detectInImage(image)
                    .addOnSuccessListener { results ->
                        Timber.i("ResultSize = ${results.size}")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e,"Failed detecting")
                    }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
