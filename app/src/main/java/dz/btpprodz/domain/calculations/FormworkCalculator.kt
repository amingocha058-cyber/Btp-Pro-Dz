package dz.btpprodz.domain.calculations

import java.math.BigDecimal

/** Geometric contact area of formwork surfaces, in square metres. No allowance for waste or reuse. */
object FormworkCalculator {
    data class Result(val areaPerElementM2: BigDecimal, val totalAreaM2: BigDecimal)

    fun columns(count: Int, sideA: BigDecimal, sideB: BigDecimal, height: BigDecimal): Result {
        validateCount(count)
        positive(sideA, "sideA"); positive(sideB, "sideB"); positive(height, "height")
        return result(count, sideA.add(sideB).multiply(TWO).multiply(height))
    }

    /** Beam formwork: two vertical sides plus soffit; top is excluded. */
    fun beams(count: Int, length: BigDecimal, width: BigDecimal, depth: BigDecimal): Result {
        validateCount(count)
        positive(length, "length"); positive(width, "width"); positive(depth, "depth")
        return result(count, length.multiply(width.add(depth.multiply(TWO))))
    }

    /** Slab soffit only. Edge shutters must be calculated separately as beams/edges. */
    fun slabSoffit(count: Int, length: BigDecimal, width: BigDecimal): Result {
        validateCount(count)
        positive(length, "length"); positive(width, "width")
        return result(count, length.multiply(width))
    }

    /** Footing side shutters only; bottom and top are excluded. */
    fun footingSides(count: Int, length: BigDecimal, width: BigDecimal, depth: BigDecimal): Result {
        validateCount(count)
        positive(length, "length"); positive(width, "width"); positive(depth, "depth")
        return result(count, length.add(width).multiply(TWO).multiply(depth))
    }

    private fun result(count: Int, area: BigDecimal) =
        Result(area, area.multiply(BigDecimal.valueOf(count.toLong())))

    private fun validateCount(count: Int) { require(count > 0) { "Count must be greater than zero." } }
    private fun positive(value: BigDecimal, name: String) { require(value > BigDecimal.ZERO) { "$name must be greater than zero." } }
    private val TWO = BigDecimal("2")
}