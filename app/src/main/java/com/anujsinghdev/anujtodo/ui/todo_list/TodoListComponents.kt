package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.model.TodoList
import com.anujsinghdev.anujtodo.ui.components.MacosFolderIcon

// --- Colors ---
val Zinc200 = Color(0xFFE4E4E7)
val Zinc700 = Color(0xFF3F3F46)
val Blue400 = Color(0xFF60A5FA)
val DialogBg = Color(0xFF1E1E1E)
val LoginBlue = Color(0xFF00A9E0)

// --- Search Bar Colors ---
val Red200 = Color(0xFFFECACA)
val Yellow200 = Color(0xFFFEF08A)
val Green200 = Color(0xFFBBF7D0)
val Blue200 = Color(0xFFBFDBFE)
val Transparent = Color.Transparent

@Composable
fun ProfileHeader(
    name: String,
    email: String, // Kept for compatibility, unused in UI
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hide Profile Info when searching to allow expansion
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            // UPDATED: Only shows "Hi, Name"
            Row(
                modifier = Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hi, $name",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Search Box takes remaining space (fills width when profile is hidden)
        Box(modifier = Modifier.weight(1f)) {
            GlowSearchBox(
                query = query,
                onQueryChange = onQueryChange,
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange
            )
        }
    }
}

@Composable
fun GlowSearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Handle Back Press to close search
    BackHandler(enabled = isSearchActive) {
        onSearchActiveChange(false)
        focusManager.clearFocus()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // Animate border only when active/focused
    val borderBrush = if (isSearchActive) {
        Brush.sweepGradient(
            colors = listOf(Transparent, Transparent, Red200, Yellow200, Green200, Blue200, Transparent, Transparent),
            center = Offset.Unspecified
        )
    } else {
        SolidColor(Color.DarkGray)
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onSearchActiveChange(true) },
        cursorBrush = SolidColor(Zinc200),
        textStyle = TextStyle(color = Zinc200, fontSize = 16.sp),
        decorationBox = { field ->
            Row(
                modifier = Modifier
                    .shadow(elevation = if(isSearchActive) 8.dp else 0.dp, shape = CircleShape, spotColor = Blue400, ambientColor = Blue400)
                    .background(color = Zinc700, shape = CircleShape)
                    .border(
                        width = if(isSearchActive) 2.dp else 1.dp,
                        brush = if(isSearchActive)
                            sweepGradient(listOf(Transparent, Red200, Yellow200, Green200, Blue200, Transparent), rotation = rotation)
                        else SolidColor(Color.Transparent),
                        shape = CircleShape
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon changes based on state
                if (isSearchActive) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Zinc200,
                        modifier = Modifier.clickable {
                            onSearchActiveChange(false)
                            focusManager.clearFocus()
                        }
                    )
                } else {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = Zinc200)
                }

                Spacer(Modifier.width(12.dp))
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text("Search", color = Zinc200.copy(alpha = 0.5f))
                    }
                    field()
                }
            }
        }
    )
}

