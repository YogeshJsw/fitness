package com.yogeshj.fitnessapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.yogeshj.fitnessapp.databinding.ActivityFitnessBinding


class FitnessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFitnessBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back button
        binding.back.setOnClickListener {
            finish()
        }

        //heading
        val title = intent.getStringExtra("title")
        binding.heading.text = title


        //About data
        val about = intent.getStringExtra("about")
        val lines = about?.split("\n")
        var formattedAbout = """
            <html>
                <body>
                    <p style="text-align: left; line-height: 1.5;">
                        
                        ${if (lines?.firstOrNull() == title) "<b>${lines?.first()}</b>" else lines?.first()}<br/>
                    </p>
                    <ul style="text-align: left; padding-left: 20px;">
                    """.trimIndent()

        if (lines != null) {
            for (i in 1 until lines.size) {
                formattedAbout += if (lines[i] == "Benefits:")
                    "<b>${lines[i]}</b>"
                else
                    "<li>${lines[i]}</li>"
            }
        }

        formattedAbout += """
                    </ul>
                </body>
            </html>
        """.trimIndent()

        binding.aboutText.text = Html.fromHtml(formattedAbout, Html.FROM_HTML_MODE_COMPACT)


        //Steps Data
        val steps = intent.getStringExtra("steps")
        val stepsLines = steps?.split("\n")

        var formattedSteps = """
            <html>
                <body>
                    <p style="text-align: left; line-height: 1.5;">
                    """.trimIndent()

        if (stepsLines != null) {
            for ((index, step) in stepsLines.withIndex()) {
                formattedSteps += "<b>Step${index + 1}:</b> $step<br/>"
            }
        }

        formattedSteps += """
                    </p>
                </body>
            </html>
        """.trimIndent()

        binding.stepsText.text = Html.fromHtml(formattedSteps, Html.FROM_HTML_MODE_COMPACT)
        Log.d("Steps Data", formattedSteps)

        //Video player
        val videoId = intent.getStringExtra("link")
        val str =
            "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/${videoId}?si=IuHVwo1lER8_pCCW&showinfo=0\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture;\"></iframe>"

        val webView = binding.webView
        webView.loadData(str, "text/html", "utf-8")
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()


        //Button Fragments
        binding.stepsBtn.setTextColor(Color.BLACK)
        binding.stepsBtn.setOnClickListener {
            binding.stepsBtn.setTextColor(Color.WHITE)
            binding.stepsScroll.visibility = View.VISIBLE
            binding.aboutScroll.visibility = View.GONE
            binding.aboutBtn.setTextColor(Color.BLACK)
        }
        binding.aboutBtn.setOnClickListener {
            binding.aboutBtn.setTextColor(Color.WHITE)
            binding.aboutScroll.visibility = View.VISIBLE
            binding.stepsScroll.visibility = View.GONE
            binding.stepsBtn.setTextColor(Color.BLACK)
        }
    }
}