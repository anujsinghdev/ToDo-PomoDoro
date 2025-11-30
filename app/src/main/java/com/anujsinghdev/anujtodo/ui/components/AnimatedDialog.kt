package com.anujsinghdev.anujtodo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedDialog(
    onDismissRequest: () -> Unit,
    content: @Composable (triggerDismiss: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Animation States
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    // Function to run exit animation then dismiss
    val triggerDismiss: () -> Unit = {
        scope.launch {
            // Animate out
            launch { scale.animateTo(0.5f, animationSpec = tween(200)) }
            launch { alpha.animateTo(0f, animationSpec = tween(200)) }
            delay(200) // Wait for animation
            onDismissRequest() // Actually dismiss
        }
    }

    LaunchedEffect(Unit) {
        // Animate in
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(300))
        }
    }

    Dialog(
        onDismissRequest = triggerDismiss, // Intercept click outside to animate out first
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allows us to control the width/padding
            decorFitsSystemWindows = false
        )
    ) {
        // Transparent container to center our animated content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f * alpha.value)) // Custom dim
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    // Clicking background triggers dismiss
                    triggerDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    // Stop click propagation so clicking the dialog doesn't dismiss it
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                // Pass the dismiss trigger to the content so buttons can use it
                content(triggerDismiss)
            }
        }
    }
}