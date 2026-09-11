package com.example.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calculations.basic.*
import com.example.calculations.battery.*
import com.example.calculations.model.CalculationOutput
import com.example.calculations.power.*
import com.example.core.units.SiParser
import com.example.ui.components.EngineeringInputField
import com.example.ui.components.ResultDisplayCard

@Composable
fun CalculatorForm(
    calculatorId: String,
    onResultCalculated: (CalculationOutput, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (calculatorId) {
            // --- 1. BASIC ELECTRONICS ---
            "ohms_law" -> OhmsLawForm(onResultCalculated)
            "power_dissipation" -> PowerDissipationForm(onResultCalculated)
            "series_parallel_resistors" -> SeriesParallelResistorsForm(onResultCalculated)
            "series_parallel_capacitors" -> SeriesParallelCapacitorsForm(onResultCalculated)
            "voltage_divider" -> VoltageDividerForm(onResultCalculated)
            "led_resistor" -> LedResistorForm(onResultCalculated)
            "rc_filter" -> RcFilterForm(onResultCalculated)
            "rl_rlc_resonance" -> RlcResonanceForm(onResultCalculated)
            "op_amp" -> OpAmpForm(onResultCalculated)
            "db_calculator" -> DbForm(onResultCalculated)
            "timer_555" -> Timer555Form(onResultCalculated)
            "resistor_codes" -> ResistorCodeForm(onResultCalculated)
            "tolerance_analysis" -> ToleranceForm(onResultCalculated)

            // --- 2. POWER & EMBEDDED ---
            "power_budget" -> PowerBudgetForm(onResultCalculated)
            "battery_runtime" -> BatteryRuntimeForm(onResultCalculated)
            "dcdc_converter" -> DcDcForm(onResultCalculated)
            "ldo_regulator" -> LdoForm(onResultCalculated)
            "wire_voltage_drop" -> WireDropForm(onResultCalculated)
            "fuse_sizing" -> FuseForm(onResultCalculated)
            "pcb_trace_width" -> PcbTraceForm(onResultCalculated)
            "decoupling_cap" -> DecouplingForm(onResultCalculated)
            "mcu_timer" -> McuTimerForm(onResultCalculated)
            "uart_baud" -> UartBaudForm(onResultCalculated)
            "i2c_pullup" -> I2cPullupForm(onResultCalculated)
            "can_termination" -> CanTerminationForm(onResultCalculated)
            "adc_calculator" -> AdcForm(onResultCalculated)
            "pwm_calculator" -> PwmForm(onResultCalculated)
            "unit_converter" -> UnitConverterForm(onResultCalculated)

            // --- 3. BATTERIES ---
            "battery_pack_config" -> BatteryPackConfigForm(onResultCalculated)
            "target_pack_finder" -> TargetPackFinderForm(onResultCalculated)
            "c_rate" -> CRateForm(onResultCalculated)
            "charging_time" -> ChargingTimeForm(onResultCalculated)
            "voltage_sag" -> VoltageSagForm(onResultCalculated)
            "bms_check" -> BmsCheckForm(onResultCalculated)
            "cell_balancing" -> CellBalancingForm(onResultCalculated)
            "nickel_strip" -> NickelStripForm(onResultCalculated)
            "cell_compatibility" -> CellCompatibilityForm(onResultCalculated)

            else -> Text("Calculator form under construction.")
        }
    }
}

// -------------------------------------------------------------
// BASIC ELECTRONICS FORMS
// -------------------------------------------------------------

@Composable
private fun OhmsLawForm(onResult: (CalculationOutput, String) -> Unit) {
    var vStr by remember { mutableStateOf("12") }
    var iStr by remember { mutableStateOf("") }
    var rStr by remember { mutableStateOf("470") }
    var pStr by remember { mutableStateOf("") }

    val output = remember(vStr, iStr, rStr, pStr) {
        val v = SiParser.parse(vStr)
        val i = SiParser.parse(iStr)
        val r = SiParser.parse(rStr)
        val p = SiParser.parse(pStr)
        OhmsLawCalculator.calculate(v, i, r, p)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) {
            onResult(output, "V=$vStr, I=$iStr, R=$rStr, P=$pStr")
        }
    }

    Text("Enter any two known values (leave other fields empty):", style = MaterialTheme.typography.bodySmall)
    EngineeringInputField("Voltage (V)", vStr, { vStr = it }, unit = "V", placeholder = "e.g. 12 or 3.3")
    EngineeringInputField("Current (I)", iStr, { iStr = it }, unit = "A", placeholder = "e.g. 25m or 2.5")
    EngineeringInputField("Resistance (R)", rStr, { rStr = it }, unit = "Ω", placeholder = "e.g. 4.7k or 220")
    EngineeringInputField("Power (P)", pStr, { pStr = it }, unit = "W", placeholder = "e.g. 500m or 10")
}

@Composable
private fun PowerDissipationForm(onResult: (CalculationOutput, String) -> Unit) {
    var vStr by remember { mutableStateOf("5") }
    var iStr by remember { mutableStateOf("100m") }
    var rStr by remember { mutableStateOf("") }

    val output = remember(vStr, iStr, rStr) {
        val v = SiParser.parse(vStr)
        val i = SiParser.parse(iStr)
        val r = SiParser.parse(rStr)
        PowerDissipationCalculator.calculate(v, i, r)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "V=$vStr, I=$iStr, R=$rStr")
    }

    Text("Enter any 2 parameters to calculate power & safety rating:", style = MaterialTheme.typography.bodySmall)
    EngineeringInputField("Voltage (V)", vStr, { vStr = it }, unit = "V")
    EngineeringInputField("Current (I)", iStr, { iStr = it }, unit = "A")
    EngineeringInputField("Resistance (R, optional)", rStr, { rStr = it }, unit = "Ω")
}

@Composable
private fun SeriesParallelResistorsForm(onResult: (CalculationOutput, String) -> Unit) {
    var listStr by remember { mutableStateOf("10k, 4.7k, 2.2k") }
    var vSourceStr by remember { mutableStateOf("12") }

    val output = remember(listStr, vSourceStr) {
        val list = listStr.split(',').mapNotNull { SiParser.parse(it) }
        val v = SiParser.parse(vSourceStr)
        SeriesParallelResistorsCalculator.calculate(list, v)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Resistors: $listStr, Vs: $vSourceStr")
    }

    EngineeringInputField("Resistors (comma-separated)", listStr, { listStr = it }, placeholder = "e.g. 10k, 4.7k, 1k, 220")
    EngineeringInputField("Source Voltage (optional)", vSourceStr, { vSourceStr = it }, unit = "V")
}

