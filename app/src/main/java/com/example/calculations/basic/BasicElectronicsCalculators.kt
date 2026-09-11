package com.example.calculations.basic

import com.example.calculations.model.CalculationOutput
import com.example.calculations.model.CalculationWarning
import com.example.calculations.model.WarningLevel
import com.example.core.units.SiParser
import kotlin.math.*

object OhmsLawCalculator {
    enum class SolveTarget { VOLTAGE, CURRENT, RESISTANCE, POWER }

    fun calculate(
        v: Double?,
        i: Double?,
        r: Double?,
        p: Double?
    ): CalculationOutput {
        val knownCount = listOfNotNull(v, i, r, p).size
        if (knownCount < 2) {
            return CalculationOutput.error("Please provide at least 2 known values out of V, I, R, P.")
        }

        var voltage = v
        var current = i
        var resistance = r
        var power = p

        // Solve based on known pairs
        when {
            voltage != null && current != null -> {
                if (current == 0.0) return CalculationOutput.error("Current cannot be zero when voltage is defined.")
                resistance = voltage / current
                power = voltage * current
            }
            voltage != null && resistance != null -> {
                if (resistance <= 0.0) return CalculationOutput.error("Resistance must be greater than zero.")
                current = voltage / resistance
                power = (voltage * voltage) / resistance
            }
            voltage != null && power != null -> {
                if (voltage == 0.0) return CalculationOutput.error("Voltage cannot be zero when power is defined.")
                current = power / voltage
                if (current <= 0.0) return CalculationOutput.error("Current must be positive.")
                resistance = (voltage * voltage) / power
            }
            current != null && resistance != null -> {
                if (resistance <= 0.0) return CalculationOutput.error("Resistance must be greater than zero.")
                voltage = current * resistance
                power = current * current * resistance
            }
            current != null && power != null -> {
                if (current == 0.0) return CalculationOutput.error("Current cannot be zero when power is defined.")
                voltage = power / current
                resistance = power / (current * current)
            }
            resistance != null && power != null -> {
                if (resistance <= 0.0) return CalculationOutput.error("Resistance must be greater than zero.")
                if (power < 0.0) return CalculationOutput.error("Power cannot be negative.")
                voltage = sqrt(power * resistance)
                current = sqrt(power / resistance)
            }
        }

        val warnings = mutableListOf<CalculationWarning>()
        if (voltage!! > 60.0) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "Voltage exceeds 60V (Safety Extra-Low Voltage limit). Take high voltage precautions."))
        }
        if (power!! > 100.0) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "High power dissipation ($power W). Ensure adequate heat-sinking and fire safety."))
        }
        if (current!! > 20.0) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "High current ($current A). Verify conductor cross-section and thermal limits."))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(voltage, "V")} | ${SiParser.formatWithSi(current, "A")}",
            primaryLabel = "V & I",
            formula = "V = I × R,  P = V × I = I²R = V²/R",
            substitution = "V = ${SiParser.formatWithSi(voltage, "V")}, I = ${SiParser.formatWithSi(current, "A")}, R = ${SiParser.formatWithSi(resistance!!, "Ω")}, P = ${SiParser.formatWithSi(power, "W")}",
            intermediateSteps = listOf(
                "Voltage (V)" to SiParser.formatWithSi(voltage, "V"),
                "Current (I)" to SiParser.formatWithSi(current, "A"),
                "Resistance (R)" to SiParser.formatWithSi(resistance, "Ω"),
                "Power (P)" to SiParser.formatWithSi(power, "W")
            ),
            warnings = warnings,
            notes = listOf(
                "Ohm's Law is valid for ohmic (linear) conductors at constant temperature.",
                "For AC circuits, replace Resistance (R) with Impedance (Z)."
            )
        )
    }
}

