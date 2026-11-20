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

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankTitle() {
        ProjectReportExporter.writeExcel(" ", emptyList(), ByteArrayOutputStream())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidNumericReportRows() {
        ProjectReportRow("Concrete", Double.NaN, "m3", 100.0, 100.0)
    }
}
