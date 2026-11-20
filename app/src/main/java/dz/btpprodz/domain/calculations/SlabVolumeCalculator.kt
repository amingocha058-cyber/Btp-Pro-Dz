package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/** Geometric volume of a constant-thickness rectangular slab; excludes beams, openings and waste. */
object SlabVolumeCalculator {
    fun calculate(
        count: Int,
        length: BigDecimal,
        width: BigDecimal,
        thickness: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): BigDecimal {
        require(count > 0) { "Count must be greater than zero." }
        require(length > BigDecimal.ZERO && width > BigDecimal.ZERO && thickness > BigDecimal.ZERO) {
            "All slab dimensions must be greater than zero."
        }
        val l = unit.toMeters(length)
        val w = unit.toMeters(width)
        val t = unit.toMeters(thickness)
        return l.multiply(w).multiply(t).multiply(BigDecimal.valueOf(count.toLong()))
    }
}