@Composable
private fun SeriesParallelCapacitorsForm(onResult: (CalculationOutput, String) -> Unit) {
    var listStr by remember { mutableStateOf("10u, 22u, 47u") }
    var voltStr by remember { mutableStateOf("25, 25, 25") }

    val output = remember(listStr, voltStr) {
        val caps = listStr.split(',').mapNotNull { SiParser.parse(it) }
        val volts = voltStr.split(',').mapNotNull { SiParser.parse(it) }
        SeriesParallelCapacitorsCalculator.calculate(caps, if (volts.size == caps.size) volts else null)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Capacitors: $listStr, Ratings: $voltStr")
    }

    EngineeringInputField("Capacitors (comma-separated)", listStr, { listStr = it }, placeholder = "e.g. 10u, 100n, 10u")
    EngineeringInputField("Rated Voltages (comma-separated)", voltStr, { voltStr = it }, placeholder = "e.g. 25, 25, 50")
}

@Composable
private fun VoltageDividerForm(onResult: (CalculationOutput, String) -> Unit) {
    var vinStr by remember { mutableStateOf("12") }
    var r1Str by remember { mutableStateOf("10k") }
    var r2Str by remember { mutableStateOf("3.3k") }
    var rLoadStr by remember { mutableStateOf("") }

    val output = remember(vinStr, r1Str, r2Str, rLoadStr) {
        val vin = SiParser.parse(vinStr) ?: 12.0
        val r1 = SiParser.parse(r1Str) ?: 10000.0
        val r2 = SiParser.parse(r2Str) ?: 3300.0
        val rLoad = SiParser.parse(rLoadStr)
        VoltageDividerCalculator.calculate(vin, r1, r2, rLoad)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vin=$vinStr, R1=$r1Str, R2=$r2Str, Rload=$rLoadStr")
    }

    EngineeringInputField("Input Voltage (Vin)", vinStr, { vinStr = it }, unit = "V")
    EngineeringInputField("Top Resistor (R1)", r1Str, { r1Str = it }, unit = "Ω")
    EngineeringInputField("Bottom Resistor (R2)", r2Str, { r2Str = it }, unit = "Ω")
    EngineeringInputField("Load Resistance (R_load, optional)", rLoadStr, { rLoadStr = it }, unit = "Ω", placeholder = "Leave empty for unloaded")
}

@Composable
private fun LedResistorForm(onResult: (CalculationOutput, String) -> Unit) {
    var vsStr by remember { mutableStateOf("5") }
    var vfStr by remember { mutableStateOf("2.0") }
    var countStr by remember { mutableStateOf("1") }
    var iLedStr by remember { mutableStateOf("20m") }

    val output = remember(vsStr, vfStr, countStr, iLedStr) {
        val vs = SiParser.parse(vsStr) ?: 5.0
        val vf = SiParser.parse(vfStr) ?: 2.0
        val count = countStr.toIntOrNull() ?: 1
        val iLed = SiParser.parse(iLedStr) ?: 0.02
        LedResistorCalculator.calculate(vs, vf, count, iLed)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vs=$vsStr, Vf=$vfStr, N=$countStr, I=$iLedStr")
    }

    EngineeringInputField("Supply Voltage (Vs)", vsStr, { vsStr = it }, unit = "V")
    EngineeringInputField("LED Forward Voltage (Vf)", vfStr, { vfStr = it }, unit = "V", placeholder = "e.g. Red ~2.0V, Blue ~3.2V")
    EngineeringInputField("Number of Series LEDs", countStr, { countStr = it }, unit = "pcs")
    EngineeringInputField("Desired LED Current (I_led)", iLedStr, { iLedStr = it }, unit = "A", placeholder = "e.g. 20m or 10m")
}

@Composable
private fun RcFilterForm(onResult: (CalculationOutput, String) -> Unit) {
    var rStr by remember { mutableStateOf("10k") }
    var cStr by remember { mutableStateOf("100n") }
    var fcStr by remember { mutableStateOf("") }

    val output = remember(rStr, cStr, fcStr) {
        val r = SiParser.parse(rStr)
        val c = SiParser.parse(cStr)
        val fc = SiParser.parse(fcStr)
        RcCircuitCalculator.calculateFilter(r, c, fc)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "R=$rStr, C=$cStr, fc=$fcStr")
    }

    Text("Enter any two to calculate the third:", style = MaterialTheme.typography.bodySmall)
    EngineeringInputField("Resistance (R)", rStr, { rStr = it }, unit = "Ω")
    EngineeringInputField("Capacitance (C)", cStr, { cStr = it }, unit = "F")
    EngineeringInputField("Cutoff Frequency (fc)", fcStr, { fcStr = it }, unit = "Hz", placeholder = "Leave empty to calculate fc")
}

@Composable
private fun RlcResonanceForm(onResult: (CalculationOutput, String) -> Unit) {
    var rStr by remember { mutableStateOf("10") }
    var lStr by remember { mutableStateOf("10u") }
    var cStr by remember { mutableStateOf("100n") }
    var fStr by remember { mutableStateOf("100k") }

    val output = remember(rStr, lStr, cStr, fStr) {
        val r = SiParser.parse(rStr) ?: 10.0
        val l = SiParser.parse(lStr) ?: 1e-5
        val c = SiParser.parse(cStr) ?: 1e-7
        val f = SiParser.parse(fStr)
        RlRlcCircuitCalculator.calculate(r, l, c, f)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "R=$rStr, L=$lStr, C=$cStr, f=$fStr")
    }

    EngineeringInputField("Resistance (R)", rStr, { rStr = it }, unit = "Ω")
    EngineeringInputField("Inductance (L)", lStr, { lStr = it }, unit = "H")
    EngineeringInputField("Capacitance (C)", cStr, { cStr = it }, unit = "F")
    EngineeringInputField("Test Frequency (f, optional)", fStr, { fStr = it }, unit = "Hz")
}

