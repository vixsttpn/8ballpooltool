package com.eightball.pro.engine

import android.graphics.PointF
import android.graphics.RectF
import com.eightball.pro.model.Ball
import com.eightball.pro.model.ShotResult
import kotlin.math.*

object PhysicsEngine {

    private const val POCKET_RADIUS = 34f
    private const val ENERGY = 0.92f
    private const val MIN_SPEED = 0.08f

    fun pockets(b: RectF): List<PointF> = listOf(
        PointF(b.left + 14f, b.top + 14f),
        PointF(b.centerX(), b.top - 4f),
        PointF(b.right - 14f, b.top + 14f),
        PointF(b.right - 14f, b.bottom - 14f),
        PointF(b.centerX(), b.bottom + 4f),
        PointF(b.left + 14f, b.bottom - 14f)
    )

    fun compute(
        cue: Ball, aimX: Float, aimY: Float,
        table: RectF, targets: List<Ball>, bounces: Int
    ): ShotResult {
        val r = cue.radius
        val inner = RectF(table.left + r, table.top + r, table.right - r, table.bottom - r)
        val dx = aimX - cue.x; val dy = aimY - cue.y
        val len = hypot(dx, dy)
        if (len < 2f) return ShotResult(listOf(PointF(cue.x, cue.y)), null, null)
        val vx = dx / len; val vy = dy / len

        val (hitBall, hitT) = firstCollision(cue.x, cue.y, vx, vy, r, targets.filter { it.active })

        return if (hitBall != null && hitT != null && hitT > r * 0.6f) {
            val gx = cue.x + vx * hitT; val gy = cue.y + vy * hitT
            val nx = (hitBall.x - gx) / (r * 2f); val ny = (hitBall.y - gy) / (r * 2f)
            val dot = vx * nx + vy * ny
            val tvx = dot * nx * ENERGY; val tvy = dot * ny * ENERGY
            val cvx = (vx - dot * nx) * ENERGY; val cvy = (vy - dot * ny) * ENERGY
            val cutDeg = Math.toDegrees(acos(dot.toDouble().coerceIn(-1.0, 1.0))).toFloat()

            val cuePath = mutableListOf(PointF(cue.x, cue.y), PointF(gx, gy))
            val cLen = hypot(cvx, cvy)
            if (cLen > MIN_SPEED) traceBounces(gx, gy, cvx / cLen, cvy / cLen, inner, bounces, cuePath)

            val targetPath = mutableListOf(PointF(hitBall.x, hitBall.y))
            val tLen = hypot(tvx, tvy)
            var pocketIdx: Int? = null
            if (tLen > MIN_SPEED) pocketIdx = traceBounces(hitBall.x, hitBall.y, tvx / tLen, tvy / tLen, inner, bounces, targetPath, table)

            ShotResult(cuePath, targetPath, PointF(gx, gy), cutDeg, pocketIdx)
        } else {
            val cuePath = mutableListOf(PointF(cue.x, cue.y))
            traceBounces(cue.x, cue.y, vx, vy, inner, bounces, cuePath)
            ShotResult(cuePath, null, null)
        }
    }

    fun traceBounces(
        sx: Float, sy: Float, vx: Float, vy: Float,
        bounds: RectF, maxBounces: Int, path: MutableList<PointF>,
        tableForPockets: RectF? = null
    ): Int? {
        var cx = sx; var cy = sy; var cvx = vx; var cvy = vy; var speed = 1f
        for (b in 0..maxBounces) {
            speed *= ENERGY; if (speed < MIN_SPEED) break
            val (t, wall) = nearestWall(cx, cy, cvx, cvy, bounds)
            val nx = cx + cvx * t; val ny = cy + cvy * t
            if (tableForPockets != null) {
                val stepCount = 20
                for (s in 1..stepCount) {
                    val f = s.toFloat() / stepCount
                    val px = cx + cvx * t * f; val py = cy + cvy * t * f
                    for ((idx, p) in pockets(tableForPockets).withIndex()) {
                        if (hypot(px - p.x, py - p.y) < POCKET_RADIUS) {
                            path.add(PointF(p.x, p.y)); return idx
                        }
                    }
                }
            }
            path.add(PointF(nx, ny)); cx = nx; cy = ny
            when (wall) { 0, 1 -> cvx = -cvx; 2, 3 -> cvy = -cvy; else -> break }
        }
        return null
    }

    private fun firstCollision(cx: Float, cy: Float, vx: Float, vy: Float, r: Float, balls: List<Ball>): Pair<Ball?, Float?> {
        var minT: Float? = null; var best: Ball? = null
        val D = r * 2f
        for (b in balls) {
            val fx = cx - b.x; val fy = cy - b.y
            val bv = 2f * (fx * vx + fy * vy)
            val c = fx * fx + fy * fy - D * D
            val disc = bv * bv - 4f * c
            if (disc >= 0f) {
                val t = (-bv - sqrt(disc)) / 2f
                if (t > r * 0.5f && (minT == null || t < minT!!)) { minT = t; best = b }
            }
        }
        return Pair(best, minT)
    }

    private fun nearestWall(cx: Float, cy: Float, vx: Float, vy: Float, b: RectF): Pair<Float, Int> {
        var tMin = Float.MAX_VALUE; var wall = -1
        fun check(t: Float, w: Int) { if (t > 0.5f && t < tMin) { tMin = t; wall = w } }
        if (vx > 1e-4f) check((b.right - cx) / vx, 0)
        if (vx < -1e-4f) check((b.left - cx) / vx, 1)
        if (vy > 1e-4f) check((b.bottom - cy) / vy, 2)
        if (vy < -1e-4f) check((b.top - cy) / vy, 3)
        return Pair(if (tMin == Float.MAX_VALUE) 1000f else tMin, wall)
    }
}
