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
        assertEquals(0, BigDecimal("120").compareTo(result.totalTtc))
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
    @Test fun roundsHalfUpAtCentimeBoundary() {
        val result = DevisTotalsCalculator.calculate(
            BigDecimal("0.03"), BigDecimal.ZERO, BigDecimal("19")
        )
        assertEquals(BigDecimal("0.01"), result.tvaAmount)
        assertEquals(BigDecimal("0.04"), result.totalTtc)
    }

    @Test fun acceptsOneHundredPercentTva() {
        val result = DevisTotalsCalculator.calculate(
            BigDecimal("250.00"), BigDecimal.ZERO, BigDecimal("100")
        )
        assertEquals(BigDecimal("250.00"), result.tvaAmount)
        assertEquals(BigDecimal("500.00"), result.totalTtc)
    }

    @Test fun rejectsNegativeTva() {
        assertThrows(IllegalArgumentException::class.java) {
            DevisTotalsCalculator.calculate(BigDecimal("100"), BigDecimal.ZERO, BigDecimal("-1"))
        }
    }

}
