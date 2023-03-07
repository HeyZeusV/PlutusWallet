package com.heyzeusv.plutuswallet.ui

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule

/**
 *  Checks that node has test tag with given String resource [id]
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithTTStrId(
    @StringRes id: Int
): SemanticsNodeInteraction = onNode(hasTestTag(activity.getString(id)))