package dz.btpprodz.export

import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProjectReportExportInstrumentedTest {
    @Test
    fun createsPdfWithArabicDevisSummaryOnAndroidRuntime() {
        val output = ByteArrayOutputStream()
        val summary = ProjectReportSummary(
            materialsLabel = "مجموع المواد HT",
            materialsHt = 320.0,
            laborLabel = "مجموع اليد العاملة HT",
            laborHt = 150.0,
            totalHtLabel = "المجموع HT",
            totalHt = 470.0,
            tvaLabel = "TVA",
            tvaPercent = 19.0,
            tvaAmount = 89.30,
            totalTtcLabel = "المجموع TTC",
            totalTtc = 559.30
        )
        ProjectReportExporter.writePdf(
            "عرض سعر - مشروع تجريبي",
            listOf(
                ProjectReportRow("خرسانة مسلحة", 2.0, "م³", 160.0, 320.0),
                ProjectReportRow("وصف طويل لبند الخرسانة المسلحة مع النقل والضخ واليد العاملة ومستلزمات التنفيذ والتجهيزات الإضافية للمشروع", 1.0, "بند", 250.0, 250.0)
            ),
            output,
            summary
        )
        val bytes = output.toByteArray()
        assertTrue("PDF output should not be empty", bytes.size > 500)
        assertTrue("PDF header must be valid", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII) == "%PDF")
    }

    @Test
    fun paginatesLongDevisAndCountsFinancialSummaryPage() {
        val output = ByteArrayOutputStream()
        val rows = (1..40).map { index ->
            ProjectReportRow("بند أشغال رقم $index", 1.0, "وحدة", 10.0, 10.0)
        }
        val summary = ProjectReportSummary(
            materialsLabel = "مجموع المواد HT",
            materialsHt = 400.0,
            laborLabel = "مجموع اليد العاملة HT",
            laborHt = 0.0,
            totalHtLabel = "المجموع HT",
            totalHt = 400.0,
            tvaLabel = "TVA",
            tvaPercent = 19.0,
            tvaAmount = 76.0,
            totalTtcLabel = "المجموع TTC",
            totalTtc = 476.0
        )

        ProjectReportExporter.writePdf("عرض سعر متعدد الصفحات", rows, output, summary)

        val pdf = output.toByteArray().toString(Charsets.ISO_8859_1)
        assertTrue("PDF should contain a three-page page tree (two detail pages plus summary)", Regex("/Count\\s+3\\b").containsMatchIn(pdf))
    }

    @Test
    fun createsReadableRtlXlsxPackageOnAndroidRuntime() {
        val output = ByteArrayOutputStream()
        ProjectReportExporter.writeExcel(
            "عرض سعر عربي",
            listOf(ProjectReportRow("خرسانة مسلحة", 2.0, "م³", 160.0, 320.0)),
            output
        )
        val entries = mutableMapOf<String, String>()
        ZipInputStream(output.toByteArray().inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertTrue(entries.containsKey("xl/workbook.xml"))
        val sheet = entries["xl/worksheets/sheet1.xml"].orEmpty()
        assertTrue(sheet.contains("rightToLeft=\"1\""))
        assertTrue(sheet.contains("خرسانة مسلحة"))
    }
}