@Composable
fun SearchResultsList(
    lists: List<TodoList>,
    tasks: List<TodoItem>,
    onListClick: (TodoList) -> Unit,
    onTaskClick: (TodoItem) -> Unit
) {
    if (lists.isEmpty() && tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            Text("No results found", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (lists.isNotEmpty()) {
                Text("Lists", color = LoginBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                lists.forEach { list ->
                    UserListItem(
                        list = UiTaskList(id = list.id, name = list.name),
                        onClick = { onListClick(list) }
                    )
                }
            }

            if (tasks.isNotEmpty()) {
                Text("Tasks", color = LoginBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskClick(task) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if(task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if(task.isCompleted) LoginBlue else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(task.title, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- Reused Components ---

@Composable
fun VerticalMenuItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit,
    function: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListItem(
    list: UiTaskList,
    isNested: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isSortMode: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp, horizontal = if (isNested) 16.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Sorting Arrows ---
        if (isSortMode) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, null, tint = LoginBlue, modifier = Modifier.size(24.dp).clickable { onMoveUp() })
                Icon(Icons.Default.KeyboardArrowDown, null, tint = LoginBlue, modifier = Modifier.size(24.dp).clickable { onMoveDown() })
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.List,
            contentDescription = "List",
            tint = LoginBlue,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = list.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (list.count > 0) {
            Text(text = list.count.toString(), color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderView(
    folder: UiFolder,
    onToggleExpand: () -> Unit,
    onAddListToFolder: () -> Unit,  // ✅ No parameter - triggers parent dialog
    onListClick: (UiTaskList) -> Unit,
    onLongClick: () -> Unit = {},
    isSortMode: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onToggleExpand,
                    onLongClick = onLongClick
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Sorting Arrows ---
            if (isSortMode) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = LoginBlue, modifier = Modifier.size(24.dp).clickable { onMoveUp() })
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = LoginBlue, modifier = Modifier.size(24.dp).clickable { onMoveDown() })
                }
            }

            MacosFolderIcon(modifier = Modifier.size(28.dp))

            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = folder.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            if (folder.lists.isNotEmpty()) {
                Text(text = folder.lists.size.toString(), color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            val rotation by animateFloatAsState(if (folder.isExpanded) 180f else 0f, label = "arrow")
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.Gray,
                modifier = Modifier.rotate(rotation)
            )
        }

        AnimatedVisibility(
            visible = folder.isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 24.dp)) {
                folder.lists.forEach { list ->
                    UserListItem(list = list, isNested = true, onClick = { onListClick(list) })
                }

                // ✅ FIXED: Triggers modern dialog in parent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddListToFolder() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("New list", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CreateDialog(title: String, placeholder: String, onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder, color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = LoginBlue,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = LoginBlue
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { if (text.isNotBlank()) onCreate(text) },
                        enabled = text.isNotBlank()
                    ) {
                        Text(
                            text = title.uppercase().replace("A ", ""),
                            color = if (text.isNotBlank()) LoginBlue else Color.DarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun BottomBarAction(onNewListClick: () -> Unit, onNewGroupClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { onNewListClick() }.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New List",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "New list",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onNewGroupClick) {
            Icon(
                imageVector = Icons.Outlined.CreateNewFolder,
                contentDescription = "New Group",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Stable
fun sweepGradient(colors: List<Color>, center: Offset = Offset.Unspecified, rotation: Float = 0f): Brush {
    require(colors.size >= 2) { "At least 2 colors are required" }
    val rotationFraction = (-rotation / 360f) % 1f
    val normalizedRotation = if (rotationFraction < 0) rotationFraction + 1f else rotationFraction
    val step = 1f / colors.size
    val rotatedStops = mutableListOf<Pair<Float, Color>>()
    for (i in colors.indices) {
        val originalStop = i * step
        val rotatedStop = (originalStop + normalizedRotation) % 1f
        rotatedStops.add(rotatedStop to colors[i])
    }
    rotatedStops.sortBy { it.first }
    val firstStop = rotatedStops.first().first
    val lastStop = rotatedStops.last().first
    if (firstStop > 0.0001f) {
        val colorBeforeWrap = rotatedStops.last().second
        val colorAfterWrap = rotatedStops.first().second
        val gapSize = (1f - lastStop) + firstStop
        val fractionAt0 = (1f - lastStop) / gapSize
        val interpolatedColor = lerp(colorBeforeWrap, colorAfterWrap, fractionAt0)
        rotatedStops.add(0, 0f to interpolatedColor)
        rotatedStops.add(1f to interpolatedColor)
    } else {
        val lastColor = rotatedStops.last().second
        rotatedStops.add(1f to lastColor)
    }
    return Brush.sweepGradient(colorStops = rotatedStops.toTypedArray(), center = center)
}
