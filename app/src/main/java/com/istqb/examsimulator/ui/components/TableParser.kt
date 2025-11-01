package com.istqb.examsimulator.ui.components

data class TableData(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class ParsedContent(
    val beforeTable: String,
    val tables: List<TableData>,
    val textSegments: List<String> // text between tables
)

object TableParser {
    private val tablePattern = """┌[─┬]+┐[\s\S]*?└[─┴]+┘""".toRegex()
    
    fun parseContent(text: String): ParsedContent {
        val tables = mutableListOf<TableData>()
        val textSegments = mutableListOf<String>()
        
        var lastIndex = 0
        tablePattern.findAll(text).forEach { match ->
            // Add text before this table
            textSegments.add(text.substring(lastIndex, match.range.first).trim())
            
            // Parse the table
            val tableText = match.value
            val table = parseTable(tableText)
            if (table != null) {
                tables.add(table)
            }
            
            lastIndex = match.range.last + 1
        }
        
        // Add remaining text after last table
        if (lastIndex < text.length) {
            textSegments.add(text.substring(lastIndex).trim())
        }
        
        // If no tables found, return all as beforeTable
        val beforeTable = if (tables.isEmpty()) text else ""
        
        return ParsedContent(
            beforeTable = beforeTable,
            tables = tables,
            textSegments = textSegments
        )
    }
    
    private fun parseTable(tableText: String): TableData? {
        try {
            val lines = tableText.lines()
                .filter { it.isNotBlank() }
                .map { it.trim() }
            
            if (lines.size < 3) return null // Need at least top, header, bottom
            
            // Find header row (first row after top border that contains │)
            var headerIndex = -1
            for (i in 1 until lines.size - 1) {
                if (lines[i].contains("│") && !lines[i].startsWith("├")) {
                    headerIndex = i
                    break
                }
            }
            
            if (headerIndex == -1) return null
            
            // Parse header
            val headerCells = lines[headerIndex]
                .split("│")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            // Parse all data rows (skip separators and borders)
            val rows = mutableListOf<List<String>>()
            
            for (i in headerIndex + 1 until lines.size) {
                val line = lines[i]
                // Skip border and separator lines
                if (line.startsWith("├") || 
                    line.startsWith("└") || 
                    line.startsWith("┌") ||
                    line.startsWith("┬") ||
                    line.startsWith("┴")) continue
                
                if (line.contains("│")) {
                    val cells = line
                        .split("│")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    // Only add rows that have content and match column count
                    if (cells.isNotEmpty() && cells.size == headerCells.size) {
                        rows.add(cells)
                    }
                }
            }
            
            return TableData(headers = headerCells, rows = rows)
        } catch (e: Exception) {
            return null
        }
    }
    
    fun hasTable(text: String): Boolean {
        return tablePattern.containsMatchIn(text)
    }
}
