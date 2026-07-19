package com.tidy.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Material Symbols "arrows_outward" -- not part of the classic Material Icons set that
 * ships in material-icons-extended, so it's vendored directly from Google Fonts:
 * https://fonts.gstatic.com/render/v1/Material+Symbols+Outlined/24dp/arrows_outward.kt
 */
val Icons.Outlined.ArrowsOutward: ImageVector
    get() {
        if (_arrowsOutward != null) {
            return _arrowsOutward!!
        }
        _arrowsOutward = ImageVector.Builder(
            name = "Outlined.ArrowsOutward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(17f, 17f)
                lineTo(15.6f, 15.6f)
                lineTo(18.18f, 13f)
                horizontalLineTo(13f)
                verticalLineTo(11f)
                horizontalLineToRelative(5.18f)
                lineTo(15.6f, 8.4f)
                lineTo(17f, 7f)
                lineToRelative(5f, 5f)
                lineToRelative(-5f, 5f)
                close()
                moveTo(7f, 17f)
                lineTo(2f, 12f)
                lineTo(7f, 7f)
                lineTo(8.4f, 8.4f)
                lineTo(5.83f, 11f)
                horizontalLineTo(11f)
                verticalLineToRelative(2f)
                horizontalLineTo(5.83f)
                lineTo(8.4f, 15.6f)
                lineTo(7f, 17f)
                close()
            }
        }.build()
        return _arrowsOutward!!
    }

private var _arrowsOutward: ImageVector? = null
