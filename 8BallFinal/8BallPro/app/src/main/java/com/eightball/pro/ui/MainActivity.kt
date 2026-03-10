package com.eightball.pro.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.eightball.pro.overlay.OverlayService

class MainActivity : AppCompatActivity() {

    private var permStatusText: TextView? = null
    private var launchBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF030912.toInt()
        window.navigationBarColor = 0xFF030912.toInt()
        setContentView(buildUI())
        if (savedInstanceState == null) showDisclaimer()
    }

    override fun onResume() {
        super.onResume()
        updatePermStatus()
    }

    private fun showDisclaimer() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF07111E.toInt())
            setPadding(44, 36, 44, 28)
        }
        layout.addView(TextView(this).apply {
            text = "⚠  ВАЖНО — ПРОЧИТАЙ"
            textSize = 18f; setTextColor(0xFFFF4444.toInt())
            android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD).also { typeface = it }
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 16)
        })
        listOf("❌ Нарушает Terms of Service Miniclip", "❌ Может привести к бану аккаунта", "❌ Разработчик не несёт ответственности").forEach { item ->
            layout.addView(TextView(this).apply { text = item; textSize = 13f; setTextColor(0xFFCCDDEE.toInt()); setPadding(0, 6, 0, 6) })
        }
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; setPadding(0, 20, 0, 0) }
        val d = AlertDialog.Builder(this).create()
        btnRow.addView(Button(this).apply { text = "ОТКАЗАТЬСЯ"; setTextColor(0xFF556677.toInt()); setBackgroundColor(0); setOnClickListener { d.dismiss(); finish() } })
        btnRow.addView(Button(this).apply { text = "✓ ПРИНИМАЮ"; setTextColor(0xFFFF4444.toInt()); android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD).also { typeface = it }; setBackgroundColor(0); setOnClickListener { d.dismiss() } })
        layout.addView(btnRow)
        d.setView(layout); d.setCancelable(false); d.show()
    }

    private fun buildUI(): View {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF030912.toInt())
            setPadding(28, 52, 28, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        root.addView(TextView(this).apply { text = "🎱"; textSize = 72f; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) })
        root.addView(TextView(this).apply {
            text = "8BALL PRO"; textSize = 32f; setTextColor(0xFF00FFCC.toInt())
            android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD).also { typeface = it }
            gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        })
        root.addView(TextView(this).apply {
            text = "TRAJECTORY TOOL v4.0"; textSize = 13f; setTextColor(0xFF1A3A5A.toInt())
            gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); setPadding(0, 0, 0, 32)
        })

        val pst = TextView(this).apply {
            textSize = 14f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }
        permStatusText = pst
        root.addView(pst)

        root.addView(bigBtn("🔐  ВЫДАТЬ РАЗРЕШЕНИЕ", 0xFF001833.toInt(), 0xFF0088FF.toInt()) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        root.addView(vspace(12))

        val lb = bigBtn("▶  ЗАПУСТИТЬ OVERLAY", 0xFF001A0D.toInt(), 0xFF00FF88.toInt()) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "⚠ Сначала выдай разрешение!", Toast.LENGTH_LONG).show()
                return@bigBtn
            }
            val i = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
            Toast.makeText(this, "✅ Запущен! Сверни приложение.", Toast.LENGTH_SHORT).show()
        }
        launchBtn = lb
        root.addView(lb)
        root.addView(vspace(12))

        root.addView(bigBtn("■  ОСТАНОВИТЬ", 0xFF1A0505.toInt(), 0xFFFF4455.toInt()) {
            stopService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "Overlay остановлен.", Toast.LENGTH_SHORT).show()
        })
        root.addView(vspace(24))

        root.addView(bigBtn("📖  ТУТОРИАЛ", 0xFF0A0A1A.toInt(), 0xFFFF9900.toInt()) {
            startActivity(Intent(this, TutorialActivity::class.java))
        })
        root.addView(vspace(24))

        root.addView(card("✨  ВОЗМОЖНОСТИ", "• Физика отскоков до 8 раз\n• Ball-to-ball коллизии\n• Ghost ball позиция\n• Cut angle метка\n• Подсветка лузы\n• 4 цветовые темы\n• Neon glow анимация\n• Floating панель управления\n• HIDE/SHOW из уведомления", 0xFF00FFCC.toInt(), 0xFF050F1A.toInt()))
        root.addView(vspace(12))
        root.addView(card("⚠  РИСКИ", "Использование нарушает ToS Miniclip.\nБан аккаунта возможен.\nВся ответственность на пользователе.", 0xFFFF4444.toInt(), 0xFF1A0505.toInt()))
        root.addView(vspace(20))
        root.addView(TextView(this).apply {
            text = "8Ball Pro v4.0 • Educational purposes only"
            textSize = 10f; setTextColor(0xFF1A2A3A.toInt()); gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        scroll.addView(root)
        return scroll
    }

    private fun bigBtn(label: String, bg: Int, accent: Int, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 15f; setTextColor(accent)
        android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD).also { typeface = it }
        background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 18f; setColor(bg); setStroke(2, accent) }
        setPadding(0, 32, 0, 32)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        setOnClickListener { action() }
    }

    private fun card(title: String, body: String, accent: Int, bg: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 20f; setColor(bg); setStroke(2, accent and 0x00FFFFFF or 0x44000000) }
        setPadding(24, 18, 24, 18)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        addView(TextView(context).apply {
            text = title; textSize = 14f; setTextColor(accent)
            android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD).also { typeface = it }
            setPadding(0, 0, 0, 8)
        })
        addView(TextView(context).apply { text = body; textSize = 13f; setTextColor(0xFFAABBCC.toInt()); lineSpacingMultiplier = 1.45f })
    }

    private fun vspace(dp: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (dp * resources.displayMetrics.density).toInt())
    }

    private fun updatePermStatus() {
        val has = Settings.canDrawOverlays(this)
        permStatusText?.text = if (has) "✅ Разрешение выдано" else "❌ Разрешение не выдано"
        permStatusText?.setTextColor(if (has) 0xFF00FF88.toInt() else 0xFFFF6644.toInt())
        launchBtn?.alpha = if (has) 1f else 0.5f
    }
}
