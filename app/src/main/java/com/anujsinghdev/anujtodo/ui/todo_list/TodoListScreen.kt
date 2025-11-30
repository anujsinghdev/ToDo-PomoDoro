package com.anujsinghdev.anujtodo.ui.todo_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.BottomNavItem
import com.anujsinghdev.anujtodo.ui.components.GlassBottomNavigation
import com.anujsinghdev.anujtodo.ui.util.Screen

@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val backgroundColor = Color.Black
    // Removed rememberScrollState() because LazyColumn handles its own scrolling

    // Observe Search State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(initial = TodoListSearchResults())

    // --- Sort Mode State (From Step 4) ---
    // Ensure you have added 'val isSortMode = mutableStateOf(false)' to your TodoListViewModel
    val isSortMode by viewModel.isSortMode

    // --- State for Dialogs ---
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    // --- Navigation State ---
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Focus", Icons.Filled.CenterFocusStrong, Icons.Outlined.CenterFocusStrong),
        BottomNavItem("Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    )

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            // Hide Bottom Bar when searching
            if (!isSearchActive) {
                Column {
                    // 1. Action Bar
                    BottomBarAction(
                        onNewListClick = { showCreateListDialog = true },
                        onNewGroupClick = { showCreateGroupDialog = true }
                    )

                    // 2. Glassmorphic Navigation
                    GlassBottomNavigation(
                        items = navItems,
                        selectedItem = selectedNavIndex,
                        onItemClick = { index ->
                            selectedNavIndex = index
                            // Handle Navigation Here
                            when(index) {
                                0 -> { /* Already Home */ }
                                1 -> {
                                    navController.navigate(Screen.PomodoroScreen.route)
                                }
                                2 -> { /* Navigate to Stats Screen */ }
                            }
                        }
                    )
                }
            }
        },
        // ADDED: Done Sorting Button
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
            Spacer(modifier = Modifier.height(16.dp))

            ProfileHeader(
                name = viewModel.userName.value,
                email = viewModel.userEmail.value,
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                isSearchActive = isSearchActive,
                onSearchActiveChange = viewModel::onSearchActiveChange
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                // --- NORMAL DASHBOARD VIEW (UPDATED) ---
                // Switched to LazyColumn for performance and sorting support
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Menu Items (My Day, Completed, Archive)
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
                                        Color.Blue,
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

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // 2. FOLDERS (Moved ABOVE Lists)
                    itemsIndexed(viewModel.folders) { index, folder ->
                        FolderView(
                            folder = folder,
                            onToggleExpand = { viewModel.toggleFolderExpanded(folder.id) },
                            onAddListToFolder = { name -> viewModel.addListToFolder(folder.id, name) },
                            onListClick = { list -> navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name)) },
                            // --- Sort Params ---
                            onLongClick = { viewModel.isSortMode.value = true },
                            isSortMode = isSortMode,
                            onMoveUp = { viewModel.moveFolder(index, index - 1) },
                            onMoveDown = { viewModel.moveFolder(index, index + 1) }
                        )
                    }

                    // 3. ROOT LISTS (Moved BELOW Folders)
                    itemsIndexed(viewModel.rootLists) { index, list ->
                        UserListItem(
                            list = list,
                            onClick = {
                                // Disable navigation when sorting to prevent accidental clicks
                                if (!isSortMode) {
                                    navController.navigate(Screen.ListDetailScreen.createRoute(list.id, list.name))
                                }
                            },
                            // --- Sort Params ---
                            onLongClick = { viewModel.isSortMode.value = true },
                            isSortMode = isSortMode,
                            onMoveUp = { viewModel.moveRootList(index, index - 1) },
                            onMoveDown = { viewModel.moveRootList(index, index + 1) }
                        )
                    }

                    // Spacer at the bottom
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }
    }

    // --- Dialogs ---
    if (showCreateGroupDialog) {
        CreateDialog("Create a group", "Name this group", { showCreateGroupDialog = false }, { viewModel.createFolder(it); showCreateGroupDialog = false })
    }
    if (showCreateListDialog) {
        CreateDialog("Create a list", "Name this list", { showCreateListDialog = false }, { viewModel.createList(it); showCreateListDialog = false })
    }
}