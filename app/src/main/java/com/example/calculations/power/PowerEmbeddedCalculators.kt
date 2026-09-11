package com.example.calculations.power

import com.example.calculations.basic.ResistorCodeCalculator
import com.example.calculations.model.CalculationOutput
import com.example.calculations.model.CalculationWarning
import com.example.calculations.model.WarningLevel
import com.example.core.units.SiParser
import kotlin.math.*

object PowerBudgetCalculator {
    data class OperatingMode(
        val name: String,
        val currentMa: Double,
        val dutyPercent: Double // 0 to 100%
    )

    fun calculate(modes: List<OperatingMode>, batteryCapacityMah: Double? = null): CalculationOutput {
        if (modes.isEmpty()) return CalculationOutput.error("Add at least one operating mode.")
        val totalDuty = modes.sumOf { it.dutyPercent }
        if (totalDuty <= 0) return CalculationOutput.error("Total duty cycle must be greater than 0%.")

        // Weighted average current in mA
        val avgCurrentMa = modes.sumOf { it.currentMa * (it.dutyPercent / 100.0) }
        val dailyMah = avgCurrentMa * 24.0

        val steps = mutableListOf<Pair<String, String>>()
        modes.forEach { mode ->
            val contribution = mode.currentMa * (mode.dutyPercent / 100.0)
            steps.add("${mode.name} Mode (${mode.dutyPercent}%)" to "${SiParser.formatFixed(mode.currentMa, 2)} mA → contrib: ${SiParser.formatFixed(contribution, 3)} mA")
        }
        steps.add("Total Average Current" to "${SiParser.formatFixed(avgCurrentMa, 3)} mA (${SiParser.formatWithSi(avgCurrentMa * 1e-3, "A")})")
        steps.add("Daily Consumption" to "${SiParser.formatFixed(dailyMah, 1)} mAh / day")

        val warnings = mutableListOf<CalculationWarning>()
        if (abs(totalDuty - 100.0) > 0.1) {
            warnings.add(CalculationWarning(
                WarningLevel.INFO,
                "Total duty cycle is ${SiParser.formatFixed(totalDuty, 1)}% (normalized to 100% of cycle)."
            ))
        }

        if (batteryCapacityMah != null && batteryCapacityMah > 0) {
            val usableCapacity = batteryCapacityMah * 0.85 // 85% derating
            val hours = usableCapacity / avgCurrentMa
            val days = hours / 24.0
            val months = days / 30.4
            val years = days / 365.25

            steps.add("Battery Usable Capacity (85% derated)" to "${SiParser.formatFixed(usableCapacity, 0)} mAh")
            steps.add("Estimated Battery Lifetime" to "${SiParser.formatFixed(hours, 1)} hours (${SiParser.formatFixed(days, 1)} days)")
            if (years >= 1.0) {
                steps.add("Multi-Year Lifetime" to "${SiParser.formatFixed(years, 2)} years")
            } else if (months >= 1.0) {
                steps.add("Monthly Lifetime" to "${SiParser.formatFixed(months, 1)} months")
            }
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(avgCurrentMa, 3)} mA avg",
            primaryLabel = "Average System Current",
            formula = "I_avg = Σ (I_mode × Duty_mode),  Daily = I_avg × 24h",
            substitution = "I_avg = ${SiParser.formatFixed(avgCurrentMa, 3)} mA",
            intermediateSteps = steps,
            warnings = warnings,
            notes = listOf(
                "Optimize sleep current and minimize active duty cycle for maximum IoT battery life.",
                "Include sensor warm-up and crystal start-up times in active state duration."
            )
        )
    }
}

