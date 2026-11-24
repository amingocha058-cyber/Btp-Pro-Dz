package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FormworkCalculatorTest {
    @Test fun columnFourSides() {
        val r = FormworkCalculator.columns(12, BigDecimal("0.30"), BigDecimal("0.30"), BigDecimal("3"))
        assertEquals(0, r.areaPerElementM2.compareTo(BigDecimal("3.6")))
        assertEquals(0, r.totalAreaM2.compareTo(BigDecimal("43.2")))
    }
    @Test fun beamTwoSidesAndSoffit() {
        val r = FormworkCalculator.beams(2, BigDecimal("5"), BigDecimal("0.25"), BigDecimal("0.50"))
        assertEquals(0, r.areaPerElementM2.compareTo(BigDecimal("6.25")))
        assertEquals(0, r.totalAreaM2.compareTo(BigDecimal("12.50")))
    }
    @Test fun slabSoffit() {
        val r = FormworkCalculator.slabSoffit(2, BigDecimal("10"), BigDecimal("10"))
        assertEquals(0, r.totalAreaM2.compareTo(BigDecimal("200")))
    }
    @Test fun footingSides() {
        val r = FormworkCalculator.footingSides(12, BigDecimal("1.5"), BigDecimal("1.5"), BigDecimal("0.45"))
        assertEquals(0, r.totalAreaM2.compareTo(BigDecimal("16.20")))
    }
    @Test fun rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            FormworkCalculator.columns(1, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE)
        }
    }
}