@Composable
private fun OpAmpForm(onResult: (CalculationOutput, String) -> Unit) {
    var isNonInverting by remember { mutableStateOf(false) }
    var r1Str by remember { mutableStateOf("1k") }
    var rfStr by remember { mutableStateOf("10k") }
    var vinStr by remember { mutableStateOf("0.5") }

    val output = remember(isNonInverting, r1Str, rfStr, vinStr) {
        val r1 = SiParser.parse(r1Str) ?: 1000.0
        val rf = SiParser.parse(rfStr) ?: 10000.0
        val vin = SiParser.parse(vinStr)
        if (isNonInverting) {
            OpAmpCalculator.calculateNonInverting(r1, rf, vin)
        } else {
            OpAmpCalculator.calculateInverting(r1, rf, vin)
        }
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Mode=${if (isNonInverting) "Non-Inverting" else "Inverting"}, R1=$r1Str, Rf=$rfStr")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isNonInverting,
            onClick = { isNonInverting = false },
            label = { Text("Inverting (-Rf/Rin)") }
        )
        FilterChip(
            selected = isNonInverting,
            onClick = { isNonInverting = true },
            label = { Text("Non-Inverting (1+Rf/R1)") }
        )
    }

    EngineeringInputField(if (isNonInverting) "Input Resistor (R1)" else "Input Resistor (Rin)", r1Str, { r1Str = it }, unit = "Ω")
    EngineeringInputField("Feedback Resistor (Rf)", rfStr, { rfStr = it }, unit = "Ω")
    EngineeringInputField("Input Voltage (Vin, optional)", vinStr, { vinStr = it }, unit = "V")
}

@Composable
private fun DbForm(onResult: (CalculationOutput, String) -> Unit) {
    var modeIndex by remember { mutableStateOf(0) }
    var inputVal by remember { mutableStateOf("0") }

    val output = remember(modeIndex, inputVal) {
        val num = SiParser.parse(inputVal) ?: 0.0
        when (modeIndex) {
            0 -> DbCalculator.dbmToMilliwatts(num)
            1 -> DbCalculator.milliwattsToDbm(num)
            2 -> DbCalculator.dbToVoltageRatio(num)
            3 -> DbCalculator.voltageRatioToDb(num)
            else -> DbCalculator.powerRatioToDb(num)
        }
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Mode=$modeIndex, Input=$inputVal")
    }

    val modes = listOf("dBm → mW", "mW → dBm", "dB → Ratio", "Ratio → dB")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        modes.forEachIndexed { index, label ->
            FilterChip(
                selected = modeIndex == index,
                onClick = { modeIndex = index; inputVal = if (index == 1) "10" else if (index == 3) "2" else "0" },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }

    val unit = when (modeIndex) {
        0 -> "dBm"
        1 -> "mW"
        2 -> "dB"
        3 -> "×"
        else -> ""
    }
    EngineeringInputField("Input Value", inputVal, { inputVal = it }, unit = unit)
}

@Composable
private fun Timer555Form(onResult: (CalculationOutput, String) -> Unit) {
    var isMonostable by remember { mutableStateOf(false) }
    var r1Str by remember { mutableStateOf("1k") }
    var r2Str by remember { mutableStateOf("10k") }
    var cStr by remember { mutableStateOf("100n") }

    val output = remember(isMonostable, r1Str, r2Str, cStr) {
        val r1 = SiParser.parse(r1Str) ?: 1000.0
        val r2 = SiParser.parse(r2Str) ?: 10000.0
        val c = SiParser.parse(cStr) ?: 1e-7
        if (isMonostable) {
            Timer555Calculator.calculateMonostable(r1, c)
        } else {
            Timer555Calculator.calculateAstable(r1, r2, c)
        }
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Mode=${if (isMonostable) "Mono" else "Astable"}, R1=$r1Str, R2=$r2Str, C=$cStr")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isMonostable,
            onClick = { isMonostable = false },
            label = { Text("Astable (Oscillator)") }
        )
        FilterChip(
            selected = isMonostable,
            onClick = { isMonostable = true },
            label = { Text("Monostable (One-Shot)") }
        )
    }

    EngineeringInputField("Resistor R1", r1Str, { r1Str = it }, unit = "Ω")
    if (!isMonostable) {
        EngineeringInputField("Resistor R2", r2Str, { r2Str = it }, unit = "Ω")
    }
    EngineeringInputField("Timing Capacitor (C)", cStr, { cStr = it }, unit = "F")
}

@Composable
private fun ResistorCodeForm(onResult: (CalculationOutput, String) -> Unit) {
    var smdCode by remember { mutableStateOf("472") }
    var targetR by remember { mutableStateOf("4.85k") }

    val output = remember(smdCode, targetR) {
        val parsedTarget = SiParser.parse(targetR)
        if (smdCode.isNotBlank()) {
            ResistorCodeCalculator.decodeSmd(smdCode)
        } else if (parsedTarget != null) {
            val e24 = ResistorCodeCalculator.findNearestE24(parsedTarget)
            val e12 = ResistorCodeCalculator.findNearestE12(parsedTarget)
            CalculationOutput(
                primaryValue = "${SiParser.formatWithSi(e24, "Ω")} (E24)",
                primaryLabel = "Nearest Standard Resistor",
                formula = "E12 / E24 Series standard preferred values",
                substitution = "Target = ${SiParser.formatWithSi(parsedTarget, "Ω")}",
                intermediateSteps = listOf(
                    "Nearest E24 (5% / 1%)" to SiParser.formatWithSi(e24, "Ω"),
                    "Nearest E12 (10%)" to SiParser.formatWithSi(e12, "Ω")
                )
            )
        } else {
            CalculationOutput.error("Enter an SMD code or target resistance.")
        }
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "SMD: $smdCode, Target: $targetR")
    }

    EngineeringInputField("SMD Marking Code", smdCode, { smdCode = it; targetR = "" }, placeholder = "e.g. 472, 1002, 4R7, R050")
    EngineeringInputField("Or Find Nearest E-Series for Target R", targetR, { targetR = it; smdCode = "" }, unit = "Ω", placeholder = "e.g. 4.85k or 215")
}

