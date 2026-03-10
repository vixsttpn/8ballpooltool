package com.eightball.pro.engine

import android.graphics.*
import com.eightball.pro.model.*
import kotlin.math.*

object Renderer {

    data class Theme(val cueLine: Int, val targetLine: Int, val ghost: Int, val border: Int, val pocket: Int, val bounce: Int)

    val themes = listOf(
        Theme(0xFF00FFCC.toInt(), 0xFFFFDD00.toInt(), 0x88FFFFFF.toInt(), 0x5500FFCC.toInt(), 0xFF001100.toInt(), 0xFFFF4488.toInt()),
        Theme(0xFF00FF66.toInt(), 0xFF88FFDD.toInt(), 0x88FFFFFF.toInt(), 0x5500FF66.toInt(), 0xFF001100.toInt(), 0xFF00CCFF.toInt()),
        Theme(0xFFFFDD00.toInt(), 0xFFFF6600.toInt(), 0x88FFFFFF.toInt(), 0x55FFDD00.toInt(), 0xFF110800.toInt(), 0xFF44DDFF.toInt()),
        Theme(0xFFFF44CC.toInt(), 0xFF44FFFF.toInt(), 0x88FFFFFF.toInt(), 0x55FF44CC.toInt(), 0xFF110011.toInt(), 0xFFFFFF44.toInt())
    )

