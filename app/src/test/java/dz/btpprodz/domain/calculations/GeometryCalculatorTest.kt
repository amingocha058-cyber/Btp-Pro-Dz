package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GeometryCalculatorTest {

    @Test
    fun rectangleInMetersReturnsAreaAndPerimeter() {
        val result = GeometryCalculator.rectangle(
            length = BigDecimal("5"),
            width = BigDecimal("3")
        )

        assertEquals(0, result.areaSquareMeters.compareTo(BigDecimal("15")))
        assertEquals(0, result.perimeterMeters.compareTo(BigDecimal("16")))
    }

    @Test
    fun rectangleConvertsCentimetersToMeters() {
        val result = GeometryCalculator.rectangle(
            length = BigDecimal("500"),
            width = BigDecimal("300"),
            unit = LengthUnit.CENTIMETER
        )

        assertEquals(0, result.areaSquareMeters.compareTo(BigDecimal("15")))
        assertEquals(0, result.perimeterMeters.compareTo(BigDecimal("16")))
    }

    @Test
    fun cuboidReturnsVolumeInCubicMeters() {
        val result = GeometryCalculator.cuboid(
            length = BigDecimal("5"),
            width = BigDecimal("3"),
            height = BigDecimal("2")
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("30")))
    }

    @Test
    fun zeroDimensionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GeometryCalculator.rectangle(
                length = BigDecimal.ZERO,
                width = BigDecimal("3")
            )
        }
    }

    @Test
    fun negativeDimensionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GeometryCalculator.cuboid(
                length = BigDecimal("5"),
                width = BigDecimal("3"),
                height = BigDecimal("-2")
            )
        }
    }

    @Test
    fun rectangleConvertsMillimetersToSquareMeters() {
        val result = GeometryCalculator.rectangle(
            length = BigDecimal("5000"),
            width = BigDecimal("3000"),
            unit = LengthUnit.MILLIMETER
        )

        assertEquals(0, result.areaSquareMeters.compareTo(BigDecimal("15")))
        assertEquals(0, result.perimeterMeters.compareTo(BigDecimal("16")))
    }

    @Test
    fun cuboidConvertsMillimetersToCubicMeters() {
        val result = GeometryCalculator.cuboid(
            length = BigDecimal("1000"),
            width = BigDecimal("500"),
            height = BigDecimal("200"),
            unit = LengthUnit.MILLIMETER
        )

        assertEquals(0, result.volumeCubicMeters.compareTo(BigDecimal("0.1")))
    }

}
