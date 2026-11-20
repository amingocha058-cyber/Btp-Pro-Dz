package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LinearElementVolumeCalculatorTest {
    @Test
    fun calculatesColumnVolumeAndCount() {
        val result = LinearElementVolumeCalculator.calculate(4, BigDecimal("0.30"), BigDecimal("0.30"), BigDecimal("3"))
        assertEquals(BigDecimal("0.2700"), result.volumePerElementM3)
        assertEquals(BigDecimal("1.0800"), result.totalVolumeM3)
    }

    @Test
    fun calculatesBeamVolume() {
        val result = LinearElementVolumeCalculator.calculate(2, BigDecimal("5"), BigDecimal("0.25"), BigDecimal("0.40"))
        assertEquals(BigDecimal("0.5000"), result.volumePerElementM3)
        assertEquals(BigDecimal("1.0000"), result.totalVolumeM3)
    }

    @Test
    fun rejectsZeroCount() {
        assertThrows(IllegalArgumentException::class.java) {
            LinearElementVolumeCalculator.calculate(0, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        }
    }

    @Test
    fun rejectsNegativeDimension() {
        assertThrows(IllegalArgumentException::class.java) {
            LinearElementVolumeCalculator.calculate(1, BigDecimal.ONE, BigDecimal("-0.2"), BigDecimal.ONE)
        }
    }
}
