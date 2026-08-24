package com.example.ui.screens.impact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
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
fun ImpactDetailScreen(
    initiativeId: String,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: ImpactViewModel
) {
    val initiatives by viewModel.initiatives.collectAsState()
    val initiative = initiatives.find { it.id == initiativeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل المبادرة", color = QabasGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع", tint = QabasGold)
                    }
                },
                actions = {
                    if (initiative != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(initiative.id, initiative.isFavorite) }) {
                            Icon(
                                if (initiative.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
        if (initiative != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Replace with Coil Image
                    Surface(modifier = Modifier.fillMaxSize(), color = QabasSurfaceDarkElevated) {}
                    Text("صورة المبادرة (قريباً)", color = Color.Gray)
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = initiative.titleAr,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = initiative.category.titleAr,
                        color = QabasGold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = QabasSurfaceDarkElevated,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = QabasGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${initiative.approximateTimeMinutes} دقيقة", color = Color.White, fontSize = 13.sp)
                            }
                        }
                        
                        Surface(
                            color = QabasSurfaceDarkElevated,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = QabasGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("الجهد: ${initiative.effortLevel}", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }

                    Text("عن المبادرة:", color = QabasGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = initiative.descriptionAr ?: "",
                        color = Color.LightGray,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text("خطوات العمل:", color = QabasGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = initiative.detailedSteps,
                        color = Color.White,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (initiative.source != null) {
                        Surface(
                            color = QabasSurfaceDarkElevated.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("المصدر:", color = QabasGold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                                Text(initiative.source?.name ?: "", color = Color.LightGray, fontSize = 14.sp)
                                if (initiative.source?.reference != null) {
                                    Text(initiative.source?.reference ?: "", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { /* TODO: Open external link or trigger action */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QabasGold)
                    ) {
                        Text("اعرف المزيد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = QabasGold)
            }
        }
    }
}
