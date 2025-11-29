package com.anujsinghdev.anujtodo.ui.components

import android.annotation.SuppressLint
import android.graphics.BlendMode
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Colors defined in your snippet ---
val Emerald500 = Color(0xFF10B981)
val Indigo700 = Color(0xFF4338CA)
val Red500 = Color(0xFFEF4444)
val Sky400 = Color(0xFF38BDF8)
val Sky500 = Color(0xFF0EA5E9)
val Sky600 = Color(0xFF0284C7)
val Slate50 = Color(0xFFF8FAFC)
val Zinc800 = Color(0xFF27272A)

@Composable
fun MeshGradientButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // 0 = Idle, 1 = Loading, 2 = Success/Error
    var state by remember { mutableIntStateOf(0) }

    // Logic to simulate the states as per your request
    // In a real app, you would control 'state' from outside (ViewModel)
    val handleOnClick = {
        if (state == 0) {
            onClick() // Trigger the actual login logic
            state = 1 // Show loading
            scope.launch {
                // Simulation of loading process
                delay(2000)
                // Reset for now (In real app, ViewModel sets success/error)
                state = 0
            }
        }
    }

    val animatable = remember { Animatable(.1f) }
    LaunchedEffect(state) {
        when (state) {
            1 -> {
                while (true) {
                    animatable.animateTo(.4f, animationSpec = tween(500))
                    animatable.animateTo(.94f, animationSpec = tween(500))
                }
            }
            2 -> animatable.animateTo(-.9f, animationSpec = tween(900))
            else -> animatable.animateTo(.5f, animationSpec = tween(900))
        }
    }

    val color = remember { Animatable(Sky600) }
    LaunchedEffect(state) {
        when (state) {
            1 -> {
                while (true) {
                    color.animateTo(Emerald500, animationSpec = tween(500))
                    color.animateTo(Sky400, animationSpec = tween(500))
                }
            }
            2 -> color.animateTo(Red500, animationSpec = tween(900))
            else -> color.animateTo(Sky500, animationSpec = tween(900))
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            //.pointerHoverIcon(PointerIcon.Hand) // Requires Compose 1.5+
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = handleOnClick
            )
            .meshGradient(
                points = listOf(
                    listOf(
                        Offset(0f, 0f) to Zinc800,
                        Offset(.5f, 0f) to Zinc800,
                        Offset(1f, 0f) to Zinc800,
                    ),
                    listOf(
                        Offset(0f, .5f) to Indigo700,
                        Offset(.5f, animatable.value) to Indigo700,
                        Offset(1f, .5f) to Indigo700,
                    ),
                    listOf(
                        Offset(0f, 1f) to color.value,
                        Offset(.5f, 1f) to color.value,
                        Offset(1f, 1f) to color.value,
                    ),
                ),
                resolutionX = 64,
            )
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                )
            )
    ) {
        AnimatedContent(
            targetState = state,
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp) // Adjusted padding for better fit
                .defaultMinSize(minHeight = 24.dp)
                .align(Alignment.Center),
            transitionSpec = {
                slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                        slideOutVertically(targetOffsetY = { it }) + fadeOut() using SizeTransform(
                    clip = false,
                    sizeAnimationSpec = { _, _ -> spring(stiffness = Spring.StiffnessHigh) }
                )
            },
            label = "ButtonContent"
        ) { targetState ->
            when (targetState) {
                1 -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Slate50,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round,
                    )
                }
                2 -> {
                    Text(
                        text = "Wrong!",
                        color = Slate50,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else -> {
                    Text(
                        text = "Sign In", // Changed text to match your app
                        color = Slate50,
                        fontSize = 18.sp, // Adjusted font size
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// --- The Custom Modifier Implementation (The Magic Part) ---
fun Modifier.meshGradient(
    points: List<List<Pair<Offset, Color>>>,
    resolutionX: Int = 1,
): Modifier = this.drawBehind {
    val resolutionY = resolutionX
    val vertices = mutableListOf<Float>()
    val colors = mutableListOf<Int>()
    val indices = mutableListOf<Short>()

    // Generate vertices grid
    for (y in 0..resolutionY) {
        for (x in 0..resolutionX) {
            val u = x.toFloat() / resolutionX
            val v = y.toFloat() / resolutionY

            // Interpolate position based on control points (Bilinear interpolation)
            val p = getInterpolatedPoint(u, v, points, size)
            vertices.add(p.x)
            vertices.add(p.y)

            // Interpolate color
            val c = getInterpolatedColor(u, v, points)
            colors.add(c.toArgb())
        }
    }

    // Generate indices for triangles
    var index = 0
    for (y in 0 until resolutionY) {
        for (x in 0 until resolutionX) {
            val i = (y * (resolutionX + 1) + x).toShort()
            val j = (i + 1).toShort()
            val k = ((y + 1) * (resolutionX + 1) + x).toShort()
            val l = (k + 1).toShort()

            // Triangle 1
            indices.add(i)
            indices.add(k)
            indices.add(j)

            // Triangle 2
            indices.add(j)
            indices.add(k)
            indices.add(l)
        }
    }

    drawIntoCanvas { canvas ->
        // Native drawVertices for mesh rendering
        canvas.nativeCanvas.drawVertices(
            android.graphics.Canvas.VertexMode.TRIANGLES,
            vertices.size,
            vertices.toFloatArray(),
            0,
            null,
            0,
            colors.toIntArray(),
            0,
            indices.toShortArray(),
            0,
            indices.size,
            android.graphics.Paint()
        )
    }
}

// Helper: Bilinear interpolation for Points
@SuppressLint("RestrictedApi")
private fun getInterpolatedPoint(
    u: Float,
    v: Float,
    points: List<List<Pair<Offset, Color>>>,
    size: androidx.compose.ui.geometry.Size
): Offset {
    val row = (points.size - 1) * v
    val col = (points[0].size - 1) * u

    val r1 = row.toInt()
    val c1 = col.toInt()
    val r2 = (r1 + 1).coerceAtMost(points.size - 1)
    val c2 = (c1 + 1).coerceAtMost(points[0].size - 1)

    val rFrac = row - r1
    val cFrac = col - c1

    // Scale normalized offsets to actual size
    val p00 = points[r1][c1].first.scale(size)
    val p10 = points[r1][c2].first.scale(size)
    val p01 = points[r2][c1].first.scale(size)
    val p11 = points[r2][c2].first.scale(size)

    val top = lerp(p00, p10, cFrac)
    val bottom = lerp(p01, p11, cFrac)
    return lerp(top, bottom, rFrac)
}

// Helper: Bilinear interpolation for Colors
private fun getInterpolatedColor(
    u: Float,
    v: Float,
    points: List<List<Pair<Offset, Color>>>
): Color {
    val row = (points.size - 1) * v
    val col = (points[0].size - 1) * u

    val r1 = row.toInt()
    val c1 = col.toInt()
    val r2 = (r1 + 1).coerceAtMost(points.size - 1)
    val c2 = (c1 + 1).coerceAtMost(points[0].size - 1)

    val rFrac = row - r1
    val cFrac = col - c1

    val c00 = points[r1][c1].second
    val c10 = points[r1][c2].second
    val c01 = points[r2][c1].second
    val c11 = points[r2][c2].second

    val top = lerp(c00, c10, cFrac)
    val bottom = lerp(c01, c11, cFrac)
    return lerp(top, bottom, rFrac)
}

private fun Offset.scale(size: androidx.compose.ui.geometry.Size): Offset {
    return Offset(x * size.width, y * size.height)
}