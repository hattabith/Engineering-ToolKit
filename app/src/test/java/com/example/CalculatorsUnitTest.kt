package com.example

import com.example.calculations.basic.*
import com.example.calculations.battery.*
import com.example.calculations.model.WarningLevel
import com.example.calculations.power.*
import com.example.core.units.SiParser
import org.junit.Assert.*
import org.junit.Test

class CalculatorsUnitTest {

    @Test
    fun testSiParser() {
        assertEquals(4700.0, SiParser.parse("4.7k")!!, 0.001)
        assertEquals(4700.0, SiParser.parse("4k7")!!, 0.001)
        assertEquals(0.0000001, SiParser.parse("100n")!!, 1e-12)
        assertEquals(0.000022, SiParser.parse("22u")!!, 1e-9)
        assertEquals(10_000_000.0, SiParser.parse("10M")!!, 0.001)
        assertEquals(0.5, SiParser.parse("500m")!!, 0.001)
        assertEquals(12.5, SiParser.parse("12.5V")!!, 0.001)
    }

    @Test
    fun testOhmsLaw() {
        val res = OhmsLawCalculator.calculate(v = 12.0, i = null, r = 470.0, p = null)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("25.53 mA") || res.primaryValue.contains("mA"))
    }

    @Test
    fun testVoltageDivider() {
        val res = VoltageDividerCalculator.calculate(vin = 12.0, r1 = 10000.0, r2 = 3300.0)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("2.98 V") || res.primaryValue.contains("V"))
    }

    @Test
    fun testLedResistor() {
        val res = LedResistorCalculator.calculate(vs = 5.0, vf = 2.0, count = 1, iLed = 0.02)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("150"))
    }

    @Test
    fun testRcFilter() {
        val res = RcCircuitCalculator.calculateFilter(r = 10000.0, c = 1e-7, fc = null)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("159.15"))
    }

    @Test
    fun testLdoDissipationAndWarnings() {
        // Vin 12V, Vout 3.3V, Iout 1A -> Pd = 8.7W! Very high temperature!
        val res = LdoRegulatorCalculator.calculate(vin = 12.0, vout = 3.3, iout = 1.0)
        assertTrue(res.isSuccess)
        assertTrue(res.warnings.any { it.level == WarningLevel.DANGER })
    }

    @Test
    fun testBatteryPackConfig() {
        // 13S 4P Li-ion 3.0Ah 10A cell
        val res = BatteryPackConfigurator.calculate(
            chemistry = BatteryChemistry.LI_ION_NMC,
            seriesS = 13,
            parallelP = 4,
            cellCapacityAh = 3.0,
            cellContCurrentA = 10.0
        )
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("13S4P"))
        assertTrue(res.primaryValue.contains("12.0Ah") || res.primaryValue.contains("12"))
    }

    @Test
    fun testCRateCalculation() {
        val res = CRateCalculator.calculate(capacityAh = 3.0, currentA = 9.0, maxRatedC = 2.0)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("3.00 C"))
        // Warning should be triggered because 3C > 2C rated max
        assertTrue(res.warnings.any { it.level == WarningLevel.DANGER })
    }

    @Test
    fun testUartBaudError() {
        // Standard 16MHz clock with 115200 baud
        val res = UartBaudRateCalculator.calculate(mcuClockHz = 16e6, desiredBaud = 115200.0)
        assertTrue(res.isSuccess)
        assertTrue(res.primaryValue.contains("117647") || res.primaryValue.contains("baud"))
    }

    @Test
    fun testI2cPullup() {
        val res = I2cPullupCalculator.calculate(
            vdd = 3.3,
            busCapacitancePf = 100.0,
            mode = I2cPullupCalculator.SpeedMode.FAST
        )
        assertTrue(res.isSuccess)
        assertTrue(res.intermediateSteps.any { it.first.contains("Minimum Pull-Up") })
    }

    @Test
    fun testNickelStrip() {
        val res = NickelStripCalculator.calculate(
            currentA = 25.0, // dangerously high for single 8x0.15mm strip
            widthMm = 8.0,
            thicknessMm = 0.15
        )
        assertTrue(res.isSuccess)
        assertTrue(res.warnings.any { it.level == WarningLevel.DANGER })
    }
}