object PowerDissipationCalculator {
    fun calculate(v: Double?, i: Double?, r: Double?): CalculationOutput {
        val nonNulls = listOfNotNull(v, i, r).size
        if (nonNulls < 2) return CalculationOutput.error("Provide at least 2 parameters (V, I, R).")

        val power = when {
            v != null && i != null -> v * i
            i != null && r != null -> i * i * r
            v != null && r != null -> {
                if (r <= 0) return CalculationOutput.error("Resistance must be positive.")
                (v * v) / r
            }
            else -> 0.0
        }

        val recRating1_5 = power * 1.5
        val recRating2_0 = power * 2.0

        val standardRating = when {
            recRating1_5 <= 0.125 -> "1/8 W (0.125W, SMD 0805)"
            recRating1_5 <= 0.25 -> "1/4 W (0.25W, SMD 1206 or THT)"
            recRating1_5 <= 0.5 -> "1/2 W (0.5W)"
            recRating1_5 <= 1.0 -> "1 W"
            recRating1_5 <= 2.0 -> "2 W"
            recRating1_5 <= 5.0 -> "5 W (Wirewound / Power)"
            else -> "${ceil(recRating2_0)} W (Heatsink recommended)"
        }

        val warnings = mutableListOf<CalculationWarning>()
        if (power > 0.25) {
            warnings.add(CalculationWarning(WarningLevel.INFO, "Dissipation > 250mW: standard SMD 0805/0603 will overheat. Use 1206+ or 1W rating."))
        }
        if (power > 2.0) {
            warnings.add(CalculationWarning(WarningLevel.DANGER, "High dissipation ($power W)! Requires active or chassis heatsinking."))
        }

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(power, "W"),
            primaryLabel = "Dissipated Power",
            formula = "P = V × I = I²R = V²/R",
            substitution = "P = ${SiParser.formatWithSi(power, "W")}",
            intermediateSteps = listOf(
                "Actual Power" to SiParser.formatWithSi(power, "W"),
                "Min Derating (1.5×)" to SiParser.formatWithSi(recRating1_5, "W"),
                "Recommended (2.0×)" to SiParser.formatWithSi(recRating2_0, "W"),
                "Recommended Resistor" to standardRating
            ),
            warnings = warnings,
            notes = listOf(
                "Design Rule: Always derate resistor power by 50% (2× margin) for high reliability and lower surface temperature."
            )
        )
    }
}

object SeriesParallelResistorsCalculator {
    fun calculate(resistors: List<Double>, sourceVoltage: Double? = null): CalculationOutput {
        if (resistors.isEmpty()) return CalculationOutput.error("Enter at least one resistor value.")
        if (resistors.any { it <= 0 }) return CalculationOutput.error("All resistor values must be positive.")

        // Series: R_eq = sum(R)
        val seriesReq = resistors.sum()

        // Parallel: 1/R_eq = sum(1/R)
        val parallelInv = resistors.sumOf { 1.0 / it }
        val parallelReq = 1.0 / parallelInv

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Resistor Count" to "${resistors.size} pcs")
        steps.add("Series Equivalent (R_series)" to SiParser.formatWithSi(seriesReq, "Ω"))
        steps.add("Parallel Equivalent (R_parallel)" to SiParser.formatWithSi(parallelReq, "Ω"))

        if (sourceVoltage != null && sourceVoltage > 0) {
            val seriesCurrent = sourceVoltage / seriesReq
            steps.add("Series Total Current" to SiParser.formatWithSi(seriesCurrent, "A"))
            steps.add("Series Total Power" to SiParser.formatWithSi(sourceVoltage * seriesCurrent, "W"))

            val parallelCurrent = sourceVoltage / parallelReq
            steps.add("Parallel Total Current" to SiParser.formatWithSi(parallelCurrent, "A"))
            steps.add("Parallel Total Power" to SiParser.formatWithSi(sourceVoltage * parallelCurrent, "W"))

            resistors.forEachIndexed { idx, r ->
                val vDropSeries = seriesCurrent * r
                val pSeries = vDropSeries * seriesCurrent
                steps.add("R${idx + 1} (${SiParser.formatWithSi(r, "Ω")}) in Series" to "V = ${SiParser.formatWithSi(vDropSeries, "V")}, P = ${SiParser.formatWithSi(pSeries, "W")}")
            }
        }

        return CalculationOutput(
            primaryValue = "Series: ${SiParser.formatWithSi(seriesReq, "Ω")} | Parallel: ${SiParser.formatWithSi(parallelReq, "Ω")}",
            primaryLabel = "Equivalent Resistance",
            formula = "R_series = Σ R_i,  1 / R_parallel = Σ (1 / R_i)",
            substitution = "R_series = ${SiParser.formatWithSi(seriesReq, "Ω")}, R_parallel = ${SiParser.formatWithSi(parallelReq, "Ω")}",
            intermediateSteps = steps,
            warnings = emptyList(),
            notes = listOf(
                "Parallel equivalent is always strictly less than the smallest resistor in the network.",
                "Series equivalent is always strictly greater than the largest resistor."
            )
        )
    }
}

