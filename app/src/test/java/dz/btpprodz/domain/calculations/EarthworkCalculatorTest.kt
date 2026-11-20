package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EarthworkCalculatorTest {

    @Test
    fun rectangularExcavationCalculatesCubicMeters() {
        val result = EarthworkCalculator.rectangularExcavation(
            length = BigDecimal("4"),
            width = BigDecimal("3"),
            depth = BigDecimal("1.5")
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("18")))
    }

    @Test
    fun excavationConvertsCentimetersToMeters() {
        val result = EarthworkCalculator.rectangularExcavation(
            length = BigDecimal("400"),
            width = BigDecimal("300"),
            depth = BigDecimal("150"),
            unit = LengthUnit.CENTIMETER
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("18")))
    }

    @Test
    fun trenchExcavationUsesSameGeometricVolumeFormula() {
        val result = EarthworkCalculator.trenchExcavation(
            length = BigDecimal("10"),
            width = BigDecimal("0.6"),
            depth = BigDecimal("0.8")
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("4.8")))
    }

    @Test
    fun backfillSubtractsOccupiedStructureVolume() {
        val result = EarthworkCalculator.backfill(
            excavationVolume = BigDecimal("18"),
            occupiedVolume = BigDecimal("6.5")
        )

        assertEquals(0, result.netVolumeCubicMeters.compareTo(BigDecimal("11.5")))
    }

    @Test
    fun backfillRejectsOccupiedVolumeGreaterThanExcavation() {
        assertThrows(IllegalArgumentException::class.java) {
            EarthworkCalculator.backfill(
                excavationVolume = BigDecimal("5"),
                occupiedVolume = BigDecimal("6")
            )
        }
    }

    @Test
    fun zeroExcavationDimensionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            EarthworkCalculator.rectangularExcavation(
                length = BigDecimal("4"),
                width = BigDecimal("3"),
                depth = BigDecimal.ZERO
            )
        }
    }

    @Test
    fun excavationConvertsMillimetersToCubicMeters() {
        val result = EarthworkCalculator.rectangularExcavation(
            length = BigDecimal("1000"),
            width = BigDecimal("500"),
            depth = BigDecimal("200"),
            unit = LengthUnit.MILLIMETER
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("0.1")))
    }

    @Test
    fun backfillAllowsOccupiedVolumeEqualToExcavation() {
        val result = EarthworkCalculator.backfill(
            excavationVolume = BigDecimal("5"),
            occupiedVolume = BigDecimal("5")
        )

        assertEquals(0, result.netVolumeCubicMeters.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun backfillAllowsZeroOccupiedVolume() {
        val result = EarthworkCalculator.backfill(
            excavationVolume = BigDecimal("5"),
            occupiedVolume = BigDecimal.ZERO
        )

        assertEquals(0, result.netVolumeCubicMeters.compareTo(BigDecimal("5")))
    }


    @Test
    fun backfillRejectsNegativeOccupiedVolume() {
        assertThrows(IllegalArgumentException::class.java) {
            EarthworkCalculator.backfill(BigDecimal("5"), BigDecimal("-0.1"))
        }
    }

    @Test
    fun backfillRejectsZeroExcavationVolume() {
        assertThrows(IllegalArgumentException::class.java) {
            EarthworkCalculator.backfill(BigDecimal.ZERO, BigDecimal.ZERO)
        }
    }

    @Test
    fun excavationRejectsNegativeDimension() {
        assertThrows(IllegalArgumentException::class.java) {
            EarthworkCalculator.trenchExcavation(
                BigDecimal("10"), BigDecimal("0.6"), BigDecimal("-0.8")
            )
        }
    }

}
