package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import com.example.data.Goal
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

data class GoalPresetItem(
    val name: String,
    val iconKey: String,
    val icon: ImageVector,
    val colorHex: String
)

val CuratedGoalIcons: List<Pair<String, ImageVector>> = listOf(
    "DirectionsCar" to Icons.Rounded.DirectionsCar,
    "Home" to Icons.Rounded.Home,
    "Flight" to Icons.Rounded.Flight,
    "BeachAccess" to Icons.Rounded.BeachAccess,
    "School" to Icons.Rounded.School,
    "Savings" to Icons.Rounded.Savings,
    "LocalHospital" to Icons.Rounded.LocalHospital,
    "Favorite" to Icons.Rounded.Favorite,
    "Celebration" to Icons.Rounded.Celebration,
    "ChildCare" to Icons.Rounded.ChildCare,
    "CardGiftcard" to Icons.Rounded.CardGiftcard,
    "ShoppingBag" to Icons.Rounded.ShoppingBag,
    "Restaurant" to Icons.Rounded.Restaurant,
    "FitnessCenter" to Icons.Rounded.FitnessCenter,
    "Pets" to Icons.Rounded.Pets,
    "Laptop" to Icons.Rounded.Laptop,
    "Phone" to Icons.Rounded.Phone,
    "CameraAlt" to Icons.Rounded.CameraAlt,
    "MusicNote" to Icons.Rounded.MusicNote,
    "SportsEsports" to Icons.Rounded.SportsEsports,
    "Palette" to Icons.Rounded.Palette,
    "Build" to Icons.Rounded.Build,
    "Work" to Icons.Rounded.Work,
    "AttachMoney" to Icons.Rounded.AttachMoney,
    "TrendingUp" to Icons.Rounded.TrendingUp,
    "Diamond" to Icons.Rounded.Diamond,
    "Star" to Icons.Rounded.Star,
    "Cake" to Icons.Rounded.Cake,
    "LocalCafe" to Icons.Rounded.LocalCafe,
    "DirectionsBike" to Icons.Rounded.DirectionsBike,
    "Watch" to Icons.Rounded.Watch,
    "Storefront" to Icons.Rounded.Storefront,
    "Tv" to Icons.Rounded.Tv,
    "Checkroom" to Icons.Rounded.Checkroom,
    "WbSunny" to Icons.Rounded.WbSunny,
    "Shield" to Icons.Rounded.Shield,
    "Flag" to Icons.Rounded.Flag
)

val CuratedGoalColors = listOf(
    "#00BCD4", // Cyan
    "#0097A7", // Darker Cyan
    "#1976D2", // Blue
    "#1565C0", // Royal Blue
    "#03A9F4", // Light Blue
    "#42A5F5", // Sky Blue
    "#FF6D00", // Bright Orange
    "#FF9800", // Orange
    "#FFB300", // Amber
    "#F57F17", // Gold / Deep Yellow
    "#795548", // Taupe / Brown
    "#4E342E", // Dark Brown
    "#D32F2F", // Red
    "#E91E63", // Pink / Crimson
    "#FF5722", // Coral / Red-Orange
    "#EC407A", // Rose Pink
    "#C2185B", // Magenta
    "#6A1B9A", // Deep Purple
    "#9C27B0", // Violet / Purple
    "#00695C", // Pine / Dark Teal
    "#00897B", // Emerald Teal
    "#26A69A", // Teal
    "#2E7D32", // Forest Green
    "#4CAF50", // Green
    "#76FF03", // Lime Green
    "#212121", // Dark Charcoal
    "#546E7A", // Slate Gray
    "#37474F", // Dark Slate
    "#4376F6"  // Brand Blue
)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF4376F6)): Color {
    return try {
        Color(hex.toColorInt())
    } catch (e: Exception) {
        fallback
    }
}

fun getIconByKey(key: String): ImageVector {
    return CuratedGoalIcons.firstOrNull { it.first == key }?.second ?: Icons.Rounded.Flag
}

