package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SurfaceAreaCalculatorTest {
    @Test
    fun subtractsTotalOpeningsFromCombinedWallArea() {
        val result = SurfaceAreaCalculator.netRectangularArea(
            count = 2,
            lengthM = BigDecimal("5"),
            heightM = BigDecimal("3"),
            totalOpeningsM2 = BigDecimal("4.5")
        )
        assertEquals(0, result.compareTo(BigDecimal("25.5")))
    }

    @Test
    fun allowsNoOpenings() {
        val result = SurfaceAreaCalculator.netRectangularArea(
            1, BigDecimal("4"), BigDecimal("2"), BigDecimal.ZERO
        )
        assertEquals(0, result.compareTo(BigDecimal("8")))
    }

    @Test
    fun rejectsOpeningsLargerThanGrossArea() {
        assertThrows(IllegalArgumentException::class.java) {
            SurfaceAreaCalculator.netRectangularArea(
                1, BigDecimal("4"), BigDecimal("2"), BigDecimal("8.01")
            )
        }
    }

    @Test
    fun rejectsNegativeOpeningArea() {
        assertThrows(IllegalArgumentException::class.java) {
            SurfaceAreaCalculator.netRectangularArea(
                1, BigDecimal("4"), BigDecimal("2"), BigDecimal("-1")
            )
        }
    }
}
