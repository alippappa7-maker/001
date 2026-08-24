package com.example.ui.screens.impact

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.QabasDarkBackground
import com.example.ui.theme.QabasGold
import com.example.ui.theme.QabasSurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactDetailScreen(
    initiativeId: String,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: ImpactViewModel
) {
    val initiatives by viewModel.initiatives.collectAsState()
    val initiative = initiatives.find { it.id == initiativeId }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = QabasDarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text("تفاصيل المبادرة", color = QabasGold, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع", tint = QabasGold)
                        }
                    },
                    actions = {
                        if (initiative != null) {
                            IconButton(onClick = { viewModel.toggleFavorite(initiative.id, initiative.isFavorite) }) {
                                Icon(
                                    imageVector = if (initiative.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
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
            if (initiative != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = initiative.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = initiative.category.titleAr,
                        color = QabasGold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(QabasSurfaceDarkElevated)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = QabasGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الوقت: ${initiative.approximateTimeMinutes} دقيقة", color = Color.White, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = QabasGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الجهد: ${initiative.effortLevel.titleAr}", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "عن المبادرة",
                        color = QabasGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = initiative.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "خطوات التنفيذ",
                        color = QabasGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = initiative.detailedSteps,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "المصدر: ${initiative.source}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* Simulated Learn More - No external links per requirements */ },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QabasGold)
                    ) {
                        Text("اعرف المزيد", color = QabasDarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("المبادرة غير موجودة", color = Color.White)
                }
            }
        }
    }
}
