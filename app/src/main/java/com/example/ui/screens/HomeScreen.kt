package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculations.model.CalculatorCategory
import com.example.calculations.registry.CalculatorItem
import com.example.data.repository.AppThemeMode
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCalculator: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReference: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calculators by viewModel.calculators.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val onlyFavorites by viewModel.onlyFavorites.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showSearchField by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PrecisionManufacturing,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Engineering Toolkit",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Offline Electronics & Embedded",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Search toggle button
                    IconButton(
                        onClick = { showSearchField = !showSearchField },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // Reference Cheat-Sheets button
                    IconButton(
                        onClick = onNavigateToReference,
                        modifier = Modifier.testTag("reference_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Reference Guide"
                        )
                    }

                    // Calculation History button
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("history_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    }

                    // Theme toggle
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                AppThemeMode.DARK -> AppThemeMode.LIGHT
                                AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
                                AppThemeMode.SYSTEM -> AppThemeMode.DARK
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            contentDescription = "Theme"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field (when toggled or text present)
            AnimatedVisibility(visible = showSearchField || searchQuery.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search 37 calculators, formulas, or components...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_text_field")
                )
            }

            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" Chip
                item {
                    FilterChip(
                        selected = selectedCategory == null && !onlyFavorites,
                        onClick = {
                            viewModel.selectCategory(null)
                            viewModel.setOnlyFavorites(false)
                        },
                        label = { Text("All (${calculators.size})") },
                        modifier = Modifier.testTag("category_chip_all")
                    )
                }

                // "Favorites" Chip
                item {
                    FilterChip(
                        selected = onlyFavorites,
                        onClick = { viewModel.setOnlyFavorites(!onlyFavorites) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (onlyFavorites) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Favorites (${favoriteIds.size})") },
                        modifier = Modifier.testTag("category_chip_favorites")
                    )
                }

                // Categories
                CalculatorCategory.values().forEach { category ->
                    item {
                        FilterChip(
                            selected = selectedCategory == category && !onlyFavorites,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.displayName) },
                            modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // Calculators List
            if (calculators.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (onlyFavorites) "No favorite calculators yet" else "No matching calculators found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (onlyFavorites) "Tap the star icon on any calculator to pin it here." else "Try searching by unit (e.g., ohm, uF, mAh), name, or formula.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(calculators, key = { it.id }) { item ->
                        CalculatorCard(
                            item = item,
                            isFavorite = favoriteIds.contains(item.id),
                            onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                            onClick = { onNavigateToCalculator(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorCard(
    item: CalculatorItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("calculator_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when (item.category) {
                    CalculatorCategory.BASIC_ELECTRONICS -> MaterialTheme.colorScheme.primaryContainer
                    CalculatorCategory.POWER_EMBEDDED -> MaterialTheme.colorScheme.secondaryContainer
                    CalculatorCategory.BATTERIES -> MaterialTheme.colorScheme.tertiaryContainer
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = when (item.category) {
                            CalculatorCategory.BASIC_ELECTRONICS -> MaterialTheme.colorScheme.primary
                            CalculatorCategory.POWER_EMBEDDED -> MaterialTheme.colorScheme.secondary
                            CalculatorCategory.BATTERIES -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title, Subtitle, and Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = item.titleUk,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Formula pill badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = item.formulaBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Favorite Star
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("fav_button_${item.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
