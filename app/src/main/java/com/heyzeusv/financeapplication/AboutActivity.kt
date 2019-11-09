package com.heyzeusv.financeapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 *  Displays information about Application such as version and external libraries used.
 */
class AboutActivity : AppCompatActivity() {

    // views
    private lateinit var aboutLayout               : ConstraintLayout
    private lateinit var androidChartButton        : MaterialButton
    private lateinit var circleIndicatorButton     : MaterialButton
    private lateinit var androidChartScrollView    : NestedScrollView
    private lateinit var circleIndicatorScrollView : NestedScrollView
    private lateinit var androidChartGitHub        : TextView
    private lateinit var androidChartLicense       : TextView
    private lateinit var circleIndicatorGitHub     : TextView
    private lateinit var circleIndicatorLicense    : TextView

    // used to tell state of LicenseButtons
    private var mpButton = false
    private var ciButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // displays back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        aboutLayout               = findViewById(R.id.about_constraint)
        androidChartButton        = findViewById(R.id.androidChartButton)
        androidChartGitHub        = findViewById(R.id.androidChartGitHubTextView)
        androidChartLicense       = findViewById(R.id.androidChartLicense)
        androidChartScrollView    = findViewById(R.id.androidChartScrollView)
        circleIndicatorButton     = findViewById(R.id.circleIndicatorButton)
        circleIndicatorGitHub     = findViewById(R.id.circleIndicatorGitHubTextView)
        circleIndicatorLicense    = findViewById(R.id.circleIndicatorLicense)
        circleIndicatorScrollView = findViewById(R.id.circleIndicatorScrollView)

        // converts from HTML
        androidChartGitHub   .text = HtmlCompat.fromHtml(resources.getText(R.string.mpandroidchart_github ).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        circleIndicatorGitHub.text = HtmlCompat.fromHtml(resources.getText(R.string.circleIndicator_github).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        // allows text to open link in browser
        androidChartGitHub   .movementMethod = LinkMovementMethod.getInstance()
        circleIndicatorGitHub.movementMethod = LinkMovementMethod.getInstance()

        // strings holding text file content
        var mpLicense                   = ""
        var ciLicense                   = ""
        // reads through files
        var mpReader  : BufferedReader? = null
        var ciReader  : BufferedReader? = null

        try {

            // opens files
            mpReader  = BufferedReader(InputStreamReader(assets.open("MPAndroidChartLicense.txt")))
            ciReader  = BufferedReader(InputStreamReader(assets.open("CircleIndicatorLicense.txt")))
            // reads through file
            mpLicense = mpReader.readLines().joinToString("\n")
            ciLicense = ciReader.readLines().joinToString("\n")
        } catch (e : IOException) {

            e.printStackTrace()
        } finally {

            try {

                // close readers
                mpReader?.close()
                ciReader?.close()
            } catch (e : IOException) {

                e.printStackTrace()
            }

            // sets text to content from files
            androidChartLicense   .text = mpLicense
            circleIndicatorLicense.text = ciLicense
        }
    }

    override fun onStart() {
        super.onStart()

        androidChartButton.setOnClickListener {

            // hides or displays license
            if (!mpButton) {

                mpButton                         = true
                androidChartButton    .text      = resources.getString(R.string.hide_license)
                androidChartScrollView.isVisible = true
            } else {

                mpButton                         = false
                androidChartButton    .text      = resources.getString(R.string.show_license)
                androidChartScrollView.isVisible = false
            }
        }

        circleIndicatorButton.setOnClickListener {

            // hides or displays license
            if (!ciButton) {

                ciButton                            = true
                circleIndicatorButton    .text      = resources.getString(R.string.hide_license)
                circleIndicatorScrollView.isVisible = true
                val ciConstraintSet = ConstraintSet()
                ciConstraintSet.clone(aboutLayout)
                ciConstraintSet.connect(R.id.androidChartTextView, ConstraintSet.TOP,
                                        R.id.spacer2, ConstraintSet.BOTTOM, 0)
                ciConstraintSet.applyTo(aboutLayout)
            } else {

                ciButton                            = false
                circleIndicatorButton    .text      = resources.getString(R.string.show_license)
                circleIndicatorScrollView.isVisible = false
                val ciConstraintSet = ConstraintSet()
                ciConstraintSet.clone(aboutLayout)
                ciConstraintSet.connect(R.id.androidChartTextView, ConstraintSet.TOP,
                                        R.id.spacer1, ConstraintSet.BOTTOM, 0)
                ciConstraintSet.applyTo(aboutLayout)
            }
        }
    }
}