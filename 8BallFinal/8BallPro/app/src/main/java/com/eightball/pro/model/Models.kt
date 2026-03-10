package com.eightball.pro.model

import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.sqrt

enum class BallType { CUE, SOLID, STRIPE, EIGHT }

data class Ball(
    var x: Float,
    var y: Float,
    val radius: Float = BALL_RADIUS,
    val type: BallType = BallType.SOLID,
    val number: Int = 1,
    var active: Boolean = true
) {
    fun contains(px: Float, py: Float, extra: Float = 0f) =
        sqrt((px - x) * (px - x) + (py - y) * (py - y)) <= radius + extra

    companion object {
        const val BALL_RADIUS = 26f
    }
}

object BallColors {
    private val solid = intArrayOf(
        0xFFFFFFFF.toInt(), 0xFFFFCC00.toInt(), 0xFF1155FF.toInt(),
        0xFFCC0000.toInt(), 0xFF7700BB.toInt(), 0xFFFF6600.toInt(),
        0xFF005500.toInt(), 0xFF881100.toInt(), 0xFF111111.toInt()
    )
    private val stripe = intArrayOf(
        0xFFFFCC00.toInt(), 0xFF1155FF.toInt(), 0xFFCC0000.toInt(),
        0xFF7700BB.toInt(), 0xFFFF6600.toInt(), 0xFF005500.toInt(), 0xFF881100.toInt()
    )

    fun forNumber(n: Int): Int = when {
        n == 0 -> solid[0]
        n in 1..8 -> solid[n]
        n in 9..15 -> stripe[n - 9]
        else -> solid[1]
    }

    fun typeForNumber(n: Int): BallType = when (n) {
        0 -> BallType.CUE
        8 -> BallType.EIGHT
        in 1..7 -> BallType.SOLID
        else -> BallType.STRIPE
    }
}

data class ShotResult(
    val cuePath: List<PointF>,
    val targetPath: List<PointF>?,
    val ghostPos: PointF?,
    val cutAngleDeg: Float = 0f,
    val pocketIndex: Int? = null
)

data class AppSettings(
    var bounceCount: Int = 5,
    var showGhostBall: Boolean = true,
    var showCutAngle: Boolean = true,
    var neonGlow: Boolean = true,
    var vibrateFeedback: Boolean = true,
    var tableColorPreset: Int = 0
)
