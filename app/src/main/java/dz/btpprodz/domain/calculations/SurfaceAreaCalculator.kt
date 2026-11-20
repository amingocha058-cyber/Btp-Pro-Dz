package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/** Net rectangular wall/finish area after subtracting the total measured openings. */
object SurfaceAreaCalculator {
    fun netRectangularArea(
        count: Int,
        lengthM: BigDecimal,
        heightM: BigDecimal,
        totalOpeningsM2: BigDecimal,
        unit: LengthUnit = LengthUnit.METER
    ): BigDecimal {
        require(count > 0) { "Count must be greater than zero." }
        require(lengthM > BigDecimal.ZERO && heightM > BigDecimal.ZERO) {
            "Length and height must be greater than zero."
        }
        require(totalOpeningsM2 >= BigDecimal.ZERO) {
            "Opening area cannot be negative."
        }
        val gross = unit.toMeters(lengthM).multiply(unit.toMeters(heightM)).multiply(BigDecimal.valueOf(count.toLong()))
        require(totalOpeningsM2 <= gross) {
            "Opening area cannot exceed gross area."
        }
        return gross.subtract(totalOpeningsM2)
    }
}
