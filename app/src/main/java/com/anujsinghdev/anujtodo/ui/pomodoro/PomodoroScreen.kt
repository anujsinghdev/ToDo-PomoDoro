package com.anujsinghdev.anujtodo.ui.pomodoro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.BottomNavItem
import com.anujsinghdev.anujtodo.ui.components.GlassBottomNavigation
import com.anujsinghdev.anujtodo.ui.util.Screen
import java.util.Locale

val TimerFont = FontFamily.Monospace

@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val timeLeft by viewModel.timeLeftInMillis
    val initialTime by viewModel.initialTimeInMillis
    val isRunning by viewModel.isTimerRunning
    val customDurations by viewModel.customDurations.collectAsState() // Collect custom durations

    val isSessionActive = remember(timeLeft, initialTime, isRunning) {
        isRunning || timeLeft != initialTime
    }

    var showTimeDialog by remember { mutableStateOf(false) }

    val progress = remember(timeLeft, initialTime) {
        if (initialTime > 0) 1f - (timeLeft.toFloat() / initialTime.toFloat())
        else 0f
    }

    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Focus", Icons.Filled.CenterFocusStrong, Icons.Outlined.CenterFocusStrong),
        BottomNavItem("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            GlassBottomNavigation(
                items = navItems,
                selectedItem = 1,
                onItemClick = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.TodoListScreen.route) {
                            popUpTo(Screen.TodoListScreen.route) { inclusive = true }
                        }
                        1 -> { /* Already on Focus */ }
                        2 -> { /* Navigate to Stats */ }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Timer Section - Takes most of the space and keeps timer centered
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Main centered timer content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- CIRCULAR TIMER WITH PROGRESS ---
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(380.dp)
                    ) {
                        // Animated circular progress indicator
                        CircularProgressTimer(
                            progress = progress,
                            isRunning = isRunning,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Timer text overlay - Always centered
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formatTime(timeLeft),
                                color = Color.White,
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = TimerFont,
                                letterSpacing = 4.sp,
                                style = MaterialTheme.typography.displayLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Status indicator with animation
                            AnimatedContent(
                                targetState = if (isRunning) "Focusing..." else if (isSessionActive) "Paused" else "Ready",
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                            fadeOut(animationSpec = tween(300))
                                },
                                label = "status"
                            ) { status ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isRunning) {
                                        PulsingDot()
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = status,
                                        color = if (isRunning) Color(0xFF64B5F6) else Color.Gray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- EDIT BUTTON (Positioned at bottom of Box, doesn't affect center) ---
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isSessionActive,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.clickable { showTimeDialog = true },
                        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Edit Duration",
                                color = Color(0xFFAAAAAA),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- CONTROLS SECTION (Fixed at bottom) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = isSessionActive,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "controls"
                ) { active ->
                    if (active) {
                        // Active state controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernButton(
                                text = "Stop",
                                onClick = { viewModel.resetTimer() },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                isPrimary = false
                            )
                            ModernButton(
                                text = if (isRunning) "Pause" else "Resume",
                                onClick = {
                                    if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                isPrimary = true
                            )
                        }
                    } else {
                        // Idle state
                        ModernButton(
                            text = "Start Focus",
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            isPrimary = true
                        )
                    }
                }
            }
        }
    }

    // Updated dialog with custom duration support
    if (showTimeDialog) {
        TimeSelectionDialog(
            currentMinutes = (initialTime / 1000 / 60).toInt(),
            customDurations = customDurations,
            onDismiss = { showTimeDialog = false },
            onTimeSelected = { minutes ->
                viewModel.updateDuration(minutes)
                showTimeDialog = false
            },
            onAddCustomDuration = { minutes ->
                viewModel.addCustomDuration(minutes)
            },
            onRemoveCustomDuration = { minutes ->
                viewModel.removeCustomDuration(minutes)
            }
        )
    }
}

@Composable
fun CircularProgressTimer(
    progress: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val gradientColors = listOf(
        Color(0xFF64B5F6),
        Color(0xFF42A5F5),
        Color(0xFF2196F3)
    )

    Canvas(modifier = modifier.padding(20.dp)) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter + strokeWidth) / 2, (size.height - diameter + strokeWidth) / 2)

        // Background circle
        drawArc(
            color = Color(0xFF1A1A1A),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc with gradient
        if (animatedProgress > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter - strokeWidth, diameter - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                Color(0xFF64B5F6).copy(alpha = alpha),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color(0xFF2196F3) else Color(0xFF1A1A1A),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 4.dp else 0.dp
        ),
        border = if (!isPrimary) BorderStroke(1.dp, Color(0xFF2A2A2A)) else null
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TimeSelectionDialog(
    currentMinutes: Int,
    customDurations: List<Int>,
    onDismiss: () -> Unit,
    onTimeSelected: (Int) -> Unit,
    onAddCustomDuration: (Int) -> Unit,
    onRemoveCustomDuration: (Int) -> Unit
) {
    val defaultOptions = listOf(15, 20, 25, 30, 45, 60, 90, 120)
    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Focus Duration",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select your focus time",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )

                // Default durations
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    defaultOptions.chunked(4).forEach { rowItems ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { mins ->
                                TimeButton(
                                    minutes = mins,
                                    isSelected = mins == currentMinutes,
                                    onSelect = { onTimeSelected(mins) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Custom durations section
                if (customDurations.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Custom Durations",
                            color = Color(0xFF64B5F6),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Display custom durations in rows
                        customDurations.chunked(4).forEach { rowItems ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { mins ->
                                    TimeButton(
                                        minutes = mins,
                                        isSelected = mins == currentMinutes,
                                        onSelect = { onTimeSelected(mins) },
                                        onLongPress = { onRemoveCustomDuration(mins) },
                                        modifier = Modifier.weight(1f),
                                        isCustom = true
                                    )
                                }
                                // Fill remaining space if row not full
                                repeat(4 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        Text(
                            "Long press to remove",
                            color = Color(0xFF666666),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Add custom duration button/input
                if (showCustomInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customMinutesText,
                            onValueChange = {
                                if (it.length <= 3) customMinutesText = it.filter { char -> char.isDigit() }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Minutes", color = Color(0xFF666666)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val mins = customMinutesText.toIntOrNull()
                                    if (mins != null && mins > 0 && mins <= 999) {
                                        onAddCustomDuration(mins)
                                        customMinutesText = ""
                                        showCustomInput = false
                                    }
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                val mins = customMinutesText.toIntOrNull()
                                if (mins != null && mins > 0 && mins <= 999) {
                                    onAddCustomDuration(mins)
                                    customMinutesText = ""
                                    showCustomInput = false
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Add",
                                tint = Color(0xFF2196F3)
                            )
                        }

                        IconButton(onClick = {
                            showCustomInput = false
                            customMinutesText = ""
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = Color(0xFF888888)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0D1821)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add custom",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Custom Duration",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeButton(
    minutes: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    isCustom: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onLongPress
            ),
        color = if (isSelected) Color(0xFF2196F3) else Color(0xFF252525),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFF42A5F5))
        } else if (isCustom) {
            BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
        } else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$minutes",
                    color = if (isSelected) Color.White else Color(0xFF999999),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 18.sp
                )
                Text(
                    "min",
                    color = if (isSelected) Color.White.copy(0.7f) else Color(0xFF666666),
                    fontSize = 10.sp
                )
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
