package com.example.ui.screens.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.model.content.ContentItem
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

    var fontSize by remember { mutableStateOf(16f) }

    LaunchedEffect(scrollState.value) {
        if (article != null && scrollState.maxValue > 0) {
            val progress = scrollState.value.toFloat() / scrollState.maxValue
            viewModel.updateReadingProgress(article.id, progress, scrollState.value)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(article?.titleAr ?: "تحميل...", color = QabasGold, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع", tint = QabasGold)
                    }
                },
                actions = {
                    IconButton(onClick = { fontSize = (fontSize - 2f).coerceAtLeast(12f) }) {
                        Icon(Icons.Default.Remove, contentDescription = "تصغير الخط", tint = Color.White)
                    }
                    IconButton(onClick = { fontSize = (fontSize + 2f).coerceAtMost(32f) }) {
                        Icon(Icons.Default.Add, contentDescription = "تكبير الخط", tint = Color.White)
                    }
                    if (article != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(article.id, article.isFavorite) }) {
                            Icon(
                                if (article.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = QabasGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = QabasDarkBackground)
            )
        },
        containerColor = QabasDarkBackground
    ) { paddingValues ->
        if (article != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                if (article.isIntroductory) {
                    Surface(
                        color = QabasSurfaceDarkElevated,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                    ) {
                        Text(
                            "محتوى تمهيدي",
                            color = QabasGold,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = article.titleAr,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSize + 8).sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (article.source != null) {
                    Text(
                        text = "المصدر: ${article.source?.name}",
                        color = QabasGold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = article.bodyAr ?: "",
                    color = Color.LightGray,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.6).sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = QabasGold)
            }
        }
    }
}
