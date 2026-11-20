package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FoundationVolumeCalculatorTest {
    @Test fun isolatedFootingsMultiplyCountByRectangularVolume() {
        val r = FoundationVolumeCalculator.isolatedFootings(4, BigDecimal("1.2"), BigDecimal("1.2"), BigDecimal("0.4"))
        assertEquals(0, r.volumePerElementM3.compareTo(BigDecimal("0.576")))
        assertEquals(0, r.totalVolumeM3.compareTo(BigDecimal("2.304")))
    }

    @Test fun stripFootingRunsCalculateEachContinuousRun() {
        val r = FoundationVolumeCalculator.stripFootingRuns(2, BigDecimal("10"), BigDecimal("0.6"), BigDecimal("0.3"))
        assertEquals(0, r.volumePerElementM3.compareTo(BigDecimal("1.8")))
        assertEquals(0, r.totalVolumeM3.compareTo(BigDecimal("3.6")))
    }

    @Test fun gradeBeamRunsCalculateEachRun() {
        val r = FoundationVolumeCalculator.gradeBeamRuns(3, BigDecimal("5"), BigDecimal("0.25"), BigDecimal("0.4"))
        assertEquals(0, r.volumePerElementM3.compareTo(BigDecimal("0.5")))
        assertEquals(0, r.totalVolumeM3.compareTo(BigDecimal("1.5")))
    }

    @Test fun rejectsZeroCount() {
        assertThrows(IllegalArgumentException::class.java) {
            FoundationVolumeCalculator.isolatedFootings(0, BigDecimal("1"), BigDecimal("1"), BigDecimal("1"))
        }
    }

    @Test fun rejectsNegativeDimension() {
        assertThrows(IllegalArgumentException::class.java) {
            FoundationVolumeCalculator.stripFootingRuns(1, BigDecimal("5"), BigDecimal("-0.3"), BigDecimal("0.4"))
        }
    }

    @Test fun rejectsNegativeCount() {
        assertThrows(IllegalArgumentException::class.java) {
            FoundationVolumeCalculator.isolatedFootings(-1, BigDecimal("1"), BigDecimal("1"), BigDecimal("1"))
        }
    }

    @Test fun rejectsZeroLengthAndZeroDepth() {
        assertThrows(IllegalArgumentException::class.java) {
            FoundationVolumeCalculator.isolatedFootings(1, BigDecimal.ZERO, BigDecimal("1"), BigDecimal("1"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            FoundationVolumeCalculator.gradeBeamRuns(1, BigDecimal("1"), BigDecimal("1"), BigDecimal.ZERO)
        }
    }

    @Test fun decimalScaleDoesNotChangeGeometricResult() {
        val result = FoundationVolumeCalculator.isolatedFootings(
            2, BigDecimal("1.200"), BigDecimal("0.5000"), BigDecimal("0.250")
        )
        assertEquals(0, result.volumePerElementM3.compareTo(BigDecimal("0.15")))
        assertEquals(0, result.totalVolumeM3.compareTo(BigDecimal("0.30")))
    }

    @Test fun convertsCentimeterDimensionsToMeters() {
        val result = FoundationVolumeCalculator.isolatedFootings(
            4, BigDecimal("120"), BigDecimal("120"), BigDecimal("40"), LengthUnit.CENTIMETER
        )
        assertEquals(0, result.volumePerElementM3.compareTo(BigDecimal("0.576")))
        assertEquals(0, result.totalVolumeM3.compareTo(BigDecimal("2.304")))
    }

}
