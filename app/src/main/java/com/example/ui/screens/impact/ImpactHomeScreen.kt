package com.example.ui.screens.impact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.model.impact.ImpactCategory
import com.example.domain.model.impact.ImpactInitiative
import com.example.ui.screens.knowledge.CategoryChip
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasSurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactHomeScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: ImpactViewModel
) {
    val initiatives by viewModel.initiatives.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text("الأثر", color = QabasGold, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع", tint = QabasGold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "المفضلة",
                                tint = QabasGold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = QabasSurfaceDarkElevated)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Categories
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CategoryChip(
                            title = "الكل",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }
                    items(ImpactCategory.entries) { category ->
                        CategoryChip(
                            title = category.titleAr,
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Initiatives List
                if (initiatives.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد مبادرات", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(initiatives) { initiative ->
                            InitiativeCard(
                                initiative = initiative,
                                onClick = { navController.navigate("impact_detail/${initiative.id}") },
                                onFavoriteClick = { viewModel.toggleFavorite(initiative.id, initiative.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InitiativeCard(initiative: ImpactInitiative, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QabasSurfaceDarkElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = initiative.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = initiative.category.titleAr,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (initiative.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "المفضلة",
                        tint = QabasGold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = initiative.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = QabasGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${initiative.approximateTimeMinutes} دقيقة", color = QabasGold, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = QabasGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الجهد: ${initiative.effortLevel.titleAr}", color = QabasGold, fontSize = 11.sp)
                }
            }
        }
    }
}