object BatteryRuntimeCalculator {
    fun calculate(
        capacityMah: Double,
        avgCurrentMa: Double,
        efficiencyPct: Double = 90.0,
        deratingPct: Double = 80.0
    ): CalculationOutput {
        if (capacityMah <= 0) return CalculationOutput.error("Battery capacity must be positive.")
        if (avgCurrentMa <= 0) return CalculationOutput.error("Average current must be positive.")

        val eff = (efficiencyPct / 100.0).coerceIn(0.1, 1.0)
        val derating = (deratingPct / 100.0).coerceIn(0.1, 1.0)

        // Effective usable mAh = capacity * derating * efficiency
        val usableCapacity = capacityMah * derating * eff
        val runtimeHours = usableCapacity / avgCurrentMa
        val runtimeDays = runtimeHours / 24.0
        val runtimeMonths = runtimeDays / 30.4375
        val runtimeYears = runtimeDays / 365.25

        val displayTime = when {
            runtimeHours < 24.0 -> "${SiParser.formatFixed(runtimeHours, 1)} hours"
            runtimeDays < 60.0 -> "${SiParser.formatFixed(runtimeDays, 1)} days (${SiParser.formatFixed(runtimeHours, 0)} hrs)"
            runtimeMonths < 24.0 -> "${SiParser.formatFixed(runtimeMonths, 1)} months (${SiParser.formatFixed(runtimeDays, 0)} days)"
            else -> "${SiParser.formatFixed(runtimeYears, 2)} years"
        }

        val warnings = mutableListOf<CalculationWarning>()
        if (runtimeYears > 5.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "Projected runtime exceeds 5 years. Battery self-discharge (~1-3% / year for primary lithium, 5-10% for Li-ion) will dominate actual lifespan!"
            ))
        }

        return CalculationOutput(
            primaryValue = displayTime,
            primaryLabel = "Estimated Runtime",
            formula = "Time = (Capacity × Derating × Efficiency) / I_avg",
            substitution = "Time = ($capacityMah mAh × $deratingPct% × $efficiencyPct%) / $avgCurrentMa mA = ${SiParser.formatFixed(runtimeHours, 1)} h",
            intermediateSteps = listOf(
                "Nominal Capacity" to "$capacityMah mAh",
                "Usable Capacity (after derating & eff)" to "${SiParser.formatFixed(usableCapacity, 1)} mAh",
                "Runtime in Hours" to "${SiParser.formatFixed(runtimeHours, 1)} h",
                "Runtime in Days" to "${SiParser.formatFixed(runtimeDays, 1)} days",
                "Runtime in Months" to "${SiParser.formatFixed(runtimeMonths, 1)} months",
                "Runtime in Years" to "${SiParser.formatFixed(runtimeYears, 2)} years"
            ),
            warnings = warnings,
            notes = listOf(
                "Standard derating factors: 80% accounts for end-of-discharge voltage cutoff, battery aging, and cold temperature degradation."
            )
        )
    }
}

object DcDcConverterCalculator {
    fun calculate(
        vin: Double,
        vout: Double,
        iout: Double,
        efficiencyPct: Double
    ): CalculationOutput {
        if (vin <= 0 || vout <= 0 || iout <= 0) return CalculationOutput.error("Vin, Vout, and Iout must be positive.")
        if (efficiencyPct <= 0 || efficiencyPct > 100) return CalculationOutput.error("Efficiency must be between 1% and 100%.")

        val eff = efficiencyPct / 100.0
        val pOut = vout * iout
        val pIn = pOut / eff
        val iIn = pIn / vin
        val pLoss = pIn - pOut

        val warnings = mutableListOf<CalculationWarning>()
        if (pLoss > 2.0) {
            warnings.add(CalculationWarning(WarningLevel.WARNING, "Converter loss is ${SiParser.formatWithSi(pLoss, "W")}. Ensure adequate thermal copper area or heatsinking."))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(iIn, "A")} input current | ${SiParser.formatWithSi(pLoss, "W")} loss",
            primaryLabel = "DC/DC Parameters",
            formula = "P_out = Vout × Iout,  P_in = P_out / η,  I_in = P_in / Vin,  P_loss = P_in - P_out",
            substitution = "P_out = $vout × $iout = ${SiParser.formatWithSi(pOut, "W")}, P_loss = ${SiParser.formatWithSi(pLoss, "W")}",
            intermediateSteps = listOf(
                "Output Power (P_out)" to SiParser.formatWithSi(pOut, "W"),
                "Input Power (P_in)" to SiParser.formatWithSi(pIn, "W"),
                "Input Current (I_in)" to SiParser.formatWithSi(iIn, "A"),
                "Power Loss (Heat)" to SiParser.formatWithSi(pLoss, "W"),
                "Efficiency" to "$efficiencyPct %"
            ),
            warnings = warnings,
            notes = listOf(
                "Switching frequency ripple requires proper input/output ceramic filtering capacitors with low ESR."
            )
        )
    }
}

