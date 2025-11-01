package com.istqb.examsimulator.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResponsiveTable(
    tableData: TableData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
            ) {
                tableData.headers.forEach { header ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Data Rows
            tableData.rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                ) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                                .border(
                                    width = 0.3.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats raw question text from JSON into PDF-like format with:
 * - Proper line spacing and paragraphs
 * - Bullet points (•) for lists
 * - Indentation for sub-items (using spaces)
 */
private fun formatQuestionText(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = rawText.split("\n")
        
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()
            
            when {
                // Empty line - add spacing
                trimmedLine.isEmpty() && index > 0 -> {
                    append("\n")
                }
                
                // Bullet point pattern: "• text" or "○ text"
                trimmedLine.startsWith("•") || trimmedLine.startsWith("○") -> {
                    append("\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                        append("• ")
                    }
                    append(trimmedLine.removePrefix("•").removePrefix("○").trim())
                }
                
                // Sub-item pattern: starts with "o " (lowercase o)
                trimmedLine.startsWith("o ") -> {
                    append("\n  ") // Indent with spaces
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append("○ ")
                    }
                    append(trimmedLine.removePrefix("o ").trim())
                }
                
                // Regular line
                else -> {
                    if (index > 0) append("\n")
                    append(trimmedLine)
                }
            }
        }
    }
}

@Composable
fun QuestionWithTables(
    text: String,
    modifier: Modifier = Modifier
) {
    val parsedContent = TableParser.parseContent(text)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Add horizontal padding for small screens
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (parsedContent.beforeTable.isNotEmpty()) {
            // No tables - format and display text
            Text(
                text = formatQuestionText(parsedContent.beforeTable),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Has tables - display formatted text segments and tables
            parsedContent.textSegments.forEachIndexed { index, textSegment ->
                if (textSegment.isNotEmpty()) {
                    Text(
                        text = formatQuestionText(textSegment),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Display table after this text segment (if exists)
                if (index < parsedContent.tables.size) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ResponsiveTable(
                        tableData = parsedContent.tables[index],
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
