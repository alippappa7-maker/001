package com.example.ui.screens.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasSurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeArticleScreen(
    articleId: String,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: KnowledgeViewModel
) {
    val articles by viewModel.articles.collectAsState()
    val article = articles.find { it.id == articleId }
    val scrollState = rememberScrollState()
    var fontSizeMultiplier by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(article) {
        if (article != null && article.lastReadPosition > 0) {
            scrollState.scrollTo(article.lastReadPosition)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (article != null) {
                val progress = if (scrollState.maxValue > 0) {
                    scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                } else {
                    1f // If content doesn't scroll, it's 100% read
                }
                viewModel.updateReadingProgress(article.id, progress, scrollState.value)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(article?.title ?: "تحميل...", color = QabasGold, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع", tint = QabasGold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { fontSizeMultiplier = (fontSizeMultiplier - 0.2f).coerceAtLeast(0.8f) }) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير الخط", tint = Color.White)
                        }
                        IconButton(onClick = { fontSizeMultiplier = (fontSizeMultiplier + 0.2f).coerceAtMost(2f) }) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير الخط", tint = Color.White)
                        }
                        if (article != null) {
                            IconButton(onClick = { viewModel.toggleFavorite(article.id, article.isFavorite) }) {
                                Icon(
                                    imageVector = if (article.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "المفضلة",
                                    tint = QabasGold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = QabasSurfaceDarkElevated)
                )
            }
        ) { paddingValues ->
            if (article != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (article.isIntroductory) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(QabasSurfaceDarkElevated)
                                .padding(12.dp)
                        ) {
                            Text(
                                "محتوى تمهيدي",
                                color = QabasGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = article.content,
                        color = Color.White,
                        fontSize = (18 * fontSizeMultiplier).sp,
                        lineHeight = (28 * fontSizeMultiplier).sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Source
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "المصدر: ${article.source}",
                            color = QabasGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("جاري التحميل أو المقال غير موجود", color = Color.White)
                }
            }
        }
    }
}
