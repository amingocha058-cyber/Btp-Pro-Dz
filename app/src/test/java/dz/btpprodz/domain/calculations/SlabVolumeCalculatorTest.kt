package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SlabVolumeCalculatorTest {
    @Test fun calculatesOneSlabReferenceVolume() {
        assertEquals(
            0,
            SlabVolumeCalculator.calculate(1, BigDecimal("5"), BigDecimal("4"), BigDecimal("0.15"))
                .compareTo(BigDecimal("3.00"))
        )
    }

    @Test fun multipliesBySlabCount() {
        assertEquals(
            0,
            SlabVolumeCalculator.calculate(3, BigDecimal("5"), BigDecimal("4"), BigDecimal("0.15"))
                .compareTo(BigDecimal("9.00"))
        )
    }

    @Test fun convertsMillimetersBeforeMultiplying() {
        assertEquals(
            0,
            SlabVolumeCalculator.calculate(
                1, BigDecimal("5000"), BigDecimal("4000"), BigDecimal("150"), LengthUnit.MILLIMETER
            ).compareTo(BigDecimal("3.000000000"))
        )
    }

    @Test fun rejectsZeroCountAndNonPositiveDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            SlabVolumeCalculator.calculate(0, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SlabVolumeCalculator.calculate(1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO)
        }
    }

    @Test fun convertsCentimetersAndMetersToSameVolume() {
        val meters = SlabVolumeCalculator.calculate(
            1, BigDecimal("5"), BigDecimal("4"), BigDecimal("0.15"), LengthUnit.METER
        )
        val centimeters = SlabVolumeCalculator.calculate(
            1, BigDecimal("500"), BigDecimal("400"), BigDecimal("15"), LengthUnit.CENTIMETER
        )
        assertEquals(0, meters.compareTo(centimeters))
        assertEquals(0, centimeters.compareTo(BigDecimal("3.00")))
    }

    @Test fun rejectsNegativeDimensionsAndNegativeCount() {
        assertThrows(IllegalArgumentException::class.java) {
            SlabVolumeCalculator.calculate(-1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SlabVolumeCalculator.calculate(1, BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ONE)
        }
    }

}
