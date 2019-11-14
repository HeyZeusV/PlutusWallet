package com.heyzeusv.financeapplication

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
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
    private lateinit var changelogButton           : MaterialButton
    private lateinit var circleIndicatorButton     : MaterialButton
    private lateinit var androidChartScrollView    : NestedScrollView
    private lateinit var changelogScrollView       : NestedScrollView
    private lateinit var circleIndicatorScrollView : NestedScrollView
    private lateinit var androidChartGitHub        : TextView
    private lateinit var androidChartLicense       : TextView
    private lateinit var changelog                 : TextView
    private lateinit var circleIndicatorGitHub     : TextView
    private lateinit var circleIndicatorLicense    : TextView
    private lateinit var email                     : TextView

    // used to tell state of LicenseButtons
    private var mpButton = false
    private var clButton = false
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
        changelogButton           = findViewById(R.id.changelogButton)
        changelog                 = findViewById(R.id.changelogTextView)
        changelogScrollView       = findViewById(R.id.changelogScrollView)
        circleIndicatorButton     = findViewById(R.id.circleIndicatorButton)
        circleIndicatorGitHub     = findViewById(R.id.circleIndicatorGitHubTextView)
        circleIndicatorLicense    = findViewById(R.id.circleIndicatorLicense)
        circleIndicatorScrollView = findViewById(R.id.circleIndicatorScrollView)
        email                     = findViewById(R.id.emailTextView)

        // converts from HTML
        androidChartGitHub   .text = HtmlCompat.fromHtml(resources.getText(R.string.library_mpandroidchart_github ).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        circleIndicatorGitHub.text = HtmlCompat.fromHtml(resources.getText(R.string.library_circleIndicator_github).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        email                .text = HtmlCompat.fromHtml(resources.getText(R.string.about_email).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        // allows text to open web link/email
        androidChartGitHub   .movementMethod = LinkMovementMethod.getInstance()
        circleIndicatorGitHub.movementMethod = LinkMovementMethod.getInstance()
        email                .movementMethod = LinkMovementMethod.getInstance()

        // strings holding text file content
        var mpLicense                   = ""
        var clFile                      = ""
        var ciLicense                   = ""

        // reads through files
        var mpReader  : BufferedReader? = null
        var clReader  : BufferedReader? = null
        var ciReader  : BufferedReader? = null

        try {

            // opens files
            mpReader  = BufferedReader(InputStreamReader(assets.open("MPAndroidChartLicense.txt")))
            clReader  = BufferedReader(InputStreamReader(assets.open("Changelog.txt")))
            ciReader  = BufferedReader(InputStreamReader(assets.open("CircleIndicatorLicense.txt")))

            // reads through file
            mpLicense = mpReader.readLines().joinToString("\n")
            clFile    = clReader.readLines().joinToString("\n")
            ciLicense = ciReader.readLines().joinToString("\n")
        } catch (e : IOException) {

            e.printStackTrace()
        } finally {

            try {

                // close readers
                mpReader?.close()
                clReader?.close()
                ciReader?.close()
            } catch (e : IOException) {

                e.printStackTrace()
            }

            // sets text to content from files
            androidChartLicense   .text = mpLicense
            changelog             .text = clFile
            circleIndicatorLicense.text = ciLicense
        }
    }

    override fun onStart() {
        super.onStart()

        androidChartButton.setOnClickListener {

            // hides or displays license
            if (!mpButton) {

                mpButton                          = true
                androidChartScrollView.visibility = View.VISIBLE
            } else {

                mpButton                          = false
                androidChartScrollView.visibility = View.GONE
            }
        }

        changelogButton.setOnClickListener {

            // hides or displays changelog
            if (!clButton) {

                clButton = true
                changelogScrollView.visibility = View.VISIBLE
                val clConstraintSet = ConstraintSet()
                clConstraintSet.clone(aboutLayout)
                clConstraintSet.connect(R.id.developerTextView, ConstraintSet.TOP,
                                        R.id.spacer5, ConstraintSet.BOTTOM, 0)
                clConstraintSet.applyTo(aboutLayout)
            } else {

                clButton = false
                changelogScrollView.visibility = View.GONE
                val clConstraintSet = ConstraintSet()
                clConstraintSet.clone(aboutLayout)
                clConstraintSet.connect(R.id.developerTextView, ConstraintSet.TOP,
                                        R.id.spacer4, ConstraintSet.BOTTOM, 0)
                clConstraintSet.applyTo(aboutLayout)
            }
        }

        circleIndicatorButton.setOnClickListener {

            // hides or displays license
            if (!ciButton) {

                ciButton                             = true
                circleIndicatorScrollView.visibility = View.VISIBLE
                val ciConstraintSet = ConstraintSet()
                ciConstraintSet.clone(aboutLayout)
                ciConstraintSet.connect(R.id.androidChartTextView, ConstraintSet.TOP,
                                        R.id.spacer2, ConstraintSet.BOTTOM, 0)
                ciConstraintSet.applyTo(aboutLayout)
            } else {

                ciButton                             = false
                circleIndicatorScrollView.visibility = View.GONE
                val ciConstraintSet = ConstraintSet()
                ciConstraintSet.clone(aboutLayout)
                ciConstraintSet.connect(R.id.androidChartTextView, ConstraintSet.TOP,
                                        R.id.spacer1, ConstraintSet.BOTTOM, 0)
                ciConstraintSet.applyTo(aboutLayout)
            }
        }
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean {

        when (item?.itemId) {

            // returns user to previous activity if they select back arrow
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}