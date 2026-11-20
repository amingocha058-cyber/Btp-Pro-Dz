package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DevisTotalsCalculatorTest {
    @Test fun calculatesHtTvaAndTtcWithCentimeRounding() {
        val result = DevisTotalsCalculator.calculate(
            BigDecimal("1000.10"), BigDecimal("500.20"), BigDecimal("19")
        )
        assertEquals(BigDecimal("1500.30"), result.totalHt)
        assertEquals(BigDecimal("285.06"), result.tvaAmount)
        assertEquals(BigDecimal("1785.36"), result.totalTtc)
    }

    @Test fun allowsZeroTva() {
        val result = DevisTotalsCalculator.calculate(BigDecimal("120"), BigDecimal.ZERO, BigDecimal.ZERO)
        assertEquals(0, BigDecimal.ZERO.compareTo(result.tvaAmount))
        assertEquals(BigDecimal("120"), result.totalTtc)
    }

    @Test fun rejectsTvaAboveOneHundred() {
        assertThrows(IllegalArgumentException::class.java) {
            DevisTotalsCalculator.calculate(BigDecimal("100"), BigDecimal.ZERO, BigDecimal("101"))
        }
    }

    @Test fun rejectsNegativeCosts() {
        assertThrows(IllegalArgumentException::class.java) {
            DevisTotalsCalculator.calculate(BigDecimal("-1"), BigDecimal.ZERO, BigDecimal("19"))
        }
    }
}
