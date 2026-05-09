package com.example.filltracking2.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filltracking2.R
import com.example.filltracking2.ui.state.LoginUiState
import com.example.filltracking2.ui.theme.*
import com.example.filltracking2.ui.viewmodel.LoginViewModel
import com.example.filltracking2.util.PreferenceManager
import com.example.filltracking2.util.LocaleManager
import kotlinx.coroutines.launch

val MoroccoGold = Color(0xFFD4AF37) // Premium Metallic Gold
val MoroccoGoldLight = Color(0xFFF9E4B7) // Light/Champagne Gold

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToFaq: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LoginScreenContent(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onRememberMeChange = viewModel::onRememberMeChange,
        onLoginClick = { viewModel.login(context, onLoginSuccess) },
        onResetPassword = { pin, onSuccess, onError -> 
            viewModel.resetPassword(context, pin, onSuccess, onError)
        },
        onNavigateToFaq = onNavigateToFaq
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onResetPassword: (String, () -> Unit, (String) -> Unit) -> Unit,
    onNavigateToFaq: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetPin by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    var resetSuccess by remember { mutableStateOf(false) }

    val currentLocale = LocaleManager.LocalAppLocale.current
    val languageMap = mapOf(
        "en" to "English",
        "fr" to "Français",
        "ar" to "العربية",
        "de" to "Deutsch",
        "es" to "Español"
    )

    // Theme Switch Animation
    val isDark = ThemeManager.isDarkTheme
    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) DarkBg else MoroccoSurface,
        animationSpec = tween(durationMillis = 500)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative Top Edge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(modifier = Modifier.fillMaxHeight().weight(2f).background(MoroccoPrimaryContainer))
                Box(modifier = Modifier.fillMaxHeight().weight(1f).background(MoroccoSecondary))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Top Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Theme Toggle
                    IconButton(
                        onClick = { ThemeManager.isDarkTheme = !ThemeManager.isDarkTheme }
                    ) {
                        AnimatedContent(
                            targetState = isDark,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            }
                        ) { dark ->
                            Icon(
                                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (dark) MoroccoGold else MoroccoPrimary
                            )
                        }
                    }

                    // Language Selector
                    LanguageSelector(
                        currentLanguage = languageMap[currentLocale] ?: "العربية",
                        onClick = { showLanguageDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Branding Section
                BrandingSection()

                Spacer(modifier = Modifier.height(40.dp))

                // Login Card
                LoginCard(
                    uiState = uiState,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    onRememberMeChange = onRememberMeChange,
                    onLoginClick = onLoginClick,
                    onForgotPasswordClick = { showResetDialog = true }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Footer
                InstitutionalFooter(onNavigateToFaq = onNavigateToFaq)

                Spacer(modifier = Modifier.height(56.dp)) // Extra space for navigation bar
            }

            // Language Dialog
            if (showLanguageDialog) {
                OptionDialog(
                    title = stringResource(R.string.language),
                    options = languageMap.values.toList(),
                    onDismiss = { showLanguageDialog = false },
                    onSelect = { selectedName ->
                        val code = languageMap.filterValues { it == selectedName }.keys.firstOrNull() ?: "ar"
                        scope.launch {
                            PreferenceManager.setLocale(context, code)
                        }
                        showLanguageDialog = false
                    }
                )
            }

            // Reset Password Dialog
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showResetDialog = false
                        resetPin = ""
                        resetError = null
                        resetSuccess = false
                    },
                    title = { Text(stringResource(R.string.forgot_password)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!resetSuccess) {
                                Text(stringResource(R.string.reset_info))
                                OutlinedTextField(
                                    value = resetPin,
                                    onValueChange = { if (it.length <= 4) resetPin = it },
                                    label = { Text(stringResource(R.string.pin_code)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (resetError != null) {
                                    Text(resetError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            } else {
                                Text(stringResource(R.string.password_changed), color = MoroccoPrimaryContainer)
                            }
                        }
                    },
                    confirmButton = {
                        if (!resetSuccess) {
                            Button(
                                onClick = {
                                    onResetPassword(resetPin, {
                                        resetSuccess = true
                                        resetError = null
                                    }, {
                                        resetError = it
                                    })
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MoroccoPrimaryContainer)
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        } else {
                            Button(onClick = { showResetDialog = false }) {
                                Text(stringResource(R.string.success))
                            }
                        }
                    },
                    dismissButton = {
                        if (!resetSuccess) {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    }
                )
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MoroccoPrimary)
                }
            }
        }
    }
}

