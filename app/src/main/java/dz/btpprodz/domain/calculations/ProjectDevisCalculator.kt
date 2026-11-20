package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import java.math.RoundingMode

data class DevisCostLine(
    val quantity: Double,
    val unitPrice: Double,
    val storedTotal: Double
)

/** Validates saved project cost lines and aggregates them into deterministic Devis totals. */
object ProjectDevisCalculator {
    fun calculate(
        materialLines: List<DevisCostLine>,
        laborLines: List<DevisCostLine>,
        tvaPercent: BigDecimal
    ): DevisTotals {
        fun total(lines: List<DevisCostLine>): BigDecimal = lines.fold(BigDecimal.ZERO) { sum, line ->
            require(
                line.quantity.isFinite() && line.quantity > 0.0 &&
                    line.unitPrice.isFinite() && line.unitPrice >= 0.0 &&
                    line.storedTotal.isFinite() && line.storedTotal >= 0.0
            ) { "Invalid quote line values." }
            val expected = BigDecimal.valueOf(line.quantity)
                .multiply(BigDecimal.valueOf(line.unitPrice))
                .setScale(2, RoundingMode.HALF_UP)
            val stored = BigDecimal.valueOf(line.storedTotal).setScale(2, RoundingMode.HALF_UP)
            require(expected.compareTo(stored) == 0) {
                "Stored line total does not match quantity multiplied by unit price."
            }
            sum.add(stored)
        }
        return DevisTotalsCalculator.calculate(total(materialLines), total(laborLines), tvaPercent)
    }
}
