package com.api.ruletaeuropea.fisica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TapMappingTest {
    private fun normalizeDeg(d: Double): Double = ((d % 360.0) + 360.0) % 360.0

    @Test
    fun tapCenterOf8MapsToIndex8() {
        val wheelRadiusPx = 170f
        val ballRadiusPx = 7f
        val engine = RoulettePhysics(wheelRadiusPx = wheelRadiusPx, ballRadiusPx = ballRadiusPx)
        val idx8 = engine.numbers.indexOf(8)
        assertTrue(idx8 >= 0)
        val sectorSize = 360.0 / engine.numbers.size
        val start = engine.sectorStartVisualAngle(idx8)
        val centerVisual = normalizeDeg(start + sectorSize / 2.0)
        val visualCompensated = normalizeDeg(centerVisual - 0.0) // wheelAngleUi = 0
        val idx = engine.sectorIndexFromVisual(visualCompensated)
        assertEquals(idx8, idx)
    }

    @Test
    fun tapCenterOf33MapsToIndex33() {
        val engine = RoulettePhysics(wheelRadiusPx = 170f, ballRadiusPx = 7f)
        val idx33 = engine.numbers.indexOf(33)
        assertTrue(idx33 >= 0)
        val sectorSize = 360.0 / engine.numbers.size
        val start = engine.sectorStartVisualAngle(idx33)
        val centerVisual = normalizeDeg(start + sectorSize / 2.0)
        val visualCompensated = normalizeDeg(centerVisual - 0.0)
        val idx = engine.sectorIndexFromVisual(visualCompensated)
        assertEquals(idx33, idx)
    }

    @Test
    fun tapsMapCorrectlyWithWheelAngleAndAssetOrientation() {
        val wheelAngleUi = 90.0
        val numberToTest = 8
        for (assetCCW in listOf(true, false)) {
            val engine = RoulettePhysics(wheelRadiusPx = 170f, ballRadiusPx = 7f, assetCCW = assetCCW)
            val idx = engine.numbers.indexOf(numberToTest)
            val sectorSize = 360.0 / engine.numbers.size
            val start = engine.sectorStartVisualAngle(idx)
            // screen angle if wheelAngleUi == 90
            val screenAngle = normalizeDeg(start + wheelAngleUi)
            val visualCompensated = normalizeDeg(screenAngle - wheelAngleUi)
            val result = engine.sectorIndexFromVisual(visualCompensated)
            assertEquals(idx, result)
        }
    }

    @Test
    fun calibrateZeroSetsAssetOffsetCompatibleWithMotor() {
        val engine = RoulettePhysics(wheelRadiusPx = 170f, ballRadiusPx = 7f)
        val targetIdx = engine.numbers.indexOf(8)
        val sectorSize = 360.0 / engine.numbers.size
        val start = engine.sectorStartVisualAngle(targetIdx)
        val rawVisual = normalizeDeg(start + sectorSize / 2.0) // tap at center on screen with wheelAngleUi=0
        val wheelAngleUi = 0.0
        val newOffset = normalizeDeg(rawVisual - wheelAngleUi - sectorSize / 2.0)

        val engine2 = RoulettePhysics(wheelRadiusPx = 170f, ballRadiusPx = 7f, assetOffsetDeg = newOffset, assetCCW = engine.assetCCW)
        val visualComp = normalizeDeg(rawVisual - wheelAngleUi)
        val idxAfter = engine2.sectorIndexFromVisual(visualComp)
        assertEquals(targetIdx, idxAfter)
    }
}

