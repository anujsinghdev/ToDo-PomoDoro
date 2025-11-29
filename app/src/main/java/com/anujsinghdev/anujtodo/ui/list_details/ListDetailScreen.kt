package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants
val PinkAccent = Color(0xFF0091EA)
val BackgroundColor = Color.Black
val SurfaceColor = Color(0xFF1E1E1E)
val TextSecondary = Color.Gray

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    listId: Long,
    listName: String,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val tasks by viewModel.getTasksForList(listId).collectAsState(initial = emptyList())

    // Split tasks into Active and Completed
    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }

    // States
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var selectedTaskToEdit by remember { mutableStateOf<TodoItem?>(null) }
    var isCompletedExpanded by remember { mutableStateOf(true) } // Default expanded

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(listName, color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PinkAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
            // Only show Add button if NOT in the special "Completed" view (optional preference)
            if (listId != SMART_LIST_COMPLETED_ID) {
                FloatingActionButton(
                    onClick = { showAddTaskSheet = true },
                    containerColor = PinkAccent,
                    shape = CircleShape,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Task")
                }
            }
        }
    ) { padding ->
        if (tasks.isEmpty() && listId != SMART_LIST_COMPLETED_ID) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView(listName)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Active Tasks
                items(items = activeTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                        TaskItemView(
                            todo = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onClick = { selectedTaskToEdit = task }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 2. Completed Header (Only if there are completed tasks)
                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CompletedHeader(
                            count = completedTasks.size,
                            isExpanded = isCompletedExpanded,
                            onClick = { isCompletedExpanded = !isCompletedExpanded }
                        )
                    }
                }

                // 3. Completed Tasks (Collapsible)
                if (isCompletedExpanded) {
                    items(items = completedTasks, key = { it.id }) { task ->
                        Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                            TaskItemView(
                                todo = task,
                                onToggle = { viewModel.toggleTask(task) },
                                onClick = { selectedTaskToEdit = task }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Add bottom spacing for FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- Popups ---
    if (showAddTaskSheet) {
        TaskInputBottomSheet(
            title = "Add Task",
            initialText = "",
            initialDate = null,
            initialRepeat = RepeatMode.NONE,
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, date, repeat ->
                viewModel.addTask(title, date, repeat, listId)
                showAddTaskSheet = false
            }
        )
    }

    if (selectedTaskToEdit != null) {
        TaskInputBottomSheet(
            title = "Edit Task",
            initialText = selectedTaskToEdit!!.title,
            initialDate = selectedTaskToEdit!!.dueDate,
            initialRepeat = selectedTaskToEdit!!.repeatMode,
            onDismiss = { selectedTaskToEdit = null },
            onSave = { title, date, repeat ->
                viewModel.updateTask(selectedTaskToEdit!!.copy(
                    title = title,
                    dueDate = date,
                    repeatMode = repeat
                ))
                selectedTaskToEdit = null
            }
        )
    }
}

// --- Components ---

@Composable
fun CompletedHeader(
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "Arrow")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier
                .rotate(rotation)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Completed $count",
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaskItemView(
    todo: TodoItem,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val textColor = if (todo.isCompleted) Color.Gray else Color.White
    val textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    // Background Card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF252525))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle Button
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = "Toggle",
                tint = if (todo.isCompleted) PinkAccent else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.title,
                color = textColor,
                fontSize = 16.sp,
                textDecoration = textDecoration
            )
            // Optional: If you want to show list name for the "All Completed" screen,
            // you'd need the list name passed in or fetched via relation.
            // For now, displaying Due Date/Repeat info:
            if (todo.dueDate != null || todo.repeatMode != RepeatMode.NONE) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    if (todo.dueDate != null) {
                        Text(
                            text = formatDate(todo.dueDate),
                            color = if (todo.dueDate < System.currentTimeMillis() && !todo.isCompleted) Color.Red else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    if (todo.repeatMode != RepeatMode.NONE) {
                        if (todo.dueDate != null) Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Repeat, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Star Icon Removed as per request
    }
}

// --- Inputs & Helpers (Same as before) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInputBottomSheet(
    title: String,
    initialText: String,
    initialDate: Long?,
    initialRepeat: RepeatMode,
    onDismiss: () -> Unit,
    onSave: (String, Long?, RepeatMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(initialText) }
    var date by remember { mutableStateOf(initialDate) }
    var repeat by remember { mutableStateOf(initialRepeat) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp).imePadding()) {
            Text(title, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("What needs to be done?", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PinkAccent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { if (text.isNotBlank()) onSave(text, date, repeat) },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.background(if (text.isNotBlank()) PinkAccent else Color.DarkGray, RoundedCornerShape(8.dp)).size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                ActionChip(Icons.Default.CalendarMonth, if (date != null) formatDate(date!!) else "Set due date", date != null) { showDatePicker = true }
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    ActionChip(Icons.Default.Repeat, if (repeat != RepeatMode.NONE) repeat.name.lowercase().capitalize() else "Repeat", repeat != RepeatMode.NONE) { showRepeatMenu = true }
                    DropdownMenu(expanded = showRepeatMenu, onDismissRequest = { showRepeatMenu = false }, containerColor = Color(0xFF2C2C2C)) {
                        RepeatMode.values().forEach { mode ->
                            DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White) }, onClick = { repeat = mode; showRepeatMenu = false })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { date = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK", color = PinkAccent) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = PinkAccent) } },
            colors = DatePickerDefaults.colors(containerColor = SurfaceColor)
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(headlineContentColor = Color.White, titleContentColor = Color.White, weekdayContentColor = Color.White, dayContentColor = Color.White, selectedDayContainerColor = PinkAccent, todayDateBorderColor = PinkAccent, yearContentColor = Color.White, currentYearContentColor = Color.White, selectedYearContainerColor = PinkAccent))
        }
    }
}

@Composable
fun ActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(8.dp), color = if (isActive) PinkAccent.copy(alpha = 0.2f) else Color.Transparent, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isActive) PinkAccent else Color.Gray, modifier = Modifier.size(18.dp))
            if (isActive || label != "Set due date") { Spacer(modifier = Modifier.width(8.dp)); Text(label, color = if (isActive) PinkAccent else Color.Gray, fontSize = 14.sp) }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun EmptyStateView(listName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No tasks in $listName", color = Color.Gray)
    }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }