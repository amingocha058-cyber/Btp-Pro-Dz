package dz.btpprodz.export

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectReportExporterTest {
    @Test fun createsReadableXlsxPackageWithExpectedRows() {
        val output = ByteArrayOutputStream()
        ProjectReportExporter.writeExcel(
            "Villa & Client",
            listOf(ProjectReportRow("Concrete <C25>", 2.5, "m3", 12000.0, 30000.0)),
            output
        )
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertTrue(entries.containsKey("[Content_Types].xml"))
        assertTrue(entries.containsKey("xl/workbook.xml"))
        assertTrue(entries.containsKey("xl/worksheets/sheet1.xml"))
        val workbook = entries.getValue("xl/workbook.xml")
        assertTrue(workbook.contains("Villa &amp; Client"))
        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet.contains("Concrete &lt;C25&gt;"))
        assertTrue(sheet.contains("<v>2.5</v>"))
        assertTrue(sheet.contains("<v>12000.0</v>"))
        assertTrue(sheet.contains("<v>30000.0</v>"))
    }

    @Test
    fun configuresExcelWorksheetForRightToLeftArabicAndReadableColumns() {
        val output = ByteArrayOutputStream()
        ProjectReportExporter.writeExcel("عرض عربي", listOf(ProjectReportRow("خرسانة", 1.0, "م3", 100.0, 100.0)), output)
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet.contains("rightToLeft=\"1\""))
        assertTrue(sheet.contains("customWidth=\"1\""))
        assertTrue(sheet.contains("خرسانة"))
    }

    @Test fun supportsEmptyReportAndEscapesXmlCharacters() {
        val output = ByteArrayOutputStream()
        ProjectReportExporter.writeExcel("Empty", emptyList(), output)
        assertTrue(output.size() > 0)
        val zip = ZipInputStream(ByteArrayInputStream(output.toByteArray()))
        var foundSheet = false
        while (true) {
            val entry = zip.nextEntry ?: break
            if (entry.name == "xl/worksheets/sheet1.xml") {
                foundSheet = zip.readBytes().toString(Charsets.UTF_8).contains("Description")
            }
        }
        zip.close()
        assertTrue(foundSheet)
    }

    @Test fun sanitizesExcelSheetNamesAndLimitsLength() {
        assertEquals("Villa__Client", ProjectReportExporter.excelSheetName("Villa:/Client"))
        assertEquals("Devis", ProjectReportExporter.excelSheetName("'''"))
        assertEquals(31, ProjectReportExporter.excelSheetName("A".repeat(40)).length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankTitle() {
        ProjectReportExporter.writeExcel(" ", emptyList(), ByteArrayOutputStream())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidNumericReportRows() {
        ProjectReportRow("Concrete", Double.NaN, "m3", 100.0, 100.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReportRowsWhoseStoredTotalDoesNotMatchQuantityAndPrice() {
        ProjectReportRow("Concrete", 2.0, "m3", 100.0, 199.99)
    }

    @Test
    fun acceptsRoundedLineTotalsAtTwoDecimalPlaces() {
        ProjectReportRow("Concrete", 1.0, "m3", 10.005, 10.01)
    }

    @Test
    fun includesCompleteFinancialSummaryInExcelExport() {
        val output = ByteArrayOutputStream()
        val summary = ProjectReportSummary(
            materialsLabel = "مواد HT",
            materialsHt = 320.0,
            laborLabel = "يد عاملة HT",
            laborHt = 150.0,
            totalHtLabel = "المجموع HT",
            totalHt = 470.0,
            tvaLabel = "TVA",
            tvaPercent = 19.0,
            tvaAmount = 89.30,
            totalTtcLabel = "المجموع TTC",
            totalTtc = 559.30
        )
        ProjectReportExporter.writeExcel("Devis Test", emptyList(), output, summary)
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet.contains("مواد HT"))
        assertTrue(sheet.contains("يد عاملة HT"))
        assertTrue(sheet.contains("المجموع HT"))
        assertTrue(sheet.contains("89.3"))
        assertTrue(sheet.contains("559.3"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFinancialSummaryWithInconsistentTotals() {
        ProjectReportSummary(
            materialsLabel = "Materials",
            materialsHt = 320.0,
            laborLabel = "Labor",
            laborHt = 150.0,
            totalHtLabel = "HT",
            totalHt = 471.0,
            tvaLabel = "TVA",
            tvaPercent = 19.0,
            tvaAmount = 89.49,
            totalTtcLabel = "TTC",
            totalTtc = 560.49
        )
    }


    @Test
    fun acceptsZeroVatAndZeroValueLinesInFinancialSummary() {
        val summary = ProjectReportSummary(
            materialsLabel = "Materials HT",
            materialsHt = 0.0,
            laborLabel = "Labor HT",
            laborHt = 0.0,
            totalHtLabel = "Total HT",
            totalHt = 0.0,
            tvaLabel = "TVA",
            tvaPercent = 0.0,
            tvaAmount = 0.0,
            totalTtcLabel = "Total TTC",
            totalTtc = 0.0
        )
        assertEquals(0.0, summary.totalTtc, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsVatRateAboveOneHundredPercent() {
        ProjectReportSummary(
            materialsLabel = "Materials",
            materialsHt = 100.0,
            laborLabel = "Labor",
            laborHt = 0.0,
            totalHtLabel = "HT",
            totalHt = 100.0,
            tvaLabel = "TVA",
            tvaPercent = 101.0,
            tvaAmount = 101.0,
            totalTtcLabel = "TTC",
            totalTtc = 201.0
        )
    }

}
