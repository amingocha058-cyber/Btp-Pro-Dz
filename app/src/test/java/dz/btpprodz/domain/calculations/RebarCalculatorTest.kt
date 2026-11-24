package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RebarCalculatorTest {
    @Test fun straightBarsCalculateLengthAndMass() {
        val r = RebarCalculator.straightBars(10, BigDecimal("6"), BigDecimal("12"))
        assertEquals(0, r.totalStraightLengthM.compareTo(BigDecimal("60")))
        assertEquals(0, r.totalMassKg.compareTo(BigDecimal("53.333")))
    }
    @Test fun stirrupCountIncludesBothEnds() {
        val r = RebarCalculator.stirrups(BigDecimal("3"), BigDecimal("0.15"), BigDecimal("8"), BigDecimal("1.1"))
        assertEquals(21, r.totalStraightLengthM.divide(BigDecimal("1.1")).toInt())
    }
    @Test fun rejectsZeroDiameter() {
        assertThrows(IllegalArgumentException::class.java) {
            RebarCalculator.straightBars(4, BigDecimal("6"), BigDecimal.ZERO)
        }
    }
    @Test fun rejectsZeroSpacing() {
        assertThrows(IllegalArgumentException::class.java) {
            RebarCalculator.stirrups(BigDecimal("3"), BigDecimal.ZERO, BigDecimal("8"), BigDecimal("1"))
        }
    }
}