@Composable
private fun ToleranceForm(onResult: (CalculationOutput, String) -> Unit) {
    var vinStr by remember { mutableStateOf("5") }
    var vinTolStr by remember { mutableStateOf("2") }
    var r1Str by remember { mutableStateOf("10k") }
    var r1TolStr by remember { mutableStateOf("1") }
    var r2Str by remember { mutableStateOf("10k") }
    var r2TolStr by remember { mutableStateOf("1") }

    val output = remember(vinStr, vinTolStr, r1Str, r1TolStr, r2Str, r2TolStr) {
        val vin = SiParser.parse(vinStr) ?: 5.0
        val vinTol = vinTolStr.toDoubleOrNull() ?: 2.0
        val r1 = SiParser.parse(r1Str) ?: 10000.0
        val r1Tol = r1TolStr.toDoubleOrNull() ?: 1.0
        val r2 = SiParser.parse(r2Str) ?: 10000.0
        val r2Tol = r2TolStr.toDoubleOrNull() ?: 1.0
        ToleranceAnalysisCalculator.calculateVoltageDividerWorstCase(vin, vinTol, r1, r1Tol, r2, r2Tol)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vin=$vinStr(±$vinTolStr%), R1=$r1Str(±$r1TolStr%), R2=$r2Str(±$r2TolStr%)")
    }

    EngineeringInputField("Nominal Vin", vinStr, { vinStr = it }, unit = "V")
    EngineeringInputField("Vin Tolerance (± %)", vinTolStr, { vinTolStr = it }, unit = "%")
    EngineeringInputField("R1 Value", r1Str, { r1Str = it }, unit = "Ω")
    EngineeringInputField("R1 Tolerance (± %)", r1TolStr, { r1TolStr = it }, unit = "%")
    EngineeringInputField("R2 Value", r2Str, { r2Str = it }, unit = "Ω")
    EngineeringInputField("R2 Tolerance (± %)", r2TolStr, { r2TolStr = it }, unit = "%")
}

// -------------------------------------------------------------
// POWER & EMBEDDED FORMS
// -------------------------------------------------------------

@Composable
private fun PowerBudgetForm(onResult: (CalculationOutput, String) -> Unit) {
    var activeMaStr by remember { mutableStateOf("25") }
    var activeDutyStr by remember { mutableStateOf("2") }
    var sleepMaStr by remember { mutableStateOf("0.015") }
    var sleepDutyStr by remember { mutableStateOf("98") }
    var battCapStr by remember { mutableStateOf("2400") }

    val output = remember(activeMaStr, activeDutyStr, sleepMaStr, sleepDutyStr, battCapStr) {
        val aI = SiParser.parse(activeMaStr) ?: 25.0
        val aD = activeDutyStr.toDoubleOrNull() ?: 2.0
        val sI = SiParser.parse(sleepMaStr) ?: 0.015
        val sD = sleepDutyStr.toDoubleOrNull() ?: 98.0
        val batt = SiParser.parse(battCapStr) ?: 2400.0

        val modes = listOf(
            PowerBudgetCalculator.OperatingMode("Active", aI, aD),
            PowerBudgetCalculator.OperatingMode("Sleep", sI, sD)
        )
        PowerBudgetCalculator.calculate(modes, batt)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Active=${activeMaStr}mA (${activeDutyStr}%), Sleep=${sleepMaStr}mA, Batt=${battCapStr}mAh")
    }

    EngineeringInputField("Active State Current", activeMaStr, { activeMaStr = it }, unit = "mA")
    EngineeringInputField("Active Duty Cycle", activeDutyStr, { activeDutyStr = it }, unit = "%")
    EngineeringInputField("Sleep State Current", sleepMaStr, { sleepMaStr = it }, unit = "mA", placeholder = "e.g. 0.015 mA = 15 µA")
    EngineeringInputField("Sleep Duty Cycle", sleepDutyStr, { sleepDutyStr = it }, unit = "%")
    EngineeringInputField("Battery Capacity", battCapStr, { battCapStr = it }, unit = "mAh")
}

@Composable
private fun BatteryRuntimeForm(onResult: (CalculationOutput, String) -> Unit) {
    var capStr by remember { mutableStateOf("3000") }
    var iStr by remember { mutableStateOf("50m") }
    var effStr by remember { mutableStateOf("90") }
    var derateStr by remember { mutableStateOf("80") }

    val output = remember(capStr, iStr, effStr, derateStr) {
        val cap = SiParser.parse(capStr) ?: 3000.0
        val i = (SiParser.parse(iStr) ?: 0.05) * 1000.0 // convert to mA
        val eff = effStr.toDoubleOrNull() ?: 90.0
        val derate = derateStr.toDoubleOrNull() ?: 80.0
        BatteryRuntimeCalculator.calculate(cap, i, eff, derate)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Cap=${capStr}mAh, Iavg=$iStr, Eff=$effStr%")
    }

    EngineeringInputField("Battery Nominal Capacity", capStr, { capStr = it }, unit = "mAh")
    EngineeringInputField("Average Load Current", iStr, { iStr = it }, unit = "A", placeholder = "e.g. 50m, 200m, 1.5")
    EngineeringInputField("Regulator Efficiency", effStr, { effStr = it }, unit = "%")
    EngineeringInputField("Available Capacity Derating", derateStr, { derateStr = it }, unit = "%", placeholder = "80% standard")
}

@Composable
private fun DcDcForm(onResult: (CalculationOutput, String) -> Unit) {
    var vinStr by remember { mutableStateOf("12") }
    var voutStr by remember { mutableStateOf("3.3") }
    var ioutStr by remember { mutableStateOf("1.5") }
    var effStr by remember { mutableStateOf("92") }

    val output = remember(vinStr, voutStr, ioutStr, effStr) {
        val vin = SiParser.parse(vinStr) ?: 12.0
        val vout = SiParser.parse(voutStr) ?: 3.3
        val iout = SiParser.parse(ioutStr) ?: 1.5
        val eff = effStr.toDoubleOrNull() ?: 92.0
        DcDcConverterCalculator.calculate(vin, vout, iout, eff)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vin=$vinStr, Vout=$voutStr, Iout=$ioutStr, Eff=$effStr%")
    }

    EngineeringInputField("Input Voltage (Vin)", vinStr, { vinStr = it }, unit = "V")
    EngineeringInputField("Output Voltage (Vout)", voutStr, { voutStr = it }, unit = "V")
    EngineeringInputField("Output Current (Iout)", ioutStr, { ioutStr = it }, unit = "A")
    EngineeringInputField("Converter Efficiency", effStr, { effStr = it }, unit = "%")
}

