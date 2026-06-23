package com.aistudio.pubgbooster.fxghqz

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.pubgbooster.fxghqz.ui.theme.*
import com.aistudio.pubgbooster.fxghqz.util.CommandResult

@Composable
fun ResultBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    results: List<CommandResult>,
    onRetryFailed: (List<CommandResult>) -> Unit,
    isArabic: Boolean,
    gameAutoLaunched: Boolean = false,
    gameAutoLaunchFailed: Boolean = false
) {
    if (!isOpen) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Stop clicks propagating to the background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(CyberSurface)
                .border(2.dp, Brush.verticalGradient(listOf(CyberPrimary, Color.Transparent)), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}
                .padding(top = 10.dp)
        ) {
            // Drag handle indicator
            Box(
                modifier = Modifier
                    .size(45.dp, 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "نتيجة التفعيل والتحسين" else "Activation Metrics Report",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "ملخص تشغيل أكود المهندس إسلام" else "Elite ADB Optimization Engine results",
                        color = CyberGrayText,
                        fontSize = 11.sp
                    )
                }
                
                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CyberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Statistics Summary Card (e.g., 8 out of 10 succeeded)
            val total = results.size
            val succeeded = results.count { it.success }
            val failed = total - succeeded
            val successRate = if (total > 0) (succeeded.toFloat() / total.toFloat()) else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF04070F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) {
                                "$succeeded من أصل $total أوامر نجحت بنجاح! ✅"
                            } else {
                                "$succeeded of $total commands succeeded! ✅"
                            },
                            color = if (successRate >= 0.8f) CyberGreen else CyberYellow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) {
                                "نسبة النجاح الكلية: ${(successRate * 100).toInt()}% | الأخطاء: $failed"
                            } else {
                                "Overall Success: ${(successRate * 100).toInt()}% | Failed: $failed"
                            },
                            color = CyberGrayText,
                            fontSize = 11.sp
                        )
                    }

                    // Success Indicator Circular Progress Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(60.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { successRate },
                            modifier = Modifier.fillMaxSize(),
                            color = if (successRate >= 0.8f) CyberGreen else CyberYellow,
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        Text(
                            text = "${(successRate * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            val AccentGreen = CyberGreen
            val AccentRed = Color(0xFFFF3366)

            if (gameAutoLaunched) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isArabic) "✅ تم فتح اللعبة تلقائياً بنجاح!" else "✅ Game launched successfully",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGreen
                    )
                }
            }
            if (gameAutoLaunchFailed) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isArabic) "⚠️ تعذر فتح اللعبة تلقائياً — يرجى تشغيلها يدوياً" else "⚠️ Could not auto-launch game — open it manually",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp, color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Actual Implemented Commands / Executed Log" box
            var showExecutedCommandLog by remember { mutableStateOf(false) }
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val context = androidx.compose.ui.platform.LocalContext.current

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09101F).copy(alpha = 0.85f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "📋 الأوامر الفعلية النشطة (سجل التشغيل)" else "📋 Actual Implemented Commands",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Expand/Collapse text
                        TextButton(
                            onClick = { showExecutedCommandLog = !showExecutedCommandLog },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (showExecutedCommandLog) {
                                    if (isArabic) "إخفاء السجل" else "Hide Log"
                                } else {
                                    if (isArabic) "عرض السجل" else "Show Log"
                                },
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showExecutedCommandLog) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isArabic) {
                                "سجل بجميع الأكواد التي تم تفعيلها وتطبيقها بالكامل على هاتفك بنجاح. يمكنك نسخ السجل كاملاً لمعرفة الأكواد المنفذة نهائياً:"
                            } else {
                                "List of all active commands executed and applied to your device. You can copy the entire terminal list:"
                            },
                            color = CyberGrayText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Format the log text for display and for copy
                        val formattedLogs = remember(results) {
                            results.mapIndexed { idx, res ->
                                val label = if (res.success) "✓ SUCCESS" else "✗ FAILED"
                                "# ${idx + 1}: ${res.command}\n# STATUS: $label [${res.timeMs}ms]\n"
                            }.joinToString("\n")
                        }

                        // Terminal container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.Black, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = formattedLogs,
                                    color = CyberGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Copy Button
                        Button(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(formattedLogs))
                                android.widget.Toast.makeText(
                                    context,
                                    if (isArabic) "تم نسخ السجل بالكامل بنجاح! 📋" else "Entire command log copied to clipboard! 📋",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "نسخ السجل بالكامل" else "Copy Complete Log",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output List Scrollable Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { res ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.success) Color(0xFF0C1324) else Color(0xFF1F0E13)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (res.success) CyberGreen.copy(alpha = 0.15f) else CyberAccent.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (res.success) CyberGreen else CyberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = res.command,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (res.success) CyberGreen.copy(alpha = 0.12f) else CyberAccent.copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (res.success) "SUCCESS" else "FAILED",
                                        color = if (res.success) CyberGreen else CyberAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Output and Details Section
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "وقت التنفيذ: ${res.timeMs}ms" else "Elapsed: ${res.timeMs}ms",
                                    color = CyberGrayText,
                                    fontSize = 10.sp
                                )

                                if (!res.deviceCompatible) {
                                    Text(
                                        text = if (isArabic) "غير متوافق تلقائياً ⚠️" else "Incompatible / Fallback ⚠️",
                                        color = CyberYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Expanded output console view
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    if (res.output.isNotBlank()) {
                                        Text(
                                            text = "Stdout:\n${res.output}",
                                            color = CyberGreen.copy(alpha = 0.85f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    if (!res.error.isNullOrBlank()) {
                                        if (res.output.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Stderr:\n${res.error}",
                                            color = CyberAccent.copy(alpha = 0.85f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    if (res.output.isBlank() && res.error.isNullOrBlank()) {
                                        Text(
                                            text = "No console feedback returned.",
                                            color = CyberGrayText.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Buttons (Retry Failed & Dismiss)
            Column(
                modifier = Modifier
                    .fillOpacityWithBackground()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Retry failed ONLY button
                if (failed > 0) {
                    val failedResults = results.filter { !it.success }
                    Button(
                        onClick = {
                            onRetryFailed(failedResults)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) {
                                "إعادة وبث محاولة الأكواد الفاشلة فقط ($failed)"
                            } else {
                                "Retry Failed Only ($failed)"
                            },
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Done dismiss button
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = if (isArabic) "إغلاق التقرير" else "Dismiss Report",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Simple modifier helper function for clean layout
private fun Modifier.fillOpacityWithBackground(): Modifier = this
    .fillMaxWidth()
    .background(Color.Black.copy(alpha = 0.15f))
