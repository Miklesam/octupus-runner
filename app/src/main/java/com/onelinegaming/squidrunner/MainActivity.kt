package com.onelinegaming.squidrunner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Window
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.FontCallback.getHandler
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var width = 0
    private var height = 0
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var timer: Timer? = null
    private var run = true
    lateinit var lastResult: Pose

    var currentXPos = 0

    private var cnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        val windowManager = windowManager
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        width = size.x
        height = size.y


        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (timer == null)
            timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post(Runnable {
                    if (run)
                        updatePlayerImage()
                })
            }
        }, 0, 20)

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time_counter.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {

            }
        }.start()

    }

    private fun updatePlayerImage() {
        increaseAttackCount()
        when (imCount) {
            0 -> {
                player_view.setImageResource(R.drawable.first_scaled)
            }
            1 -> {
                player_view.setImageResource(R.drawable.second_scaled)
            }
            2 -> {
                player_view.setImageResource(R.drawable.third_scaled)
            }
            3 -> {
                player_view.setImageResource(R.drawable.forth_scaled)
            }
            4 -> {
                player_view.setImageResource(R.drawable.fifth_scaled)
            }
            5 -> {
                player_view.setImageResource(R.drawable.sixth_scaled)
            }
            6 -> {
                player_view.setImageResource(R.drawable.seventh_scaled)
            }
            7 -> {
                player_view.setImageResource(R.drawable.eight_scaled)
            }
            else -> {

            }
        }
    }

    protected fun increaseAttackCount() {
        if (animateCount < ANIMATE_COUNT) {
            animateCount++
        } else {
            animateCount = 0
        }
        if (animateCount == ANIMATE_COUNT) {
            if (imCount < 7) {
                imCount++
            } else {
                imCount = 0
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

//            // Preview
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(view_finder.createSurfaceProvider())
//                }

            val imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(width, height))
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer(graphic_overlay, cameraSelector))
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private inner class ImageAnalyzer(
        val graphicOverlay: GraphicOverlay,
        val cameraSelector: CameraSelector
    ) : ImageAnalysis.Analyzer {

        private val poseDetector: PoseDetector

        init {
            poseDetector = initPoseRecognition()
        }

        private fun initPoseRecognition(): PoseDetector {
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()

            return PoseDetection.getClient(options)
        }

        override fun analyze(imageProxy: ImageProxy) {
            val isImageFlipped = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                graphicOverlay.setImageSourceInfo(
                    imageProxy.width, imageProxy.height, isImageFlipped
                )
            } else {
                graphicOverlay.setImageSourceInfo(
                    imageProxy.height, imageProxy.width, isImageFlipped
                )
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to an ML Kit Vision API
                cnt += 1
                if (true) {
                    Log.w("frameRateCounter", cnt.toString())
                    poseDetector.process(image)
                        .addOnSuccessListener { results ->
                            graphicOverlay.clear()
                            //check(results)
                            if (this@MainActivity::lastResult.isInitialized) {
                                checkDebounce(lastResult, results)
                            }
                            lastResult = results
                            graphicOverlay.add(
                                PoseGraphic(
                                    graphicOverlay,
                                    results
                                )
                            )
                        }
                        .addOnFailureListener { e ->
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }


            }
        }
    }

    fun checkDebounce(previous: Pose, current: Pose) {
        val leftKnee = checkForPoseLandMark(PoseLandmark.LEFT_KNEE, previous, current, true)
        val rightKnee = checkForPoseLandMark(PoseLandmark.RIGHT_KNEE, previous, current, true)
        val leftWrist = checkForPoseLandMark(PoseLandmark.LEFT_WRIST, previous, current)
        val rightWrist = checkForPoseLandMark(PoseLandmark.RIGHT_WRIST, previous, current)
        if (leftKnee || rightKnee || leftWrist || rightWrist) {
            current_state.text = "Moving"
        } else {
            run = false
            current_state.text = "Stay"
        }
    }


    private fun checkForPoseLandMark(
        posePoint: Int,
        previous: Pose,
        current: Pose,
        canRun: Boolean = false
    ): Boolean {
        val previousX =
            previous.getPoseLandmark(posePoint)?.position?.x ?: 0f
        val currentX =
            current.getPoseLandmark(posePoint)?.position?.x ?: 0f
        val previousY =
            previous.getPoseLandmark(posePoint)?.position?.y ?: 0f
        val currentY =
            current.getPoseLandmark(posePoint)?.position?.y ?: 0f

        val diffX = currentX - previousX
        val diffY = currentY - previousY
        return if (diffY > ALLOWED_DEBOUNCE) {
            if (canRun) {
                player_view.x += SPEED
                run = true
            }
            true
        } else if (diffX < ALLOWED_DEBOUNCE && diffX > -ALLOWED_DEBOUNCE) {
            false
        } else diffX > ALLOWED_DEBOUNCE || diffX < -ALLOWED_DEBOUNCE
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val ANIMATE_COUNT = 4
        const val ALLOWED_DEBOUNCE = 4f
        const val SPEED = 2f
    }

    protected var animateCount = 0
    protected var imCount = 0

}