package com.example.ui.screens.knowledge

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.domain.model.knowledge.KnowledgeArticle
import com.example.domain.model.knowledge.KnowledgeCategory
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasSurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeHomeScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: KnowledgeViewModel
) {
    val articles by viewModel.articles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text("طلب العلم", color = QabasGold, fontWeight = FontWeight.Bold)
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
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("ابحث في الدروس والمقالات...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QabasGold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QabasGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    items(KnowledgeCategory.entries) { category ->
                        CategoryChip(
                            title = category.titleAr,
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Articles List
                if (articles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد مقالات متطابقة", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(articles) { article ->
                            ArticleCard(
                                article = article,
                                onClick = { navController.navigate("knowledge_article/${article.id}") },
                                onFavoriteClick = { viewModel.toggleFavorite(article.id, article.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) QabasGold else QabasSurfaceDarkElevated)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) QabasDarkBackground else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ArticleCard(article: KnowledgeArticle, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
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
                    if (article.isIntroductory) {
                        Text("محتوى تمهيدي", color = QabasGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = article.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.category.titleAr,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (article.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "المفضلة",
                        tint = QabasGold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (article.progressPercent > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { article.progressPercent },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = QabasGold,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(article.progressPercent * 100).toInt()}%",
                        color = QabasGold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
