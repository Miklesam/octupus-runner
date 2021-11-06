package com.onelinegaming.squidrunner

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

object MotionDetector {

    fun checkDebounce(previous: Pose, current: Pose) {
        val leftKnee = checkForPoseLandMark(PoseLandmark.LEFT_KNEE, previous, current)
        val rightKnee = checkForPoseLandMark(PoseLandmark.RIGHT_KNEE, previous, current)
        val leftWrist = checkForPoseLandMark(PoseLandmark.LEFT_WRIST, previous, current)
        val rightWrist = checkForPoseLandMark(PoseLandmark.RIGHT_WRIST, previous, current)
        if (leftKnee || rightKnee || leftWrist || rightWrist) {
            initLose()
        }
    }

    private fun checkForPoseLandMark(
        posePoint: Int,
        previous: Pose,
        current: Pose,
    ): Boolean {
        val previousX =
            previous.getPoseLandmark(posePoint)?.position?.x ?: 0f
        val currentX =
            current.getPoseLandmark(posePoint)?.position?.x ?: 0f

        val diffX = currentX - previousX
        return if (diffX < StartActivity.ALLOWED_DEBOUNCE && diffX > -StartActivity.ALLOWED_DEBOUNCE) {
            false
        } else diffX > StartActivity.ALLOWED_DEBOUNCE || diffX < -StartActivity.ALLOWED_DEBOUNCE
    }

    private fun initLose() = "You Lose"
}