package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import com.example.data.Category
import com.example.data.SavingsVault

import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ReorderableColumnList
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.theme.EarningGreen
import com.example.ui.theme.ExpenseRed
import com.example.viewmodel.FinanceViewModel
import com.example.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToAuth: () -> Unit, 
    onNavigateToOthers: () -> Unit = {},
    onNavigateToDataManagement: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories by viewModel.allCategories.collectAsState()
    val savingsVaults by viewModel.allSavingsVault.collectAsState()
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentCalendar by viewModel.selectedCalendar.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val selectedYear = currentCalendar.get(Calendar.YEAR)
    
    val summaryRows = remember(selectedYear, allTransactions) {
        viewModel.getYearlySummary(selectedYear, allTransactions)
    }
    
    val monthlyTransactions = remember(selectedYear, allTransactions) {
        allTransactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == selectedYear
        }.sortedBy { it.date }
    }

    // Adding category state
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryType by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"

    // Adding vault state
    var newVaultName by remember { mutableStateOf("") }

    val incomeCategories = remember(categories) { categories.filter { it.type == "INCOME" } }
    val expenseCategories = remember(categories) { categories.filter { it.type == "EXPENSE" } }
    

    val isUserSignedIn by viewModel.isUserSignedInFlow.collectAsState()
    val currentUserName = viewModel.currentUserName
    val isOfflineGuest by viewModel.isOfflineGuest.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showVaultDialog by remember { mutableStateOf(false) }
    var selectedVaultToRename by remember { mutableStateOf<SavingsVault?>(null) }
    var selectedCategoryToRename by remember { mutableStateOf<Category?>(null) }

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


    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Manage Categories", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    // Input 
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (newCategoryType == "EXPENSE") ExpenseRed else Color.Transparent).clickable { newCategoryType = "EXPENSE" }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Expense", color = if (newCategoryType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (newCategoryType == "INCOME") EarningGreen else Color.Transparent).clickable { newCategoryType = "INCOME" }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Income", color = if (newCategoryType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("Category Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        FloatingActionButton(onClick = {
                            if (newCategoryName.isNotBlank()) { viewModel.addCategory(newCategoryName.trim(), newCategoryType); newCategoryName = "" }
                        }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Text("Income Sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EarningGreen)
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
                            dragHandleModifier = dragHandleModifier,
                            onRename = { selectedCategoryToRename = cat },
                            onDelete = { viewModel.deleteCategory(cat.id) }
                        )
                    }

                    Text("Expense Slates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ExpenseRed)
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
                            dragHandleModifier = dragHandleModifier,
                            onRename = { selectedCategoryToRename = cat },
                            onDelete = { viewModel.deleteCategory(cat.id) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showCategoryDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }

    if (showVaultDialog) {
        Dialog(onDismissRequest = { showVaultDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 600.dp), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Manage Savings Vaults", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newVaultName, onValueChange = { newVaultName = it }, label = { Text("Vault Name") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        FloatingActionButton(onClick = {
                            if (newVaultName.isNotBlank()) { viewModel.addSavingsVault(newVaultName.trim(), 0.0); newVaultName = "" }
                        }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Text("My Asset Vaults", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (savingsVaults.isEmpty()) {
                        Text("No Vaults Found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
                                dragHandleModifier = dragHandleModifier,
                                onRename = { selectedVaultToRename = vault },
                                onDelete = { viewModel.deleteSavingsVault(vault.id) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showVaultDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
        val timeGreeting = when (currentHour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
        val rotatingMessages = listOf(
            "Hope you're doing well today.",
            "Let's keep your finances on track.",
            "Every entry counts.",
            "You're doing great managing your money."
        )
        val randomMessage = remember { rotatingMessages.random() }

        // App title profile card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isUserSignedIn) "Hello, $currentUserName 👋" else "Hello, Guest Tracker 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$timeGreeting! $randomMessage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        if (isUserSignedIn && !viewModel.isEmailVerifiedFlow.value) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(8.dp))
                    Text("Please verify your email address to unlock cloud backup.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        // Settings rows
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (isDarkTheme) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column {
                if (isUserSignedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToProfile() }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("👤 Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { onNavigateToAuth() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("🔐 Login / Signup (Sync to Cloud)")
                    }
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showCategoryDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Income & Expense Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showVaultDialog = true }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Manage Savings Vaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTheme() }
                        .padding(16.dp)
                        .testTag("settings_theme_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val themeMode by viewModel.themeMode.collectAsState()
                        Text(themeMode, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Open Theme Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDataManagement() }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open Data Management")
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToOthers() }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Others", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }


        
        } // end of weighted column
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Made with ❤️ by Sahadat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 96.dp))
    }
}

@Composable
fun CategoryRow(
    category: Category,
    color: Color,
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
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
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
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
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
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
