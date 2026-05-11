package com.example.filltracking2.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.filltracking2.R
import com.example.filltracking2.ui.viewmodel.FileViewModel
import com.example.filltracking2.util.AttachmentOpener
import com.example.filltracking2.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailScreen(
    serial: String,
    viewModel: FileViewModel,
    onNavigateBack: () -> Unit,
    onEditFile: (String) -> Unit,
    onOpenImageViewer: () -> Unit
) {
    val records by viewModel.records.collectAsState()
    val record = records.find { it.internalSerial == serial || it.originalSerial == serial }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDark = ThemeManager.isDarkTheme
    val goldColor = Color(0xFFD4AF37)
    val primaryColor = Color(0xFF004824)
    val secondaryColor = Color(0xFFB71F27)

    if (showDeleteDialog && record != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(record)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.doc_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (record != null) {
                        IconButton(onClick = { onEditFile(record.internalSerial) }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit), tint = primaryColor)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete), tint = secondaryColor)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (record == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.doc_not_found))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(if (isDark) Color(0xFF121212) else Color(0xFFF8F9FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Document ID Hero Section
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Gold top accent
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(goldColor))
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.internal_serial_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.internalSerial,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) goldColor else primaryColor
                                    )
                                }
                                VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.original_serial_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.originalSerial,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) goldColor else primaryColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            StatusPill(status = record.status, urgency = record.urgency)
                        }
                    }
                }

                // Document Subject & Destination
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = record.subject,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider(modifier = Modifier.alpha(0.5f))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRowSmall(icon = Icons.Default.Business, label = stringResource(R.string.source_label), value = record.source)
                            
                            // Destination Sectors
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.destination_sectors),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        record.sectors.forEach { sector ->
                                            Surface(
                                                color = primaryColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = sector,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = primaryColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Recipient Row with Initials
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val initials = record.recipientName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.take(1).uppercase() }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.recipient),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = record.recipientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Tracking Timeline
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.tracking_timeline),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) goldColor else primaryColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TimelineItem(
                            label = stringResource(R.string.received_gov_label),
                            date = record.dateReceivedGov,
                            color = Color(0xFFFFA726),
                            isFirst = true
                        )
                        TimelineItem(
                            label = stringResource(R.string.registered_label),
                            date = record.dateRegistered,
                            color = primaryColor
                        )
                        TimelineItem(
                            label = stringResource(R.string.delivered_label),
                            date = record.dateDeliveredToDomain,
                            color = Color(0xFF66BB6A),
                            isLast = true
                        )
                    }
                }

                // 4. Manager Notes
                if (record.notes.isNotBlank()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawWithContent {
                                    drawContent()
                                    drawLine(
                                        color = goldColor,
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(0f, this.size.height),
                                        strokeWidth = 4.dp.toPx()
                                    )
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Note,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.notes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor
                                )
                                Text(
                                    text = record.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Attachments Section
                if (record.attachments.isNotEmpty()) {
                    Text(
                        text = "${stringResource(R.string.attachments)} (${record.attachments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    val context = LocalContext.current
                    val errorOpenPdf = stringResource(R.string.error_open_pdf)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        record.attachments.forEach { attachment ->
                            if (AttachmentOpener.isPdf(attachment)) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!AttachmentOpener.openPdf(context, attachment)) {
                                                android.widget.Toast.makeText(context, errorOpenPdf, android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Description, null, tint = primaryColor)
                                        Column {
                                            Text(attachment.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("PDF Document", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            } else {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = File(attachment.path),
                                        contentDescription = attachment.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                            .background(Color.LightGray)
                                            .clickable {
                                                val imageAttachments = record.attachments.filterNot(AttachmentOpener::isPdf)
                                                val imagePaths = imageAttachments.map { it.path }
                                                val imageIndex = imageAttachments.indexOf(attachment)
                                                
                                                viewModel.openImageViewer(imagePaths, imageIndex)
                                                onOpenImageViewer()
                                            },
                                        contentScale = ContentScale.Fit,
                                        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) } },
                                        error = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) { Icon(painterResource(android.R.drawable.stat_notify_error), null, tint = MaterialTheme.colorScheme.error) } }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BrandingFooter()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Removed BrandingFooter here to avoid conflict with DashboardScreen.kt
// It will use the one defined in the same package.

@Composable
fun TimelineItem(
    label: String,
    date: String,
    color: Color,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(if (isFirst) Color.Transparent else Color.Gray.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(if (isLast) Color.Transparent else Color.Gray.copy(alpha = 0.3f))
            )
        }
        Column(
            modifier = Modifier
                .padding(bottom = if (isLast) 0.dp else 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (date.isBlank()) "---" else date,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoRowSmall(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