object SeriesParallelCapacitorsCalculator {
    fun calculate(capacitors: List<Double>, ratedVoltages: List<Double>? = null): CalculationOutput {
        if (capacitors.isEmpty()) return CalculationOutput.error("Enter at least one capacitor value.")
        if (capacitors.any { it <= 0 }) return CalculationOutput.error("Capacitance must be positive.")

        // Parallel: C_eq = sum(C)
        val parallelCeq = capacitors.sum()

        // Series: 1/C_eq = sum(1/C)
        val seriesInv = capacitors.sumOf { 1.0 / it }
        val seriesCeq = 1.0 / seriesInv

        val warnings = mutableListOf<CalculationWarning>()
        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Parallel Equivalent (C_parallel)" to SiParser.formatWithSi(parallelCeq, "F"))
        steps.add("Series Equivalent (C_series)" to SiParser.formatWithSi(seriesCeq, "F"))

        if (!ratedVoltages.isNullOrEmpty() && ratedVoltages.size == capacitors.size) {
            val minRated = ratedVoltages.minOrNull() ?: 0.0
            val sumRated = ratedVoltages.sum()
            steps.add("Parallel Assembly Rated Voltage" to "${SiParser.formatWithSi(minRated, "V")} (lowest rating)")
            steps.add("Series Assembly Theoretical Voltage" to "${SiParser.formatWithSi(sumRated, "V")} (sum of ratings)")

            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "In series capacitor banks, manufacturing tolerances and leakage currents cause severe voltage imbalance. Always add balancing resistors (e.g. 100kΩ to 1MΩ across each capacitor)!"
            ))
        }

        return CalculationOutput(
            primaryValue = "Parallel: ${SiParser.formatWithSi(parallelCeq, "F")} | Series: ${SiParser.formatWithSi(seriesCeq, "F")}",
            primaryLabel = "Equivalent Capacitance",
            formula = "C_parallel = Σ C_i,  1 / C_series = Σ (1 / C_i)",
            substitution = "C_parallel = ${SiParser.formatWithSi(parallelCeq, "F")}, C_series = ${SiParser.formatWithSi(seriesCeq, "F")}",
            intermediateSteps = steps,
            warnings = warnings,
            notes = listOf(
                "Parallel capacitors add capacitance directly and share the same voltage.",
                "Series capacitors reduce equivalent capacitance, similar to parallel resistors."
            )
        )
    }
}

object VoltageDividerCalculator {
    fun calculate(
        vin: Double,
        r1: Double,
        r2: Double,
        rLoad: Double? = null
    ): CalculationOutput {
        if (vin <= 0) return CalculationOutput.error("Input voltage must be positive.")
        if (r1 <= 0 || r2 <= 0) return CalculationOutput.error("Resistors R1 and R2 must be positive.")
        if (rLoad != null && rLoad <= 0) return CalculationOutput.error("Load resistance must be positive.")

        val voutUnloaded = vin * (r2 / (r1 + r2))
        val currentUnloaded = vin / (r1 + r2)
        val pR1Unloaded = (vin - voutUnloaded).pow(2) / r1
        val pR2Unloaded = voutUnloaded.pow(2) / r2

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Unloaded Vout" to SiParser.formatWithSi(voutUnloaded, "V"))
        steps.add("Divider Current (I_div)" to SiParser.formatWithSi(currentUnloaded, "A"))
        steps.add("Power in R1" to SiParser.formatWithSi(pR1Unloaded, "W"))
        steps.add("Power in R2" to SiParser.formatWithSi(pR2Unloaded, "W"))

        var voutFinal = voutUnloaded
        val warnings = mutableListOf<CalculationWarning>()

        if (rLoad != null) {
            val r2Eff = (r2 * rLoad) / (r2 + rLoad)
            val voutLoaded = vin * (r2Eff / (r1 + r2Eff))
            val loadCurrent = voutLoaded / rLoad
            voutFinal = voutLoaded

            steps.add("Effective R2 with Load" to SiParser.formatWithSi(r2Eff, "Ω"))
            steps.add("Loaded Vout" to SiParser.formatWithSi(voutLoaded, "V"))
            steps.add("Load Current" to SiParser.formatWithSi(loadCurrent, "A"))
            val sagPercent = ((voutUnloaded - voutLoaded) / voutUnloaded) * 100.0
            steps.add("Voltage Sag due to Load" to "${SiParser.formatFixed(sagPercent, 2)} %")

            if (sagPercent > 5.0) {
                warnings.add(CalculationWarning(
                    WarningLevel.WARNING,
                    "Loading effect is severe (sag > 5%). The load resistance (${SiParser.formatWithSi(rLoad, "Ω")}) should ideally be >= 10× R2 (${SiParser.formatWithSi(r2, "Ω")})."
                ))
            }
        }

        val totalPower = vin * (if (rLoad != null) (vin / (r1 + ((r2 * rLoad) / (r2 + rLoad)))) else currentUnloaded)
        if (totalPower > 0.5) {
            warnings.add(CalculationWarning(WarningLevel.INFO, "Total divider power consumption is ${SiParser.formatWithSi(totalPower, "W")}. Consider higher resistor values for battery designs."))
        }

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(voutFinal, "V"),
            primaryLabel = if (rLoad != null) "Loaded Vout" else "Output Voltage (Vout)",
            formula = if (rLoad == null) "Vout = Vin × (R2 / (R1 + R2))" else "Vout = Vin × (R2_eff / (R1 + R2_eff)), where R2_eff = R2 || R_load",
            substitution = "Vout = $vin × ($r2 / ($r1 + $r2)) = ${SiParser.formatWithSi(voutFinal, "V")}",
            intermediateSteps = steps,
            warnings = warnings,
            notes = listOf(
                "Rule of Thumb: For ADC reading, ensure divider source impedance (R1 || R2) is within the ADC's maximum input impedance limit (often <= 10kΩ)."
            )
        )
    }
}

