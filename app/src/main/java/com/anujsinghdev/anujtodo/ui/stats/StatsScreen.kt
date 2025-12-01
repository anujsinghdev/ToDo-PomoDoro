package com.anujsinghdev.anujtodo.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.BottomNavItem
import com.anujsinghdev.anujtodo.ui.components.CelebrationEffect
import com.anujsinghdev.anujtodo.ui.components.GlassBottomNavigation
import com.anujsinghdev.anujtodo.ui.util.Screen
import kotlinx.coroutines.delay
import java.util.Locale

// Modern Dark Theme Colors
val CardDark = Color(0xFF1E1E1E)
val NeonBlue = Color(0xFF00E5FF)
val NeonPurple = Color(0xFFD500F9)
val NeonGreen = Color(0xFF00E676)
val TextWhite = Color.White
val TextGrey = Color(0xFF888888)
val BackgroundBlack = Color(0xFF0A0A0A)

enum class TimePeriod {
    WEEKLY, MONTHLY, YEARLY, LIFETIME
}

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.userStats.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()
    val monthlyData by viewModel.monthlyData.collectAsState()
    val yearlyData by viewModel.yearlyData.collectAsState()
    val lifetimeData by viewModel.lifetimeData.collectAsState()

    var selectedPeriod by remember { mutableStateOf(TimePeriod.WEEKLY) }
    val showConfetti by viewModel.showCelebration.collectAsState()

    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(4000)
            viewModel.onCelebrationShown()
        }
    }

    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Focus", Icons.Filled.CenterFocusStrong, Icons.Outlined.CenterFocusStrong),
        BottomNavItem("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = BackgroundBlack,
        bottomBar = {
            GlassBottomNavigation(
                items = navItems,
                selectedItem = 2,
                onItemClick = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.TodoListScreen.route)
                        1 -> navController.navigate(Screen.PomodoroScreen.route)
                        2 -> { /* Already here */ }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // REMOVED SPACER HERE to remove top padding

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Your Journey", color = TextWhite, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("Track your productivity", color = TextGrey, fontSize = 14.sp)
                    }
                    Icon(Icons.Default.TrendingUp, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                EnhancedLevelCard(stats)

                Spacer(modifier = Modifier.height(24.dp))

                // --- NEW STATS LAYOUT ---
                // Section 1: Focus Time
                DualStatCard(
                    title = "Focus Time",
                    icon = Icons.Default.Schedule,
                    accentColor = NeonBlue,
                    label1 = "Today's Focus Time",
                    value1 = "${stats.todayFocusMinutes}m",
                    label2 = "Total Focus Time",
                    value2 = "${(stats.totalFocusMinutes / 60.0).let { "%.1f".format(it) }}h"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Tasks
                DualStatCard(
                    title = "Tasks",
                    icon = Icons.Default.CheckCircle,
                    accentColor = NeonPurple,
                    label1 = "Today's Tasks",
                    value1 = "${stats.todayTasksCompleted}",
                    label2 = "Total Tasks Completed",
                    value2 = "${stats.totalTasksCompleted}"
                )

                Spacer(modifier = Modifier.height(32.dp))

                TimePeriodSelector(selectedPeriod, { selectedPeriod = it })

                Spacer(modifier = Modifier.height(20.dp))

                // Select correct data for chart and insights
                val currentChartData = when (selectedPeriod) {
                    TimePeriod.WEEKLY -> weeklyData
                    TimePeriod.MONTHLY -> monthlyData
                    TimePeriod.YEARLY -> yearlyData
                    TimePeriod.LIFETIME -> lifetimeData
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = when (selectedPeriod) {
                                TimePeriod.WEEKLY -> "Last 7 Days"
                                TimePeriod.MONTHLY -> "Last 30 Days"
                                TimePeriod.YEARLY -> "Last 12 Months"
                                TimePeriod.LIFETIME -> "All Time History"
                            },
                            color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Focus sessions", color = TextGrey, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))

                        NeonBarChart(
                            data = currentChartData,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                    }
                }
            }

            if (showConfetti) {
                Box(modifier = Modifier.matchParentSize()) {
                    CelebrationEffect()
                }
            }
        }
    }
}

// --- NEW COMPONENT FOR 2-SECTION LAYOUT ---
@Composable
fun DualStatCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stat 1 (Today)
                Column(modifier = Modifier.weight(1f)) {
                    Text(label1, color = TextGrey, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(value1, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Stat 2 (Total)
                Column(modifier = Modifier.weight(1f)) {
                    Text(label2, color = TextGrey, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(value2, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EnhancedLevelCard(stats: UserStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp))
    ) {
        Box {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(
                    Brush.horizontalGradient(colors = listOf(NeonBlue.copy(alpha = 0.1f), NeonPurple.copy(alpha = 0.1f)))
                )
            )
            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                    CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color.White.copy(alpha = 0.1f), strokeWidth = 10.dp)
                    CircularProgressIndicator(progress = { stats.progress }, modifier = Modifier.fillMaxSize(), color = NeonBlue, strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LVL", color = TextGrey, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${stats.level}", color = TextWhite, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text(text = stats.title, color = NeonBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${stats.nextLevelHours} hrs to next level", color = TextGrey, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.1f))) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(stats.progress).background(Brush.horizontalGradient(colors = listOf(NeonBlue, NeonPurple))))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${(stats.progress * 100).toInt()}% Complete", color = TextGrey, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TimePeriod.values().forEach { period ->
            val isSelected = period == selectedPeriod
            Card(
                onClick = { onPeriodSelected(period) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NeonBlue else CardDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (period) {
                            TimePeriod.WEEKLY -> "Week"
                            TimePeriod.MONTHLY -> "Month"
                            TimePeriod.YEARLY -> "Year"
                            TimePeriod.LIFETIME -> "All"
                        },
                        color = if (isSelected) Color.Black else TextGrey,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun NeonBarChart(data: List<ChartDataPoint>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) { Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("No data available", color = TextGrey) }; return }
    val maxVal = data.maxOfOrNull { it.value } ?: 1; val scale = if (maxVal == 0) 1f else maxVal.toFloat()
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        data.forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                val barHeightFraction = (point.value / scale).coerceIn(0.05f, 1f)
                val animatedHeight by animateFloatAsState(targetValue = barHeightFraction, animationSpec = tween(1000), label = "barHeight")
                val barColor = if (point.isToday) NeonBlue else Color(0xFF2A2A2A)
                Box(modifier = Modifier.height(160.dp).width(20.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF0F0F0F)), contentAlignment = Alignment.BottomCenter) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(animatedHeight).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(colors = listOf(barColor, barColor.copy(alpha = 0.5f)))))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = point.label, color = if (point.isToday) NeonBlue else TextGrey, fontSize = 11.sp, fontWeight = if (point.isToday) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}