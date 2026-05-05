package com.example.filltracking2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.filltracking2.R
import com.example.filltracking2.data.FileRecord
import com.example.filltracking2.ui.viewmodel.FileViewModel

private val sectorMap = mapOf(
    "Educational Affairs" to R.string.sector_educational_affairs,
    "Planning" to R.string.sector_planning,
    "Orientation" to R.string.sector_orientation,
    "Buildings" to R.string.sector_buildings,
    "Mail Writing" to R.string.sector_mail_writing,
    "Finance" to R.string.sector_finance_main,
    "Information System" to R.string.sector_information_system,
    "Exams" to R.string.sector_exams,
    "Legal Affairs" to R.string.sector_legal_affairs,
    "HR Management" to R.string.sector_hr_management,
    "Inspection" to R.string.sector_inspection,
    "Security" to R.string.sector_security,
    "Admin" to R.string.sector_admin,
    "Operations" to R.string.sector_operations,
    "General" to R.string.sector_general,
    "Technical" to R.string.sector_technical
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorViewScreen(
    viewModel: FileViewModel,
    filterBySector: String = "",
    onFileClick: (FileRecord) -> Unit,
    onNavigateBack: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    val allSectors = sectorMap.keys.toList()
    
    var selectedSectorTab by remember { 
        mutableStateOf(if (filterBySector.isNotEmpty() && allSectors.contains(filterBySector)) filterBySector else allSectors[0]) 
    }

    val filteredRecords = remember(records, selectedSectorTab) {
        records.filter { it.sectors.contains(selectedSectorTab) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sector_dashboard), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = allSectors.indexOf(selectedSectorTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    val index = allSectors.indexOf(selectedSectorTab)
                    if (index != -1) {
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[index]))
                    }
                }
            ) {
                allSectors.forEach { sectorKey ->
                    Tab(
                        selected = selectedSectorTab == sectorKey,
                        onClick = { selectedSectorTab = sectorKey },
                        text = { Text(stringResource(sectorMap[sectorKey]!!)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    FileCard(record = record, onClick = { onFileClick(record) })
                }
            }
        }
    }
}
