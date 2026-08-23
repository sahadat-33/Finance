package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Category
import com.example.data.SavingsVault
import com.example.ui.components.ReorderableColumnList
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.theme.EarningGreen
import com.example.ui.theme.ExpenseRed
import com.example.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoryNamesScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.allCategories.collectAsState()
    val savingsVaults by viewModel.allSavingsVault.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var selectedTab by remember { mutableStateOf("EXPENSE") } // "EXPENSE", "INCOME", "VAULTS"
    var inputName by remember { mutableStateOf("") }

    val incomeCategories = remember(categories) { categories.filter { it.type == "INCOME" } }
    val expenseCategories = remember(categories) { categories.filter { it.type == "EXPENSE" } }

    var selectedCategoryToRename by remember { mutableStateOf<Category?>(null) }
    var selectedVaultToRename by remember { mutableStateOf<SavingsVault?>(null) }

    val categoryToRename = selectedCategoryToRename
    if (categoryToRename != null) {
        RenameCategoryDialog(
            category = categoryToRename,
            onDismiss = { selectedCategoryToRename = null },
            onRename = { newName ->
                viewModel.renameCategory(categoryToRename.id, newName)
                selectedCategoryToRename = null
            }
        )
    }

    val vaultToRename = selectedVaultToRename
    if (vaultToRename != null) {
        RenameSavingsVaultDialog(
            vault = vaultToRename,
            onDismiss = { selectedVaultToRename = null },
            onRename = { newName ->
                viewModel.renameSavingsVault(vaultToRename.id, newName)
                selectedVaultToRename = null
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("manage_category_names_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Category Names",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("manage_categories_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 3-way Segmented Toggle: Expense / Income / Vaults
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "EXPENSE") ExpenseRed else Color.Transparent)
                        .clickable { selectedTab = "EXPENSE" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_expense"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Expense",
                        color = if (selectedTab == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "INCOME") EarningGreen else Color.Transparent)
                        .clickable { selectedTab = "INCOME" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_income"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Income",
                        color = if (selectedTab == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "VAULTS") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = "VAULTS" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_vaults"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Vaults",
                        color = if (selectedTab == "VAULTS") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Input field + Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { 
                        Text(if (selectedTab == "VAULTS") "Vault Name" else "Category Name") 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                FloatingActionButton(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            val trimmed = inputName.trim()
                            when (selectedTab) {
                                "EXPENSE" -> viewModel.addCategory(trimmed, "EXPENSE")
                                "INCOME" -> viewModel.addCategory(trimmed, "INCOME")
                                "VAULTS" -> viewModel.addSavingsVault(trimmed, 0.0)
                            }
                            inputName = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("add_category_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            // Section list content
            when (selectedTab) {
                "EXPENSE" -> {
                    Text(
                        "Expense Slates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )

                    if (expenseCategories.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.ReceiptLong,
                            title = "No expense categories yet",
                            subtitle = "Add one above to start categorizing your spending."
                        )
                    } else {
                        ReorderableColumnList(
                            items = expenseCategories,
                            key = { it.id },
                            onReorderFinished = { reorderedExpense ->
                                val fullReorderedList = incomeCategories + reorderedExpense
                                viewModel.reorderCategories(fullReorderedList)
                            }
                        ) { cat, isDragging, dragHandleModifier ->
                            CategoryRow(
                                category = cat,
                                color = ExpenseRed,
                                isDragging = isDragging,
                                dragHandleModifier = dragHandleModifier,
                                onRename = { selectedCategoryToRename = cat },
                                onDelete = { viewModel.deleteCategory(cat.id) }
                            )
                        }
                    }
                }
                "INCOME" -> {
                    Text(
                        "Income Sources",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EarningGreen
                    )

                    if (incomeCategories.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.TrendingUp,
                            title = "No income sources yet",
                            subtitle = "Add one above to start tracking your earnings."
                        )
                    } else {
                        ReorderableColumnList(
                            items = incomeCategories,
                            key = { it.id },
                            onReorderFinished = { reorderedIncome ->
                                val fullReorderedList = reorderedIncome + expenseCategories
                                viewModel.reorderCategories(fullReorderedList)
                            }
                        ) { cat, isDragging, dragHandleModifier ->
                            CategoryRow(
                                category = cat,
                                color = EarningGreen,
                                isDragging = isDragging,
                                dragHandleModifier = dragHandleModifier,
                                onRename = { selectedCategoryToRename = cat },
                                onDelete = { viewModel.deleteCategory(cat.id) }
                            )
                        }
                    }
                }
                "VAULTS" -> {
                    Text(
                        "My Asset Vaults",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (savingsVaults.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Savings,
                            title = "No savings vaults yet",
                            subtitle = "Add one above to start setting money aside."
                        )
                    } else {
                        ReorderableColumnList(
                            items = savingsVaults,
                            key = { it.id },
                            onReorderFinished = { reorderedVaults ->
                                viewModel.reorderSavingsVaults(reorderedVaults)
                            }
                        ) { vault, isDragging, dragHandleModifier ->
                            VaultRow(
                                vault = vault,
                                currencySymbol = currencySymbol,
                                color = MaterialTheme.colorScheme.primary,
                                isDragging = isDragging,
                                dragHandleModifier = dragHandleModifier,
                                onRename = { selectedVaultToRename = vault },
                                onDelete = { viewModel.deleteSavingsVault(vault.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoryRow(
    category: Category,
    color: Color,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete '${category.name}'") },
            text = {
                Text("Are you sure you want to delete '${category.name}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDeleteContainer(
        onDeleteTriggered = { showDeleteConfirmDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("category_row_${category.name}"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 1.dp),
            border = if (isDragging) androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.6f)) else CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .then(dragHandleModifier)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = "Drag to reorder",
                            tint = if (isDragging) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Category",
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onRename,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_cat_btn_${category.name}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Rename category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VaultRow(
    vault: SavingsVault,
    currencySymbol: String,
    color: Color,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete '${vault.assetType}'") },
            text = {
                Text("Are you sure you want to delete '${vault.assetType}'? This will remove the vault and its balance.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDeleteContainer(
        onDeleteTriggered = { showDeleteConfirmDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_row_${vault.assetType}"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 1.dp),
            border = if (isDragging) androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.6f)) else CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .then(dragHandleModifier)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = "Drag to reorder",
                            tint = if (isDragging) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Vault",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = vault.assetType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$currencySymbol${String.format(java.util.Locale.US, "%,.2f", vault.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = onRename,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_vault_btn_${vault.assetType}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Rename vault",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
