package com.egor.mitvlauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 10, 17)
    private val panel = Color.rgb(23, 28, 43)
    private val white = Color.rgb(248, 249, 252)
    private val muted = Color.rgb(163, 172, 193)
    private val blue = Color.rgb(68, 125, 255)
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        buildHome()
    }

    private fun buildHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(42), dp(24), dp(42), dp(18))
            setBackgroundColor(bg)
            isFocusable = false
        }
        setContentView(root)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(text("tv", 28, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(text("Главная     Фильмы     Сериалы     Приложения     ⚙", 15, muted, true))
        root.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(dp(30), dp(24), dp(30), dp(26))
            background = gradient(
                intArrayOf(Color.rgb(49, 77, 137), Color.rgb(27, 35, 63), Color.rgb(12, 15, 27)),
                24
            )
        }
        hero.isFocusable = true
        hero.isFocusableInTouchMode = true
        hero.setOnFocusChangeListener { v, focused ->
            v.background = gradient(
                if (focused) intArrayOf(Color.rgb(64, 99, 169), Color.rgb(30, 40, 70), Color.rgb(12, 15, 27))
                else intArrayOf(Color.rgb(49, 77, 137), Color.rgb(27, 35, 63), Color.rgb(12, 15, 27)),
                24
            )
            v.scaleX = if (focused) 1.01f else 1f
            v.scaleY = if (focused) 1.01f else 1f
        }
        hero.addView(text("ПОДБОРКА ДНЯ  •  FULL HD", 13, Color.rgb(192, 211, 255), true))
        hero.addView(text("Большое кино начинается дома", 31, white, true).apply {
            setPadding(0, dp(8), 0, dp(8))
        })
        hero.addView(text("Ваши любимые приложения — в одном месте.", 16, Color.rgb(207, 214, 231), false))
        hero.setOnClickListener {
            Toast.makeText(this, "Выберите приложение в разделе ниже", Toast.LENGTH_SHORT).show()
        }
        root.addView(hero, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(20); bottomMargin = dp(18)
        })

        root.addView(text("Продолжить просмотр", 21, white, true).apply {
            setPadding(0, 0, 0, dp(10))
        })
        val movies = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val movieScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFocusable = false
            addView(movies)
        }
        val movieData = listOf(
            Triple("Космос", "ФАНТАСТИКА", intArrayOf(Color.rgb(28, 70, 130), Color.rgb(7, 15, 36))),
            Triple("Ночной город", "ТРИЛЛЕР", intArrayOf(Color.rgb(124, 55, 76), Color.rgb(26, 15, 32))),
            Triple("Дикая природа", "ДОКУМЕНТАЛЬНОЕ", intArrayOf(Color.rgb(40, 112, 91), Color.rgb(11, 36, 32))),
            Triple("Большое путешествие", "ПРИКЛЮЧЕНИЯ", intArrayOf(Color.rgb(166, 105, 51), Color.rgb(47, 27, 23)))
        )
        movieData.forEach { (title, genre, colors) ->
            movies.addView(movieCard(title, genre, colors))
        }
        root.addView(movieScroll, LinearLayout.LayoutParams(-1, dp(112)))

        root.addView(text("Приложения", 21, white, true).apply {
            setPadding(0, dp(18), 0, dp(10))
        })
        val appRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val appScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFocusable = false
            addView(appRow)
        }
        root.addView(appScroll, LinearLayout.LayoutParams(-1, dp(104)))

        val apps = getTvApps()
        if (apps.isEmpty()) {
            appRow.addView(appTile("⚙", "Настройки", null))
        } else {
            apps.take(12).forEachIndexed { index, app ->
                val glyph = when (app.label.lowercase()) {
                    in listOf("youtube", "youtube tv") -> "▶"
                    in listOf("netflix") -> "N"
                    in listOf("plex") -> "▰"
                    in listOf("prime video", "amazon prime video") -> "a"
                    in listOf("settings", "настройки") -> "⚙"
                    else -> app.label.take(1).uppercase()
                }
                appRow.addView(appTile(glyph, app.label, app.packageName))
            }
        }

        root.addView(text("Управление: ← → перемещение  •  OK запуск  •  Назад — свернуть",
            12, muted, false).apply { setPadding(0, dp(12), 0, 0) })
        root.post { hero.requestFocus() }
    }

    private data class TvApp(val label: String, val packageName: String)

    private fun getTvApps(): List<TvApp> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolved.mapNotNull {
            val pkg = it.activityInfo.packageName
            if (pkg == packageName) null
            else TvApp(it.loadLabel(pm)?.toString() ?: pkg, pkg)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    private fun movieCard(title: String, genre: String, colors: IntArray): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = gradient(colors, 16)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnFocusChangeListener { v, focused ->
                v.background = gradient(
                    if (focused) intArrayOf(Color.rgb(72, 111, 190), colors[0]) else colors, 16
                )
                v.scaleX = if (focused) 1.05f else 1f
                v.scaleY = if (focused) 1.05f else 1f
            }
        }
        card.addView(text(genre, 10, Color.rgb(222, 229, 246), true))
        card.addView(text(title, 16, white, true).apply { setPadding(0, dp(4), 0, 0) })
        card.setOnClickListener {
            Toast.makeText(this, "Это демонстрационная карточка. Откройте приложение ниже.", Toast.LENGTH_SHORT).show()
        }
        card.layoutParams = LinearLayout.LayoutParams(dp(218), dp(104)).apply { rightMargin = dp(12) }
        return card
    }

    private fun appTile(glyph: String, title: String, pkg: String?): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = rounded(panel, 16)
            isFocusable = true
            isFocusableInTouchMode = true
            setOnFocusChangeListener { v, focused ->
                v.background = rounded(if (focused) blue else panel, 16)
                v.scaleX = if (focused) 1.07f else 1f
                v.scaleY = if (focused) 1.07f else 1f
            }
            setOnClickListener {
                if (pkg != null) {
                    val launch = packageManager.getLaunchIntentForPackage(pkg)
                    if (launch != null) startActivity(launch)
                    else Toast.makeText(this@MainActivity, "Для этого приложения нет обычного запуска", Toast.LENGTH_SHORT).show()
                }
            }
        }
        tile.addView(text(glyph, 25, white, true).apply { gravity = Gravity.CENTER })
        tile.addView(text(title, 12, white, true).apply {
            gravity = Gravity.CENTER
            maxLines = 1
        })
        tile.contentDescription = title
        tile.layoutParams = LinearLayout.LayoutParams(dp(118), dp(88)).apply { rightMargin = dp(12) }
        return tile
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        typeface = if (bold) Typeface.create("sans-serif-medium", Typeface.NORMAL)
                   else Typeface.create("sans-serif", Typeface.NORMAL)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
        isSingleLine = false
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun gradient(colors: IntArray, radius: Int) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, colors
    ).apply { cornerRadius = dp(radius).toFloat() }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
