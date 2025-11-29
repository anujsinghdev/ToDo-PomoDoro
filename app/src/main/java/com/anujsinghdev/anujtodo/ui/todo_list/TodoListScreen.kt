package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.util.Screen

// --- Colors ---
val Zinc200 = Color(0xFFE4E4E7)
val Zinc700 = Color(0xFF3F3F46)
val Blue400 = Color(0xFF60A5FA)
val DialogBg = Color(0xFF1E1E1E) // Dark gray for dialog
val LoginBlue = Color(0xFF00A9E0)

// --- Colors for the Search Bar ---
val Zinc100 = Color(0xFFF4F4F5)
val Red200 = Color(0xFFFECACA)
val Red400 = Color(0xFFF87171)
val Yellow200 = Color(0xFFFEF08A)
val Yellow400 = Color(0xFFFACC15)
val Green200 = Color(0xFFBBF7D0)
val Green400 = Color(0xFF4ADE80)
val Blue200 = Color(0xFFBFDBFE)
val Transparent = Color.Transparent

@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val backgroundColor = Color.Black
    val scrollState = rememberScrollState()

    // --- State for Dialogs ---
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            BottomBarAction(
                onNewListClick = { showCreateListDialog = true },
                onNewGroupClick = { showCreateGroupDialog = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeader(
                name = viewModel.userName.value,
                email = viewModel.userEmail.value
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Menu Items ---
            MenuItem(
                icon = Icons.Outlined.WbSunny,
                title = "My Day",
                count = 0,
                iconColor = Color.Gray,
                onClick = { /* Navigate to My Day if implemented */ }
            )
            MenuItem(
                icon = Icons.Outlined.CheckCircle,
                title = "Completed",
                count = 0, // Ideally bind to viewModel.completedCount
                iconColor = Color.Gray,
                onClick = {
                    // Navigate to ListDetailScreen with special ID -2 for "Completed"
                    navController.navigate(Screen.ListDetailScreen.createRoute(-2L, "Completed"))
                }
            )
            MenuItem(
                icon = Icons.Outlined.Archive,
                title = "Archive",
                count = 0,
                iconColor = Color(0xFFF06292),
                onClick = { /* Navigate to Archive if implemented */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Divider ---
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- User Folders & Lists ---

            // 1. Root Lists
            viewModel.rootLists.forEach { list ->
                UserListItem(
                    list = list,
                    onClick = {
                        navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                    }
                )
            }

            // 2. Folders (Groups)
            viewModel.folders.forEach { folder ->
                FolderView(
                    folder = folder,
                    onToggleExpand = { viewModel.toggleFolderExpanded(folder.id) },
                    onAddListToFolder = { name -> viewModel.addListToFolder(folder.id, name) },
                    onListClick = { list ->
                        navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- Dialogs ---
    if (showCreateGroupDialog) {
        CreateDialog(
            title = "Create a Folder",
            placeholder = "Name this Folder",
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateGroupDialog = false
            }
        )
    }

    if (showCreateListDialog) {
        CreateDialog(
            title = "Create a list",
            placeholder = "Name this list",
            onDismiss = { showCreateListDialog = false },
            onCreate = { name ->
                viewModel.createList(name)
                showCreateListDialog = false
            }
        )
    }
}

// --- 1. The Custom Dialog ---
@Composable
fun CreateDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Field
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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
    // Auto-focus the text field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// --- 2. Folder View (Expandable) ---
@Composable
fun FolderView(
    folder: UiFolder,
    onToggleExpand: () -> Unit,
    onAddListToFolder: (String) -> Unit,
    onListClick: (UiTaskList) -> Unit
) {
    // Show/Hide Dialog for adding list inside folder
    var showAddListDialog by remember { mutableStateOf(false) }

    Column {
        // Folder Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder, // Folder Icon
                contentDescription = "Folder",
                tint = Color.Gray,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Count
            if (folder.lists.isNotEmpty()) {
                Text(
                    text = folder.lists.size.toString(),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Arrow Animation
            val rotation by animateFloatAsState(if (folder.isExpanded) 180f else 0f, label = "arrow")
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.Gray,
                modifier = Modifier.rotate(rotation)
            )
        }

        // Expanded Content
        AnimatedVisibility(
            visible = folder.isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 24.dp)) { // Indent contents
                // Lists inside folder
                folder.lists.forEach { list ->
                    UserListItem(
                        list = list,
                        isNested = true,
                        onClick = { onListClick(list) }
                    )
                }

                // "New List" button inside folder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddListDialog = true }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("New list", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
    }

    if (showAddListDialog) {
        CreateDialog(
            title = "Create list in ${folder.name}",
            placeholder = "List name",
            onDismiss = { showAddListDialog = false },
            onCreate = { name ->
                onAddListToFolder(name)
                showAddListDialog = false
            }
        )
    }
}

// --- 3. Simple List Item ---
@Composable
fun UserListItem(
    list: UiTaskList,
    isNested: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = if (isNested) 16.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Corrected Icon Usage: Using AutoMirrored List Icon
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.List,
            contentDescription = "List",
            tint = LoginBlue, // Blue color for user lists
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
            Text(
                text = list.count.toString(),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

// --- Components ---
@Composable
fun GlowSearchBox() {
    var text by remember { mutableStateOf("") }

    // Animation for rotation
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing,
            )
        ),
        label = "rotation"
    )

    // Dynamic Gradient Brush
    val borderBrush by remember {
        derivedStateOf {
            sweepGradient(
                colors = listOf(
                    Transparent,
                    Transparent,
                    Red200,
                    Yellow200,
                    Green200,
                    Blue200,
                    Transparent,
                    Transparent,
                ),
                rotation = rotation,
            )
        }
    }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        cursorBrush = SolidColor(Zinc200),
        textStyle = TextStyle(
            color = Zinc200,
            fontSize = 16.sp
        ),
        decorationBox = { field ->
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Blue400,
                        ambientColor = Blue400
                    )
                    .background(
                        color = Zinc700,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        brush = borderBrush,
                        shape = CircleShape,
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Zinc200,
                )
                Spacer(Modifier.width(12.dp))

                Box(contentAlignment = Alignment.CenterStart) {
                    if (text.isEmpty()) {
                        Text("Search", color = Zinc200.copy(alpha = 0.5f))
                    }
                    field()
                }
            }
        }
    )
}

@Composable
fun ProfileHeader(name: String, email: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(0.35f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = email,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(0.65f)) {
            GlowSearchBox()
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    count: Int = 0,
    iconColor: Color,
    onClick: () -> Unit = {} // Added onClick parameter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onClick() }, // Used onClick here
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BottomBarAction(
    onNewListClick: () -> Unit,
    onNewGroupClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // New List Action
        Row(
            modifier = Modifier
                .clickable { onNewListClick() }
                .padding(8.dp),
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

        // New Group Action
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
fun sweepGradient(
    colors: List<Color>,
    center: Offset = Offset.Unspecified,
    rotation: Float = 0f,
): Brush {
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

    return Brush.sweepGradient(
        colorStops = rotatedStops.toTypedArray(),
        center = center,
    )
}