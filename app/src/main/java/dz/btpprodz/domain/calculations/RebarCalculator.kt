package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import java.math.RoundingMode

/** Quantity takeoff only. Anchorage, hooks, bends and lap lengths must be supplied from approved drawings/specifications. */
object RebarCalculator {
    data class Result(
        val straightBarLengthPerBarM: BigDecimal,
        val totalStraightLengthM: BigDecimal,
        val unitMassKgPerM: BigDecimal,
        val totalMassKg: BigDecimal
    )

    fun straightBars(count: Int, cutLengthM: BigDecimal, diameterMm: BigDecimal): Result {
        require(count > 0) { "Bar count must be greater than zero." }
        require(cutLengthM > BigDecimal.ZERO) { "Cut length must be greater than zero." }
        require(diameterMm > BigDecimal.ZERO && diameterMm <= BigDecimal("50")) { "Diameter must be greater than zero and no more than 50 mm." }
        val totalLength = cutLengthM.multiply(BigDecimal.valueOf(count.toLong()))
        val unitMass = diameterMm.multiply(diameterMm).divide(BigDecimal("162"), 6, RoundingMode.HALF_UP)
        val totalMass = totalLength.multiply(unitMass).setScale(3, RoundingMode.HALF_UP)
        return Result(cutLengthM, totalLength, unitMass, totalMass)
    }

    /** Number of stirrups = ceil(clearLength / spacing) + 1, including both ends, so spacing is never exceeded. */
    fun stirrups(
        clearLengthM: BigDecimal,
        spacingM: BigDecimal,
        barDiameterMm: BigDecimal,
        cutLengthPerStirrupM: BigDecimal
    ): Result {
        require(clearLengthM > BigDecimal.ZERO) { "Clear length must be greater than zero." }
        require(spacingM > BigDecimal.ZERO) { "Spacing must be greater than zero." }
        require(cutLengthPerStirrupM > BigDecimal.ZERO) { "Stirrup cut length must be greater than zero." }
        val division = clearLengthM.divideAndRemainder(spacingM)
        val intervals = division[0].toBigInteger().add(if (division[1] > BigDecimal.ZERO) java.math.BigInteger.ONE else java.math.BigInteger.ZERO)
        val count = intervals.add(java.math.BigInteger.ONE)
        require(count <= java.math.BigInteger.valueOf(Int.MAX_VALUE.toLong())) { "Too many stirrups." }
        return straightBars(count.toInt(), cutLengthPerStirrupM, barDiameterMm)
    }
}