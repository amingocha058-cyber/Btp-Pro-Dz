package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/**
 * Geometric concrete quantities for rectangular foundation elements.
 * This is a quantity takeoff helper, not a structural design/check.
 * Strip footing and grade-beam lengths are entered per segment/run.
 */
object FoundationVolumeCalculator {
    data class Result(
        val volumePerElementM3: BigDecimal,
        val totalVolumeM3: BigDecimal
    )

    fun isolatedFootings(count: Int, lengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal, unit: LengthUnit = LengthUnit.METER) =
        calculate(count, lengthM, widthM, depthM, unit)

    fun stripFootingRuns(count: Int, runLengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal, unit: LengthUnit = LengthUnit.METER) =
        calculate(count, runLengthM, widthM, depthM, unit)

    fun gradeBeamRuns(count: Int, runLengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal, unit: LengthUnit = LengthUnit.METER) =
        calculate(count, runLengthM, widthM, depthM, unit)

    private fun calculate(count: Int, length: BigDecimal, width: BigDecimal, depth: BigDecimal, unit: LengthUnit): Result {
        require(count > 0) { "Count must be greater than zero." }
        require(length > BigDecimal.ZERO && width > BigDecimal.ZERO && depth > BigDecimal.ZERO) {
            "All dimensions must be greater than zero."
        }
        val volumePerElement = unit.toMeters(length).multiply(unit.toMeters(width)).multiply(unit.toMeters(depth))
        val total = volumePerElement.multiply(BigDecimal.valueOf(count.toLong()))
        return Result(volumePerElement, total)
    }
}