object LedResistorCalculator {
    fun calculate(
        vs: Double,
        vf: Double,
        count: Int,
        iLed: Double
    ): CalculationOutput {
        if (vs <= 0) return CalculationOutput.error("Supply voltage must be positive.")
        if (vf <= 0) return CalculationOutput.error("Forward voltage must be positive.")
        if (count < 1) return CalculationOutput.error("LED count must be at least 1.")
        if (iLed <= 0) return CalculationOutput.error("LED current must be positive.")

        val totalVf = vf * count
        if (vs <= totalVf) {
            return CalculationOutput.error("Supply voltage (${SiParser.formatWithSi(vs, "V")}) must exceed total LED forward voltage (${SiParser.formatWithSi(totalVf, "V")})!")
        }

        val vResistor = vs - totalVf
        val rTheoretical = vResistor / iLed
        val pResistor = vResistor * iLed
        val nearestE24 = ResistorCodeCalculator.findNearestE24(rTheoretical)
        val actualCurrent = vResistor / nearestE24
        val actualPower = vResistor * actualCurrent

        val warnings = mutableListOf<CalculationWarning>()
        if (vResistor < 0.5) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "Low headroom (${SiParser.formatWithSi(vResistor, "V")}). Minor supply fluctuations will cause large LED brightness variations."))
        }
        if (actualPower > 0.25) {
            warnings.add(CalculationWarning(WarningLevel.INFO, "Resistor power is ${SiParser.formatWithSi(actualPower, "W")}. Use a 0.5W or 1W rated resistor."))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(nearestE24, "Ω")} (Std E24)",
            primaryLabel = "Current Limiting Resistor",
            formula = "R = (Vs - N × Vf) / I_led,  P_r = (Vs - N × Vf) × I_led",
            substitution = "R = ($vs - $count × $vf) / $iLed = ${SiParser.formatWithSi(rTheoretical, "Ω")}",
            intermediateSteps = listOf(
                "Theoretical Resistor" to SiParser.formatWithSi(rTheoretical, "Ω"),
                "Nearest Standard E24" to SiParser.formatWithSi(nearestE24, "Ω"),
                "Actual LED Current with E24" to SiParser.formatWithSi(actualCurrent, "A"),
                "Resistor Voltage Drop" to SiParser.formatWithSi(vResistor, "V"),
                "Resistor Power Dissipation" to SiParser.formatWithSi(actualPower, "W"),
                "Recommended Power Rating" to "${SiParser.formatWithSi(actualPower * 2.0, "W")} (2× safety margin)"
            ),
            warnings = warnings,
            notes = listOf(
                "Typical Forward Voltages: Red/Orange ~2.0V, Yellow/Green ~2.2V, Blue/White ~3.2V.",
                "Standard indicator LED current is usually 5mA–20mA."
            )
        )
    }
}

