package dz.btpprodz.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ProjectReportRow(
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPriceDa: Double,
    val totalDa: Double
) {
    init {
        require(description.isNotBlank()) { "Description cannot be blank." }
        require(quantity.isFinite() && quantity >= 0.0) { "Quantity must be finite and non-negative." }
        require(unitPriceDa.isFinite() && unitPriceDa >= 0.0) { "Unit price must be finite and non-negative." }
        require(totalDa.isFinite() && totalDa >= 0.0) { "Total must be finite and non-negative." }
        val expectedTotal = java.math.BigDecimal.valueOf(quantity)
            .multiply(java.math.BigDecimal.valueOf(unitPriceDa))
            .setScale(2, java.math.RoundingMode.HALF_UP)
        val storedTotal = java.math.BigDecimal.valueOf(totalDa)
            .setScale(2, java.math.RoundingMode.HALF_UP)
        require(expectedTotal.compareTo(storedTotal) == 0) {
            "Report row total must match quantity multiplied by unit price."
        }
    }
}

/** Exports the same immutable report rows to a paginated PDF or a standards-based XLSX workbook. */
object ProjectReportExporter {
    fun writePdf(title: String, rows: List<ProjectReportRow>, output: OutputStream) {
        require(title.isNotBlank()) { "Report title cannot be blank." }
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val pageWidth = 595
        val pageHeight = 842
        val left = 32f
        val lineHeight = 22f
        val rowsPerPage = 30
        val chunks = rows.chunked(rowsPerPage).ifEmpty { listOf(emptyList()) }
        try {
            chunks.forEachIndexed { index, chunk ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create())
                var y = 42f
                page.canvas.drawText(title, left, y, paint.apply { textSize = 16f })
                paint.textSize = 9f
                y += 32f
                page.canvas.drawText("Description | Quantity | Unit | Unit price (DA) | Total (DA)", left, y, paint)
                y += lineHeight
                chunk.forEach { row ->
                    val label = row.description.take(38)
                    val line = "$label | ${row.quantity} | ${row.unit.take(8)} | ${"%.2f".format(java.util.Locale.US, row.unitPriceDa)} | ${"%.2f".format(java.util.Locale.US, row.totalDa)}"
                    page.canvas.drawText(line, left, y, paint)
                    y += lineHeight
                }
                page.canvas.drawText("Page ${index + 1} / ${chunks.size}", left, 820f, paint)
                document.finishPage(page)
            }
            document.writeTo(output)
            output.flush()
        } finally {
            document.close()
        }
    }

    fun writeExcel(title: String, rows: List<ProjectReportRow>, output: OutputStream) {
        require(title.isNotBlank()) { "Report title cannot be blank." }
        ZipOutputStream(output, StandardCharsets.UTF_8).use { zip ->
            fun entry(name: String, xml: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(xml.toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="${xml(title.take(31))}" sheetId="1" r:id="rId1"/></sheets></workbook>"""
            entry("xl/workbook.xml", workbookXml)
            entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            val all = buildList {
                add(listOf("Description", "Quantity", "Unit", "Unit price (DA)", "Total (DA)"))
                rows.forEach { add(listOf(it.description, it.quantity.toString(), it.unit, it.unitPriceDa.toString(), it.totalDa.toString())) }
            }
            val sheetRows = all.mapIndexed { r, cells ->
                val cellsXml = cells.mapIndexed { c, value ->
                    val ref = "${column(c + 1)}${r + 1}"
                    if (r > 0 && (c == 1 || c == 3 || c == 4)) {
                        """<c r="$ref"><v>$value</v></c>"""
                    } else {
                        """<c r="$ref" t="inlineStr"><is><t xml:space="preserve">${xml(value)}</t></is></c>"""
                    }
                }.joinToString("")
                """<row r="${r + 1}">$cellsXml</row>"""
            }.joinToString("")
            entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$sheetRows</sheetData></worksheet>""")
            zip.finish()
        }
        output.flush()
    }

    private fun column(number: Int): String {
        var n = number
        val result = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            result.append(('A'.code + rem).toChar())
            n = (n - 1) / 26
        }
        return result.reverse().toString()
    }

    private fun xml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
