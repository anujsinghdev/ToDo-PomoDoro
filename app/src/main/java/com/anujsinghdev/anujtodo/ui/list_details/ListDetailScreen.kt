package com.anujsinghdev.anujtodo.ui.list_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.ui.components.AnimatedDialog // <--- Import this
import com.anujsinghdev.anujtodo.ui.todo_list.DialogBg
import com.anujsinghdev.anujtodo.ui.todo_list.LoginBlue
import kotlinx.coroutines.launch
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
    // ... (Keep existing state variables) ...
    val tasks by viewModel.getTasksForList(listId).collectAsState(initial = emptyList())
    val realListName by viewModel.getListNameFlow(listId, listName).collectAsState(initial = listName)
    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var selectedTaskToEdit by remember { mutableStateOf<TodoItem?>(null) }
    var isCompletedExpanded by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentSortOption by viewModel.currentSortOption.collectAsState()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(realListName, color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PinkAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = PinkAccent)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = SurfaceColor,
                        modifier = Modifier.width(220.dp)
                    ) {
                        // ... (Keep existing menu items) ...
                        DropdownMenuItem(
                            text = { Text("Rename list", color = Color.White) },
                            onClick = { showMenu = false; showRenameDialog = true },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = Color.White) },
                            enabled = listId != SMART_LIST_COMPLETED_ID
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by", color = Color.White) },
                            onClick = { showMenu = false; showSortDialog = true },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Sort, null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate list", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.duplicateList(listId, realListName)
                            },
                            leadingIcon = { Icon(Icons.Outlined.FileCopy, null, tint = Color.White) },
                            enabled = listId != SMART_LIST_COMPLETED_ID
                        )
                        DropdownMenuItem(
                            text = { Text("Archive list", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.archiveList(listId)
                                navController.popBackStack()
                            },
                            leadingIcon = { Icon(Icons.Outlined.Archive, null, tint = Color.White) },
                            enabled = listId != SMART_LIST_COMPLETED_ID
                        )

                        DropdownMenuItem(
                            text = { Text("Delete list", color = Color.Red) },
                            onClick = { showMenu = false; showDeleteDialog = true },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Color.Red) },
                            enabled = listId != SMART_LIST_COMPLETED_ID
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
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
        Box(modifier = Modifier.padding(padding)) {
            // ... (Keep existing content logic: GroupedCompletedList, LazyColumn, etc.) ...
            if (listId == SMART_LIST_COMPLETED_ID) {
                val groupedTasks by viewModel.groupedCompletedTasks.collectAsState(initial = emptyMap())
                if (groupedTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateView("Completed")
                    }
                } else {
                    GroupedCompletedList(
                        groupedTasks = groupedTasks,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onFlagTask = { viewModel.toggleFlag(it) },
                        onEditTask = { selectedTaskToEdit = it }
                    )
                }
            } else {
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateView(realListName)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .rubberBandEffect()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(items = activeTasks, key = { it.id }) { task ->
                            Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                                TaskItemView(
                                    todo = task,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onFlag = { viewModel.toggleFlag(task) },
                                    onClick = { selectedTaskToEdit = task }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                CompletedHeader(
                                    title = "Completed",
                                    count = completedTasks.size,
                                    isExpanded = isCompletedExpanded,
                                    onClick = { isCompletedExpanded = !isCompletedExpanded }
                                )
                            }
                            if (isCompletedExpanded) {
                                items(items = completedTasks, key = { it.id }) { task ->
                                    Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                                        TaskItemView(
                                            todo = task,
                                            onToggle = { viewModel.toggleTask(task) },
                                            onFlag = { viewModel.toggleFlag(task) },
                                            onClick = { selectedTaskToEdit = task }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Rename Dialog (Keep as is)
    if (showRenameDialog) {
        ListNameDialog(
            title = "Rename list",
            initialName = realListName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameList(listId, newName)
                showRenameDialog = false
            }
        )
    }

    // 2. Sort Dialog (Keep as is)
    if (showSortDialog) {
        // ... (Keep existing sort dialog code) ...
        Dialog(onDismissRequest = { showSortDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DialogBg),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text("Sort by", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    val options = listOf(
                        SortOption.IMPORTANCE to "Importance",
                        SortOption.DUE_DATE to "Due date",
                        SortOption.ALPHABETICAL to "Alphabetically",
                        SortOption.CREATION_DATE to "Creation date"
                    )
                    options.forEach { (option, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSortOption(option)
                                    showSortDialog = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if(option == SortOption.IMPORTANCE) Icons.Default.StarBorder else if(option == SortOption.DUE_DATE) Icons.Default.CalendarMonth else if(option == SortOption.ALPHABETICAL) Icons.AutoMirrored.Outlined.Sort else Icons.Default.Add,
                                contentDescription = null,
                                tint = if(option == currentSortOption) PinkAccent else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, color = if(option == currentSortOption) PinkAccent else Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            if (option == currentSortOption) {
                                Icon(Icons.Default.CheckCircle, null, tint = PinkAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- REPLACED WITH ANIMATED DIALOG ---
    if (showDeleteDialog) {
        AnimatedDialog(
            onDismissRequest = { showDeleteDialog = false }
        ) { triggerDismiss ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Delete List?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to delete \"$realListName\"? This action cannot be undone.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        Button(
                            onClick = { triggerDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C2C2C),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        // Delete Button
                        Button(
                            onClick = {
                                triggerDismiss() // Animate out first
                                viewModel.deleteList(listId) {
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Delete", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // --- Task Popups (Keep as is) ---
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

// ... (Keep existing Helper Composables like TaskItemView, etc.) ...
@Composable
fun GroupedCompletedList(
    groupedTasks: Map<String, List<TodoItem>>,
    onToggleTask: (TodoItem) -> Unit,
    onFlagTask: (TodoItem) -> Unit,
    onEditTask: (TodoItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .rubberBandEffect()
            .padding(horizontal = 16.dp)
    ) {
        groupedTasks.forEach { (listName, tasks) ->
            item {
                var isExpanded by remember { mutableStateOf(true) }
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    CompletedHeader(title = listName, count = tasks.size, isExpanded = isExpanded, onClick = { isExpanded = !isExpanded })
                    if (isExpanded) {
                        tasks.forEach { task ->
                            TaskItemView(todo = task, onToggle = { onToggleTask(task) }, onFlag = { onFlagTask(task) }, onClick = { onEditTask(task) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CompletedHeader(title: String, count: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "Arrow")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.rotate(rotation).size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$title $count", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TaskItemView(todo: TodoItem, onToggle: () -> Unit, onFlag: () -> Unit, onClick: () -> Unit) {
    val textColor = if (todo.isCompleted) Color.Gray else Color.White
    val textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF252525)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
            Icon(if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle, "Toggle", tint = if (todo.isCompleted) PinkAccent else Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = todo.title, color = textColor, fontSize = 16.sp, textDecoration = textDecoration)
            if (todo.dueDate != null || todo.repeatMode != RepeatMode.NONE) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    if (todo.dueDate != null) Text(formatDate(todo.dueDate), color = if (todo.dueDate < System.currentTimeMillis() && !todo.isCompleted) Color.Red else Color.Gray, fontSize = 12.sp)
                    if (todo.repeatMode != RepeatMode.NONE) {
                        if (todo.dueDate != null) Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Repeat, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
        IconButton(onClick = onFlag, modifier = Modifier.size(24.dp)) {
            Icon(if (todo.isFlagged) Icons.Default.Star else Icons.Outlined.StarBorder, "Flag", tint = if (todo.isFlagged) Color.White else Color.Gray)
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
fun ListNameDialog(title: String, initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = DialogBg), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = text, onValueChange = { text = it }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = PinkAccent, focusedIndicatorColor = PinkAccent, unfocusedIndicatorColor = Color.Gray), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
                    TextButton(onClick = { if(text.isNotBlank()) onConfirm(text) }, enabled = text.isNotBlank()) { Text("SAVE", color = if(text.isNotBlank()) PinkAccent else Color.Gray) }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(listName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No tasks in $listName", color = Color.Gray)
    }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceColor, dragHandle = null) {
        Column(modifier = Modifier.padding(16.dp).imePadding()) {
            Text(title, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(value = text, onValueChange = { text = it }, placeholder = { Text("What needs to be done?", color = Color.Gray) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = PinkAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.weight(1f))
                IconButton(onClick = { if (text.isNotBlank()) onSave(text, date, repeat) }, enabled = text.isNotBlank(), modifier = Modifier.background(if (text.isNotBlank()) PinkAccent else Color.DarkGray, RoundedCornerShape(8.dp)).size(36.dp)) { Icon(Icons.Default.ArrowUpward, null, tint = Color.Black) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                ActionChip(Icons.Default.CalendarMonth, if (date != null) formatDate(date!!) else "Set due date", date != null) { showDatePicker = true }
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    ActionChip(Icons.Default.Repeat, if (repeat != RepeatMode.NONE) repeat.name.lowercase().capitalize() else "Repeat", repeat != RepeatMode.NONE) { showRepeatMenu = true }
                    DropdownMenu(expanded = showRepeatMenu, onDismissRequest = { showRepeatMenu = false }, containerColor = Color(0xFF2C2C2C)) { RepeatMode.values().forEach { mode -> DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White) }, onClick = { repeat = mode; showRepeatMenu = false }) } }
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

// --- OVERSCROLL ANIMATION LOGIC ---
fun Modifier.rubberBandEffect(): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If overscrolled and scrolling back, consume
                val delta = available.y
                if (offsetY.value != 0f) {
                    val newOffset = offsetY.value + delta
                    if ((offsetY.value > 0 && newOffset < 0) || (offsetY.value < 0 && newOffset > 0)) {
                        val consumed = -offsetY.value
                        scope.launch { offsetY.snapTo(0f) }
                        return Offset(0f, consumed)
                    } else {
                        scope.launch { offsetY.snapTo(newOffset) }
                        return Offset(0f, delta)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // If drag hit bounds, apply resistance
                if (source == NestedScrollSource.Drag && available.y != 0f) {
                    val delta = available.y * 0.3f // Damping
                    scope.launch { offsetY.snapTo(offsetY.value + delta) }
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY.value != 0f) {
                    offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (offsetY.value != 0f) {
                    offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    this
        .nestedScroll(connection)
        .graphicsLayer {
            translationY = offsetY.value
        }
}