object RcCircuitCalculator {
    fun calculateFilter(
        r: Double?,
        c: Double?,
        fc: Double?
    ): CalculationOutput {
        val count = listOfNotNull(r, c, fc).size
        if (count < 2) return CalculationOutput.error("Provide any 2 parameters out of R, C, Cutoff Frequency (fc).")

        var res = r
        var cap = c
        var freq = fc

        when {
            res != null && cap != null -> {
                if (res <= 0 || cap <= 0) return CalculationOutput.error("R and C must be positive.")
                freq = 1.0 / (2.0 * PI * res * cap)
            }
            res != null && freq != null -> {
                if (res <= 0 || freq <= 0) return CalculationOutput.error("R and fc must be positive.")
                cap = 1.0 / (2.0 * PI * res * freq)
            }
            cap != null && freq != null -> {
                if (cap <= 0 || freq <= 0) return CalculationOutput.error("C and fc must be positive.")
                res = 1.0 / (2.0 * PI * cap * freq)
            }
        }

        val tau = res!! * cap!!

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(freq!!, "Hz"),
            primaryLabel = "Cutoff Frequency (-3dB)",
            formula = "f_c = 1 / (2 × π × R × C),  τ = R × C",
            substitution = "f_c = 1 / (2 × π × ${SiParser.formatWithSi(res, "Ω")} × ${SiParser.formatWithSi(cap, "F")}) = ${SiParser.formatWithSi(freq, "Hz")}",
            intermediateSteps = listOf(
                "Cutoff Frequency (fc)" to SiParser.formatWithSi(freq, "Hz"),
                "Time Constant (τ)" to SiParser.formatWithSi(tau, "s"),
                "Rise Time (10% to 90%)" to SiParser.formatWithSi(2.2 * tau, "s"),
                "Time to 95% (3τ)" to SiParser.formatWithSi(3.0 * tau, "s"),
                "Time to 99.3% (5τ)" to SiParser.formatWithSi(5.0 * tau, "s")
            ),
            warnings = emptyList(),
            notes = listOf(
                "At cutoff frequency fc, output voltage drops by -3dB (gain = 0.707) and phase shift is 45°.",
                "For low-pass: passes DC and low frequencies, attenuates high frequencies at -20 dB/decade."
            )
        )
    }
}

object RlRlcCircuitCalculator {
    fun calculate(
        r: Double,
        l: Double,
        c: Double,
        f: Double?
    ): CalculationOutput {
        if (r <= 0 || l <= 0 || c <= 0) return CalculationOutput.error("R, L, and C must all be positive.")

        val tauRl = l / r
        val f0 = 1.0 / (2.0 * PI * sqrt(l * c))
        val omega0 = 2.0 * PI * f0
        val z0 = sqrt(l / c) // characteristic impedance

        // Series RLC Quality Factor
        val qSeries = (1.0 / r) * z0
        val bwSeries = if (qSeries > 0) f0 / qSeries else 0.0

        // Parallel RLC Quality Factor
        val qParallel = r / z0

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Resonant Frequency (f0)" to SiParser.formatWithSi(f0, "Hz"))
        steps.add("RL Time Constant (L/R)" to SiParser.formatWithSi(tauRl, "s"))
        steps.add("Characteristic Impedance (Z0 = √(L/C))" to SiParser.formatWithSi(z0, "Ω"))
        steps.add("Series Q Factor" to SiParser.formatFixed(qSeries, 3))
        steps.add("Series -3dB Bandwidth" to SiParser.formatWithSi(bwSeries, "Hz"))
        steps.add("Parallel Q Factor" to SiParser.formatFixed(qParallel, 3))

        if (f != null && f > 0) {
            val xl = 2.0 * PI * f * l
            val xc = 1.0 / (2.0 * PI * f * c)
            steps.add("Inductive Reactance (XL at ${SiParser.formatWithSi(f, "Hz")})" to SiParser.formatWithSi(xl, "Ω"))
            steps.add("Capacitive Reactance (XC at ${SiParser.formatWithSi(f, "Hz")})" to SiParser.formatWithSi(xc, "Ω"))
            val netX = xl - xc
            steps.add("Net Reactance (XL - XC)" to SiParser.formatWithSi(netX, "Ω"))
        }

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(f0, "Hz"),
            primaryLabel = "Resonant Frequency (f0)",
            formula = "f0 = 1 / (2π√(LC)),  Q_series = (1/R)√(L/C),  BW = f0 / Q",
            substitution = "f0 = 1 / (2π√(${SiParser.formatWithSi(l, "H")} × ${SiParser.formatWithSi(c, "F")})) = ${SiParser.formatWithSi(f0, "Hz")}",
            intermediateSteps = steps,
            warnings = emptyList(),
            notes = listOf(
                "At resonance, XL = XC, net reactance is zero, and circuit impedance is purely resistive.",
                "A higher Q factor means sharper resonance peak and narrower bandwidth."
            )
        )
    }
}