@Composable
private fun LdoForm(onResult: (CalculationOutput, String) -> Unit) {
    var vinStr by remember { mutableStateOf("5") }
    var voutStr by remember { mutableStateOf("3.3") }
    var ioutStr by remember { mutableStateOf("500m") }
    var thetaStr by remember { mutableStateOf("60") }

    val output = remember(vinStr, voutStr, ioutStr, thetaStr) {
        val vin = SiParser.parse(vinStr) ?: 5.0
        val vout = SiParser.parse(voutStr) ?: 3.3
        val iout = SiParser.parse(ioutStr) ?: 0.5
        val theta = thetaStr.toDoubleOrNull() ?: 60.0
        LdoRegulatorCalculator.calculate(vin, vout, iout, theta)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vin=$vinStr, Vout=$voutStr, Iout=$ioutStr, Theta=$thetaStr")
    }

    EngineeringInputField("Input Voltage (Vin)", vinStr, { vinStr = it }, unit = "V")
    EngineeringInputField("Output Voltage (Vout)", voutStr, { voutStr = it }, unit = "V")
    EngineeringInputField("Load Current (Iout)", ioutStr, { ioutStr = it }, unit = "A", placeholder = "e.g. 500m, 1")
    EngineeringInputField("Thermal Resistance θJA", thetaStr, { thetaStr = it }, unit = "°C/W", placeholder = "SOT-223: ~60, SOT-23: ~200")
}

@Composable
private fun WireDropForm(onResult: (CalculationOutput, String) -> Unit) {
    var vSupplyStr by remember { mutableStateOf("12") }
    var currentStr by remember { mutableStateOf("5") }
    var lengthStr by remember { mutableStateOf("10") }
    var gaugeStr by remember { mutableStateOf("1.5") }

    val output = remember(vSupplyStr, currentStr, lengthStr, gaugeStr) {
        val vs = SiParser.parse(vSupplyStr) ?: 12.0
        val i = SiParser.parse(currentStr) ?: 5.0
        val l = lengthStr.toDoubleOrNull() ?: 10.0
        val g = gaugeStr.toDoubleOrNull() ?: 1.5
        WireVoltageDropCalculator.calculate(vs, i, l, g)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vs=$vSupplyStr, I=$currentStr, L=${lengthStr}m, Gauge=${gaugeStr}mm²")
    }

    EngineeringInputField("Supply Voltage", vSupplyStr, { vSupplyStr = it }, unit = "V")
    EngineeringInputField("Load Current", currentStr, { currentStr = it }, unit = "A")
    EngineeringInputField("One-Way Cable Distance", lengthStr, { lengthStr = it }, unit = "m")
    EngineeringInputField("Wire Cross-Section", gaugeStr, { gaugeStr = it }, unit = "mm²", placeholder = "e.g. 0.75, 1.5, 2.5, 4.0")
}

@Composable
private fun FuseForm(onResult: (CalculationOutput, String) -> Unit) {
    var iOpStr by remember { mutableStateOf("2.5") }
    var inrushStr by remember { mutableStateOf("8.0") }
    var factorStr by remember { mutableStateOf("1.25") }

    val output = remember(iOpStr, inrushStr, factorStr) {
        val iOp = SiParser.parse(iOpStr) ?: 2.5
        val inrush = SiParser.parse(inrushStr)
        val f = factorStr.toDoubleOrNull() ?: 1.25
        FuseSizingCalculator.calculate(iOp, inrush, f)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Iop=$iOpStr, Inrush=$inrushStr, Factor=$factorStr")
    }

    EngineeringInputField("Continuous Operating Current", iOpStr, { iOpStr = it }, unit = "A")
    EngineeringInputField("Peak Inrush Current (optional)", inrushStr, { inrushStr = it }, unit = "A")
    EngineeringInputField("Safety Derating Factor", factorStr, { factorStr = it }, placeholder = "1.25 to 1.5")
}

@Composable
private fun PcbTraceForm(onResult: (CalculationOutput, String) -> Unit) {
    var iStr by remember { mutableStateOf("2.0") }
    var tempStr by remember { mutableStateOf("10") }
    var ozStr by remember { mutableStateOf("1.0") }
    var isExternal by remember { mutableStateOf(true) }

    val output = remember(iStr, tempStr, ozStr, isExternal) {
        val i = SiParser.parse(iStr) ?: 2.0
        val t = tempStr.toDoubleOrNull() ?: 10.0
        val oz = ozStr.toDoubleOrNull() ?: 1.0
        PcbTraceWidthCalculator.calculate(i, t, oz, isExternal)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "I=$iStr, TempRise=$tempStr°C, Cu=$ozStr oz, Ext=$isExternal")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isExternal, onClick = { isExternal = true }, label = { Text("External Layer") })
        FilterChip(selected = !isExternal, onClick = { isExternal = false }, label = { Text("Internal Layer") })
    }

    EngineeringInputField("Trace Current", iStr, { iStr = it }, unit = "A")
    EngineeringInputField("Allowable Temp Rise (ΔT)", tempStr, { tempStr = it }, unit = "°C", placeholder = "10°C is standard")
    EngineeringInputField("Copper Thickness", ozStr, { ozStr = it }, unit = "oz", placeholder = "1 oz = 35 µm")
}

@Composable
private fun DecouplingForm(onResult: (CalculationOutput, String) -> Unit) {
    var deltaIStr by remember { mutableStateOf("500m") }
    var dtStr by remember { mutableStateOf("10n") }
    var droopStr by remember { mutableStateOf("50m") }

    val output = remember(deltaIStr, dtStr, droopStr) {
        val dI = SiParser.parse(deltaIStr) ?: 0.5
        val dt = SiParser.parse(dtStr) ?: 1e-8
        val dV = SiParser.parse(droopStr) ?: 0.05
        DecouplingCapacitorCalculator.calculate(dI, dt, dV)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "ΔI=$deltaIStr, Δt=$dtStr, ΔV=$droopStr")
    }

    EngineeringInputField("Current Transient Surge (ΔI)", deltaIStr, { deltaIStr = it }, unit = "A", placeholder = "e.g. 500m")
    EngineeringInputField("Surge Duration (Δt)", dtStr, { dtStr = it }, unit = "s", placeholder = "e.g. 10n = 10 ns")
    EngineeringInputField("Allowed Voltage Droop (ΔV)", droopStr, { droopStr = it }, unit = "V", placeholder = "e.g. 50m = 50 mV")
}

@Composable
private fun McuTimerForm(onResult: (CalculationOutput, String) -> Unit) {
    var clockStr by remember { mutableStateOf("16M") }
    var pscStr by remember { mutableStateOf("15") }
    var arrStr by remember { mutableStateOf("999") }

    val output = remember(clockStr, pscStr, arrStr) {
        val clk = SiParser.parse(clockStr) ?: 16e6
        val psc = pscStr.toLongOrNull() ?: 15
        val arr = arrStr.toLongOrNull() ?: 999
        McuTimerCalculator.calculate(clk, psc, arr)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Clock=$clockStr, PSC=$pscStr, ARR=$arrStr")
    }

    EngineeringInputField("MCU Clock Frequency", clockStr, { clockStr = it }, unit = "Hz", placeholder = "e.g. 16M, 64M, 168M")
    EngineeringInputField("Prescaler (PSC)", pscStr, { pscStr = it }, placeholder = "e.g. 15 for ÷16")
    EngineeringInputField("Auto-Reload Register (ARR)", arrStr, { arrStr = it }, placeholder = "e.g. 999 for 1000 ticks")
}

