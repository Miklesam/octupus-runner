package com.onelinegaming.squidrunner

import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.android.synthetic.main.fragment_game_one.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.MediaPlayer
import android.view.animation.DecelerateInterpolator


class GameOneFragment : Fragment(R.layout.fragment_game_one) {

    val gameOneViewModel: GameOneViewModel by viewModels()
    private var width = 0
    private var height = 0
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var run = true
    lateinit var lastResult: Pose
    protected var animateCount = 0
    protected var imCount = 0
    private var movingAllowed = true
    var mediaPlayer: MediaPlayer? = null
    private var plaing = false

    private var cnt = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val windowManager = requireActivity().windowManager
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        width = size.x
        height = size.y
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                StartActivity.REQUIRED_PERMISSIONS,
                StartActivity.REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        gameOneViewModel.canRun.observe(viewLifecycleOwner) {
            if (plaing) {
                if (it) {
                    movingAllowed = it
                    playMusic()
                    signal_to_run.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_run
                        )
                    )
                } else {
                    movingAllowed = false
                    stopMusic()
                    signal_to_run.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_stay
                        )
                    )
                }
            }
        }
        gameOneViewModel.timeLeft.observe(viewLifecycleOwner) {
            if (it <= 60) {
                if (it > 50)
                    time_counter.text = "00:0${60 - it}"
                else
                    time_counter.text = "00:${60 - it}"
            } else
                initLose("Time is over")
        }

        gameOneViewModel.gameStartLeft.observe(viewLifecycleOwner) {
            if (it == 4) {
                plaing = true
                game_start.visibility = View.GONE
            } else if (it in 1..3) {
                animateCounText(4 - it)
            }
        }


        gameOneViewModel.updateFrame.observe(viewLifecycleOwner) {
            if (run && plaing)
                updateRunningMan()
        }


    }

    private fun animateCounText(value: Int) {
        game_start.text = value.toString()
        game_start.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(500L).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun updateRunningMan() {
        increaseRunCount()
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
        }
    }

    protected fun increaseRunCount() {
        if (animateCount < StartActivity.ANIMATE_COUNT) {
            animateCount++
        } else {
            animateCount = 0
        }
        if (animateCount == StartActivity.ANIMATE_COUNT) {
            if (imCount < 3) {
                imCount++
            } else {
                imCount = 0
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

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
                //Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
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
                            if (this@GameOneFragment::lastResult.isInitialized && plaing) {
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
            if (!movingAllowed)
                initLose("Motion Detected")
        } else {
            run = false
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        mediaPlayer = null
        gameOneViewModel.disposables.dispose()
        super.onDestroy()
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
        return if (diffY > StartActivity.ALLOWED_DEBOUNCE) {
            if (canRun) {
                player_view?.let {
                    if (player_view.x + player_view.width / 2 > finish_line.x)
                        initWin()
                    it.x += StartActivity.SPEED
                }
                run = true
            }
            true
        } else if (diffX < StartActivity.ALLOWED_DEBOUNCE && diffX > -StartActivity.ALLOWED_DEBOUNCE) {
            false
        } else diffX > StartActivity.ALLOWED_DEBOUNCE || diffX < -StartActivity.ALLOWED_DEBOUNCE
    }

    private fun playMusic() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.squid_song);
        mediaPlayer?.start()
    }

    private fun stopMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null;
        }
    }

    private fun allPermissionsGranted() = StartActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initLose(loseMessage: String) {
        motion_detected.text = loseMessage
        animateToLose()
    }

    private fun initWin() {
        animateToWin()
    }

    private fun closeGame(win: Boolean) {
        stopMusic()
        if (win)
            findNavController().navigate(R.id.action_gameOneFragment_to_fragmentWin)
        else
            findNavController().navigate(R.id.action_gameOneFragment_to_fragmentLose)

    }

    private fun animateToLose() {
        gameOneViewModel.disposables.dispose()
        cameraExecutor.shutdown()
        aim_shot.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            y = this@GameOneFragment.height.toFloat()
            animate().alpha(1f).setDuration(1500L).setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    closeGame(false)
                }
                .start()
            animate().y((this@GameOneFragment.height / 3).toFloat()).setDuration(1500L)
                .setInterpolator(DecelerateInterpolator())
        }
        motion_detected.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(1500L).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun animateToWin() {
        gameOneViewModel.disposables.dispose()
        cameraExecutor.shutdown()
        pass_image.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            y = this@GameOneFragment.height.toFloat()
            animate().alpha(1f).setDuration(1500L).setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    closeGame(true)
                }
                .start()
            animate().y((this@GameOneFragment.height / 3).toFloat()).setDuration(1500L)
                .setInterpolator(DecelerateInterpolator())
        }
        pass_text.apply {
            this.visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(1500L).setInterpolator(DecelerateInterpolator()).start()
        }
    }


}