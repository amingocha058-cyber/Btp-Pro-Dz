package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/** Geometric concrete volume only; this is not structural design. Dimensions are metres. */
data class LinearElementVolumeResult(
    val volumePerElementM3: BigDecimal,
    val totalVolumeM3: BigDecimal
)

object LinearElementVolumeCalculator {
    fun calculate(count: Int, lengthM: BigDecimal, widthM: BigDecimal, heightM: BigDecimal, unit: LengthUnit = LengthUnit.METER): LinearElementVolumeResult {
        require(count > 0) { "Count must be greater than zero." }
        require(lengthM > BigDecimal.ZERO) { "Length must be greater than zero." }
        require(widthM > BigDecimal.ZERO) { "Width must be greater than zero." }
        require(heightM > BigDecimal.ZERO) { "Height must be greater than zero." }
        val volumePerElement = unit.toMeters(lengthM).multiply(unit.toMeters(widthM)).multiply(unit.toMeters(heightM))
        val total = volumePerElement.multiply(BigDecimal.valueOf(count.toLong()))
        return LinearElementVolumeResult(volumePerElement, total)
    }
}