@Composable
private fun UartBaudForm(onResult: (CalculationOutput, String) -> Unit) {
    var clockStr by remember { mutableStateOf("16M") }
    var baudStr by remember { mutableStateOf("115200") }

    val output = remember(clockStr, baudStr) {
        val clk = SiParser.parse(clockStr) ?: 16e6
        val baud = baudStr.toDoubleOrNull() ?: 115200.0
        UartBaudRateCalculator.calculate(clk, baud)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Clock=$clockStr, Baud=$baudStr")
    }

    EngineeringInputField("MCU Clock Frequency", clockStr, { clockStr = it }, unit = "Hz")
    EngineeringInputField("Desired Baud Rate", baudStr, { baudStr = it }, unit = "bps", placeholder = "9600, 115200, 921600")
}

@Composable
private fun I2cPullupForm(onResult: (CalculationOutput, String) -> Unit) {
    var vddStr by remember { mutableStateOf("3.3") }
    var cbStr by remember { mutableStateOf("100") }
    var modeIndex by remember { mutableStateOf(1) } // Fast 400kHz

    val mode = when (modeIndex) {
        0 -> I2cPullupCalculator.SpeedMode.STANDARD
        1 -> I2cPullupCalculator.SpeedMode.FAST
        else -> I2cPullupCalculator.SpeedMode.FAST_PLUS
    }

    val output = remember(vddStr, cbStr, mode) {
        val vdd = SiParser.parse(vddStr) ?: 3.3
        val cb = cbStr.toDoubleOrNull() ?: 100.0
        I2cPullupCalculator.calculate(vdd, cb, mode)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Vdd=$vddStr, Cb=${cbStr}pF, Mode=${mode.label}")
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("100 kHz", "400 kHz", "1 MHz").forEachIndexed { idx, label ->
            FilterChip(
                selected = modeIndex == idx,
                onClick = { modeIndex = idx },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }

    EngineeringInputField("Bus Supply Voltage (Vdd)", vddStr, { vddStr = it }, unit = "V")
    EngineeringInputField("Estimated Bus Capacitance (Cb)", cbStr, { cbStr = it }, unit = "pF", placeholder = "Typical: 50-200 pF")
}

@Composable
private fun CanTerminationForm(onResult: (CalculationOutput, String) -> Unit) {
    var countStr by remember { mutableStateOf("2") }

    val output = remember(countStr) {
        val count = countStr.toIntOrNull() ?: 2
        CanTerminationCalculator.calculate(count)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Terminators: $countStr")
    }

    EngineeringInputField("Number of 120 Ω Bus Terminators", countStr, { countStr = it }, unit = "pcs", placeholder = "Standard is 2")
}

@Composable
private fun AdcForm(onResult: (CalculationOutput, String) -> Unit) {
    var bitsStr by remember { mutableStateOf("12") }
    var vRefStr by remember { mutableStateOf("3.3") }
    var codeStr by remember { mutableStateOf("2048") }

    val output = remember(bitsStr, vRefStr, codeStr) {
        val bits = bitsStr.toIntOrNull() ?: 12
        val vRef = SiParser.parse(vRefStr) ?: 3.3
        val code = codeStr.toLongOrNull()
        AdcCalculator.calculate(bits, vRef, adcCode = code)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Bits=$bitsStr, Vref=$vRefStr, Code=$codeStr")
    }

    EngineeringInputField("ADC Resolution (Bits)", bitsStr, { bitsStr = it }, unit = "bits", placeholder = "10, 12, 16, 24")
    EngineeringInputField("Reference Voltage (Vref)", vRefStr, { vRefStr = it }, unit = "V")
    EngineeringInputField("ADC Code", codeStr, { codeStr = it }, placeholder = "e.g. 2048")
}

@Composable
private fun PwmForm(onResult: (CalculationOutput, String) -> Unit) {
    var clockStr by remember { mutableStateOf("16M") }
    var pscStr by remember { mutableStateOf("0") }
    var arrStr by remember { mutableStateOf("999") }
    var dutyStr by remember { mutableStateOf("50") }

    val output = remember(clockStr, pscStr, arrStr, dutyStr) {
        val clk = SiParser.parse(clockStr) ?: 16e6
        val psc = pscStr.toLongOrNull() ?: 0
        val arr = arrStr.toLongOrNull() ?: 999
        val duty = dutyStr.toDoubleOrNull() ?: 50.0
        PwmCalculator.calculate(clk, psc, arr, duty)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Clock=$clockStr, ARR=$arrStr, Duty=$dutyStr%")
    }

    EngineeringInputField("Timer Clock Frequency", clockStr, { clockStr = it }, unit = "Hz")
    EngineeringInputField("Prescaler (PSC)", pscStr, { pscStr = it })
    EngineeringInputField("Period / ARR", arrStr, { arrStr = it })
    EngineeringInputField("Duty Cycle", dutyStr, { dutyStr = it }, unit = "%")
}

@Composable
private fun UnitConverterForm(onResult: (CalculationOutput, String) -> Unit) {
    var valStr by remember { mutableStateOf("2200") }

    val output = remember(valStr) {
        val num = SiParser.parse(valStr) ?: 2200.0
        val ah = num / 1000.0
        val coulombs = UnitConverterCalculator.convertAhToCoulombs(ah)
        val wh = ah * 3.7
        val joules = UnitConverterCalculator.convertWhToJoules(wh)
        val degF = UnitConverterCalculator.celsiusToFahrenheit(num)

        CalculationOutput(
            primaryValue = "${SiParser.formatFixed(coulombs, 0)} Coulombs (for $valStr mAh)",
            primaryLabel = "Unit Conversions",
            formula = "1 Ah = 1000 mAh = 3600 C,  1 Wh = 3600 J",
            substitution = "$valStr mAh = $ah Ah = ${SiParser.formatFixed(coulombs, 0)} C",
            intermediateSteps = listOf(
                "Energy in Coulombs" to "${SiParser.formatFixed(coulombs, 1)} C",
                "Equivalent Energy at 3.7V" to "${SiParser.formatFixed(wh, 2)} Wh (${SiParser.formatFixed(joules, 0)} Joules)",
                "If °C → °F" to "${SiParser.formatFixed(degF, 1)} °F"
            )
        )
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Input: $valStr")
    }

    EngineeringInputField("Value to Convert (mAh)", valStr, { valStr = it }, unit = "mAh")
}

// -------------------------------------------------------------
// BATTERY PACK FORMS
// -------------------------------------------------------------

@Composable
private fun BatteryPackConfigForm(onResult: (CalculationOutput, String) -> Unit) {
    var chemIndex by remember { mutableStateOf(0) }
    var seriesSStr by remember { mutableStateOf("13") } // e.g. 48V ebike pack
    var parallelPStr by remember { mutableStateOf("4") }
    var cellCapStr by remember { mutableStateOf("3.0") }
    var cellContAStr by remember { mutableStateOf("10.0") }

    val chem = BatteryChemistry.values()[chemIndex]

    val output = remember(chem, seriesSStr, parallelPStr, cellCapStr, cellContAStr) {
        val s = seriesSStr.toIntOrNull() ?: 13
        val p = parallelPStr.toIntOrNull() ?: 4
        val cap = cellCapStr.toDoubleOrNull() ?: 3.0
        val contA = cellContAStr.toDoubleOrNull() ?: 10.0
        BatteryPackConfigurator.calculate(chem, s, p, cap, contA)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "${seriesSStr}S${parallelPStr}P ${chem.label}")
    }

    Text("Chemistry:", style = MaterialTheme.typography.bodySmall)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Li-ion (3.7V)", "LiFePO4 (3.2V)", "LiPo (3.7V)").forEachIndexed { idx, label ->
            FilterChip(
                selected = chemIndex == idx,
                onClick = { chemIndex = idx },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }

    EngineeringInputField("Series Cell Count (S)", seriesSStr, { seriesSStr = it }, unit = "S", placeholder = "e.g. 10S, 13S, 16S")
    EngineeringInputField("Parallel Cell Count (P)", parallelPStr, { parallelPStr = it }, unit = "P", placeholder = "e.g. 3P, 4P, 6P")
    EngineeringInputField("Single Cell Capacity", cellCapStr, { cellCapStr = it }, unit = "Ah", placeholder = "e.g. 2.5 or 3.0")
    EngineeringInputField("Cell Max Continuous Current", cellContAStr, { cellContAStr = it }, unit = "A", placeholder = "e.g. 10 or 20")
}

@Composable
private fun TargetPackFinderForm(onResult: (CalculationOutput, String) -> Unit) {
    var targetVStr by remember { mutableStateOf("48") }
    var targetAhStr by remember { mutableStateOf("15") }
    var cellVStr by remember { mutableStateOf("3.6") }
    var cellAhStr by remember { mutableStateOf("3.0") }

    val output = remember(targetVStr, targetAhStr, cellVStr, cellAhStr) {
        val tgtV = targetVStr.toDoubleOrNull() ?: 48.0
        val tgtAh = targetAhStr.toDoubleOrNull() ?: 15.0
        val cV = cellVStr.toDoubleOrNull() ?: 3.6
        val cAh = cellAhStr.toDoubleOrNull() ?: 3.0
        TargetPackFinder.findConfig(tgtV, tgtAh, cV, cAh)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Target: ${targetVStr}V, ${targetAhStr}Ah")
    }

    EngineeringInputField("Target Pack Voltage", targetVStr, { targetVStr = it }, unit = "V")
    EngineeringInputField("Target Pack Capacity", targetAhStr, { targetAhStr = it }, unit = "Ah")
    EngineeringInputField("Single Cell Nominal Voltage", cellVStr, { cellVStr = it }, unit = "V")
    EngineeringInputField("Single Cell Capacity", cellAhStr, { cellAhStr = it }, unit = "Ah")
}

@Composable
private fun CRateForm(onResult: (CalculationOutput, String) -> Unit) {
    var capStr by remember { mutableStateOf("3.0") }
    var currentStr by remember { mutableStateOf("6.0") }
    var maxCStr by remember { mutableStateOf("3.0") }

    val output = remember(capStr, currentStr, maxCStr) {
        val cap = cellCapacityOrNull(capStr) ?: 3.0
        val cur = currentStr.toDoubleOrNull() ?: 6.0
        val maxC = maxCStr.toDoubleOrNull()
        CRateCalculator.calculate(cap, cur, maxC)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Cap=${capStr}Ah, Current=${currentStr}A")
    }

    EngineeringInputField("Battery Capacity", capStr, { capStr = it }, unit = "Ah")
    EngineeringInputField("Discharge / Charge Current", currentStr, { currentStr = it }, unit = "A")
    EngineeringInputField("Max Rated C-Rate (optional)", maxCStr, { maxCStr = it }, unit = "C", placeholder = "e.g. 2C, 5C")
}

private fun cellCapacityOrNull(str: String): Double? {
    val p = SiParser.parse(str) ?: return null
    return if (p > 100) p / 1000.0 else p
}

@Composable
private fun ChargingTimeForm(onResult: (CalculationOutput, String) -> Unit) {
    var capStr by remember { mutableStateOf("5000") }
    var currentStr by remember { mutableStateOf("2000") }
    var socStr by remember { mutableStateOf("10") }

    val output = remember(capStr, currentStr, socStr) {
        val cap = capStr.toDoubleOrNull() ?: 5000.0
        val cur = currentStr.toDoubleOrNull() ?: 2000.0
        val soc = socStr.toDoubleOrNull() ?: 10.0
        ChargingTimeCalculator.calculate(cap, cur, soc)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Cap=${capStr}mAh, Current=${currentStr}mA")
    }

    EngineeringInputField("Battery Capacity", capStr, { capStr = it }, unit = "mAh")
    EngineeringInputField("Charging Current", currentStr, { currentStr = it }, unit = "mA")
    EngineeringInputField("Starting State-of-Charge (SoC)", socStr, { socStr = it }, unit = "%")
}

@Composable
private fun VoltageSagForm(onResult: (CalculationOutput, String) -> Unit) {
    var irStr by remember { mutableStateOf("20") } // 20 mΩ
    var sStr by remember { mutableStateOf("10") }
    var pStr by remember { mutableStateOf("4") }
    var iLoadStr by remember { mutableStateOf("30") }

    val output = remember(irStr, sStr, pStr, iLoadStr) {
        val ir = irStr.toDoubleOrNull() ?: 20.0
        val s = sStr.toIntOrNull() ?: 10
        val p = pStr.toIntOrNull() ?: 4
        val iLoad = iLoadStr.toDoubleOrNull() ?: 30.0
        VoltageSagCalculator.calculate(ir, s, p, iLoad)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "IR=${irStr}mΩ, ${sStr}S${pStr}P, I=${iLoadStr}A")
    }

    EngineeringInputField("Cell Internal Resistance (IR)", irStr, { irStr = it }, unit = "mΩ", placeholder = "e.g. 15-25 mΩ")
    EngineeringInputField("Series Count (S)", sStr, { sStr = it }, unit = "S")
    EngineeringInputField("Parallel Count (P)", pStr, { pStr = it }, unit = "P")
    EngineeringInputField("Load Current", iLoadStr, { iLoadStr = it }, unit = "A")
}

