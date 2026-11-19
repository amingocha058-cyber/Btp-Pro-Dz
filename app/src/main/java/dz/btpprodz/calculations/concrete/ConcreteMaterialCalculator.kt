package dz.btpprodz.calculations.concrete

/**
 * Estimates material quantities from a user-supplied mix dosage per 1 m³.
 * No default mix ratio is assumed: the user or project specification supplies the dosage.
 */
data class ConcreteDosage(
    val cementKgPerM3: Double,
    val sandM3PerM3: Double,
    val gravelM3PerM3: Double
)

data class ConcreteMaterialInput(
    val concreteVolumeM3: Double,
    val dosage: ConcreteDosage,
    val wastePercent: Double = 0.0
)

data class ConcreteMaterialResult(
    val cementKg: Double,
    val sandM3: Double,
    val gravelM3: Double,
    val appliedVolumeM3: Double
)

object ConcreteMaterialCalculator {
    fun calculate(input: ConcreteMaterialInput): ConcreteMaterialResult {
        require(input.concreteVolumeM3.isFinite() && input.concreteVolumeM3 >= 0.0) {
            "Concrete volume must be finite and cannot be negative."
        }
        require(input.wastePercent.isFinite() && input.wastePercent in 0.0..100.0) {
            "Waste percentage must be between 0 and 100."
        }
        val dosage = input.dosage
        require(dosage.cementKgPerM3.isFinite() && dosage.cementKgPerM3 >= 0.0) {
            "Cement dosage must be finite and cannot be negative."
        }
        require(dosage.sandM3PerM3.isFinite() && dosage.sandM3PerM3 >= 0.0) {
            "Sand dosage must be finite and cannot be negative."
        }
        require(dosage.gravelM3PerM3.isFinite() && dosage.gravelM3PerM3 >= 0.0) {
            "Gravel dosage must be finite and cannot be negative."
        }
        require(
            dosage.cementKgPerM3 > 0.0 ||
                dosage.sandM3PerM3 > 0.0 ||
                dosage.gravelM3PerM3 > 0.0
        ) { "At least one material dosage must be greater than zero." }

        val appliedVolume = input.concreteVolumeM3 * (1.0 + input.wastePercent / 100.0)
        require(appliedVolume.isFinite()) { "Applied volume is outside the supported range." }

        return ConcreteMaterialResult(
            cementKg = appliedVolume * dosage.cementKgPerM3,
            sandM3 = appliedVolume * dosage.sandM3PerM3,
            gravelM3 = appliedVolume * dosage.gravelM3PerM3,
            appliedVolumeM3 = appliedVolume
        )
    }
}
