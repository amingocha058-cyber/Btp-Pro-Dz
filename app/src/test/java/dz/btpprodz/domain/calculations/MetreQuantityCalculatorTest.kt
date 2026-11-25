package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MetreQuantityCalculatorTest {
    private fun d(value: String) = BigDecimal(value)

    @Test fun volumeMatchesSampleSheetFirstRow() {
        val row = MetreQuantityCalculator.Row("CH3", d("18.20"), d("0.30"), d("0.35"), 2,
            MetreQuantityCalculator.MeasureType.VOLUME_M3)
        assertEquals(0, row.quantity().compareTo(d("3.822")))
    }

    @Test fun volumeMatchesSampleSheetSecondRow() {
        val row = MetreQuantityCalculator.Row("CH4", d("4.65"), d("0.30"), d("0.45"), 2,
            MetreQuantityCalculator.MeasureType.VOLUME_M3)
        assertEquals(0, row.quantity().compareTo(d("1.2555")))
    }

    @Test fun surfaceMatchesSampleSheet() {
        val row = MetreQuantityCalculator.Row("02+02", d("4.70"), d("3.575"), null, 4,
            MetreQuantityCalculator.MeasureType.SURFACE_M2)
        assertEquals(0, row.quantity().compareTo(d("67.210")))
    }

    @Test fun slashWidthMeansLengthTimesCountForSurface() {
        val row = MetreQuantityCalculator.Row("03", d("21.00"), null, null, 2,
            MetreQuantityCalculator.MeasureType.SURFACE_M2)
        assertEquals(0, row.quantity().compareTo(d("42.000")))
    }

    @Test fun totalSumsOnlyMatchingMeasureType() {
        val volume = MetreQuantityCalculator.Row("V1", d("2"), d("1"), d("0.5"), 2,
            MetreQuantityCalculator.MeasureType.VOLUME_M3)
        val surface = MetreQuantityCalculator.Row("S1", d("2"), null, null, 2,
            MetreQuantityCalculator.MeasureType.SURFACE_M2)
        assertEquals(0, MetreQuantityCalculator.total(listOf(volume, surface), MetreQuantityCalculator.MeasureType.VOLUME_M3)
            .compareTo(d("2.000")))
    }

    @Test fun totalRoundsAfterSummingUnroundedRows() {
        val first = MetreQuantityCalculator.Row("V1", d("1.2344"), d("1"), d("1"), 1,
            MetreQuantityCalculator.MeasureType.VOLUME_M3)
        val second = MetreQuantityCalculator.Row("V2", d("1.2344"), d("1"), d("1"), 1,
            MetreQuantityCalculator.MeasureType.VOLUME_M3)
        // Each displayed row would be 1.234, but the correct total is 2.469.
        assertEquals(0, MetreQuantityCalculator.total(
            listOf(first, second), MetreQuantityCalculator.MeasureType.VOLUME_M3
        ).compareTo(d("2.469")))
    }

    @Test fun volumeRequiresThickness() {
        assertThrows(IllegalArgumentException::class.java) {
            MetreQuantityCalculator.Row("V", d("2"), d("1"), null, 1, MetreQuantityCalculator.MeasureType.VOLUME_M3)
        }
    }

    @Test fun rejectsZeroCount() {
        assertThrows(IllegalArgumentException::class.java) {
            MetreQuantityCalculator.Row("S", d("2"), null, null, 0, MetreQuantityCalculator.MeasureType.SURFACE_M2)
        }
    }
}
