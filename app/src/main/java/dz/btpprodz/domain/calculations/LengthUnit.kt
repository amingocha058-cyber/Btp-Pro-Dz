package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/**
 * Supported input units for linear dimensions.
 * Values are converted to meters before any calculation.
 */
enum class LengthUnit(val metersPerUnit: BigDecimal) {
    METER(BigDecimal.ONE),
    CENTIMETER(BigDecimal("0.01")),
    MILLIMETER(BigDecimal("0.001"));

    fun toMeters(value: BigDecimal): BigDecimal = value.multiply(metersPerUnit)
}
