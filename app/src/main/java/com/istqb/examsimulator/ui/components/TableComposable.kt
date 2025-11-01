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
    
    // Format text with proper spacing for readability
    fun formatQuestionText(input: String): String {
        return input
            // Split into sentences and add spacing for better readability
            .replace(". ", ".\n")
            .replace("? ", "?\n")
            .replace(": ", ":\n")
            .trim()
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (parsedContent.beforeTable.isNotEmpty()) {
            // No tables, display as normal text
            Text(
                text = formatQuestionText(parsedContent.beforeTable),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        } else {
            // Has tables, display text segments and tables
            parsedContent.textSegments.forEachIndexed { index, textSegment ->
                if (textSegment.isNotEmpty()) {
                    Text(
                        text = formatQuestionText(textSegment),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
                
                // Display table after this text segment (if exists)
                if (index < parsedContent.tables.size) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResponsiveTable(tableData = parsedContent.tables[index])
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
