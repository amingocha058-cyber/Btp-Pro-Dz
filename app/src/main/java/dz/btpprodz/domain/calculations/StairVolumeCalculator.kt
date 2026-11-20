package dz.btpprodz.domain.calculations

import java.math.BigDecimal

data class StairVolumeResult(
    val waistSlabVolumeM3: BigDecimal,
    val stepWedgesVolumeM3: BigDecimal,
    val totalVolumeM3: BigDecimal
)

/**
 * Geometric concrete estimate for a straight stair flight.
 * Assumes a constant-thickness waist slab measured along the slope and triangular step wedges.
 * Excludes landings, supporting beams, reinforcement, openings and waste.
 */
object StairVolumeCalculator {
    fun calculate(
        count: Int,
        stairWidth: BigDecimal,
        slopingLength: BigDecimal,
        waistThickness: BigDecimal,
        stepCount: Int,
        treadDepth: BigDecimal,
        riserHeight: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): StairVolumeResult {
        require(count > 0) { "Count must be greater than zero." }
        require(stepCount > 0) { "Step count must be greater than zero." }
        require(
            stairWidth > BigDecimal.ZERO && slopingLength > BigDecimal.ZERO &&
                waistThickness > BigDecimal.ZERO && treadDepth > BigDecimal.ZERO &&
                riserHeight > BigDecimal.ZERO
        ) { "All stair dimensions must be greater than zero." }

        val widthM = unit.toMeters(stairWidth)
        val slopeM = unit.toMeters(slopingLength)
        val thicknessM = unit.toMeters(waistThickness)
        val treadM = unit.toMeters(treadDepth)
        val riserM = unit.toMeters(riserHeight)
        val flights = BigDecimal.valueOf(count.toLong())
        val steps = BigDecimal.valueOf(stepCount.toLong())
        val triangularStepFactor = steps.multiply(steps.add(BigDecimal.ONE))
            .divide(TWO)

        val waist = widthM.multiply(slopeM).multiply(thicknessM).multiply(flights)
        val wedges = widthM.multiply(treadM).multiply(riserM)
            .multiply(triangularStepFactor).multiply(flights)
        return StairVolumeResult(waist, wedges, waist.add(wedges))
    }

    private val TWO = BigDecimal("2")
}
