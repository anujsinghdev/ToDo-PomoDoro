package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.AnimatedDialog
import com.anujsinghdev.anujtodo.ui.components.BottomNavItem
import com.anujsinghdev.anujtodo.ui.components.GlassBottomNavigation
import com.anujsinghdev.anujtodo.ui.util.Screen

// Dark Theme Colors for Dialog
val DarkDialogBg = Color(0xFF1A1A1A)
val DarkerGrey = Color(0xFF0F0F0F)
val LightGrey = Color(0xFF404040)
val TextWhite = Color.White
val TextGrey = Color(0xFFB0B0B0)
val SkyBlue = Color(0xFF87CEEB)

@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val backgroundColor = Color.Black

    // Observe Search State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(initial = TodoListSearchResults())

    // Sort Mode State
    val isSortMode by viewModel.isSortMode

    // State for Dialogs
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var selectedFolderForNewList by remember { mutableStateOf<Long?>(null) }

    // Navigation State
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Focus", Icons.Filled.CenterFocusStrong, Icons.Outlined.CenterFocusStrong),
        BottomNavItem("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            if (!isSearchActive) {
                Column {
                    BottomBarAction(
                        onNewListClick = { showCreateListDialog = true },
                        onNewGroupClick = { showCreateGroupDialog = true }
                    )

                    GlassBottomNavigation(
                        items = navItems,
                        selectedItem = selectedNavIndex,
                        onItemClick = { index ->
                            selectedNavIndex = index
                            when(index) {
                                0 -> { /* Already Home */ }
                                1 -> {
                                    navController.navigate(Screen.PomodoroScreen.route)
                                }
                                2 -> {
                                    navController.navigate(Screen.StatsScreen.route)
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (isSortMode) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.isSortMode.value = false },
                    containerColor = LoginBlue,
                    contentColor = Color.White
                ) {
                    Text("Done Sorting")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            // Modified ProfileHeader to remove email and use "Hi, Name"
            ProfileHeader(
                name = viewModel.userName.value,
                // Pass empty string or remove parameter if you updated ProfileHeader signature
                email = "",
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                isSearchActive = isSearchActive,
                onSearchActiveChange = viewModel::onSearchActiveChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearchActive) {
                SearchResultsList(
                    lists = searchResults.lists,
                    tasks = searchResults.tasks,
                    onListClick = { list ->
                        navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                    },
                    onTaskClick = { task ->
                        val targetListId = task.listId ?: -1L
                        navController.navigate(Screen.ListDetailScreen.createRoute(targetListId, "Task List"))
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    VerticalMenuItem(
                                        Icons.Outlined.WbSunny,
                                        "My Day",
                                        SkyBlue,
                                        onClick = { navController.navigate(Screen.MyDayScreen.route) }) {}
                                }
                                VerticalDivider(modifier = Modifier.height(40.dp), color = Zinc700, thickness = 1.dp)
                                Box(modifier = Modifier.weight(1f)) {
                                    VerticalMenuItem(
                                        Icons.Outlined.CheckCircle, "Completed", Color.Green,
                                        onClick = { navController.navigate(Screen.ListDetailScreen.createRoute(-2L, "Completed")) }
                                    ) {}
                                }
                                VerticalDivider(modifier = Modifier.height(40.dp), color = Zinc700, thickness = 1.dp)
                                Box(modifier = Modifier.weight(1f)) {
                                    VerticalMenuItem(Icons.Outlined.Archive, "Archive", Color(0xFFF06292),
                                        onClick = { navController.navigate(Screen.ArchiveScreen.route) }) {}
                                }
                            }

                            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                        }
                    }

                    itemsIndexed(viewModel.folders) { index, folder ->
                        FolderView(
                            folder = folder,
                            onToggleExpand = { viewModel.toggleFolderExpanded(folder.id) },
                            onAddListToFolder = {
                                selectedFolderForNewList = folder.id
                            },
                            onListClick = { list -> navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name)) },
                            onLongClick = { viewModel.isSortMode.value = true },
                            isSortMode = isSortMode,
                            onMoveUp = { viewModel.moveFolder(index, index - 1) },
                            onMoveDown = { viewModel.moveFolder(index, index + 1) }
                        )
                    }

                    itemsIndexed(viewModel.rootLists) { index, list ->
                        UserListItem(
                            list = list,
                            onClick = {
                                if (!isSortMode) {
                                    navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                                }
                            },
                            onLongClick = { viewModel.isSortMode.value = true },
                            isSortMode = isSortMode,
                            onMoveUp = { viewModel.moveRootList(index, index - 1) },
                            onMoveDown = { viewModel.moveRootList(index, index + 1) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }
            }
        }
    }

    // Modern Animated Dialogs (Group, List, Folder Add)
    if (showCreateGroupDialog) {
        ModernCreateDialog(
            title = "Create Group",
            subtitle = "Organize your lists into a folder",
            icon = Icons.Default.FolderOpen,
            iconColor = Color(0xFFFFA726),
            placeholder = "Group name",
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showCreateGroupDialog = false
            }
        )
    }

    if (showCreateListDialog) {
        ModernCreateDialog(
            title = "Create List",
            subtitle = "Add a new task list",
            icon = Icons.Default.List,
            iconColor = LoginBlue,
            placeholder = "List name",
            onDismiss = { showCreateListDialog = false },
            onConfirm = { name ->
                viewModel.createList(name)
                showCreateListDialog = false
            }
        )
    }

    selectedFolderForNewList?.let { folderId ->
        ModernCreateDialog(
            title = "Add List to Folder",
            subtitle = "Create a new list in this folder",
            icon = Icons.Default.List,
            iconColor = LoginBlue,
            placeholder = "List name",
            onDismiss = { selectedFolderForNewList = null },
            onConfirm = { name ->
                viewModel.addListToFolder(folderId, name)
                selectedFolderForNewList = null
            }
        )
    }
}

// ==================== MODERN CREATE DIALOG ====================

@Composable
fun ModernCreateDialog(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AnimatedDialog(onDismissRequest = onDismiss) { triggerDismiss ->
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkDialogBg),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .width(360.dp)
                .padding(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = BorderStroke(1.dp, LightGrey.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = { triggerDismiss() },
                        shape = CircleShape,
                        color = LightGrey.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextGrey,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Icon with Gradient Background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = 0.2f),
                                    iconColor.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Input Field with Modern Design
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            placeholder,
                            color = TextGrey,
                            fontSize = 15.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkerGrey,
                        unfocusedContainerColor = DarkerGrey,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = iconColor,
                        focusedBorderColor = iconColor,
                        unfocusedBorderColor = LightGrey.copy(alpha = 0.3f),
                        focusedPlaceholderColor = TextGrey,
                        unfocusedPlaceholderColor = TextGrey
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = { triggerDismiss() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextGrey,
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.5.dp, LightGrey.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    // Create Button
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onConfirm(text)
                                triggerDismiss()
                            }
                        },
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iconColor,
                            contentColor = Color.White,
                            disabledContainerColor = LightGrey.copy(alpha = 0.2f),
                            disabledContentColor = TextGrey.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp,
                            disabledElevation = 0.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text(
                            "Create",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
