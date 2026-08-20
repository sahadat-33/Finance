package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FloatingNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun FloatingCapsuleNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onQuickAddClick: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    // Subtle border and translucent surface colors matching Samsung Gallery capsule style
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val capsuleSurfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val fabIconColor = if (isDarkTheme) Color.White else Color.Black
    val fabIconBgColor = if (isDarkTheme) Color(0xFF414141) else Color(0xFFD9D9D9)

    val navItems = listOf(
        FloatingNavItem(
            route = "dashboard",
            label = "Home",
            icon = Icons.Rounded.Home,
            testTag = "nav_tab_dashboard"
        ),
        FloatingNavItem(
            route = "timeline",
            label = "Transactions",
            icon = Icons.Rounded.ReceiptLong,
            testTag = "nav_tab_timeline"
        ),
        FloatingNavItem(
            route = "yearly",
            label = "Analytics",
            icon = Icons.Rounded.Insights,
            testTag = "nav_tab_yearly"
        ),
        FloatingNavItem(
            route = "settings",
            label = "Menu",
            icon = Icons.Rounded.Menu,
            testTag = "nav_tab_settings"
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main floating capsule container (4 items)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(percent = 50),
                    clip = false
                )
                .testTag("app_navigation_bar"),
            shape = RoundedCornerShape(percent = 50),
            color = capsuleSurfaceColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    FloatingNavTab(
                        item = item,
                        isSelected = isSelected,
                        isDarkTheme = isDarkTheme,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Separate circular FAB for quick add
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .clickable { onQuickAddClick() }
                .testTag("quick_add_fab"),
            shape = CircleShape,
            color = capsuleSurfaceColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(fabIconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Transaction",
                        tint = fabIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavTab(
    item: FloatingNavItem,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Selected highlight background: #D9D9D9 in Light mode, #414141 in Dark mode
    val selectedHighlightBg = if (isDarkTheme) Color(0xFF414141) else Color(0xFFD9D9D9)
    val tabContentColor = if (isDarkTheme) Color.White else Color.Black

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) selectedHighlightBg else Color.Transparent,
        animationSpec = tween(150),
        label = "nav_tab_bg"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 5.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(animatedBgColor)
            .clickable { onClick() }
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tabContentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                color = tabContentColor,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
