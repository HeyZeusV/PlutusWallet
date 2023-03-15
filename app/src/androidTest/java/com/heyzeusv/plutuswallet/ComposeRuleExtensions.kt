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
 *  Checks that node has test tag with given String resource [id] with [args] (if any).
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTTStrId(
    @StringRes id: Int,
    vararg args: Any?
): SemanticsNodeInteraction = onNode(hasTestTag(activity.getString(id, *args)))

/**
 *  Checks that node has text with given String resource [id] with [args] (if any).
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTextId(
    @StringRes id: Int,
    vararg args: Any?
): SemanticsNodeInteraction = onNodeWithText((activity.getString(id, *args)))

/**
 *  Checks that node has uppercase text with given String resource [id] with [args] (if any).
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTextIdUp(
    @StringRes id: Int,
    vararg args: Any?
): SemanticsNodeInteraction = onNodeWithText((activity.getString(id, *args)).uppercase())

/**
 *  Checks that node has content description with given String resource [id] with [args] (if any).
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithContDiscId(
    @StringRes id: Int,
    vararg args: Any?
): SemanticsNodeInteraction = onNode(hasContentDescription(activity.getString(id, *args)))