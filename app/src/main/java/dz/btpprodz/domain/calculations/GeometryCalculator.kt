package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/**
 * Pure geometry calculations.
 *
 * The calculator has no Android dependencies and can be tested on the JVM.
 * All returned measurements are expressed in square meters, meters, or cubic meters.
 */
object GeometryCalculator {

    data class RectangleResult(
        val areaSquareMeters: BigDecimal,
        val perimeterMeters: BigDecimal
    )

    data class CuboidResult(
        val volumeCubicMeters: BigDecimal
    )

    fun rectangle(
        length: BigDecimal,
        width: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): RectangleResult {
        requirePositive(length, "length")
        requirePositive(width, "width")

        val lengthMeters = unit.toMeters(length)
        val widthMeters = unit.toMeters(width)

        return RectangleResult(
            areaSquareMeters = lengthMeters.multiply(widthMeters),
            perimeterMeters = lengthMeters.add(widthMeters).multiply(TWO)
        )
    }

    fun cuboid(
        length: BigDecimal,
        width: BigDecimal,
        height: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): CuboidResult {
        requirePositive(length, "length")
        requirePositive(width, "width")
        requirePositive(height, "height")

        val lengthMeters = unit.toMeters(length)
        val widthMeters = unit.toMeters(width)
        val heightMeters = unit.toMeters(height)

        return CuboidResult(
            volumeCubicMeters = lengthMeters
                .multiply(widthMeters)
                .multiply(heightMeters)
        )
    }

    private fun requirePositive(value: BigDecimal, fieldName: String) {
        require(value > BigDecimal.ZERO) { "$fieldName must be greater than zero" }
    }

    private val TWO = BigDecimal("2")
}
