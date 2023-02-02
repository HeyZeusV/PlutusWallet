package com.heyzeusv.plutuswallet.ui.about

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@OptIn(ExperimentalUnitApi::class)
@Composable
fun AboutScreen() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = dimensionResource(R.dimen.cardFullPadding)),
        shape = MaterialTheme.shapes.medium,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .padding(all = dimensionResource(R.dimen.card_content_pad))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextCenterAlign(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.h2.copy(
                    fontSize = TextUnit(50f, TextUnitType.Sp),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.Bold
                )
            )
            TextCenterAlign(text = stringResource(R.string.about_by_name))
            TextCenterAlign(text = stringResource(R.string.about_version))
            ButtonRevealContent(
                buttonText = stringResource(R.string.about_changelog),
                file = "Changelog.txt"
            )
            HyperlinkText(
                text = stringResource(R.string.about_github),
                link = stringResource(R.string.app_github_link)
            )
            HyperlinkText(
                text = stringResource(R.string.about_privacy_policy),
                link = stringResource(R.string.about_privacy_policy_link)
            )
            TextCenterAlign(text = stringResource(R.string.about_special_thanks))
            TextCenterAlign(text = stringResource(R.string.about_contact_me))
            HyperlinkText(
                text = stringResource(R.string.app_email),
                link = stringResource(R.string.about_email_link)
            )
            TextCenterAlign(text = stringResource(R.string.about_translation_warning))
            TextCenterAlign(
                text = stringResource(R.string.about_external_libraries),
                style = MaterialTheme.typography.h6
            )
            TextCenterAlign(text = stringResource(R.string.library_mpandroidchart))
            HyperlinkText(
                text = stringResource(R.string.about_github),
                link = stringResource(R.string.library_mpandroidchart_github)
            )
            ButtonRevealContent(
                buttonText = stringResource(R.string.about_license),
                file = "MPAndroidChartLicense.txt"
            )
        }
    }
}

/**
 *  Simple Text Composable with Center text alignment
 */
@Composable
fun TextCenterAlign(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text,
        modifier,
        textAlign = TextAlign.Center,
        style = style
    )
}

/**
 *  Button with [buttonText] that when pressed, reveals Surface containing Text with [file] as
 *  its content
 */
@OptIn(ExperimentalUnitApi::class)
@Composable
fun ButtonRevealContent(buttonText: String, file: String) {
    var revealContent by remember { mutableStateOf(false) }
    var composeSize by remember { mutableStateOf(Size.Zero) }

    val fileContent = loadFile(file, LocalContext.current)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { revealContent = !revealContent },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Text(
                text = buttonText.uppercase(),
                style = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.surface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = TextUnit(1f, TextUnitType.Sp)
                )
            )
        }
        AnimatedVisibility(
            visible = revealContent,
            enter = expandVertically(
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                ),
                initialHeight = { -composeSize.height.toInt() - 50 }
            ),
            exit = shrinkVertically(
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                ),
                targetHeight = { -composeSize.height.toInt() - 50 }
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.aboutNestedScrollViewHeight))
                    .onGloballyPositioned { composeSize = it.size.toSize() }
                    .verticalScroll(rememberScrollState()),
                shape = MaterialTheme.shapes.medium,
                color = Color(0x26000000)
            ) {
                TextCenterAlign(
                    text = fileContent,
                    modifier = Modifier
                        .padding(all = dimensionResource(R.dimen.AboutNestedScrollViewChildPadding))
                        .testTag("File $file"),
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}

/**
 *  Creates a ClickableText with [text] that when clicked leads to [link].
 *  Modified from https://gist.github.com/stevdza-san/ff9dbec0e072d8090e1e6d16e6b73c91
 */
@Composable
fun HyperlinkText(
    text: String,
    link: String
) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colors.secondary,
                textDecoration = TextDecoration.Underline
            ),
            start = 0,
            end = text.length
        )
        addStringAnnotation(
            tag = "tag",
            annotation = link,
            start = 0,
            end = text.length
        )
    }

    ClickableText(
        text = annotatedString,
        modifier = Modifier.testTag(link),
        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        onClick = { offset ->
            annotatedString
                .getStringAnnotations(
                    start = offset,
                    end = offset,
                ).firstOrNull()?.let { result ->
                    uriHandler.openUri(result.item)
                }
        }
    )
}

/**
 *  Uses [context] in order to be able to use BufferedReader to open [file] in order to return
 *  its contents
 */
private fun loadFile(file: String, context: Context): String {
    var fileText = ""
    var reader: BufferedReader? = null

    try {
        // open file and read through it
        reader = BufferedReader(InputStreamReader(context.assets.open(file)))
        fileText = reader.readLines().joinToString("\n")
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            // close reader
            reader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return fileText
}

@Preview
@Composable
fun AboutScreenPreview() {
    PlutusWalletTheme {
        AboutScreen()
    }
}