    fun drawTable(canvas: Canvas, bounds: RectF, themeIdx: Int) {
        val t = themes[themeIdx]
        canvas.drawRect(bounds, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x08004400; style = Paint.Style.FILL })
        canvas.drawRect(bounds, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = t.border; style = Paint.Style.STROKE; strokeWidth = 3f
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawRect(bounds, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = t.border; style = Paint.Style.STROKE; strokeWidth = 1.5f })
        val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = t.border; style = Paint.Style.FILL }
        listOf(
            PointF(bounds.left + bounds.width() * 0.25f, bounds.top),
            PointF(bounds.left + bounds.width() * 0.75f, bounds.top),
            PointF(bounds.left + bounds.width() * 0.25f, bounds.bottom),
            PointF(bounds.left + bounds.width() * 0.75f, bounds.bottom),
            PointF(bounds.left, bounds.centerY()), PointF(bounds.right, bounds.centerY())
        ).forEach { canvas.drawCircle(it.x, it.y, 5f, dp) }
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), 4f, dp)
    }

    fun drawPockets(canvas: Canvas, bounds: RectF, themeIdx: Int) {
        val t = themes[themeIdx]
        for (p in PhysicsEngine.pockets(bounds)) {
            canvas.drawCircle(p.x, p.y, 30f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = t.border and 0x00FFFFFF or 0x44000000; style = Paint.Style.FILL
                maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
            })
            canvas.drawCircle(p.x, p.y, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = t.pocket; style = Paint.Style.FILL })
            canvas.drawCircle(p.x, p.y, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = t.border; style = Paint.Style.STROKE; strokeWidth = 2f })
        }
    }

    fun drawShotResult(canvas: Canvas, result: ShotResult, settings: AppSettings, themeIdx: Int) {
        val t = themes[themeIdx]
        drawPath(canvas, result.cuePath, t.cueLine, 3f, settings.neonGlow, false)
        result.targetPath?.let { drawPath(canvas, it, t.targetLine, 3f, settings.neonGlow, true) }
        if (settings.showGhostBall) result.ghostPos?.let { drawGhostBall(canvas, it.x, it.y, Ball.BALL_RADIUS, t.ghost, settings.neonGlow) }
        if (settings.showCutAngle && result.cutAngleDeg > 0f) {
            result.ghostPos?.let { g ->
                val label = when {
                    result.cutAngleDeg < 15f -> "STRAIGHT"
                    result.cutAngleDeg < 35f -> "THIN CUT"
                    result.cutAngleDeg < 55f -> "HALF BALL"
                    result.cutAngleDeg < 75f -> "THICK CUT"
                    else -> "FULL BALL"
                }
                canvas.drawText("${label} ${result.cutAngleDeg.toInt()}°", g.x + 35f, g.y - 35f,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = t.cueLine; textSize = 22f; alpha = 220
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    })
            }
        }
        result.pocketIndex?.let { idx ->
            val pk = PhysicsEngine.pockets(RectF()).getOrNull(idx)
        }
    }

    private fun drawPath(canvas: Canvas, path: List<PointF>, color: Int, lw: Float, glow: Boolean, dashed: Boolean) {
        if (path.size < 2) return
        for (i in 0 until path.size - 1) {
            val alpha = ((1f - i.toFloat() / (path.size + 1)) * 255).toInt().coerceIn(40, 255)
            val p1 = path[i]; val p2 = path[i + 1]
            if (glow) canvas.drawLine(p1.x, p1.y, p2.x, p2.y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; style = Paint.Style.STROKE; strokeWidth = lw + 12f
                strokeCap = Paint.Cap.ROUND; this.alpha = (alpha * 0.3f).toInt()
                maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
            })
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; style = Paint.Style.STROKE; strokeWidth = lw
                strokeCap = Paint.Cap.ROUND; this.alpha = alpha
                if (dashed && i > 0) pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
            })
            if (i > 0) canvas.drawCircle(p1.x, p1.y, 5f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; this.alpha = alpha; style = Paint.Style.FILL
                if (glow) maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            })
        }
        if (path.size >= 2) drawArrow(canvas, path[path.size - 2], path[path.size - 1], color)
    }

    private fun drawArrow(canvas: Canvas, from: PointF, to: PointF, color: Int) {
        val dx = to.x - from.x; val dy = to.y - from.y
        val len = hypot(dx, dy); if (len < 1f) return
        val nx = dx / len; val ny = dy / len; val size = 14f
        val p = Path().apply {
            moveTo(to.x, to.y)
            lineTo(to.x - nx * size - ny * size * 0.5f, to.y - ny * size + nx * size * 0.5f)
            lineTo(to.x - nx * size + ny * size * 0.5f, to.y - ny * size - nx * size * 0.5f)
            close()
        }
        canvas.drawPath(p, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL; alpha = 180 })
    }

    fun drawGhostBall(canvas: Canvas, x: Float, y: Float, r: Float, color: Int, glow: Boolean) {
        if (glow) canvas.drawCircle(x, y, r + 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawCircle(x, y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0x44FFFFFF; style = Paint.Style.FILL })
        canvas.drawCircle(x, y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = 0xCCFFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(8f, 5f), 0f)
        })
    }

    fun drawBall(canvas: Canvas, ball: Ball, pulse: Float = 0f) {
        val color = BallColors.forNumber(ball.number)
        val r = ball.radius
        canvas.drawCircle(ball.x + 3f, ball.y + 4f, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = 0x44000000; style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        })
        if (pulse > 0f) canvas.drawCircle(ball.x, ball.y, r + 14f * pulse, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color and 0x00FFFFFF or ((80 * pulse).toInt() shl 24)
            style = Paint.Style.FILL; maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL })
        if (ball.type == BallType.STRIPE) {
            canvas.save()
            canvas.clipRect(ball.x - r, ball.y - r * 0.45f, ball.x + r, ball.y + r * 0.45f)
            canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL })
            canvas.restore()
        }
        if (ball.number != 0) {
            val nr = r * 0.46f
            canvas.drawCircle(ball.x, ball.y, nr, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL })
            canvas.drawText(ball.number.toString(), ball.x, ball.y + nr * 0.38f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = if (ball.type == BallType.STRIPE) color else 0xFF111111.toInt()
                    textSize = nr * 1.3f; textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                })
        }
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0x99FFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(ball.x - r * 0.28f, ball.y - r * 0.32f, r * 0.55f, 0x99FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP)
        })
    }

    fun drawCueBall(canvas: Canvas, ball: Ball, pulse: Float = 0f) {
        val r = ball.radius
        canvas.drawCircle(ball.x + 3f, ball.y + 4f, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x44000000; style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawCircle(ball.x, ball.y, r + 8f + pulse * 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x4400FFCC; style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL })
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF00FFCC.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f })
        canvas.drawText("CUE", ball.x, ball.y + r * 0.3f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88888888.toInt(); textSize = r * 0.55f; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })
        canvas.drawCircle(ball.x, ball.y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(ball.x - r * 0.28f, ball.y - r * 0.32f, r * 0.55f, 0x99FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP)
        })
    }

    fun drawCornerHandle(canvas: Canvas, x: Float, y: Float, color: Int) {
        canvas.drawCircle(x, y, 20f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.FILL; alpha = 180
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        })
        canvas.drawCircle(x, y, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL; alpha = 220 })
        canvas.drawCircle(x, y, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0xFFFFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f; alpha = 120 })
    }
}