object LdoRegulatorCalculator {
    fun calculate(
        vin: Double,
        vout: Double,
        iout: Double,
        thetaJa: Double = 60.0, // °C/W (e.g. SOT-223 on PCB)
        ambientTemp: Double = 25.0
    ): CalculationOutput {
        if (vin <= vout) return CalculationOutput.error("Vin ($vin V) must be greater than Vout ($vout V) for an LDO.")
        if (iout <= 0) return CalculationOutput.error("Load current must be positive.")

        val vDrop = vin - vout
        val pLoss = vDrop * iout
        val deltaT = pLoss * thetaJa
        val junctionTemp = ambientTemp + deltaT

        val warnings = mutableListOf<CalculationWarning>()
        if (junctionTemp > 125.0) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "CRITICAL OVERHEATING: Estimated junction temperature is ${SiParser.formatFixed(junctionTemp, 1)}°C (Max rated is typically 125°C - 150°C)! Thermal shutdown will occur. Switch to a switching buck regulator!"
            ))
        } else if (junctionTemp > 85.0 || pLoss > 1.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "HIGH TEMPERATURE: Tj = ${SiParser.formatFixed(junctionTemp, 1)}°C. High dissipation (${SiParser.formatWithSi(pLoss, "W")}). Requires large copper thermal pour."
            ))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(pLoss, "W")} dissipated (Tj ≈ ${SiParser.formatFixed(junctionTemp, 0)}°C)",
            primaryLabel = "LDO Power Dissipation",
            formula = "P_loss = (Vin - Vout) × Iout,  ΔT = P_loss × θ_JA,  Tj = Ta + ΔT",
            substitution = "P_loss = ($vin - $vout) × $iout = ${SiParser.formatWithSi(pLoss, "W")}",
            intermediateSteps = listOf(
                "Voltage Dropout" to "${SiParser.formatFixed(vDrop, 2)} V",
                "Power Loss (P_loss)" to SiParser.formatWithSi(pLoss, "W"),
                "Efficiency" to "${SiParser.formatFixed((vout / vin) * 100.0, 1)} %",
                "Thermal Resistance (θ_JA)" to "$thetaJa °C/W",
                "Temperature Rise (ΔT)" to "+${SiParser.formatFixed(deltaT, 1)} °C",
                "Estimated Junction Temp (Tj)" to "${SiParser.formatFixed(junctionTemp, 1)} °C"
            ),
            warnings = warnings,
            notes = listOf(
                "LDO efficiency is strictly Vout / Vin. For large (Vin - Vout) differentials and high currents, LDOs act as space heaters.",
                "Typical θ_JA: SOT-23 (~200°C/W), SOT-223 (~60°C/W), DPAK (~35°C/W), TO-220 (~25°C/W)."
            )
        )
    }
}

object WireVoltageDropCalculator {
    enum class ConductorMaterial(val resistivity: Double, val label: String) {
        COPPER(1.68e-8, "Copper (Cu)"),
        ALUMINUM(2.82e-8, "Aluminum (Al)")
    }

    // AWG to mm² conversion: mm² = 0.012668 * 92^((36 - AWG)/19.5)
    fun awgToMm2(awg: Double): Double {
        return 0.012668 * 92.0.pow((36.0 - awg) / 19.5)
    }

    fun mm2ToAwg(mm2: Double): Double {
        if (mm2 <= 0) return 0.0
        return 36.0 - 19.5 * (ln(mm2 / 0.012668) / ln(92.0))
    }

