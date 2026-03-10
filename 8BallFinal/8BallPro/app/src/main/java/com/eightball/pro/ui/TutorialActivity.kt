package com.eightball.pro.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    private data class Step(val emoji: String, val title: String, val desc: String, val color: Int)

    private val steps = listOf(
        Step("🔐", "Шаг 1: Разрешение", "Нажми «ВЫДАТЬ РАЗРЕШЕНИЕ» и разреши overlay поверх других приложений.", 0xFF0088FF.toInt()),
        Step("▶", "Шаг 2: Запуск", "Нажми «ЗАПУСТИТЬ OVERLAY». Сверни приложение кнопкой Home.", 0xFF00FF88.toInt()),
        Step("🎱", "Шаг 3: Открой 8 Ball Pool", "Открой игру. Поверх неё появится прозрачный overlay с панелью управления.", 0xFF00FFCC.toInt()),
        Step("📐", "Шаг 4: Настрой рамку", "Перетащи 4 угловых маркера точно по краям игрового стола.", 0xFFFFDD00.toInt()),
        Step("⚪", "Шаг 5: Позиционируй шары", "Перетащи CUE шар на позицию белого. Нажми + BALL для целевых шаров.", 0xFFFFFFFF.toInt()),
        Step("👆", "Шаг 6: Прицеливание", "Тапни по столу — автоматически рассчитается вся траектория с отскоками!", 0xFFFF6600.toInt()),
        Step("✨", "Шаг 7: Читай линии", "CYAN = биток • ЖЁЛТЫЙ = целевой шар • Ghost ball = куда встанет биток", 0xFF00FFCC.toInt()),
        Step("📊", "Cut Angle", "STRAIGHT (0-15°) • THIN CUT (15-35°) • HALF BALL (35-55°) • THICK CUT (55-75°) • FULL BALL (75-90°)", 0xFFFF4488.toInt()),
        Step("🔧", "Советы", "• VIEW MODE — overlay не мешает тапать\n• HIDE/SHOW из уведомления\n• Меняй тему под свой вкус", 0xFF44DDFF.toInt()),
    )

    private var currentStep = 0
    private var titleTv: TextView? = null
    private var descTv: TextView? = null
    private var emojiTv: TextView? = null
    private var progressTv: TextView? = null
    private var nextBtn: Button? = null
    private var prevBtn: Button? = null
    private var cardBg: android.graphics.drawable.GradientDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF030912.toInt()
        window.navigationBarColor = 0xFF030912.toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF030912.toInt())
            setPadding(28, 52, 28, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        root.addView(TextView(this).apply {
            text = "📖  ТУТОРИАЛ"; textSize = 22f; setTextColor(0xFF00FFCC.toInt())
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 24)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        val pt = TextView(this).apply {
            textSize = 12f; setTextColor(0xFF334455.toInt()); gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }
        progressTv = pt; root.addView(pt)

        val bg = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 24f }
        cardBg = bg
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32); background = bg
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val et = TextView(this).apply {
            textSize = 72f; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }
        emojiTv = et; card.addView(et)

        val tt = TextView(this).apply {
            textSize = 20f; gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
        }
        titleTv = tt; card.addView(tt)

        val dt = TextView(this).apply {
            textSize = 14f; gravity = Gravity.CENTER; lineSpacingMultiplier = 1.5f; setTextColor(0xFFAABBCC.toInt())
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        descTv = dt; card.addView(dt); root.addView(card)

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 24, 0, 0) }
        }

        val pb = Button(this).apply {
            text = "◀  НАЗАД"; textSize = 13f; setTextColor(0xFF00FFCC.toInt())
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 14f; setColor(0x1100FFCC); setStroke(2, 0x5500FFCC) }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, 8, 0) }
            setPadding(0, 24, 0, 24)
            setOnClickListener { if (currentStep > 0) { currentStep--; update() } }
        }
        prevBtn = pb; navRow.addView(pb)

        val nb = Button(this).apply {
            text = "ДАЛЕЕ  ▶"; textSize = 13f; setTextColor(0xFF030912.toInt())
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(8, 0, 0, 0) }
            setPadding(0, 24, 0, 24)
            setOnClickListener { if (currentStep < steps.size - 1) { currentStep++; update() } else finish() }
        }
        nextBtn = nb; navRow.addView(nb); root.addView(navRow)

        setContentView(ScrollView(this).apply { addView(root) })
        update()
    }

    private fun update() {
        val s = steps[currentStep]
        emojiTv?.text = s.emoji
        titleTv?.text = s.title; titleTv?.setTextColor(s.color)
        descTv?.text = s.desc
        cardBg?.setColor(s.color and 0x00FFFFFF or 0x11000000)
        cardBg?.setStroke(2, s.color and 0x00FFFFFF or 0x44000000)
        progressTv?.text = "ШАГ ${currentStep + 1} / ${steps.size}"
        prevBtn?.alpha = if (currentStep == 0) 0.3f else 1f
        nextBtn?.text = if (currentStep == steps.size - 1) "ЗАВЕРШИТЬ ✓" else "ДАЛЕЕ  ▶"
        nextBtn?.background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 14f; setColor(s.color); setStroke(2, s.color) }
    }
}
