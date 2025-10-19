package com.example.core_ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp; val sm = 8.dp; val md = 12.dp; val lg = 16.dp; val xl = 24.dp
}
object Radius { val sm = 8.dp; val md = 12.dp; val lg = 16.dp }
object Elevation { val card = 2.dp; val raised = 6.dp }
object Dimens { val buttonHeight = 48.dp }
val AppShapes = Shapes(
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg)
)
