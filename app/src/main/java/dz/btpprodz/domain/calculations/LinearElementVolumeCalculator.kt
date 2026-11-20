package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/** Geometric concrete volume only; this is not structural design. Dimensions are metres. */
data class LinearElementVolumeResult(
    val volumePerElementM3: BigDecimal,
    val totalVolumeM3: BigDecimal
)

object LinearElementVolumeCalculator {
    fun calculate(count: Int, lengthM: BigDecimal, widthM: BigDecimal, heightM: BigDecimal): LinearElementVolumeResult {
        require(count > 0) { "Count must be greater than zero." }
        require(lengthM > BigDecimal.ZERO) { "Length must be greater than zero." }
        require(widthM > BigDecimal.ZERO) { "Width must be greater than zero." }
        require(heightM > BigDecimal.ZERO) { "Height must be greater than zero." }
        val unit = lengthM.multiply(widthM).multiply(heightM)
        val total = unit.multiply(BigDecimal.valueOf(count.toLong()))
        return LinearElementVolumeResult(unit, total)
    }
}
