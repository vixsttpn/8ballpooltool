package com.eightball.pro.overlay

import android.content.Context
import android.graphics.*
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import com.eightball.pro.engine.*
import com.eightball.pro.model.*
import kotlin.math.*

class OverlayView(context: Context) : View(context) {

    val balls = mutableListOf<Ball>()
    var cueBall = Ball(0f, 0f, Ball.BALL_RADIUS, BallType.CUE, 0)
    var aimX = 0f
    var aimY = 0f
    var hasAim = false
    var tableBounds = RectF(60f, 140f, 1020f, 580f)
    var settings = AppSettings()
    var editMode = true
    var onBallCountChanged: ((Int) -> Unit)? = null

    private var shotResult: ShotResult? = null
    private var dragBall: Ball? = null
    private var dragOffX = 0f
    private var dragOffY = 0f
    private var dragCorner = -1
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var pulsePhase = 0f
    private var ballCounter = 1

    private val pulseRunnable = object : Runnable {
        override fun run() {
            pulsePhase = (pulsePhase + 0.05f) % (Math.PI.toFloat() * 2f)
            invalidate()
            postDelayed(this, 16)
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        cueBall = Ball(tableBounds.centerX(), tableBounds.centerY() + 100f, Ball.BALL_RADIUS, BallType.CUE, 0)
        aimX = tableBounds.centerX()
        aimY = tableBounds.top + 80f
        hasAim = true
        recompute()
        post(pulseRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(pulseRunnable)
    }

    fun addBall() {
        val number = (ballCounter % 15) + 1
        ballCounter++
        val x = tableBounds.left + tableBounds.width() * (0.25f + Math.random().toFloat() * 0.5f)
        val y = tableBounds.top + tableBounds.height() * (0.2f + Math.random().toFloat() * 0.6f)
        balls.add(Ball(x, y, Ball.BALL_RADIUS, BallColors.typeForNumber(number), number))
        onBallCountChanged?.invoke(balls.size)
        recompute()
        invalidate()
    }

    fun clearBalls() {
        balls.clear()
        ballCounter = 1
        onBallCountChanged?.invoke(0)
        recompute()
        invalidate()
    }

    fun resetCue() {
        cueBall.x = tableBounds.centerX()
        cueBall.y = tableBounds.centerY() + 100f
        recompute()
        invalidate()
    }

    fun recompute() {
        if (!hasAim) return
        shotResult = PhysicsEngine.compute(cueBall, aimX, aimY, tableBounds, balls, settings.bounceCount)
    }

    override fun onDraw(canvas: Canvas) {
        val t = settings.tableColorPreset
        val pulse = (sin(pulsePhase) * 0.5f + 0.5f)

        Renderer.drawTable(canvas, tableBounds, t)
        Renderer.drawPockets(canvas, tableBounds, t)
        shotResult?.let { Renderer.drawShotResult(canvas, it, settings, t) }

        if (hasAim) {
            val dx = aimX - cueBall.x
            val dy = aimY - cueBall.y
            val len = hypot(dx, dy)
            if (len > 2f) {
                val nx = dx / len
                val ny = dy / len
                canvas.drawLine(
                    cueBall.x + nx * cueBall.radius,
                    cueBall.y + ny * cueBall.radius,
                    cueBall.x + nx * (cueBall.radius + 80f),
                    cueBall.y + ny * (cueBall.radius + 80f),
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Renderer.themes[t].cueLine
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        alpha = 80
                        pathEffect = DashPathEffect(floatArrayOf(6f, 8f), 0f)
                    }
                )
            }
        }

        for (b in balls) {
            Renderer.drawBall(canvas, b, if (dragBall == b) pulse else 0f)
        }
        Renderer.drawCueBall(canvas, cueBall, pulse * 0.5f)

        if (editMode) {
            val c = Renderer.themes[t].border
            for (pt in tableCorners()) {
                Renderer.drawCornerHandle(canvas, pt.x, pt.y, c)
            }
        }

        shotResult?.pocketIndex?.let { idx ->
            PhysicsEngine.pockets(tableBounds).getOrNull(idx)?.let { p ->
                canvas.drawCircle(p.x, p.y, 28f + pulse * 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Renderer.themes[t].targetLine
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    alpha = (120 + 80 * pulse).toInt()
                    maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
                })
            }
        }

        if (balls.isNotEmpty()) {
            canvas.drawText("${balls.size} BALLS", tableBounds.left, tableBounds.top - 12f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Renderer.themes[t].cueLine
                    textSize = 18f
                    alpha = 160
                })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!editMode) return false
        val tx = event.x
        val ty = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val corners = tableCorners()
                for (i in corners.indices) {
                    val c = corners[i]
                    if (hypot(tx - c.x, ty - c.y) < 44f) {
                        dragCorner = i
                        vibrate(20)
                        return true
                    }
                }
                if (cueBall.contains(tx, ty, 24f)) {
                    dragBall = cueBall
                    dragOffX = tx - cueBall.x
                    dragOffY = ty - cueBall.y
                    vibrate(20)
                    return true
                }
                for (b in balls.reversed()) {
                    if (b.contains(tx, ty, 24f)) {
                        dragBall = b
                        dragOffX = tx - b.x
                        dragOffY = ty - b.y
                        vibrate(20)
                        return true
                    }
                }
                if (tableBounds.contains(tx, ty)) {
                    aimX = tx
                    aimY = ty
                    hasAim = true
                    recompute()
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragCorner >= 0) {
                    resizeCorner(dragCorner, tx, ty)
                    recompute()
                    invalidate()
                    return true
                }
                val db = dragBall
                if (db != null) {
                    db.x = (tx - dragOffX).coerceIn(tableBounds.left + db.radius, tableBounds.right - db.radius)
                    db.y = (ty - dragOffY).coerceIn(tableBounds.top + db.radius, tableBounds.bottom - db.radius)
                    recompute()
                    invalidate()
                    return true
                }
                if (hasAim) {
                    aimX = tx
                    aimY = ty
                    recompute()
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                dragBall = null
                dragCorner = -1
            }
        }
        return true
    }

    private fun vibrate(ms: Long) {
        if (settings.vibrateFeedback) {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun tableCorners(): List<PointF> = listOf(
        PointF(tableBounds.left, tableBounds.top),
        PointF(tableBounds.right, tableBounds.top),
        PointF(tableBounds.right, tableBounds.bottom),
        PointF(tableBounds.left, tableBounds.bottom)
    )

    private fun resizeCorner(i: Int, tx: Float, ty: Float) {
        val minW = 200f
        val minH = 100f
        when (i) {
            0 -> { tableBounds.left = tx.coerceAtMost(tableBounds.right - minW); tableBounds.top = ty.coerceAtMost(tableBounds.bottom - minH) }
            1 -> { tableBounds.right = tx.coerceAtLeast(tableBounds.left + minW); tableBounds.top = ty.coerceAtMost(tableBounds.bottom - minH) }
            2 -> { tableBounds.right = tx.coerceAtLeast(tableBounds.left + minW); tableBounds.bottom = ty.coerceAtLeast(tableBounds.top + minH) }
            3 -> { tableBounds.left = tx.coerceAtMost(tableBounds.right - minW); tableBounds.bottom = ty.coerceAtLeast(tableBounds.top + minH) }
        }
    }
}
