package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import java.math.RoundingMode

/** Deterministic quote totals. Monetary inputs are DA; tax rate is percent. */
data class DevisTotals(
    val materialsHt: BigDecimal,
    val laborHt: BigDecimal,
    val totalHt: BigDecimal,
    val tvaPercent: BigDecimal,
    val tvaAmount: BigDecimal,
    val totalTtc: BigDecimal
)

object DevisTotalsCalculator {
    fun calculate(materialsHt: BigDecimal, laborHt: BigDecimal, tvaPercent: BigDecimal): DevisTotals {
        require(materialsHt >= BigDecimal.ZERO) { "Materials total cannot be negative." }
        require(laborHt >= BigDecimal.ZERO) { "Labor total cannot be negative." }
        require(tvaPercent >= BigDecimal.ZERO && tvaPercent <= BigDecimal("100")) {
            "TVA rate must be between 0 and 100 percent."
        }
        val ht = materialsHt.add(laborHt)
        val tax = ht.multiply(tvaPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val ttc = ht.add(tax)
        return DevisTotals(materialsHt, laborHt, ht, tvaPercent, tax, ttc)
    }
}