    fun calculate(
        supplyVoltage: Double,
        current: Double,
        lengthMeters: Double,
        crossSectionMm2: Double,
        material: ConductorMaterial = ConductorMaterial.COPPER,
        isRoundTrip2Wire: Boolean = true
    ): CalculationOutput {
        if (supplyVoltage <= 0 || current <= 0 || lengthMeters <= 0 || crossSectionMm2 <= 0) {
            return CalculationOutput.error("All parameters must be positive.")
        }

        val wireLength = if (isRoundTrip2Wire) lengthMeters * 2.0 else lengthMeters
        val areaM2 = crossSectionMm2 * 1e-6
        val resistance = (material.resistivity * wireLength) / areaM2
        val vDrop = current * resistance
        val vLoad = supplyVoltage - vDrop
        val dropPct = (vDrop / supplyVoltage) * 100.0
        val pLoss = current * current * resistance
        val approxAwg = mm2ToAwg(crossSectionMm2)

        val warnings = mutableListOf<CalculationWarning>()
        if (dropPct > 5.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "Voltage drop (${SiParser.formatFixed(dropPct, 1)}%) exceeds the 3-5% recommended limit! Use thicker wire or reduce distance."
            ))
        }
        if (vLoad <= 0) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "Conductor resistance is too high: load voltage collapsed to 0V!"
            ))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(vDrop, "V")} drop (${SiParser.formatFixed(dropPct, 2)}%)",
            primaryLabel = "Voltage Drop",
            formula = "R = ρ × L / A,  V_drop = I × R,  V_load = V_supply - V_drop",
            substitution = "V_drop = $current A × ${SiParser.formatWithSi(resistance, "Ω")} = ${SiParser.formatWithSi(vDrop, "V")}",
            intermediateSteps = listOf(
                "Wire Total Resistance" to SiParser.formatWithSi(resistance, "Ω"),
                "Voltage Drop (V_drop)" to SiParser.formatWithSi(vDrop, "V"),
                "Drop Percentage" to "${SiParser.formatFixed(dropPct, 2)} %",
                "Voltage at Load (V_load)" to SiParser.formatWithSi(vLoad, "V"),
                "Power Dissipated in Cable" to SiParser.formatWithSi(pLoss, "W"),
                "Equivalent AWG" to "AWG ${SiParser.formatFixed(approxAwg, 1)}"
            ),
            warnings = warnings,
            notes = listOf(
                "Calculated for 20°C. At higher temperatures, copper resistance increases by ~0.39% per °C.",
                "Round-trip mode accounts for both supply and return conductors (2 × Length)."
            )
        )
    }
}

object FuseSizingCalculator {
    fun calculate(
        operatingCurrent: Double,
        inrushCurrent: Double? = null,
        safetyFactor: Double = 1.25
    ): CalculationOutput {
        if (operatingCurrent <= 0) return CalculationOutput.error("Operating current must be positive.")

        val minContinuousRating = operatingCurrent * safetyFactor
        val inrushVal = inrushCurrent ?: (operatingCurrent * 3.0)
        val inrushRatio = inrushVal / operatingCurrent

        val standardFuseRatings = listOf(
            0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3.15,
            4.0, 5.0, 6.3, 8.0, 10.0, 12.5, 15.0, 16.0, 20.0, 25.0, 30.0
        )
        val recommendedRating = standardFuseRatings.firstOrNull { it >= minContinuousRating }
            ?: (ceil(minContinuousRating))

        val fuseType = if (inrushRatio > 2.5) "Slow-Blow / Time-Lag (T type)" else "Fast-Acting (F type)"

        return CalculationOutput(
            primaryValue = "$recommendedRating A ($fuseType)",
            primaryLabel = "Recommended Fuse Rating",
            formula = "I_fuse ≥ I_operating × Safety_Factor (1.25 - 1.5×)",
            substitution = "I_fuse ≥ $operatingCurrent A × $safetyFactor = ${SiParser.formatFixed(minContinuousRating, 2)} A",
            intermediateSteps = listOf(
                "Continuous Operating Current" to "${SiParser.formatFixed(operatingCurrent, 2)} A",
                "Minimum Derated Threshold" to "${SiParser.formatFixed(minContinuousRating, 2)} A",
                "Nearest Standard Rating" to "$recommendedRating A",
                "Recommended Type" to fuseType,
                "Inrush Ratio" to "${SiParser.formatFixed(inrushRatio, 1)}×"
            ),
            warnings = listOf(
                CalculationWarning(
                    WarningLevel.WARNING,
                    "CRITICAL DISCLAIMER: Fuse selection must always be verified against the manufacturer's Time-Current (I²t) melting curve and ambient temperature derating profile."
                )
            ),
            notes = listOf(
                "Fuses protect wiring and PCB traces from catastrophic fire, not silicon semiconductors.",
                "Automotive blade fuses and glass cartidge fuses have distinct blow characteristics."
            )
        )
    }
}