// -------------------------------------------------------------
// SCREEN 1: Name Entry + Presets Grid
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalPresetScreen(
    onBack: () -> Unit,
    onProceedToDetails: (name: String, iconKey: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }
    var pendingIconKey by remember { mutableStateOf("Flag") }
    var pendingColorHex by remember { mutableStateOf("#4376F6") }

    val presets = remember {
        listOf(
            GoalPresetItem("New Vehicle", "DirectionsCar", Icons.Rounded.DirectionsCar, "#00BCD4"),
            GoalPresetItem("New Home", "Home", Icons.Rounded.Home, "#FF9800"),
            GoalPresetItem("Holiday Trip", "BeachAccess", Icons.Rounded.BeachAccess, "#4CAF50"),
            GoalPresetItem("Education", "School", Icons.Rounded.School, "#1976D2"),
            GoalPresetItem("Emergency Fund", "Savings", Icons.Rounded.Savings, "#9C27B0"),
            GoalPresetItem("Health Care", "LocalHospital", Icons.Rounded.LocalHospital, "#D32F2F"),
            GoalPresetItem("Party", "Celebration", Icons.Rounded.Celebration, "#FFB300"),
            GoalPresetItem("Kids Spoiling", "ChildCare", Icons.Rounded.ChildCare, "#EC407A"),
            GoalPresetItem("Charity", "CardGiftcard", Icons.Rounded.CardGiftcard, "#00897B")
        )
    }

    val isCreateEnabled = name.trim().isNotEmpty()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("new_goal_preset_screen"),
        topBar = {
            TopAppBar(
                title = { Text("New Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("new_goal_preset_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close without saving"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Heading
            Text(
                text = "What are you saving for?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("new_goal_heading")
            )

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    // If manually typed, clear preset highlight if text doesn't match
                    if (selectedPresetIndex != null && presets.getOrNull(selectedPresetIndex!!)?.name != it) {
                        selectedPresetIndex = null
                    }
                },
                label = { Text("Name") },
                placeholder = { Text("Your goal's name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_goal_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Create goal button
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        Toast.makeText(context, "Please enter a goal name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onProceedToDetails(trimmedName, pendingIconKey, pendingColorHex)
                },
                enabled = isCreateEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("new_goal_create_btn"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4376F6),
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = "Create goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Preset Goal Options Section
            Text(
                text = "Some things people save for:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 2-column grid of presets
            val chunkedPresets = presets.chunked(2)
            chunkedPresets.forEachIndexed { rowIndex, rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowPresets.forEachIndexed { colIndex, preset ->
                        val globalIndex = rowIndex * 2 + colIndex
                        val isSelected = selectedPresetIndex == globalIndex || name == preset.name
                        val presetColor = parseHexColor(preset.colorHex)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    name = preset.name
                                    selectedPresetIndex = globalIndex
                                    pendingIconKey = preset.iconKey
                                    pendingColorHex = preset.colorHex
                                }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                                .testTag("preset_item_${preset.name.lowercase().replace(' ', '_')}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(presetColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = preset.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = preset.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (rowPresets.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2: Full Details Form (Used for both New and Edit Goal)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalDetailsScreen(
    viewModel: FinanceViewModel,
    initialName: String = "",
    initialIconKey: String = "Flag",
    initialColorHex: String = "#4376F6",
    existingGoal: Goal? = null,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isEditMode = existingGoal != null

    var name by remember(existingGoal) { mutableStateOf(existingGoal?.name ?: initialName) }
    var targetAmountStr by remember(existingGoal) {
        mutableStateOf(
            if (existingGoal != null) {
                if (existingGoal.targetAmount % 1.0 == 0.0) {
                    existingGoal.targetAmount.toLong().toString()
                } else {
                    String.format(Locale.US, "%.2f", existingGoal.targetAmount)
                }
            } else "0"
        )
    }
    var savedAlreadyStr by remember(existingGoal) {
        mutableStateOf(
            if (existingGoal != null) {
                if (existingGoal.savedAmount % 1.0 == 0.0) {
                    existingGoal.savedAmount.toLong().toString()
                } else {
                    String.format(Locale.US, "%.2f", existingGoal.savedAmount)
                }
            } else "0"
        )
    }
    var note by remember(existingGoal) { mutableStateOf(existingGoal?.note ?: "") }
    var selectedColorHex by remember(existingGoal) { mutableStateOf(existingGoal?.colorHex ?: initialColorHex) }
    var selectedIconKey by remember(existingGoal) { mutableStateOf(existingGoal?.iconKey ?: initialIconKey) }
    var goalStatus by remember(existingGoal) { mutableStateOf(existingGoal?.status ?: "ACTIVE") }

    // Date state
    var deadlineDateMs by remember(existingGoal) { mutableLongStateOf(existingGoal?.deadlineDate ?: System.currentTimeMillis()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Popups & expand states
    var colorPickerExpanded by remember { mutableStateOf(false) }
    var showIconPickerDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var deductFromCash by remember { mutableStateOf(false) }

    val dateCalendar = Calendar.getInstance().apply { timeInMillis = deadlineDateMs }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                deadlineDateMs = chosen.timeInMillis
            },
            dateCalendar.get(Calendar.YEAR),
            dateCalendar.get(Calendar.MONTH),
            dateCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(if (isEditMode) "edit_goal_screen" else "new_goal_details_screen"),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit goal" else "New Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("new_goal_details_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        // Pause / Resume action
                        IconButton(
                            onClick = {
                                if (goalStatus.equals("PAUSED", ignoreCase = true)) {
                                    val newStatus = "ACTIVE"
                                    goalStatus = newStatus
                                    if (existingGoal != null) {
                                        val updated = existingGoal.copy(status = newStatus)
                                        viewModel.updateGoal(updated)
                                    }
                                    Toast.makeText(context, "Goal resumed", Toast.LENGTH_SHORT).show()
                                    onSaved()
                                } else {
                                    showPauseDialog = true
                                }
                            },
                            modifier = Modifier.testTag("edit_goal_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (goalStatus.equals("PAUSED", ignoreCase = true)) {
                                    Icons.Rounded.PlayCircleOutline
                                } else {
                                    Icons.Rounded.PauseCircleOutline
                                },
                                contentDescription = if (goalStatus.equals("PAUSED", ignoreCase = true)) "Resume Goal" else "Pause Goal"
                            )
                        }

                        // Delete action
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("edit_goal_delete_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Goal"
                            )
                        }
                    }

                    // Save / Checkmark action
                    IconButton(
                        onClick = {
                            val trimmedName = name.trim()
                            if (trimmedName.isBlank()) {
                                Toast.makeText(context, "Please enter a goal name", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
                            if (targetAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a target amount greater than 0", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val savedAmount = savedAlreadyStr.toDoubleOrNull() ?: 0.0

                            if (existingGoal != null) {
                                val updatedGoal = existingGoal.copy(
                                    name = trimmedName,
                                    targetAmount = targetAmount,
                                    savedAmount = savedAmount,
                                    deadlineDate = deadlineDateMs,
                                    colorHex = selectedColorHex,
                                    iconKey = selectedIconKey,
                                    note = note.ifBlank { null },
                                    status = goalStatus
                                )
                                viewModel.updateGoal(updatedGoal)
                                if (deductFromCash && savedAmount > existingGoal.savedAmount) {
                                    val diff = savedAmount - existingGoal.savedAmount
                                    viewModel.addTransaction(
                                        type = "EXPENSE",
                                        categoryName = "Goal Savings",
                                        amount = diff,
                                        date = System.currentTimeMillis(),
                                        note = "To $trimmedName Goal",
                                        receiptImageUri = null
                                    )
                                }
                                Toast.makeText(context, "Goal updated successfully", Toast.LENGTH_SHORT).show()
                                onSaved()
                            } else {
                                val newGoal = Goal(
                                    name = trimmedName,
                                    targetAmount = targetAmount,
                                    savedAmount = savedAmount,
                                    deadlineDate = deadlineDateMs,
                                    colorHex = selectedColorHex,
                                    iconKey = selectedIconKey,
                                    note = note.ifBlank { null },
                                    status = "ACTIVE",
                                    lastAddedAmount = if (savedAmount > 0.0) savedAmount else 0.0,
                                    lastAddedDate = if (savedAmount > 0.0) System.currentTimeMillis() else null,
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.addGoal(newGoal)
                                if (deductFromCash && savedAmount > 0.0) {
                                    viewModel.addTransaction(
                                        type = "EXPENSE",
                                        categoryName = "Goal Savings",
                                        amount = savedAmount,
                                        date = System.currentTimeMillis(),
                                        note = "To $trimmedName Goal",
                                        receiptImageUri = null
                                    )
                                }
                                Toast.makeText(context, "Goal created successfully", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                        },
                        modifier = Modifier.testTag("new_goal_details_save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Goal",
                            tint = parseHexColor("#4376F6")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("Your goal's name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_details_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Target Amount Field
            OutlinedTextField(
                value = targetAmountStr,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        targetAmountStr = input
                    }
                },
                label = { Text("Target amount ($currencySymbol)") },
                placeholder = { Text("0") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_details_target_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Saved Already Field & Deduct from cash checkbox
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = savedAlreadyStr,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            savedAlreadyStr = input
                        }
                    },
                    label = { Text("Saved already ($currencySymbol)") },
                    placeholder = { Text("0") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_details_saved_already_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deductFromCash = !deductFromCash }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deductFromCash,
                        onCheckedChange = { deductFromCash = it },
                        modifier = Modifier.testTag("goal_details_deduct_cash_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deduct from cash",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Desired Date Field
            OutlinedTextField(
                value = dateFormatter.format(Date(deadlineDateMs)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Desired date") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pick Date"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .testTag("goal_details_date_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Goal Color & Icon row selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Goal Color Field
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Goal color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { colorPickerExpanded = !colorPickerExpanded }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(parseHexColor(selectedColorHex))
                                .testTag("goal_details_current_color_preview")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (colorPickerExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle Color Palette",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Narrow expandable vertical stack of solid saturated colors directly under Goal color field
                    AnimatedVisibility(
                        visible = colorPickerExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .testTag("goal_details_color_palette_list"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CuratedGoalColors.forEach { hex ->
                                    val color = parseHexColor(hex)
                                    val isCurrent = selectedColorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color)
                                            .border(
                                                width = if (isCurrent) 3.dp else 0.dp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedColorHex = hex
                                                colorPickerExpanded = false
                                            }
                                            .testTag("details_color_swatch_$hex")
                                    )
                                }
                            }
                        }
                    }
                }

                // Icon Field (Batch 8: Removed icon name text label)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { showIconPickerDialog = true }
                            .padding(horizontal = 12.dp)
                            .testTag("goal_details_icon_selector"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = getIconByKey(selectedIconKey),
                            contentDescription = "Selected icon",
                            tint = parseHexColor(selectedColorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Note field (multi-line)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                placeholder = { Text("Add any extra details or reminders...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_details_note_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Icon Picker Dialog
    if (showIconPickerDialog) {
        Dialog(onDismissRequest = { showIconPickerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .testTag("goal_details_icon_picker_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Select Icon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(CuratedGoalIcons) { (key, vector) ->
                            val isSelected = selectedIconKey == key
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) parseHexColor(selectedColorHex).copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) parseHexColor(selectedColorHex) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedIconKey = key
                                        showIconPickerDialog = false
                                    }
                                    .testTag("details_icon_picker_item_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = key,
                                    tint = if (isSelected) parseHexColor(selectedColorHex) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showIconPickerDialog = false },
                            modifier = Modifier.testTag("details_icon_picker_cancel_btn")
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && existingGoal != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Do you really want to delete this item?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteGoal(existingGoal)
                        Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show()
                        onDeleted()
                    },
                    modifier = Modifier.testTag("delete_goal_confirm_btn")
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("delete_goal_cancel_btn")
                ) {
                    Text("No", fontWeight = FontWeight.SemiBold)
                }
            },
            modifier = Modifier.testTag("delete_goal_dialog")
        )
    }

    // Pause Confirmation Dialog
    if (showPauseDialog && existingGoal != null) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = {
                Text(
                    text = "Do you really want to pause the goal?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Goal will be moved to paused goals list, where you will be able to delete it.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        val updated = existingGoal.copy(status = "PAUSED")
                        goalStatus = "PAUSED"
                        viewModel.updateGoal(updated)
                        Toast.makeText(context, "Goal paused", Toast.LENGTH_SHORT).show()
                        onSaved()
                    },
                    modifier = Modifier.testTag("pause_goal_confirm_btn")
                ) {
                    Text("Pause", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPauseDialog = false },
                    modifier = Modifier.testTag("pause_goal_cancel_btn")
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            },
            modifier = Modifier.testTag("pause_goal_dialog")
        )
    }
}