object OpAmpCalculator {
    fun calculateInverting(rin: Double, rf: Double, vin: Double? = null): CalculationOutput {
        if (rin <= 0 || rf <= 0) return CalculationOutput.error("Resistors must be positive.")
        val gain = -rf / rin
        val gainDb = 20.0 * log10(abs(gain))

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Voltage Gain (Av)" to SiParser.formatFixed(gain, 3))
        steps.add("Gain in dB" to "${SiParser.formatFixed(gainDb, 2)} dB")
        steps.add("Input Impedance (Zin)" to SiParser.formatWithSi(rin, "Ω"))

        if (vin != null) {
            val vout = gain * vin
            steps.add("Output Voltage (Vout)" to SiParser.formatWithSi(vout, "V"))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(gain, 3)}× (${SiParser.formatFixed(gainDb, 2)} dB)",
            primaryLabel = "Inverting Gain (Av)",
            formula = "Av = -Rf / Rin,  Vout = -Vin × (Rf / Rin)",
            substitution = "Av = -${SiParser.formatWithSi(rf, "Ω")} / ${SiParser.formatWithSi(rin, "Ω")} = ${SiParser.formatFixed(gain, 3)}",
            intermediateSteps = steps,
            warnings = emptyList(),
            notes = listOf(
                "Input impedance equals Rin. For high input impedance sensors, a non-inverting amplifier or input buffer is preferred."
            )
        )
    }

    fun calculateNonInverting(r1: Double, rf: Double, vin: Double? = null): CalculationOutput {
        if (r1 <= 0 || rf <= 0) return CalculationOutput.error("Resistors must be positive.")
        val gain = 1.0 + (rf / r1)
        val gainDb = 20.0 * log10(gain)

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Voltage Gain (Av)" to SiParser.formatFixed(gain, 3))
        steps.add("Gain in dB" to "${SiParser.formatFixed(gainDb, 2)} dB")
        steps.add("Input Impedance (Zin)" to "Near Infinite (Op-Amp Gate)")

        if (vin != null) {
            val vout = gain * vin
            steps.add("Output Voltage (Vout)" to SiParser.formatWithSi(vout, "V"))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(gain, 3)}× (${SiParser.formatFixed(gainDb, 2)} dB)",
            primaryLabel = "Non-Inverting Gain (Av)",
            formula = "Av = 1 + (Rf / R1),  Vout = Vin × (1 + Rf / R1)",
            substitution = "Av = 1 + (${SiParser.formatWithSi(rf, "Ω")} / ${SiParser.formatWithSi(r1, "Ω")}) = ${SiParser.formatFixed(gain, 3)}",
            intermediateSteps = steps,
            warnings = emptyList(),
            notes = listOf(
                "Minimum gain is 1 (unity gain buffer when Rf=0 or R1=∞). Output is in phase with input."
            )
        )
    }
}

object DbCalculator {
    fun voltageRatioToDb(ratio: Double): CalculationOutput {
        if (ratio <= 0) return CalculationOutput.error("Voltage ratio must be positive.")
        val db = 20.0 * log10(ratio)
        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(db, 3)} dB",
            primaryLabel = "Voltage Ratio in dB",
            formula = "dB = 20 × log10(V2 / V1)",
            substitution = "20 × log10($ratio) = ${SiParser.formatFixed(db, 3)} dB"
        )
    }

    fun dbToVoltageRatio(db: Double): CalculationOutput {
        val ratio = 10.0.pow(db / 20.0)
        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(ratio, 4)} ×",
            primaryLabel = "Linear Voltage Ratio",
            formula = "Ratio = 10^(dB / 20)",
            substitution = "10^($db / 20) = ${SiParser.formatFixed(ratio, 4)}"
        )
    }

    fun powerRatioToDb(ratio: Double): CalculationOutput {
        if (ratio <= 0) return CalculationOutput.error("Power ratio must be positive.")
        val db = 10.0 * log10(ratio)
        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(db, 3)} dB",
            primaryLabel = "Power Ratio in dB",
            formula = "dB = 10 × log10(P2 / P1)",
            substitution = "10 × log10($ratio) = ${SiParser.formatFixed(db, 3)} dB"
        )
    }

    fun dbmToMilliwatts(dbm: Double): CalculationOutput {
        val mw = 10.0.pow(dbm / 10.0)
        val w = mw * 1e-3
        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(w, "W"),
            primaryLabel = "Power (Watts)",
            formula = "P(mW) = 10^(dBm / 10)",
            substitution = "10^($dbm / 10) = ${SiParser.formatFixed(mw, 3)} mW",
            intermediateSteps = listOf(
                "Power in mW" to "${SiParser.formatFixed(mw, 3)} mW",
                "Power in Watts" to SiParser.formatWithSi(w, "W")
            )
        )
    }

    fun milliwattsToDbm(mw: Double): CalculationOutput {
        if (mw <= 0) return CalculationOutput.error("Power in mW must be positive.")
        val dbm = 10.0 * log10(mw)
        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(dbm, 2)} dBm",
            primaryLabel = "Power in dBm (ref 1mW)",
            formula = "dBm = 10 × log10(P_mW)",
            substitution = "10 × log10($mw) = ${SiParser.formatFixed(dbm, 2)} dBm"
        )
    }
}