object PcbTraceWidthCalculator {
    // IPC-2221 standard formula:
    // I = k * (Delta_T)^0.44 * (A)^0.725
    // where k = 0.048 for external, 0.024 for internal
    // A = area in mils² -> width in mils = A / thickness_in_mils
    fun calculate(
        current: Double,
        tempRiseC: Double = 10.0,
        copperWeightOz: Double = 1.0, // 1 oz = 1.378 mil = 35 µm
        isExternal: Boolean = true
    ): CalculationOutput {
        if (current <= 0 || tempRiseC <= 0 || copperWeightOz <= 0) {
            return CalculationOutput.error("All parameters must be positive.")
        }

        val k = if (isExternal) 0.048 else 0.024
        // Area in mils² = (I / (k * DeltaT^0.44))^(1 / 0.725)
        val areaMil2 = (current / (k * tempRiseC.pow(0.44))).pow(1.0 / 0.725)
        val thicknessMil = copperWeightOz * 1.378
        val widthMil = areaMil2 / thicknessMil
        val widthMm = widthMil * 0.0254

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(widthMm, 2)} mm (${SiParser.formatFixed(widthMil, 1)} mils)",
            primaryLabel = "Minimum PCB Trace Width",
            formula = "IPC-2221: Area = (I / (k × ΔT^0.44))^(1 / 0.725),  Width = Area / Thickness",
            substitution = "Width = ${SiParser.formatFixed(widthMm, 2)} mm for $current A with $tempRiseC°C rise",
            intermediateSteps = listOf(
                "Current" to "$current A",
                "Allowable Temp Rise (ΔT)" to "$tempRiseC °C",
                "Copper Thickness" to "$copperWeightOz oz (${SiParser.formatFixed(copperWeightOz * 35.0, 1)} µm)",
                "Layer Placement" to if (isExternal) "External (Outer)" else "Internal (Inner)",
                "Cross-Sectional Area" to "${SiParser.formatFixed(areaMil2, 1)} mils²",
                "Recommended Width" to "${SiParser.formatFixed(widthMm, 2)} mm (${SiParser.formatFixed(widthMil, 1)} mils)"
            ),
            warnings = emptyList(),
            notes = listOf(
                "Internal traces need roughly twice the width of external traces due to poorer thermal dissipation through FR4.",
                "For high-current designs, consider opening solder mask and adding solder/bus wire."
            )
        )
    }
}

object DecouplingCapacitorCalculator {
    fun calculate(
        deltaCurrent: Double,
        durationSeconds: Double,
        allowableVoltageDroop: Double
    ): CalculationOutput {
        if (deltaCurrent <= 0 || durationSeconds <= 0 || allowableVoltageDroop <= 0) {
            return CalculationOutput.error("All parameters must be positive.")
        }

        // C_min = delta_I * dt / delta_V
        val cMin = (deltaCurrent * durationSeconds) / allowableVoltageDroop

        return CalculationOutput(
            primaryValue = SiParser.formatWithSi(cMin, "F"),
            primaryLabel = "Minimum Decoupling Capacitance",
            formula = "C_min = ΔI × Δt / ΔV_droop",
            substitution = "C_min = ($deltaCurrent A × ${SiParser.formatWithSi(durationSeconds, "s")}) / $allowableVoltageDroop V = ${SiParser.formatWithSi(cMin, "F")}",
            intermediateSteps = listOf(
                "Transient Current Step (ΔI)" to SiParser.formatWithSi(deltaCurrent, "A"),
                "Transient Duration (Δt)" to SiParser.formatWithSi(durationSeconds, "s"),
                "Allowed Voltage Droop (ΔV)" to SiParser.formatWithSi(allowableVoltageDroop, "V"),
                "Minimum Theoretical Capacitance" to SiParser.formatWithSi(cMin, "F"),
                "Recommended Design Value (2× margin)" to SiParser.formatWithSi(cMin * 2.0, "F")
            ),
            warnings = listOf(
                CalculationWarning(
                    WarningLevel.INFO,
                    "High-frequency decoupling requires placing small ceramic caps (0.1µF 0402/0603) physically closest to IC power pins to minimize parasitic trace inductance (ESL)."
                )
            ),
            notes = listOf(
                "Bulk capacitors (e.g. 10µF–100µF tantalum/electrolytic) buffer longer current surges.",
                "Local bypass capacitors (0.1µF X7R ceramic) supply nanosecond switching spikes."
            )
        )
    }
}