@Composable
private fun BmsCheckForm(onResult: (CalculationOutput, String) -> Unit) {
    var packSStr by remember { mutableStateOf("13") }
    var packContAStr by remember { mutableStateOf("30") }
    var bmsSStr by remember { mutableStateOf("13") }
    var bmsContAStr by remember { mutableStateOf("40") }

    val output = remember(packSStr, packContAStr, bmsSStr, bmsContAStr) {
        val s = packSStr.toIntOrNull() ?: 13
        val iCont = packContAStr.toDoubleOrNull() ?: 30.0
        val bmsS = bmsSStr.toIntOrNull() ?: 13
        val bmsCont = bmsContAStr.toDoubleOrNull() ?: 40.0
        BmsCheckCalculator.calculate(s, iCont, iCont * 1.5, iCont * 0.5, bmsS, bmsCont, bmsCont * 2.0, bmsCont * 0.5)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "Pack: ${packSStr}S ${packContAStr}A vs BMS: ${bmsSStr}S ${bmsContAStr}A")
    }

    EngineeringInputField("Pack S-Count", packSStr, { packSStr = it }, unit = "S")
    EngineeringInputField("Max Continuous Load Current", packContAStr, { packContAStr = it }, unit = "A")
    EngineeringInputField("BMS Rated S-Count", bmsSStr, { bmsSStr = it }, unit = "S")
    EngineeringInputField("BMS Continuous Rating", bmsContAStr, { bmsContAStr = it }, unit = "A")
}

