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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun QuestionWithTables(
    text: String,
    modifier: Modifier = Modifier
) {
    val parsedContent = TableParser.parseContent(text)
    
    // Format text to add line breaks for better readability
    fun formatText(input: String): String {
        return input
            // Add line break only after sentence endings followed by numbered lists
            .replace(Regex("([.!?])\\s+(\\d+\\.)")) { match ->
                val punctuation = match.groupValues[1]
                val number = match.groupValues[2]
                "$punctuation\n\n$number"
            }
            // Add line break after long sentences ending with period
            .replace(Regex("([.])\\s+([A-ZÇĞIÖŞÜ][a-zçğıöşü]{3,})")) { match ->
                val period = match.groupValues[1]
                val word = match.groupValues[2]
                "$period\n\n$word"
            }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (parsedContent.beforeTable.isNotEmpty()) {
            // No tables, display as normal text with formatting
            Text(
                text = formatText(parsedContent.beforeTable),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp
            )
        } else {
            // Has tables, display text segments and tables
            parsedContent.textSegments.forEachIndexed { index, textSegment ->
                if (textSegment.isNotEmpty()) {
                    Text(
                        text = formatText(textSegment),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp
                    )
                }
                
                // Display table after this text segment (if exists)
                if (index < parsedContent.tables.size) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ResponsiveTable(tableData = parsedContent.tables[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