object McuTimerCalculator {
    fun calculate(
        mcuClockHz: Double,
        prescalerPsc: Long,
        autoReloadArr: Long
    ): CalculationOutput {
        if (mcuClockHz <= 0) return CalculationOutput.error("MCU clock must be positive.")
        if (prescalerPsc < 0 || autoReloadArr < 0) return CalculationOutput.error("PSC and ARR must be non-negative.")

        val timerTickFreq = mcuClockHz / (prescalerPsc + 1)
        val overflowFreq = timerTickFreq / (autoReloadArr + 1)
        val periodSeconds = 1.0 / overflowFreq

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(overflowFreq, "Hz")} (${SiParser.formatWithSi(periodSeconds, "s")})",
            primaryLabel = "Timer Overflow Frequency & Period",
            formula = "f_timer = f_clk / (PSC + 1),  f_overflow = f_timer / (ARR + 1)",
            substitution = "f = ${SiParser.formatWithSi(mcuClockHz, "Hz")} / ((${prescalerPsc} + 1) × (${autoReloadArr} + 1)) = ${SiParser.formatWithSi(overflowFreq, "Hz")}",
            intermediateSteps = listOf(
                "Timer Tick Clock" to SiParser.formatWithSi(timerTickFreq, "Hz"),
                "Timer Tick Period" to SiParser.formatWithSi(1.0 / timerTickFreq, "s"),
                "Overflow Interrupt Frequency" to SiParser.formatWithSi(overflowFreq, "Hz"),
                "Overflow Period" to SiParser.formatWithSi(periodSeconds, "s")
            ),
            warnings = emptyList(),
            notes = listOf(
                "Common in STM32, AVR, ESP32, and PIC microcontroller hardware timers.",
                "Prescaler and ARR registers use (N + 1) count division."
            )
        )
    }
}

object UartBaudRateCalculator {
    fun calculate(
        mcuClockHz: Double,
        desiredBaud: Double,
        oversampling: Int = 16
    ): CalculationOutput {
        if (mcuClockHz <= 0 || desiredBaud <= 0) return CalculationOutput.error("Clock and baud rate must be positive.")

        val usartDiv = mcuClockHz / (oversampling * desiredBaud)
        val actualBaud = mcuClockHz / (oversampling * round(usartDiv))
        val errorPct = ((actualBaud - desiredBaud) / desiredBaud) * 100.0

        val warnings = mutableListOf<CalculationWarning>()
        if (abs(errorPct) > 2.0) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "High baud rate error (${SiParser.formatFixed(abs(errorPct), 2)}%). UART framing errors will occur! Error must be <= 2% (ideally < 1%)."
            ))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(actualBaud, 0)} baud (Error: ${SiParser.formatFixed(errorPct, 2)}%)",
            primaryLabel = "Actual Baud Rate & Error",
            formula = "USARTDIV = f_clk / (Oversampling × Baud),  Error = (Actual - Desired) / Desired × 100%",
            substitution = "DIV = $mcuClockHz / ($oversampling × $desiredBaud) = ${SiParser.formatFixed(usartDiv, 4)}",
            intermediateSteps = listOf(
                "Desired Baud Rate" to "${SiParser.formatFixed(desiredBaud, 0)} bps",
                "USARTDIV (Ideal)" to SiParser.formatFixed(usartDiv, 4),
                "Integer Divisor" to "${round(usartDiv).toInt()}",
                "Actual Baud Rate" to "${SiParser.formatFixed(actualBaud, 1)} bps",
                "Clock Deviation Error" to "${SiParser.formatFixed(errorPct, 2)} %"
            ),
            warnings = warnings,
            notes = listOf(
                "UART requires < 2.5% cumulative timing error across transmitter and receiver to sample the 10-bit frame correctly."
            )
        )
    }
}

object I2cPullupCalculator {
    enum class SpeedMode(val label: String, val maxTrNs: Double, val maxIolMa: Double) {
        STANDARD("Standard (100 kHz)", 1000.0, 3.0),
        FAST("Fast (400 kHz)", 300.0, 3.0),
        FAST_PLUS("Fast-mode Plus (1 MHz)", 120.0, 20.0)
    }

