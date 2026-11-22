package dz.btpprodz.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/** Immutable financial summary attached to a saved Devis report. Values are Algerian dinars. */
data class ProjectReportSummary(
    val materialsLabel: String,
    val materialsHt: Double,
    val laborLabel: String,
    val laborHt: Double,
    val totalHtLabel: String,
    val totalHt: Double,
    val tvaLabel: String,
    val tvaPercent: Double,
    val tvaAmount: Double,
    val totalTtcLabel: String,
    val totalTtc: Double
) {
    init {
        require(listOf(materialsLabel, laborLabel, totalHtLabel, tvaLabel, totalTtcLabel).all { it.isNotBlank() })
        require(listOf(materialsHt, laborHt, totalHt, tvaPercent, tvaAmount, totalTtc).all { it.isFinite() })
        require(materialsHt >= 0.0 && laborHt >= 0.0 && totalHt >= 0.0 && tvaAmount >= 0.0 && totalTtc >= 0.0)
        require(tvaPercent in 0.0..100.0)
        val expectedHt = java.math.BigDecimal.valueOf(materialsHt)
            .add(java.math.BigDecimal.valueOf(laborHt)).setScale(2, java.math.RoundingMode.HALF_UP)
        val expectedTax = expectedHt.multiply(java.math.BigDecimal.valueOf(tvaPercent))
            .divide(java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP)
        val expectedTtc = expectedHt.add(expectedTax)
        require(expectedHt.compareTo(java.math.BigDecimal.valueOf(totalHt).setScale(2, java.math.RoundingMode.HALF_UP)) == 0)
        require(expectedTax.compareTo(java.math.BigDecimal.valueOf(tvaAmount).setScale(2, java.math.RoundingMode.HALF_UP)) == 0)
        require(expectedTtc.compareTo(java.math.BigDecimal.valueOf(totalTtc).setScale(2, java.math.RoundingMode.HALF_UP)) == 0)
    }
}

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
    fun writePdf(title: String, rows: List<ProjectReportRow>, output: OutputStream, summary: ProjectReportSummary? = null) {
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
                drawBidiText(page.canvas, title.take(100), left, y, pageWidth - left * 2, paint.apply { textSize = 16f })
                paint.textSize = 9f
                y += 32f
                drawBidiText(page.canvas, "Description | Quantity | Unit | Unit price (DA) | Total (DA)", left, y, pageWidth - left * 2, paint)
                y += lineHeight
                chunk.forEach { row ->
                    val label = row.description.take(30)
                    val line = "$label | ${row.quantity} | ${row.unit.take(6)} | ${"%.2f".format(java.util.Locale.US, row.unitPriceDa)} | ${"%.2f".format(java.util.Locale.US, row.totalDa)}"
                    drawBidiText(page.canvas, line, left, y, pageWidth - left * 2, paint)
                    y += lineHeight
                }
                page.canvas.drawText("Page ${index + 1} / ${chunks.size}", left, 820f, paint)
                document.finishPage(page)
            }
            if (summary != null) {
                val pageNumber = chunks.size + 1
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                var y = 54f
                paint.textSize = 15f
                drawBidiText(page.canvas, title.take(100), left, y, pageWidth - left * 2, paint)
                paint.textSize = 11f
                y += 42f
                val summaryLines = listOf(
                    "${summary.materialsLabel}: ${"%.2f".format(java.util.Locale.US, summary.materialsHt)} DA",
                    "${summary.laborLabel}: ${"%.2f".format(java.util.Locale.US, summary.laborHt)} DA",
                    "${summary.totalHtLabel}: ${"%.2f".format(java.util.Locale.US, summary.totalHt)} DA",
                    "${summary.tvaLabel} (${"%.2f".format(java.util.Locale.US, summary.tvaPercent)}%): ${"%.2f".format(java.util.Locale.US, summary.tvaAmount)} DA",
                    "${summary.totalTtcLabel}: ${"%.2f".format(java.util.Locale.US, summary.totalTtc)} DA"
                )
                summaryLines.forEach { line ->
                    drawBidiText(page.canvas, line, left, y, pageWidth - left * 2, paint)
                    y += 28f
                }
                page.canvas.drawText("Page $pageNumber / ${chunks.size + 1}", left, 820f, paint)
                document.finishPage(page)
            }
            document.writeTo(output)
            output.flush()
        } finally {
            document.close()
        }
    }

    private fun drawBidiText(canvas: Canvas, value: String, x: Float, top: Float, width: Int, sourcePaint: Paint): Int {
        val textPaint = TextPaint(sourcePaint)
        val layout = StaticLayout.Builder.obtain(value, 0, value.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height
    }

    fun writeExcel(title: String, rows: List<ProjectReportRow>, output: OutputStream, summary: ProjectReportSummary? = null) {
        require(title.isNotBlank()) { "Report title cannot be blank." }
        ZipOutputStream(output, StandardCharsets.UTF_8).use { zip ->
            fun entry(name: String, xml: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(xml.toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="${excelSheetName(title)}" sheetId="1" r:id="rId1"/></sheets></workbook>"""
            entry("xl/workbook.xml", workbookXml)
            entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            val all = buildList {
                add(listOf("Description", "Quantity", "Unit", "Unit price (DA)", "Total (DA)"))
                rows.forEach { add(listOf(it.description, it.quantity.toString(), it.unit, it.unitPriceDa.toString(), it.totalDa.toString())) }
                summary?.let {
                    add(listOf(it.materialsLabel, "", "", "", it.materialsHt.toString()))
                    add(listOf(it.laborLabel, "", "", "", it.laborHt.toString()))
                    add(listOf(it.totalHtLabel, "", "", "", it.totalHt.toString()))
                    add(listOf("${it.tvaLabel} (${it.tvaPercent}%)", "", "", "", it.tvaAmount.toString()))
                    add(listOf(it.totalTtcLabel, "", "", "", it.totalTtc.toString()))
                }
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
            entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetViews><sheetView workbookViewId="0" rightToLeft="1"/></sheetViews><cols><col min="1" max="1" width="38" customWidth="1"/><col min="2" max="2" width="14" customWidth="1"/><col min="3" max="3" width="12" customWidth="1"/><col min="4" max="5" width="20" customWidth="1"/></cols><sheetData>$sheetRows</sheetData></worksheet>""")
            zip.finish()
        }
        output.flush()
    }

    internal fun excelSheetName(title: String): String {
        val safe = title.replace(Regex("[\\\\/:?*\\[\\]]"), "_")
            .trim().trim('\'')
            .ifBlank { "Devis" }
        return xml(safe.take(31))
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
