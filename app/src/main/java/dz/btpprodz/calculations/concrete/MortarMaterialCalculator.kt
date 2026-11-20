package dz.btpprodz.calculations.concrete

/**
 * Mortar estimation is intentionally separate from concrete and has no gravel input/output.
 * Dosages are supplied by the user per 1 m³ of mortar.
 */
data class MortarDosage(
    val cementKgPerM3: Double,
    val sandM3PerM3: Double
)

data class MortarMaterialInput(
    val mortarVolumeM3: Double,
    val dosage: MortarDosage,
    val wastePercent: Double = 0.0
)

data class MortarMaterialResult(
    val cementKg: Double,
    val sandM3: Double,
    val appliedVolumeM3: Double
)

object MortarMaterialCalculator {
    fun calculate(input: MortarMaterialInput): MortarMaterialResult {
        require(input.mortarVolumeM3.isFinite() && input.mortarVolumeM3 >= 0.0) {
            "Mortar volume must be finite and cannot be negative."
        }
        require(input.wastePercent.isFinite() && input.wastePercent in 0.0..100.0) {
            "Waste percentage must be between 0 and 100."
        }
        require(input.dosage.cementKgPerM3.isFinite() && input.dosage.cementKgPerM3 >= 0.0) {
            "Cement dosage must be finite and cannot be negative."
        }
        require(input.dosage.sandM3PerM3.isFinite() && input.dosage.sandM3PerM3 >= 0.0) {
            "Sand dosage must be finite and cannot be negative."
        }
        require(input.dosage.cementKgPerM3 > 0.0 || input.dosage.sandM3PerM3 > 0.0) {
            "At least one mortar material dosage must be greater than zero."
        }

        val appliedVolume = input.mortarVolumeM3 * (1.0 + input.wastePercent / 100.0)
        require(appliedVolume.isFinite()) { "Applied volume is outside the supported range." }

        val cementKg = appliedVolume * input.dosage.cementKgPerM3
        val sandM3 = appliedVolume * input.dosage.sandM3PerM3
        require(cementKg.isFinite() && sandM3.isFinite()) {
            "Calculated mortar quantities are outside the supported range."
        }

        return MortarMaterialResult(
            cementKg = cementKg,
            sandM3 = sandM3,
            appliedVolumeM3 = appliedVolume
        )
    }
}
