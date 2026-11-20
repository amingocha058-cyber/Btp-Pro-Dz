package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StairVolumeCalculatorTest {
    @Test fun calculatesWaistAndStepWedgeVolumesAgainstReference() {
        val result = StairVolumeCalculator.calculate(
            count = 1,
            stairWidth = BigDecimal("1.2"),
            slopingLength = BigDecimal("4.0"),
            waistThickness = BigDecimal("0.15"),
            stepCount = 10,
            treadDepth = BigDecimal("0.30"),
            riserHeight = BigDecimal("0.17")
        )
        assertEquals(0, result.waistSlabVolumeM3.compareTo(BigDecimal("0.7200")))
        assertEquals(0, result.stepWedgesVolumeM3.compareTo(BigDecimal("3.36600")))
        assertEquals(0, result.totalVolumeM3.compareTo(BigDecimal("4.08600")))
    }

    @Test fun multipliesCompleteFlightVolumeByCount() {
        val result = StairVolumeCalculator.calculate(
            2, BigDecimal("1"), BigDecimal("3"), BigDecimal("0.12"),
            5, BigDecimal("0.28"), BigDecimal("0.16")
        )
        assertEquals(0, result.totalVolumeM3.compareTo(BigDecimal("2.0640")))
    }

    @Test fun convertsMillimetersToMeters() {
        val result = StairVolumeCalculator.calculate(
            1, BigDecimal("1200"), BigDecimal("4000"), BigDecimal("150"),
            10, BigDecimal("300"), BigDecimal("170"), LengthUnit.MILLIMETER
        )
        assertEquals(0, result.totalVolumeM3.compareTo(BigDecimal("4.086000000")))
    }

    @Test fun rejectsInvalidCountsAndDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            StairVolumeCalculator.calculate(
                1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0, BigDecimal.ONE, BigDecimal.ONE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            StairVolumeCalculator.calculate(
                1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, 1, BigDecimal.ONE, BigDecimal.ONE
            )
        }
    }
}
