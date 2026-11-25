package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Quantity takeoff rules transcribed from the user's sample measurement sheet.
 *
 * Volume table: V = length * width * thickness * count.
 * Surface table: S = length * width * count.
 * A printed "/" in the width column means width is omitted: quantity = length * count.
 *
 * Dimensions are metres. Results are m3 for volume and m2 for surface.
 * This is a takeoff rule, not a structural design calculation.
 */
object MetreQuantityCalculator {
    enum class MeasureType { VOLUME_M3, SURFACE_M2 }

    data class Row(
        val reference: String,
        val lengthM: BigDecimal,
        val widthM: BigDecimal?,
        val thicknessM: BigDecimal?,
        val count: Int,
        val type: MeasureType
    ) {
        init {
            require(reference.isNotBlank()) { "Reference cannot be blank." }
            require(lengthM > BigDecimal.ZERO) { "Length must be greater than zero." }
            require(widthM == null || widthM > BigDecimal.ZERO) { "Width must be positive or omitted." }
            require(count > 0) { "Count must be greater than zero." }
            when (type) {
                MeasureType.VOLUME_M3 -> require(thicknessM != null && thicknessM > BigDecimal.ZERO) {
                    "Volume rows require a positive thickness."
                }
                MeasureType.SURFACE_M2 -> require(thicknessM == null) {
                    "Surface rows do not use thickness."
                }
            }
        }

        fun quantity(): BigDecimal {
            val plan = widthM?.let(lengthM::multiply) ?: lengthM
            val measured = if (type == MeasureType.VOLUME_M3) plan.multiply(requireNotNull(thicknessM)) else plan
            // Keep full precision for aggregation; round only presentation/final totals.
            return measured.multiply(BigDecimal.valueOf(count.toLong()))
        }
    }

    fun total(rows: List<Row>, type: MeasureType): BigDecimal =
        rows.asSequence().filter { it.type == type }.map { it.quantity() }
            .fold(BigDecimal.ZERO, BigDecimal::add).setScale(3, RoundingMode.HALF_UP)
}