@Composable
private fun CellBalancingForm(onResult: (CalculationOutput, String) -> Unit) {
    var deltaMahStr by remember { mutableStateOf("150") }
    var balMaStr by remember { mutableStateOf("50") }

    val output = remember(deltaMahStr, balMaStr) {
        val d = deltaMahStr.toDoubleOrNull() ?: 150.0
        val i = balMaStr.toDoubleOrNull() ?: 50.0
        CellBalancingCalculator.calculate(d, i)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "ΔCap=${deltaMahStr}mAh, Ibal=${balMaStr}mA")
    }

    EngineeringInputField("Cell Imbalance Capacity", deltaMahStr, { deltaMahStr = it }, unit = "mAh")
    EngineeringInputField("Balancing Current", balMaStr, { balMaStr = it }, unit = "mA", placeholder = "Standard passive BMS: 30-80 mA")
}

@Composable
private fun NickelStripForm(onResult: (CalculationOutput, String) -> Unit) {
    var currentStr by remember { mutableStateOf("10") }
    var widthStr by remember { mutableStateOf("8") }
    var thickStr by remember { mutableStateOf("0.15") }
    var isPureNickel by remember { mutableStateOf(true) }

    val output = remember(currentStr, widthStr, thickStr, isPureNickel) {
        val i = currentStr.toDoubleOrNull() ?: 10.0
        val w = widthStr.toDoubleOrNull() ?: 8.0
        val t = thickStr.toDoubleOrNull() ?: 0.15
        val mat = if (isPureNickel) NickelStripCalculator.StripMaterial.PURE_NICKEL else NickelStripCalculator.StripMaterial.NICKEL_PLATED_STEEL
        NickelStripCalculator.calculate(i, w, t, material = mat)
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "I=${currentStr}A, ${widthStr}x${thickStr}mm ${if (isPureNickel) "Pure Ni" else "Plated"}")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isPureNickel, onClick = { isPureNickel = true }, label = { Text("Pure Nickel (Ni 99.6%)") })
        FilterChip(selected = !isPureNickel, onClick = { isPureNickel = false }, label = { Text("Nickel-Plated Steel") })
    }

    EngineeringInputField("Current passing through strip", currentStr, { currentStr = it }, unit = "A")
    EngineeringInputField("Strip Width", widthStr, { widthStr = it }, unit = "mm", placeholder = "e.g. 8, 10, 12")
    EngineeringInputField("Strip Thickness", thickStr, { thickStr = it }, unit = "mm", placeholder = "e.g. 0.15, 0.2")
}

@Composable
private fun CellCompatibilityForm(onResult: (CalculationOutput, String) -> Unit) {
    var vDeltaStr by remember { mutableStateOf("15") } // 15 mV
    var capDeltaStr by remember { mutableStateOf("2.0") } // 2%
    var irDeltaStr by remember { mutableStateOf("5.0") } // 5%

    val output = remember(vDeltaStr, capDeltaStr, irDeltaStr) {
        val v = vDeltaStr.toDoubleOrNull() ?: 15.0
        val cap = capDeltaStr.toDoubleOrNull() ?: 2.0
        val ir = irDeltaStr.toDoubleOrNull() ?: 5.0
        CellCompatibilityCalculator.checkCompatibility(
            BatteryChemistry.LI_ION_NMC,
            BatteryChemistry.LI_ION_NMC,
            cap,
            ir,
            v
        )
    }

    LaunchedEffect(output) {
        if (output.isSuccess) onResult(output, "ΔV=${vDeltaStr}mV, ΔCap=${capDeltaStr}%, ΔIR=${irDeltaStr}%")
    }

    EngineeringInputField("Resting Voltage Difference", vDeltaStr, { vDeltaStr = it }, unit = "mV", placeholder = "Max safe delta: < 50 mV")
    EngineeringInputField("Capacity Difference", capDeltaStr, { capDeltaStr = it }, unit = "%", placeholder = "Max safe delta: < 5%")
    EngineeringInputField("Internal Resistance Difference", irDeltaStr, { irDeltaStr = it }, unit = "%", placeholder = "Max safe delta: < 15%")
}
