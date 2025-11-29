package com.anujsinghdev.anujtodo.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.anujsinghdev.anujtodo.ui.components.MeshGradientButton
import com.anujsinghdev.anujtodo.ui.util.Screen

// Custom Color
val LoginBlue = Color(0xFF00A9E0)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.loginSuccess.value) {
        if (viewModel.loginSuccess.value) {
            navController.navigate(Screen.TodoListScreen.route) {
                popUpTo(Screen.LoginScreen.route) { inclusive = true }
            }
        }
    }

    // 1. Beautiful Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A), // Dark Grey
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center // Centers the Column vertically & horizontally
    ) {
        // 2. Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Logo",
                tint = LoginBlue,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // -- 1. Name Field --
            AnimatedInput(
                text = viewModel.name.value,
                onTextChange = { viewModel.name.value = it },
                label = "Name",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -- 2. Email Field --
            AnimatedInput(
                text = viewModel.email.value,
                onTextChange = { viewModel.email.value = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -- 3. Password Field --
            AnimatedInput(
                text = viewModel.password.value,
                onTextChange = { viewModel.password.value = it },
                label = "Password",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(50.dp))

            // --- REPLACED BUTTON HERE ---
            MeshGradientButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.onLoginClick()
                }
            )
        }
    }
}

/**
 * Custom Animated Text Field.
 * The label floats up and scales down when focused or text is present.
 */
@Composable
fun AnimatedInput(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Animation States
    val isLabelUp = isFocused || text.isNotEmpty()

    // Animate font size scale (1f -> 0.85f)
    val labelScale by animateFloatAsState(
        targetValue = if (isLabelUp) 0.85f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "LabelScale"
    )

    // Animate vertical offset
    val labelOffset by animateDpAsState(
        targetValue = if (isLabelUp) (-24).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "LabelOffset"
    )

    // Animate Color
    val labelColor by animateColorAsState(
        targetValue = if (isFocused) LoginBlue else Color.Gray,
        label = "LabelColor"
    )

    val borderColor = if (isFocused) LoginBlue else Color.DarkGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // The Input Field
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading Icon
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isFocused) LoginBlue else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        // The Floating Label
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .offset(y = labelOffset)
                                .graphicsLayer {
                                    scaleX = labelScale
                                    scaleY = labelScale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                }
                        )

                        // Render the actual text field only when label is up or it's focused
                        if (isLabelUp) {
                            Box(modifier = Modifier.padding(top = 8.dp)) {
                                innerTextField()
                            }
                        }
                    }

                    // Trailing Icon for Password
                    if (isPassword) {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        )
    }
}