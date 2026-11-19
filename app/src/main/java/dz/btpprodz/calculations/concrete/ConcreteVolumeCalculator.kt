package dz.btpprodz.calculations.concrete

/**
 * Geometric volume only. This calculator does not estimate cement, sand or gravel.
 * All dimensions are expressed in metres and the result is expressed in cubic metres.
 */
data class ConcreteVolumeInput(
    val lengthM: Double,
    val widthM: Double,
    val heightM: Double,
    val quantity: Int = 1
)

data class ConcreteVolumeResult(
    val volumePerUnitM3: Double,
    val totalVolumeM3: Double
)

object ConcreteVolumeCalculator {
    fun calculate(input: ConcreteVolumeInput): ConcreteVolumeResult {
        require(input.lengthM.isFinite() && input.lengthM > 0.0) {
            "Length must be a finite value greater than zero."
        }
        require(input.widthM.isFinite() && input.widthM > 0.0) {
            "Width must be a finite value greater than zero."
        }
        require(input.heightM.isFinite() && input.heightM > 0.0) {
            "Height must be a finite value greater than zero."
        }
        require(input.quantity > 0) { "Quantity must be greater than zero." }

        val perUnit = input.lengthM * input.widthM * input.heightM
        require(perUnit.isFinite()) { "Calculated volume is outside the supported range." }
        val total = perUnit * input.quantity
        require(total.isFinite()) { "Total volume is outside the supported range." }

        return ConcreteVolumeResult(
            volumePerUnitM3 = perUnit,
            totalVolumeM3 = total
        )
    }
}