    fun calculate(
        vdd: Double,
        busCapacitancePf: Double,
        mode: SpeedMode = SpeedMode.STANDARD
    ): CalculationOutput {
        if (vdd <= 0 || busCapacitancePf <= 0) return CalculationOutput.error("Vdd and Bus Capacitance must be positive.")

        val vol = 0.4 // Max low-level output voltage standard
        val iol = mode.maxIolMa * 1e-3
        val cb = busCapacitancePf * 1e-12
        val tr = mode.maxTrNs * 1e-9

        val rMin = (vdd - vol) / iol
        // According to NXP UM10204: tr = 0.8473 * R_pullup * Cb  -> R_max = tr / (0.8473 * Cb)
        val rMax = tr / (0.8473 * cb)

        val warnings = mutableListOf<CalculationWarning>()
        if (rMin > rMax) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "Bus capacitance (${busCapacitancePf} pF) is too high for ${mode.label}! R_min (${SiParser.formatWithSi(rMin, "Ω")}) exceeds R_max (${SiParser.formatWithSi(rMax, "Ω")}). Add an I2C bus buffer/accelerator!"
            ))
        }

        val recR = (rMin + rMax) / 2.0
        val stdRec = ResistorCodeCalculator.findNearestE24(recR)

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(rMin, "Ω")} .. ${SiParser.formatWithSi(rMax, "Ω")} (Rec: ${SiParser.formatWithSi(stdRec, "Ω")})",
            primaryLabel = "Permissible Pull-Up Resistor Range",
            formula = "R_min = (Vdd - Vol) / Iol,  R_max = t_r / (0.8473 × C_bus)",
            substitution = "R_min = ($vdd - 0.4) / ${mode.maxIolMa}mA = ${SiParser.formatWithSi(rMin, "Ω")}, R_max = ${mode.maxTrNs}ns / (0.8473 × ${busCapacitancePf}pF)",
            intermediateSteps = listOf(
                "Minimum Pull-Up (R_min)" to SiParser.formatWithSi(rMin, "Ω"),
                "Maximum Pull-Up (R_max)" to SiParser.formatWithSi(rMax, "Ω"),
                "Recommended Value" to SiParser.formatWithSi(stdRec, "Ω"),
                "Low-State Sink Current at R_min" to "${mode.maxIolMa} mA"
            ),
            warnings = warnings,
            notes = listOf(
                "Standard I2C maximum bus capacitance limit is 400 pF (or 550 pF in Fast-mode Plus).",
                "Typical standard values: 4.7 kΩ for 100 kHz, 2.2 kΩ for 400 kHz on 3.3V bus."
            )
        )
    }
}

object CanTerminationCalculator {
    fun calculate(terminatorCount: Int, singleResistorOhms: Double = 120.0): CalculationOutput {
        if (terminatorCount < 1) return CalculationOutput.error("Enter at least 1 terminator.")
        val rEq = singleResistorOhms / terminatorCount

        val warnings = mutableListOf<CalculationWarning>()
        if (terminatorCount != 2) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "ISO 11898 standard specifies exactly TWO 120 Ω terminators (one at each physical end of the bus) giving 60 Ω equivalent. Current: $terminatorCount terminators → ${SiParser.formatFixed(rEq, 1)} Ω."
            ))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(rEq, 1)} Ω equivalent",
            primaryLabel = "CAN Bus Termination",
            formula = "R_eq = R_term / N",
            substitution = "R_eq = $singleResistorOhms Ω / $terminatorCount = ${SiParser.formatFixed(rEq, 1)} Ω",
            intermediateSteps = listOf(
                "Terminator Count" to "$terminatorCount",
                "Equivalent Differential Resistance" to "${SiParser.formatFixed(rEq, 1)} Ω",
                "Standard Requirement" to "60 Ω (Two 120 Ω at endpoints)"
            ),
            warnings = warnings,
            notes = listOf(
                "Terminating both ends absorbs high-speed signal reflections on transmission lines.",
                "Split termination (two 60 Ω with center capacitor to GND) provides superior common-mode noise suppression."
            )
        )
    }
}

