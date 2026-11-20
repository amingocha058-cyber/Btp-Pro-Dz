package dz.btpprodz.domain.calculations

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProjectDevisCalculatorTest {
    @Test fun aggregatesMaterialAndLaborSnapshotsIntoDevis() {
        val result = ProjectDevisCalculator.calculate(
            materialLines = listOf(
                DevisCostLine(2.0, 100.0, 200.0),
                DevisCostLine(1.5, 80.0, 120.0)
            ),
            laborLines = listOf(DevisCostLine(3.0, 50.0, 150.0)),
            tvaPercent = BigDecimal("19")
        )
        assertEquals(0, result.materialsHt.compareTo(BigDecimal("320.00")))
        assertEquals(0, result.laborHt.compareTo(BigDecimal("150.00")))
        assertEquals(0, result.totalHt.compareTo(BigDecimal("470.00")))
        assertEquals(0, result.tvaAmount.compareTo(BigDecimal("89.30")))
        assertEquals(0, result.totalTtc.compareTo(BigDecimal("559.30")))
    }

    @Test fun rejectsStoredLineTotalThatDoesNotMatchQuantityAndPrice() {
        assertThrows(IllegalArgumentException::class.java) {
            ProjectDevisCalculator.calculate(
                listOf(DevisCostLine(2.0, 100.0, 199.99)),
                emptyList(),
                BigDecimal("19")
            )
        }
    }

    @Test fun rejectsNonFiniteOrNegativeLineValues() {
        assertThrows(IllegalArgumentException::class.java) {
            ProjectDevisCalculator.calculate(
                listOf(DevisCostLine(Double.NaN, 10.0, 0.0)),
                emptyList(),
                BigDecimal("19")
            )
        }
    }
}
