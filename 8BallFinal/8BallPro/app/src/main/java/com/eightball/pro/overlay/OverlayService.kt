package com.eightball.pro.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.eightball.pro.model.AppSettings
import com.eightball.pro.ui.MainActivity

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var overlayView: OverlayView? = null
    private var panelView: View? = null
    private val settings = AppSettings()

    companion object {
        const val CHANNEL_ID = "8ball_overlay"
        const val NOTIF_ID = 42
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_HIDE = "ACTION_HIDE"
        const val ACTION_SHOW = "ACTION_SHOW"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(NOTIF_ID, buildNotification())
        spawnOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            ACTION_HIDE -> { overlayView?.visibility = View.GONE; panelView?.visibility = View.GONE }
            ACTION_SHOW -> { overlayView?.visibility = View.VISIBLE; panelView?.visibility = View.VISIBLE }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        safeRemove(overlayView)
        safeRemove(panelView)
        super.onDestroy()
    }

    private fun spawnOverlay() {
        val ov = OverlayView(this).also { overlayView = it }
        ov.settings = settings

        val ovParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(ov, ovParams)

        val panel = buildPanel(ov)
        panelView = panel
        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 16; y = 100 }
        wm.addView(panel, panelParams)
        makeDraggable(panel, panelParams)
    }

    private fun buildPanel(ov: OverlayView): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 20f; setColor(0xEE0A1520.toInt()); setStroke(2, 0x8800FFCC)
            }
            setPadding(16, 12, 16, 14)
            elevation = 16f
        }

        root.addView(row().also { r ->
            r.addView(label("🎱 8BALL PRO", 0xFF00FFCC.toInt(), 13f, true).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            r.addView(btn("✕", 0xFFFF4455.toInt()) { stopSelf() })
        })

        root.addView(div())

        root.addView(row().also { r ->
            r.addView(btn2("＋ BALL", 0xFF00CC66.toInt()) { ov.addBall(); invalidatePanel(root, ov) })
            r.addView(space(6))
            r.addView(btn2("CLEAR", 0xFFFF5533.toInt()) { ov.clearBalls(); invalidatePanel(root, ov) })
            r.addView(space(6))
            r.addView(btn2("RESET CUE", 0xFF0088FF.toInt()) { ov.resetCue() })
        })

        root.addView(vspace(8))

        val bounceLabel = label("BOUNCES: ${settings.bounceCount}", 0xFF00FFCC.toInt(), 11f, false).apply {
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        root.addView(row().also { r ->
            r.addView(btn("−", 0xFF223344.toInt()) {
                if (settings.bounceCount > 1) { settings.bounceCount--; bounceLabel.text = "BOUNCES: ${settings.bounceCount}"; ov.recompute(); ov.invalidate() }
            })
            r.addView(bounceLabel)
            r.addView(btn("＋", 0xFF223344.toInt()) {
                if (settings.bounceCount < 8) { settings.bounceCount++; bounceLabel.text = "BOUNCES: ${settings.bounceCount}"; ov.recompute(); ov.invalidate() }
            })
        })

        root.addView(vspace(8))

        root.addView(row().also { r ->
            val ghostBtn = toggleBtn("GHOST", settings.showGhostBall)
            ghostBtn.setOnClickListener {
                settings.showGhostBall = !settings.showGhostBall
                ov.settings = settings; ov.invalidate()
                (it as Button).text = if (settings.showGhostBall) "GHOST ✓" else "GHOST ✗"
                it.alpha = if (settings.showGhostBall) 1f else 0.5f
            }
            r.addView(ghostBtn)
            r.addView(space(6))
            val cutBtn = toggleBtn("CUT°", settings.showCutAngle)
            cutBtn.setOnClickListener {
                settings.showCutAngle = !settings.showCutAngle
                ov.settings = settings; ov.invalidate()
                (it as Button).text = if (settings.showCutAngle) "CUT° ✓" else "CUT° ✗"
                it.alpha = if (settings.showCutAngle) 1f else 0.5f
            }
            r.addView(cutBtn)
            r.addView(space(6))
            val glowBtn = toggleBtn("GLOW", settings.neonGlow)
            glowBtn.setOnClickListener {
                settings.neonGlow = !settings.neonGlow
                ov.settings = settings; ov.invalidate()
                (it as Button).text = if (settings.neonGlow) "GLOW ✓" else "GLOW ✗"
                it.alpha = if (settings.neonGlow) 1f else 0.5f
            }
            r.addView(glowBtn)
        })

        root.addView(vspace(8))

        val themeColors = listOf(0xFF00FFCC.toInt(), 0xFF00FF66.toInt(), 0xFFFFDD00.toInt(), 0xFFFF44CC.toInt())
        root.addView(row().also { r ->
            themeColors.forEachIndexed { i, c ->
                r.addView(android.widget.Button(this).apply {
                    text = listOf("CYN","EMR","GLD","PNK")[i]; textSize = 9f; setTextColor(c)
                    background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 8f; setColor(c and 0x00FFFFFF or 0x22000000); setStroke(2, c and 0x00FFFFFF or 0x88000000.toInt()) }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setPadding(4, 8, 4, 8)
                    setOnClickListener { settings.tableColorPreset = i; ov.settings = settings; ov.invalidate() }
                })
                if (i < 3) r.addView(space(4))
            }
        })

        root.addView(div())

        val editBtn = android.widget.Button(this).apply {
            text = "✏  EDIT MODE"; textSize = 11f; setTextColor(0xFF00FFCC.toInt())
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 12f; setColor(0x1100FFCC); setStroke(2, 0x5500FFCC) }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, 18, 0, 18)
            setOnClickListener {
                ov.editMode = !ov.editMode
                (it as Button).text = if (ov.editMode) "✏  EDIT MODE" else "👁  VIEW MODE"
                it.setTextColor(if (ov.editMode) 0xFF00FFCC.toInt() else 0xFF667788.toInt())
            }
        }
        root.addView(editBtn)
        return root
    }

    private fun invalidatePanel(root: LinearLayout, ov: OverlayView) {}

    private fun makeDraggable(v: View, params: WindowManager.LayoutParams) {
        var dx = 0f; var dy = 0f
        v.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { dx = params.x - event.rawX; dy = params.y - event.rawY }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (event.rawX + dx).toInt().coerceAtLeast(0)
                    params.y = (event.rawY + dy).toInt().coerceAtLeast(0)
                    wm.updateViewLayout(v, params)
                }
            }
            false
        }
    }

    private fun safeRemove(v: View?) { v?.let { try { wm.removeView(it) } catch (_: Exception) {} } }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "8Ball Overlay", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, OverlayService::class.java).apply { action = ACTION_STOP }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val hide = PendingIntent.getService(this, 2, Intent(this, OverlayService::class.java).apply { action = ACTION_HIDE }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getService(this, 3, Intent(this, OverlayService::class.java).apply { action = ACTION_SHOW }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎱 8Ball Pro активен")
            .setContentText("Overlay запущен")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(open)
            .addAction(0, "HIDE", hide)
            .addAction(0, "SHOW", show)
            .addAction(0, "STOP", stop)
            .setOngoing(true).setPriority(NotificationCompat.PRIORITY_LOW).build()
    }

    private fun row() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }
    private fun label(text: String, color: Int, size: Float, bold: Boolean) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color)
        if (bold) typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private fun btn(text: String, color: Int, onClick: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 14f; setTextColor(color); setPadding(12, 4, 12, 4)
        background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 8f; setColor(0x22000000); setStroke(2, color and 0x00FFFFFF or 0x66000000.toInt()) }
        layoutParams = LinearLayout.LayoutParams(44, 44); setOnClickListener { onClick() }
    }
    private fun btn2(text: String, color: Int, onClick: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 9f; setTextColor(color)
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        setPadding(14, 8, 14, 8)
        background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 10f; setColor(color and 0x00FFFFFF or 0x22000000); setStroke(2, color and 0x00FFFFFF or 0x88000000.toInt()) }
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); setOnClickListener { onClick() }
    }
    private fun toggleBtn(text: String, active: Boolean) = Button(this).apply {
        this.text = "$text ${if (active) "✓" else "✗"}"; textSize = 9f; setTextColor(0xFF00FFCC.toInt())
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        alpha = if (active) 1f else 0.5f; setPadding(8, 8, 8, 8)
        background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 8f; setColor(0x22003322); setStroke(2, 0x6600FFCC) }
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    private fun div() = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply { setMargins(0, 8, 0, 8) }; setBackgroundColor(0x3300FFCC)
    }
    private fun space(dp: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams((dp * resources.displayMetrics.density).toInt(), 1)
    }
    private fun vspace(dp: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (dp * resources.displayMetrics.density).toInt())
    }
}