object AdcCalculator {
    fun calculate(
        bits: Int,
        vRef: Double,
        measuredVoltage: Double? = null,
        adcCode: Long? = null,
        dividerR1: Double? = null,
        dividerR2: Double? = null
    ): CalculationOutput {
        if (bits < 1 || bits > 32) return CalculationOutput.error("Bits must be between 1 and 32.")
        if (vRef <= 0) return CalculationOutput.error("Vref must be positive.")

        val maxCode = (1L shl bits) - 1
        val lsbVoltage = vRef / (1L shl bits)

        val steps = mutableListOf<Pair<String, String>>()
        steps.add("ADC Resolution" to "$bits bits ($maxCode max counts)")
        steps.add("LSB Voltage Step" to SiParser.formatWithSi(lsbVoltage, "V"))

        var primary = "${SiParser.formatWithSi(lsbVoltage, "V")} / LSB"
        var label = "LSB Resolution"

        if (adcCode != null) {
            val vPin = adcCode * lsbVoltage
            steps.add("Pin Voltage from Code $adcCode" to SiParser.formatWithSi(vPin, "V"))
            primary = SiParser.formatWithSi(vPin, "V")
            label = "Measured ADC Pin Voltage"

            if (dividerR1 != null && dividerR2 != null && dividerR2 > 0) {
                val vSource = vPin * ((dividerR1 + dividerR2) / dividerR2)
                steps.add("Source Voltage (before divider)" to SiParser.formatWithSi(vSource, "V"))
                primary = SiParser.formatWithSi(vSource, "V")
                label = "Scaled Source Voltage"
            }
        } else if (measuredVoltage != null) {
            val code = (measuredVoltage / lsbVoltage).toLong().coerceIn(0, maxCode)
            steps.add("Calculated ADC Code" to "$code (0x${code.toString(16).uppercase()})")
            primary = "$code (0x${code.toString(16).uppercase()})"
            label = "Calculated ADC Code"
        }

        return CalculationOutput(
            primaryValue = primary,
            primaryLabel = label,
            formula = "LSB = Vref / 2^bits,  Voltage = Code × LSB,  Code = Voltage / LSB",
            substitution = "LSB = $vRef / 2^$bits = ${SiParser.formatWithSi(lsbVoltage, "V")}",
            intermediateSteps = steps,
            warnings = emptyList(),
            notes = listOf(
                "Noise floor, PCB layout, and reference stability limit effective number of bits (ENOB).",
                "Ensure source impedance does not exceed sample-and-hold charging capacity."
            )
        )
    }
}

object PwmCalculator {
    fun calculate(
        timerClockHz: Double,
        prescaler: Long,
        arrPeriod: Long,
        dutyPercent: Double
    ): CalculationOutput {
        if (timerClockHz <= 0) return CalculationOutput.error("Clock must be positive.")
        if (prescaler < 0 || arrPeriod < 0) return CalculationOutput.error("PSC and ARR must be non-negative.")

        val pwmFreq = timerClockHz / ((prescaler + 1) * (arrPeriod + 1))
        val ccrValue = round((dutyPercent / 100.0) * (arrPeriod + 1)).toLong().coerceIn(0, arrPeriod + 1)
        val resolutionBits = log2((arrPeriod + 1).toDouble())

        return CalculationOutput(
            primaryValue = "${SiParser.formatWithSi(pwmFreq, "Hz")} | CCR = $ccrValue",
            primaryLabel = "PWM Frequency & Compare Value",
            formula = "f_pwm = f_clk / ((PSC + 1) × (ARR + 1)),  CCR = (Duty / 100) × (ARR + 1)",
            substitution = "f = $timerClockHz / ((${prescaler}+1) × (${arrPeriod}+1)) = ${SiParser.formatWithSi(pwmFreq, "Hz")}",
            intermediateSteps = listOf(
                "PWM Frequency" to SiParser.formatWithSi(pwmFreq, "Hz"),
                "PWM Period" to SiParser.formatWithSi(1.0 / pwmFreq, "s"),
                "Compare Value (CCR)" to "$ccrValue / ${arrPeriod + 1}",
                "Actual Duty Cycle" to "${SiParser.formatFixed((ccrValue.toDouble() / (arrPeriod + 1)) * 100.0, 2)} %",
                "Effective Resolution" to "${arrPeriod + 1} steps (~${SiParser.formatFixed(resolutionBits, 1)} bits)"
            ),
            warnings = emptyList(),
            notes = listOf(
                "For motor control, ultrasonic frequencies (> 20 kHz) eliminate audible switching whine."
            )
        )
    }
}

object UnitConverterCalculator {
    fun convertAhToCoulombs(ah: Double) = ah * 3600.0
    fun convertCoulombsToAh(c: Double) = c / 3600.0

    fun convertWhToJoules(wh: Double) = wh * 3600.0
    fun convertJoulesToWh(j: Double) = j / 3600.0

    fun convertDbmToMw(dbm: Double) = 10.0.pow(dbm / 10.0)
    fun convertMwToDbm(mw: Double) = if (mw > 0) 10.0 * log10(mw) else Double.NaN

    fun celsiusToFahrenheit(c: Double) = (c * 9.0 / 5.0) + 32.0
    fun fahrenheitToCelsius(f: Double) = (f - 32.0) * 5.0 / 9.0

    fun hzToPeriodSeconds(hz: Double) = if (hz > 0) 1.0 / hz else Double.NaN
    fun periodSecondsToHz(s: Double) = if (s > 0) 1.0 / s else Double.NaN
}
