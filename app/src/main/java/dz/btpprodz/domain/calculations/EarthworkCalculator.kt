package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/**
 * Basic excavation and backfill quantity calculations.
 *
 * Dimensions are converted to meters before calculating. Results are in cubic meters.
 * These calculations describe geometric quantities only; they do not include
 * soil bulking, compaction, slopes, shoring, or construction tolerances.
 */
object EarthworkCalculator {

    data class ExcavationResult(
        val volumeCubicMeters: BigDecimal
    )

    data class BackfillResult(
        val netVolumeCubicMeters: BigDecimal
    )

    fun rectangularExcavation(
        length: BigDecimal,
        width: BigDecimal,
        depth: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): ExcavationResult {
        requirePositive(length, "length")
        requirePositive(width, "width")
        requirePositive(depth, "depth")

        val lengthMeters = unit.toMeters(length)
        val widthMeters = unit.toMeters(width)
        val depthMeters = unit.toMeters(depth)

        return ExcavationResult(
            volumeCubicMeters = lengthMeters
                .multiply(widthMeters)
                .multiply(depthMeters)
        )
    }

    fun trenchExcavation(
        length: BigDecimal,
        width: BigDecimal,
        depth: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): ExcavationResult = rectangularExcavation(length, width, depth, unit)

    /**
     * Calculates the geometric backfill quantity:
     * excavation volume minus the volume occupied by the structure.
     */
    fun backfill(
        excavationVolume: BigDecimal,
        occupiedVolume: BigDecimal
    ): BackfillResult {
        requirePositive(excavationVolume, "excavationVolume")
        require(occupiedVolume >= BigDecimal.ZERO) {
            "occupiedVolume must not be negative"
        }
        require(occupiedVolume <= excavationVolume) {
            "occupiedVolume cannot exceed excavationVolume"
        }

        return BackfillResult(
            netVolumeCubicMeters = excavationVolume.subtract(occupiedVolume)
        )
    }

    private fun requirePositive(value: BigDecimal, fieldName: String) {
        require(value > BigDecimal.ZERO) {
            "$fieldName must be greater than zero"
        }
    }
}