object Timer555Calculator {
    fun calculateAstable(r1: Double, r2: Double, c: Double): CalculationOutput {
        if (r1 <= 0 || r2 <= 0 || c <= 0) return CalculationOutput.error("R1, R2, and C must be positive.")

        val tHigh = 0.693 * (r1 + r2) * c
        val tLow = 0.693 * r2 * c
        val period = tHigh + tLow
        val freq = 1.0 / period
        val dutyCycle = (tHigh / period) * 100.0

        val warnings = mutableListOf<CalculationWarning>()
        if (r1 < 1000.0) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "R1 < 1 kΩ causes excessive current into discharge pin (Pin 7). Keep R1 >= 1kΩ."))
        }
        if (freq > 2e6) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "Standard NE555 operates up to ~100kHz–500kHz. For > 1MHz, use CMOS TLC555."))
        }

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(freq, "Hz"),
            primaryLabel = "Astable Frequency",
            formula = "f = 1.44 / ((R1 + 2×R2) × C),  Duty = (R1 + R2) / (R1 + 2×R2) × 100%",
            substitution = "f = 1.44 / ((${SiParser.formatWithSi(r1, "Ω")} + 2×${SiParser.formatWithSi(r2, "Ω")}) × ${SiParser.formatWithSi(c, "F")}) = ${SiParser.formatWithSi(freq, "Hz")}",
            intermediateSteps = listOf(
                "Frequency (f)" to SiParser.formatWithSi(freq, "Hz"),
                "Period (T)" to SiParser.formatWithSi(period, "s"),
                "High Time (t_high)" to SiParser.formatWithSi(tHigh, "s"),
                "Low Time (t_low)" to SiParser.formatWithSi(tLow, "s"),
                "Duty Cycle" to "${SiParser.formatFixed(dutyCycle, 1)} %"
            ),
            warnings = warnings,
            notes = listOf(
                "Without a bypass diode across R2, standard astable duty cycle is always > 50%."
            )
        )
    }

    fun calculateMonostable(r: Double, c: Double): CalculationOutput {
        if (r <= 0 || c <= 0) return CalculationOutput.error("R and C must be positive.")
        val pulseWidth = 1.1 * r * c

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(pulseWidth, "s"),
            primaryLabel = "Pulse Duration (t)",
            formula = "t = 1.1 × R × C",
            substitution = "t = 1.1 × ${SiParser.formatWithSi(r, "Ω")} × ${SiParser.formatWithSi(c, "F")} = ${SiParser.formatWithSi(pulseWidth, "s")}",
            intermediateSteps = listOf(
                "Pulse Width (t)" to SiParser.formatWithSi(pulseWidth, "s")
            ),
            warnings = emptyList(),
            notes = listOf(
                "Monostable output triggers on falling edge of Pin 2 (falling below 1/3 Vcc)."
            )
        )
    }
}

object ResistorCodeCalculator {
    val E12 = listOf(1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2)
    val E24 = listOf(
        1.0, 1.1, 1.2, 1.3, 1.5, 1.6, 1.8, 2.0, 2.2, 2.4, 2.7, 3.0,
        3.3, 3.6, 3.9, 4.3, 4.7, 5.1, 5.6, 6.2, 6.8, 7.5, 8.2, 9.1
    )

    fun findNearestE24(targetR: Double): Double {
        if (targetR <= 0) return 1.0
        val exp = floor(log10(targetR))
        val normalized = targetR / 10.0.pow(exp)
        val nearestBase = E24.minByOrNull { abs(it - normalized) } ?: 1.0
        return nearestBase * 10.0.pow(exp)
    }

    fun findNearestE12(targetR: Double): Double {
        if (targetR <= 0) return 1.0
        val exp = floor(log10(targetR))
        val normalized = targetR / 10.0.pow(exp)
        val nearestBase = E12.minByOrNull { abs(it - normalized) } ?: 1.0
        return nearestBase * 10.0.pow(exp)
    }

