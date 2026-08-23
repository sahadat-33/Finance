package com.example.ui

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Goal
import com.example.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onEditGoal: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGoals by viewModel.allGoals.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val goal = allGoals.firstOrNull { it.id == goalId }

    var showAddAmountDialog by remember { mutableStateOf(false) }
    var showReachedConfirmDialog by remember { mutableStateOf(false) }

    if (goal == null) {
        // Fallback if goal not found or deleted
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Goal detail", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Goal not found")
            }
        }
        return
    }

    val goalColor = parseHexColor(goal.colorHex)
    val dateFormatter = remember { SimpleDateFormat("M/d/yy", Locale.getDefault()) }
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
    }

    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat()
    } else 0f

    val animatedRatio by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "goal_ratio_animation"
    )

    val progressSweep = animatedRatio.coerceIn(0f, 1f)
    val percentageInt = (animatedRatio * 100).toInt().coerceAtLeast(0)

    // Calculate weekly stats
    val now = System.currentTimeMillis()
    val remainingMs = max(0L, goal.deadlineDate - now)
    val remainingWeeks = max(1, ceil(remainingMs / (1000.0 * 60 * 60 * 24 * 7)).toInt())
    val remainingAmount = max(0.0, goal.targetAmount - goal.savedAmount)
    val minWeekAmount = if (remainingWeeks > 0) remainingAmount / remainingWeeks else 0.0

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("goal_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Goal detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("goal_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onEditGoal(goal.id)
                        },
                        modifier = Modifier.testTag("goal_detail_edit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goal"
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Row: Icon badge + Name + Target date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_detail_header"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(goalColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByKey(goal.iconKey),
                        contentDescription = goal.name,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target date ${dateFormatter.format(Date(goal.deadlineDate))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Circular Progress Ring
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("goal_detail_progress_ring"),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    // Background full track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress arc
                    if (progressSweep > 0f) {
                        drawArc(
                            color = goalColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progressSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$percentageInt %",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = goalColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${numberFormat.format(goal.savedAmount)} / ${numberFormat.format(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stats Section: Minimum Week amount + Last added amount
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Minimum Week amount to reach goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol ${numberFormat.format(minWeekAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Last added amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (goal.lastAddedAmount > 0.0) {
                            "$currencySymbol ${numberFormat.format(goal.lastAddedAmount)}"
                        } else {
                            "$currencySymbol 0"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add saved amount button
                Button(
                    onClick = { showAddAmountDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_saved_amount_btn"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4376F6),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Add saved amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Set goal as reached link
                TextButton(
                    onClick = {
                        showReachedConfirmDialog = true
                    },
                    modifier = Modifier.testTag("set_goal_reached_btn")
                ) {
                    Text(
                        text = "Set goal as reached",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4376F6)
                    )
                }
            }
        }
    }

    // Add Saved Amount Dialog
    if (showAddAmountDialog) {
        var inputAmountStr by remember { mutableStateOf("") }
        var deductFromCash by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddAmountDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_saved_amount_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Saved Amount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Add funds towards '${goal.name}' ($currencySymbol)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = inputAmountStr,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    inputAmountStr = input
                                }
                            },
                            label = { Text("Amount ($currencySymbol)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_saved_amount_input"),
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
                                modifier = Modifier.testTag("add_saved_amount_deduct_cash_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Deduct from cash",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddAmountDialog = false },
                            modifier = Modifier.testTag("add_saved_amount_cancel_btn")
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val amount = inputAmountStr.toDoubleOrNull() ?: 0.0
                                if (amount <= 0.0) {
                                    Toast.makeText(context, "Please enter an amount greater than 0", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val updatedGoal = goal.copy(
                                    savedAmount = goal.savedAmount + amount,
                                    lastAddedAmount = amount,
                                    lastAddedDate = System.currentTimeMillis()
                                )
                                viewModel.updateGoal(updatedGoal)
                                if (deductFromCash) {
                                    viewModel.addTransaction(
                                        type = "EXPENSE",
                                        categoryName = "Goal Savings",
                                        amount = amount,
                                        date = System.currentTimeMillis(),
                                        note = "To ${goal.name} Goal",
                                        receiptImageUri = null
                                    )
                                }
                                Toast.makeText(context, "Added $currencySymbol ${numberFormat.format(amount)} to goal", Toast.LENGTH_SHORT).show()
                                showAddAmountDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4376F6),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("add_saved_amount_confirm_btn")
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    // Set Goal as Reached Confirmation Dialog
    if (showReachedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showReachedConfirmDialog = false },
            title = {
                Text(
                    text = "Set goal as reached",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Do you want to set goal as reached? It will moved to Reached module. You can't move back to active goals."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReachedConfirmDialog = false
                        val reachedGoal = goal.copy(status = "REACHED")
                        viewModel.updateGoal(reachedGoal)
                        Toast.makeText(context, "Goal marked as reached!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.testTag("set_goal_reached_confirm_btn")
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReachedConfirmDialog = false },
                    modifier = Modifier.testTag("set_goal_reached_cancel_btn")
                ) {
                    Text("No", fontWeight = FontWeight.SemiBold)
                }
            },
            modifier = Modifier.testTag("set_goal_reached_dialog")
        )
    }
}
