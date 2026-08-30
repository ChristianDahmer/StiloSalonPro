package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.getPaletteColors
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonTopAppBar(
    salonName: String,
    currentScreenTitle: String,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWhatsApp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = salonName.ifBlank { "Salão de Beleza" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = currentScreenTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }

                    onOpenWhatsApp?.let { onClickWhatsApp ->
                        IconButton(
                            onClick = onClickWhatsApp,
                            modifier = Modifier.testTag("whatsapp_top_button")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "Módulo WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.testTag("view_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Exibir em Linha" else "Exibir em Grade",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("global_search_input"),
                    placeholder = { Text("Buscar em $currentScreenTitle...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun ConfirmationAlertDialog(
    title: String = "Confirmação de Ação",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_dialog_button")
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("cancel_dialog_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StatusBadgeChip(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "AGENDADO" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "CONCLUIDO", "EMITIDA" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "CANCELADO", "CANCELADA" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        "BLOQUEIO" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun MaskedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maskType: String, // CPF, CNPJ, IE, PHONE, CEP
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    var errorText by remember { mutableStateOf<String?>(null) }

    // Ensure state holds raw digits
    val rawDigits = remember(value) { value.filter { it.isDigit() } }

    val visualTransformation = remember(maskType) {
        when (maskType) {
            "CPF" -> com.example.utils.CpfVisualTransformation()
            "CNPJ" -> com.example.utils.CnpjVisualTransformation()
            "PHONE" -> com.example.utils.PhoneVisualTransformation()
            "CEP" -> com.example.utils.CepVisualTransformation()
            else -> androidx.compose.ui.text.input.VisualTransformation.None
        }
    }

    OutlinedTextField(
        value = rawDigits,
        onValueChange = { input ->
            val cleanDigits = input.filter { it.isDigit() }
            val maxLen = when (maskType) {
                "CPF" -> 11
                "CNPJ" -> 14
                "PHONE" -> 11
                "CEP" -> 8
                "IE" -> 12
                else -> 20
            }
            val finalDigits = cleanDigits.take(maxLen)

            errorText = if (isRequired && finalDigits.isBlank()) {
                "Campo obrigatório"
            } else if (maskType == "CPF" && finalDigits.isNotEmpty() && finalDigits.length < 11) {
                "CPF deve ter 11 dígitos"
            } else if (maskType == "CNPJ" && finalDigits.isNotEmpty() && finalDigits.length < 14) {
                "CNPJ deve ter 14 dígitos"
            } else null

            onValueChange(finalDigits)
        },
        label = { Text(if (isRequired) "$label *" else label) },
        visualTransformation = visualTransformation,
        isError = errorText != null,
        supportingText = {
            if (errorText != null) {
                Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun QuickThemePickerDialog(
    currentPalette: String,
    currentDarkMode: String,
    onDismiss: () -> Unit,
    onSelectTheme: (paletteKey: String, darkModeOption: String) -> Unit
) {
    var selectedPalette by remember { mutableStateOf(currentPalette) }
    var selectedDarkMode by remember { mutableStateOf(currentDarkMode) }

    val palettes = listOf(
        "ROSE_GOLD" to "Rose Gold Glam",
        "ROYAL_VIOLET" to "Royal Violet Spa",
        "EMERALD_GLAM" to "Emerald Elegance",
        "GOLDEN_LUXURY" to "Golden Luxury",
        "SUNSET_COPPER" to "Sunset Copper",
        "OCEAN_BREEZE" to "Azul Oceano",
        "MIDNIGHT_LUX" to "Noite Estrelada",
        "SAGE_MINDFUL" to "Verde Sálvia Zen"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trocar Tema Visual", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escolha a paleta de cores preferida:", style = MaterialTheme.typography.labelLarge)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    palettes.forEach { (key, name) ->
                        val (p, s, a) = getPaletteColors(key)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedPalette == key) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    selectedPalette = key
                                    onSelectTheme(key, selectedDarkMode)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(p))
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(s))
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(a))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedPalette == key) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedPalette == key) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (selectedPalette == key) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Modo de Exibição:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SYSTEM" to "Sistema", "LIGHT" to "Claro", "DARK" to "Escuro").forEach { (modeKey, modeLabel) ->
                        FilterChip(
                            selected = selectedDarkMode == modeKey,
                            onClick = {
                                selectedDarkMode = modeKey
                                onSelectTheme(selectedPalette, modeKey)
                            },
                            label = { Text(modeLabel, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_theme_picker_button")) {
                Text("Concluir")
            }
        }
    )
}

@Composable
fun MasterPasswordAuthDialog(
    masterPasswordActual: String = "1234",
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var inputPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Liberação Master Necessária",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Este item possui Nota Fiscal (NFS-e) emitida e está bloqueado para edições.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Por favor, digite a Senha do Usuário Master para autorizar a alteração:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = inputPassword,
                    onValueChange = {
                        inputPassword = it
                        showError = false
                    },
                    label = { Text("Senha Master") },
                    singleLine = true,
                    isError = showError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("master_password_input")
                )
                if (showError) {
                    Text(
                        text = "Senha Master incorreta! Tente novamente.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputPassword == masterPasswordActual) {
                        onSuccess()
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("unlock_master_button")
            ) {
                Text("Desbloquear & Editar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