    /**
     * Decode 3-digit, 4-digit, or R-notation SMD code.
     * e.g. "472" -> 4.7k, "1002" -> 10k, "4R7" -> 4.7, "R05" -> 0.05
     */
    fun decodeSmd(code: String): CalculationOutput {
        val trimmed = code.trim().uppercase()
        if (trimmed.isEmpty()) return CalculationOutput.error("Enter SMD code.")

        // R notation (e.g. 4R7, R050, 0R1)
        if (trimmed.contains('R')) {
            val parsed = trimmed.replace('R', '.').toDoubleOrNull()
                ?: return CalculationOutput.error("Invalid R-notation SMD code: $trimmed")
            return CalculationOutput(
                primaryValue = SiParser.formatWithSi(parsed, "Ω"),
                primaryLabel = "Resistance",
                formula = "R represents decimal point",
                substitution = "$trimmed = $parsed Ω"
            )
        }

        // 3-digit standard code (5% tolerance)
        if (trimmed.length == 3 && trimmed.all { it.isDigit() }) {
            val sig = trimmed.substring(0, 2).toDouble()
            val mult = 10.0.pow(trimmed.substring(2, 3).toDouble())
            val r = sig * mult
            return CalculationOutput(
                primaryValue = SiParser.formatWithSi(r, "Ω"),
                primaryLabel = "SMD 3-Digit (5% tol)",
                formula = "R = [Digits 1-2] × 10^[Digit 3]",
                substitution = "$trimmed = $sig × 10^${trimmed[2]} = ${SiParser.formatWithSi(r, "Ω")}"
            )
        }

        // 4-digit standard code (1% tolerance)
        if (trimmed.length == 4 && trimmed.all { it.isDigit() }) {
            val sig = trimmed.substring(0, 3).toDouble()
            val mult = 10.0.pow(trimmed.substring(3, 4).toDouble())
            val r = sig * mult
            return CalculationOutput(
                primaryValue = SiParser.formatWithSi(r, "Ω"),
                primaryLabel = "SMD 4-Digit (1% tol)",
                formula = "R = [Digits 1-3] × 10^[Digit 4]",
                substitution = "$trimmed = $sig × 10^${trimmed[3]} = ${SiParser.formatWithSi(r, "Ω")}"
            )
        }

        return CalculationOutput.error("Unrecognized SMD format. Supported: 3-digit (e.g. 472), 4-digit (e.g. 1002), R-notation (e.g. 4R7, R05).")
    }
}

object ToleranceAnalysisCalculator {
    fun calculateVoltageDividerWorstCase(
        vin: Double,
        vinTolPct: Double,
        r1: Double,
        r1TolPct: Double,
        r2: Double,
        r2TolPct: Double
    ): CalculationOutput {
        val vinNom = vin
        val vinMin = vin * (1 - vinTolPct / 100.0)
        val vinMax = vin * (1 + vinTolPct / 100.0)

        val r1Min = r1 * (1 - r1TolPct / 100.0)
        val r1Max = r1 * (1 + r1TolPct / 100.0)

        val r2Min = r2 * (1 - r2TolPct / 100.0)
        val r2Max = r2 * (1 + r2TolPct / 100.0)

        val voutNom = vinNom * (r2 / (r1 + r2))
        val voutMin = vinMin * (r2Min / (r1Max + r2Min))
        val voutMax = vinMax * (r2Max / (r1Min + r2Max))

        val deltaMinPct = ((voutMin - voutNom) / voutNom) * 100.0
        val deltaMaxPct = ((voutMax - voutNom) / voutNom) * 100.0

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(voutNom, "V")} [${SiParser.formatFixed(deltaMinPct, 1)}% .. +${SiParser.formatFixed(deltaMaxPct, 1)}%]",
            primaryLabel = "Nominal Vout & Worst-Case Bounds",
            formula = "Vout_min = Vin_min × R2_min / (R1_max + R2_min),  Vout_max = Vin_max × R2_max / (R1_min + R2_max)",
            substitution = "Vout range = ${SiParser.formatWithSi(voutMin, "V")} to ${SiParser.formatWithSi(voutMax, "V")}",
            intermediateSteps = listOf(
                "Nominal Vout" to SiParser.formatWithSi(voutNom, "V"),
                "Worst-Case Minimum" to "${SiParser.formatWithSi(voutMin, "V")} (${SiParser.formatFixed(deltaMinPct, 2)}%)",
                "Worst-Case Maximum" to "${SiParser.formatWithSi(voutMax, "V")} (+${SiParser.formatFixed(deltaMaxPct, 2)}%)",
                "Total Error Span" to "${SiParser.formatWithSi(voutMax - voutMin, "V")} (${SiParser.formatFixed(deltaMaxPct - deltaMinPct, 2)}%)"
            ),
            warnings = emptyList(),
            notes = listOf(
                "Use 0.1% or 1% thin-film resistors in precision ADC voltage divider networks."
            )
        )
    }
}
