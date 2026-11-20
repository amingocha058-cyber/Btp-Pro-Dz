package dz.btpprodz.calculations.concrete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ConcreteCalculatorsTest {
    @Test
    fun concreteVolumeCalculatesOneUnitAndQuantity() {
        val result = ConcreteVolumeCalculator.calculate(
            ConcreteVolumeInput(lengthM = 5.0, widthM = 2.0, heightM = 0.2, quantity = 3)
        )

        assertEquals(2.0, result.volumePerUnitM3, 0.000001)
        assertEquals(6.0, result.totalVolumeM3, 0.000001)
    }

    @Test
    fun concreteMaterialUsesUserDosageAndWaste() {
        val result = ConcreteMaterialCalculator.calculate(
            ConcreteMaterialInput(
                concreteVolumeM3 = 2.0,
                dosage = ConcreteDosage(
                    cementKgPerM3 = 350.0,
                    sandM3PerM3 = 0.5,
                    gravelM3PerM3 = 0.8
                ),
                wastePercent = 5.0
            )
        )

        assertEquals(735.0, result.cementKg, 0.000001)
        assertEquals(1.05, result.sandM3, 0.000001)
        assertEquals(1.68, result.gravelM3, 0.000001)
        assertEquals(2.1, result.appliedVolumeM3, 0.000001)
    }

    @Test
    fun mortarCalculatorHasOnlyCementAndSand() {
        val result = MortarMaterialCalculator.calculate(
            MortarMaterialInput(
                mortarVolumeM3 = 1.5,
                dosage = MortarDosage(cementKgPerM3 = 300.0, sandM3PerM3 = 1.1)
            )
        )

        assertEquals(450.0, result.cementKg, 0.000001)
        assertEquals(1.65, result.sandM3, 0.000001)
        assertEquals(1.5, result.appliedVolumeM3, 0.000001)
    }

    @Test
    fun volumeCalculatorRejectsZeroDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            ConcreteVolumeCalculator.calculate(
                ConcreteVolumeInput(lengthM = 0.0, widthM = 2.0, heightM = 0.2)
            )
        }
    }

    @Test
    fun mortarCalculatorRejectsNegativeVolume() {
        assertThrows(IllegalArgumentException::class.java) {
            MortarMaterialCalculator.calculate(
                MortarMaterialInput(
                    mortarVolumeM3 = -1.0,
                    dosage = MortarDosage(cementKgPerM3 = 300.0, sandM3PerM3 = 1.0)
                )
            )
        }
    }

    @Test
    fun concreteMaterialRejectsOverflowingAppliedVolume() {
        assertThrows(IllegalArgumentException::class.java) {
            ConcreteMaterialCalculator.calculate(
                ConcreteMaterialInput(
                    concreteVolumeM3 = Double.MAX_VALUE,
                    dosage = ConcreteDosage(
                        cementKgPerM3 = 1.0,
                        sandM3PerM3 = 1.0,
                        gravelM3PerM3 = 1.0
                    ),
                    wastePercent = 100.0
                )
            )
        }
    }

    @Test
    fun concreteMaterialRejectsOverflowingMaterialQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            ConcreteMaterialCalculator.calculate(
                ConcreteMaterialInput(
                    concreteVolumeM3 = Double.MAX_VALUE,
                    dosage = ConcreteDosage(
                        cementKgPerM3 = 2.0,
                        sandM3PerM3 = 0.0,
                        gravelM3PerM3 = 0.0
                    )
                )
            )
        }
    }

    @Test
    fun mortarMaterialRejectsOverflowingMaterialQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            MortarMaterialCalculator.calculate(
                MortarMaterialInput(
                    mortarVolumeM3 = Double.MAX_VALUE,
                    dosage = MortarDosage(cementKgPerM3 = 2.0, sandM3PerM3 = 0.0)
                )
            )
        }
    }
}
