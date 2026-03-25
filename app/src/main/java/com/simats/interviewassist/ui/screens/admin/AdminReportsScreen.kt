package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.network.ReportedContentResponse


data class ReportedContent(
    val id: String,
    val type: String, // "experience" or "question"
    val time: String,
    val title: String,
    val snippet: String,
    val reportedBy: String,
    val reason: String,
    val status: String,
    val contentCreator: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onBack: () -> Unit = {},
    onNavigateToReviewDetail: (Int, String, Int?) -> Unit = { _, _, _ -> }
) {
    val reports = remember { mutableStateListOf<ReportedContentResponse>() }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    fun fetchReports() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    reports.clear()
                    reports.addAll(response.body() ?: emptyList())
                } else {
                    Toast.makeText(context, "Failed to load reports", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchReports()
    }

    var selectedReport by remember { mutableStateOf<com.simats.interviewassist.data.network.ReportedContentResponse?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showSheet && selectedReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            ReportDetailSheet(
                reportTitle = selectedReport!!.experienceTitle,
                reportSnippet = selectedReport!!.experienceSnippet,
                contentCreator = selectedReport!!.contentCreator,
                reason = selectedReport!!.reason,
                onKeep = {
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.keepContent(selectedReport!!.id)
                            if (response.isSuccessful) {
                                reports.remove(selectedReport!!)
                                Toast.makeText(context, "Content kept", Toast.LENGTH_SHORT).show()
                                showSheet = false
                            } else {
                                Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRemove = {
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.removeContent(selectedReport!!.id)
                            if (response.isSuccessful) {
                                reports.remove(selectedReport!!)
                                Toast.makeText(context, "Content removed", Toast.LENGTH_SHORT).show()
                                showSheet = false
                            } else {
                                Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Reports",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                        if (reports.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = LocalAppColors.current.errorBg,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    "${reports.size} Pending",
                                    color = LocalAppColors.current.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        if (isLoading && reports.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (reports.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No pending reports", color = LocalAppColors.current.iconTint)
                        }
                    }
                }
                items(reports) { report ->
                    ReportCard(
                        type = report.contentType,
                        title = report.experienceTitle,
                        snippet = report.experienceSnippet,
                        reportedBy = report.reportedBy,
                        reason = report.reason,
                        time = report.timeAgo,
                        contentCreator = report.contentCreator,
                        onReview = if (report.contentType == "experience") ({
                            onNavigateToReviewDetail(report.experienceId, report.experienceTitle.replace("Interview at ", ""), report.id)
                        }) else null,
                        onKeep = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.keepContent(report.id)
                                    if (response.isSuccessful) {
                                        reports.remove(report)
                                        Toast.makeText(context, "Content kept", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onRemove = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.removeContent(report.id)
                                    if (response.isSuccessful) {
                                        reports.remove(report)
                                        Toast.makeText(context, "Content removed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    type: String,
    title: String,
    snippet: String,
    reportedBy: String,
    reason: String,
    time: String,
    contentCreator: String,
    onReview: (() -> Unit)? = null,
    onKeep: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header: Icon, Type, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = LocalAppColors.current.errorBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Flag, null, modifier = Modifier.size(24.dp), tint = LocalAppColors.current.error)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = LocalAppColors.current.primaryHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 12.sp,
                            color = LocalAppColors.current.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(time, fontSize = 12.sp, color = LocalAppColors.current.iconTint)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Preview Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = LocalAppColors.current.background
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Text("Reported content by", fontSize = 12.sp, color = LocalAppColors.current.iconTint)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(contentCreator, fontSize = 12.sp, color = LocalAppColors.current.textSecondary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(snippet, fontSize = 14.sp, color = LocalAppColors.current.textBody, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reporter and Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text("Reported by", fontSize = 12.sp, color = LocalAppColors.current.iconTint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(reportedBy, fontSize = 12.sp, color = LocalAppColors.current.textTitle, fontWeight = FontWeight.Bold)
                }
                Text(reason, fontSize = 12.sp, color = LocalAppColors.current.error, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onReview != null) {
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.borderLight),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp), tint = LocalAppColors.current.textTitle)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review", color = LocalAppColors.current.textTitle, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Button(
                    onClick = onKeep,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.divider),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(18.dp), tint = LocalAppColors.current.textTitle)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keep", color = LocalAppColors.current.textTitle, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp), tint = LocalAppColors.current.surface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove", color = LocalAppColors.current.surface, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetailSheet(
    reportTitle: String,
    reportSnippet: String,
    contentCreator: String,
    reason: String,
    onKeep: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            "Review Report",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.textTitle
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Reported Content", fontSize = 14.sp, color = LocalAppColors.current.iconTint)
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LocalAppColors.current.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(reportTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    reportSnippet,
                    fontSize = 15.sp,
                    color = LocalAppColors.current.textBody,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "By $contentCreator",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.iconTint
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Report Reason", fontSize = 14.sp, color = LocalAppColors.current.iconTint)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            reason,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalAppColors.current.error
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onKeep,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.surfaceHighlight)
            ) {
                Text("Keep Content", color = LocalAppColors.current.textTitle, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onRemove,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error)
            ) {
                Text("Remove Content", color = LocalAppColors.current.surface, fontWeight = FontWeight.Bold)
            }
        }
    }
}
