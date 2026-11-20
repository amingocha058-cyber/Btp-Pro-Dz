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

    fun isolatedFootings(count: Int, lengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal) =
        calculate(count, lengthM, widthM, depthM)

    fun stripFootingRuns(count: Int, runLengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal) =
        calculate(count, runLengthM, widthM, depthM)

    fun gradeBeamRuns(count: Int, runLengthM: BigDecimal, widthM: BigDecimal, depthM: BigDecimal) =
        calculate(count, runLengthM, widthM, depthM)

    private fun calculate(count: Int, length: BigDecimal, width: BigDecimal, depth: BigDecimal): Result {
        require(count > 0) { "Count must be greater than zero." }
        require(length > BigDecimal.ZERO && width > BigDecimal.ZERO && depth > BigDecimal.ZERO) {
            "All dimensions must be greater than zero."
        }
        val unit = length.multiply(width).multiply(depth)
        val total = unit.multiply(BigDecimal.valueOf(count.toLong()))
        return Result(unit, total)
    }
}