@Composable
fun BrandingSection() {
    val isDark = ThemeManager.isDarkTheme
    
    // Smooth transition for text colors - Using Gold for Premium Lux look in Dark Mode
    val primaryTextColor by animateColorAsState(
        targetValue = if (isDark) MoroccoGold else MoroccoPrimary,
        animationSpec = tween(500)
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = if (isDark) MoroccoGoldLight.copy(alpha = 0.8f) else MoroccoOnSurface.copy(alpha = 0.7f),
        animationSpec = tween(500)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo - Now using the transparent version with no artificial background
        Image(
            painter = painterResource(id = R.drawable.app_logo_transparent),
            contentDescription = "Coat of Arms of Morocco",
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.morocco_kingdom),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.ministry_name),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.regional_academy),
                fontSize = 14.sp,
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.directorate_info),
                fontSize = 12.sp,
                color = secondaryTextColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCard(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val isDark = ThemeManager.isDarkTheme
    val cardColor by animateColorAsState(
        targetValue = if (isDark) SurfaceDark else MoroccoSurfaceContainerLowest,
        animationSpec = tween(500)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isDark) MoroccoOutline.copy(alpha = 0.1f) else MoroccoOutline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockPerson,
                    contentDescription = null,
                    tint = if (isDark) MoroccoGold else MoroccoPrimary
                )
                Text(
                    text = stringResource(R.string.secure_login),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) MoroccoGold else MoroccoPrimary
                )
            }

            // Username
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelWithIcon(icon = Icons.Default.Person, label = stringResource(R.string.username))
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    placeholder = { Text(stringResource(R.string.username_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) MoroccoGold else MoroccoPrimaryContainer,
                        unfocusedBorderColor = if (isDark) MoroccoOutline.copy(alpha = 0.3f) else MoroccoOutline
                    )
                )
            }

            // Password
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelWithIcon(icon = Icons.Default.Key, label = stringResource(R.string.password))
                    Text(
                        text = stringResource(R.string.forgot_password),
                        color = if (isDark) MoroccoGold else MoroccoSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onForgotPasswordClick() }
                    )
                }
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = { Text(stringResource(R.string.password_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (isDark) OnSurfaceDark.copy(alpha = 0.6f) else MoroccoOnSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) MoroccoGold else MoroccoPrimaryContainer,
                        unfocusedBorderColor = if (isDark) MoroccoOutline.copy(alpha = 0.3f) else MoroccoOutline
                    )
                )
            }

            // Remember Me
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isDark) MoroccoGold else MoroccoPrimaryContainer,
                        checkmarkColor = if (isDark) Color.Black else Color.White
                    )
                )
                Text(
                    text = stringResource(R.string.remember_me), 
                    fontSize = 14.sp,
                    color = if (isDark) OnSurfaceDark else MoroccoOnSurface
                )
            }

            // Error Message
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Animated Login Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Button(
                onClick = onLoginClick,
                enabled = !uiState.isLoading,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(scale),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoroccoGold else MoroccoPrimaryContainer,
                    contentColor = if (isDark) Color.Black else Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (isDark) Color.Black else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_action),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun LabelWithIcon(icon: ImageVector, label: String) {
    val isDark = ThemeManager.isDarkTheme
    val contentColor by animateColorAsState(
        targetValue = if (isDark) MoroccoGold.copy(alpha = 0.9f) else MoroccoOnSurface.copy(alpha = 0.7f),
        animationSpec = tween(500)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = ThemeManager.isDarkTheme
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (isDark) MoroccoGold.copy(alpha = 0.3f) else MoroccoOutline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isDark) Color.Black.copy(alpha = 0.3f) else MoroccoSurfaceContainerLowest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isDark) MoroccoGold else MoroccoOnSurface.copy(alpha = 0.7f)
            )
            Text(
                text = currentLanguage,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) MoroccoGold else MoroccoOnSurface.copy(alpha = 0.7f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (isDark) MoroccoGold else MoroccoOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InstitutionalFooter(onNavigateToFaq: () -> Unit) {
    val isDark = ThemeManager.isDarkTheme
    val helpColor by animateColorAsState(
        targetValue = if (isDark) MoroccoGold else MoroccoSecondary,
        animationSpec = tween(500)
    )
    val footerTextColor by animateColorAsState(
        targetValue = if (isDark) MoroccoGold.copy(alpha = 0.5f) else MoroccoOnSurface.copy(alpha = 0.5f),
        animationSpec = tween(500)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HorizontalDivider(color = if (isDark) MoroccoGold.copy(alpha = 0.1f) else MoroccoOutline.copy(alpha = 0.2f))
        
        Text(
            text = stringResource(R.string.help),
            fontSize = 16.sp,
            color = helpColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onNavigateToFaq() }
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.footer_copyright),
                fontSize = 12.sp,
                color = footerTextColor
            )
            Text(
                text = stringResource(R.string.footer_portal),
                fontSize = 12.sp,
                color = footerTextColor
            )
        }

        // "Made by" Section with both logos
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Made by",
                fontSize = 10.sp,
                color = footerTextColor.copy(alpha = 0.6f)
            )
            Image(
                painter = painterResource(id = R.drawable.brand),
                contentDescription = "Brand Logo",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "&",
                fontSize = 10.sp,
                color = footerTextColor.copy(alpha = 0.6f)
            )
            Image(
                painter = painterResource(id = R.drawable.app_logo_transparent),
                contentDescription = "Gov Logo",
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoginScreenPreview() {
    FillTrackingTheme(darkTheme = false) {
        LoginScreenContent(
            uiState = LoginUiState(),
            onUsernameChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onResetPassword = { _, _, _ -> },
            onNavigateToFaq = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LoginScreenDarkPreview() {
    FillTrackingTheme(darkTheme = true) {
        LoginScreenContent(
            uiState = LoginUiState(),
            onUsernameChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onResetPassword = { _, _, _ -> },
            onNavigateToFaq = {}
        )
    }
}
