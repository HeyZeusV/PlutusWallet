package com.heyzeusv.plutuswallet.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.theme.PWLightColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.theme.chipTextStyle

/**
 *  Composables used by multiple Screens will be placed here
 */

/**
 *  Composable for selectable button/chip. [selected] determines if chip is selected. [onClick]
 *  is called when chip is selected. [label] is the text displayed on the chip. [showIcon]
 *  determines if a trailing icon should be displayed.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PWButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    selectedBackgroundColor: Color = Color.Unspecified,
    selectedTextColor: Color = MaterialTheme.colors.secondary,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(label),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = dimensionResource(R.dimen.button_chip_border_width),
            color = if (selected) {
                MaterialTheme.colors.secondary
            } else {
                LocalPWColors.current.unselected
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor
        ),
        leadingIcon = {
            // this icon is used to center text when trailing icon exists
            if (showIcon) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0f)
                )
            }
        },
        trailingIcon = {
            if (showIcon) {
                Icon(
                    imageVector = if (selected) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = stringResource(R.string.icon_cd_expandcollapse),
                    tint = if (selected) {
                        MaterialTheme.colors.secondary
                    } else {
                        LocalPWColors.current.unselected
                    }
                )
            }
        },
        content = {
            Text(
                text = label.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = if (selected) {
                    selectedTextColor
                } else {
                    LocalPWColors.current.unselected
                },
                style = chipTextStyle
            )
        }
    )
}

/**
 *  Used only by *Preview() Composables. This allows for quick set up of Composable previews of
 *  [content] which are full screens.
 */
@Composable
fun PreviewHelper(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPWColors provides PWLightColors) {
        PlutusWalletTheme {
            content()
        }
    }
}

/**
 *  Used only by *Preview() Composables. This allows for quick set up of Composable previews of
 *  [content] while providing Card() as a background.
 */
@Composable
fun PreviewHelperCard(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPWColors provides PWLightColors) {
        PlutusWalletTheme {
            Card {
                content()
            }
        }
    }
}