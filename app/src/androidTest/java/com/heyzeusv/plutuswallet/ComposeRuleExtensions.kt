package com.heyzeusv.plutuswallet

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule

/**
 *  Checks that node has test tag with given String resource [id]
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTTStrId(
    @StringRes id: Int
): SemanticsNodeInteraction = onNode(hasTestTag(activity.getString(id)))

/**
 *  Checks that node has text with given String resource [id]
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTextId(
    @StringRes id: Int
): SemanticsNodeInteraction = onNodeWithText((activity.getString(id)))

/**
 *  Checks that node has content description with given String resource [id]
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithContDiscId(
    @StringRes id: Int
): SemanticsNodeInteraction = onNode(hasContentDescription(activity.getString(id)))