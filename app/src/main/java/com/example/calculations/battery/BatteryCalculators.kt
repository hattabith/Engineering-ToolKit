package com.example.calculations.battery

import com.example.calculations.model.CalculationOutput
import com.example.calculations.model.CalculationWarning
import com.example.calculations.model.WarningLevel
import com.example.core.units.SiParser
import kotlin.math.*

enum class BatteryChemistry(
    val label: String,
    val nominalV: Double,
    val maxV: Double,
    val minV: Double
) {
    LI_ION_NMC("Li-ion (NMC/INR) 3.6V-3.7V", 3.65, 4.20, 3.00),
    LIFEPO4("LiFePO4 (LFP) 3.2V", 3.20, 3.65, 2.50),
    LIPO("LiPo (Lithium Polymer) 3.7V", 3.70, 4.20, 3.20),
    LTO("LTO (Lithium Titanate) 2.3V", 2.30, 2.80, 1.50),
    LEAD_ACID("Lead-Acid 2.0V/cell (12V AGM)", 2.00, 2.40, 1.75)
}

object BatteryPackConfigurator {
    fun calculate(
        chemistry: BatteryChemistry,
        seriesS: Int,
        parallelP: Int,
        cellCapacityAh: Double,
        cellContCurrentA: Double,
        cellPeakCurrentA: Double? = null
    ): CalculationOutput {
        if (seriesS < 1 || parallelP < 1) return CalculationOutput.error("Series and Parallel counts must be at least 1.")
        if (cellCapacityAh <= 0 || cellContCurrentA <= 0) return CalculationOutput.error("Cell capacity and current must be positive.")

        val totalCells = seriesS * parallelP
        val vNom = seriesS * chemistry.nominalV
        val vMax = seriesS * chemistry.maxV
        val vMin = seriesS * chemistry.minV

        val packCapacityAh = parallelP * cellCapacityAh
        val packEnergyWh = vNom * packCapacityAh
        val packContCurrentA = parallelP * cellContCurrentA
        val peakCurrent = cellPeakCurrentA ?: (cellContCurrentA * 2.0)
        val packPeakCurrentA = parallelP * peakCurrent

        val warnings = mutableListOf<CalculationWarning>()
        if (vMax > 60.0) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "HIGH VOLTAGE HAZARD: Pack maximum voltage is ${SiParser.formatFixed(vMax, 1)} V (> 60V DC safety threshold). Lethal electric shock hazard! Use insulated tools and certified isolation."
            ))
        }
        if (packContCurrentA > 80.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "HIGH CURRENT: Continuous discharge is ${SiParser.formatFixed(packContCurrentA, 1)} A. Ensure heavy-duty busbars, copper plates, and multi-point nickel strip welding."
            ))
        }
        warnings.add(CalculationWarning(
            WarningLevel.WARNING,
            "SAFETY DISCLAIMER: Always incorporate an appropriately rated Battery Management System (BMS) with cell balancing, overcurrent, and thermal cutoff."
        ))

        return CalculationOutput(
            primaryValue = "${seriesS}S${parallelP}P: ${SiParser.formatFixed(vNom, 1)}V, ${SiParser.formatFixed(packCapacityAh, 1)}Ah (${SiParser.formatFixed(packEnergyWh, 0)} Wh)",
            primaryLabel = "Pack Summary",
            formula = "V_pack = S × V_cell,  Ah_pack = P × Ah_cell,  Wh = V_nom × Ah",
            substitution = "${seriesS}S × ${chemistry.nominalV}V = ${SiParser.formatFixed(vNom, 1)}V,  ${parallelP}P × ${cellCapacityAh}Ah = ${SiParser.formatFixed(packCapacityAh, 1)}Ah",
            intermediateSteps = listOf(
                "Total Cell Count" to "$totalCells cells",
                "Nominal Voltage" to "${SiParser.formatFixed(vNom, 2)} V",
                "Full Charge Voltage (100%)" to "${SiParser.formatFixed(vMax, 2)} V",
                "Cutoff Discharge Voltage (0%)" to "${SiParser.formatFixed(vMin, 2)} V",
                "Total Pack Capacity" to "${SiParser.formatFixed(packCapacityAh, 2)} Ah",
                "Total Pack Energy" to "${SiParser.formatFixed(packEnergyWh, 1)} Wh (${SiParser.formatFixed(packEnergyWh / 1000.0, 2)} kWh)",
                "Max Continuous Current" to "${SiParser.formatFixed(packContCurrentA, 1)} A",
                "Max Peak Current" to "${SiParser.formatFixed(packPeakCurrentA, 1)} A",
                "Continuous Power Delivery" to SiParser.formatWithSi(vNom * packContCurrentA, "W")
            ),
            warnings = warnings,
            notes = listOf(
                "All cells must be matched in capacity, internal resistance, and voltage before assembly."
            )
        )
    }
}

object TargetPackFinder {
    fun findConfig(
        targetVoltage: Double,
        targetCapacityAh: Double,
        cellNomVoltage: Double,
        cellCapacityAh: Double,
        targetCurrentA: Double? = null,
        cellMaxCurrentA: Double? = null
    ): CalculationOutput {
        if (targetVoltage <= 0 || targetCapacityAh <= 0 || cellNomVoltage <= 0 || cellCapacityAh <= 0) {
            return CalculationOutput.error("All target parameters must be positive.")
        }

        val s = max(1, round(targetVoltage / cellNomVoltage).toInt())
        var p = max(1, ceil(targetCapacityAh / cellCapacityAh).toInt())

        if (targetCurrentA != null && cellMaxCurrentA != null && cellMaxCurrentA > 0) {
            val pForCurrent = ceil(targetCurrentA / cellMaxCurrentA).toInt()
            p = max(p, pForCurrent)
        }

        val achievedV = s * cellNomVoltage
        val achievedAh = p * cellCapacityAh
        val achievedWh = achievedV * achievedAh
        val totalCells = s * p

        return CalculationOutput(
            primaryValue = "${s}S ${p}P ($totalCells cells)",
            primaryLabel = "Optimal Battery Configuration",
            formula = "S = round(Target_V / Cell_V),  P = ceil(Target_Ah / Cell_Ah)",
            substitution = "S = round($targetVoltage / $cellNomVoltage) = $s, P = ceil($targetCapacityAh / $cellCapacityAh) = $p",
            intermediateSteps = listOf(
                "Recommended S Count" to "$s Series",
                "Recommended P Count" to "$p Parallel",
                "Total Cells Required" to "$totalCells pcs",
                "Achieved Nominal Voltage" to "${SiParser.formatFixed(achievedV, 2)} V (Target: ${targetVoltage}V)",
                "Achieved Capacity" to "${SiParser.formatFixed(achievedAh, 2)} Ah (Target: ${targetCapacityAh}Ah)",
                "Achieved Energy" to "${SiParser.formatFixed(achievedWh, 1)} Wh"
            ),
            warnings = listOf(
                CalculationWarning(
                    WarningLevel.INFO,
                    "Verify enclosure physical volume, cell spacing (1-2mm for thermal dissipation), and BMS balancing leads."
                )
            ),
            notes = listOf(
                "For high vibration environments, use injection molded cell spacers / brackets."
            )
        )
    }
}

object CRateCalculator {
    fun calculate(
        capacityAh: Double,
        currentA: Double,
        maxRatedC: Double? = null
    ): CalculationOutput {
        if (capacityAh <= 0 || currentA <= 0) return CalculationOutput.error("Capacity and current must be positive.")

        val cRate = currentA / capacityAh
        val theoreticalDischargeHours = 1.0 / cRate
        val theoreticalDischargeMinutes = theoreticalDischargeHours * 60.0

        val warnings = mutableListOf<CalculationWarning>()
        if (maxRatedC != null && cRate > maxRatedC) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "EXCEEDING MAXIMUM C-RATE: Current is ${SiParser.formatFixed(cRate, 2)}C, but cell is only rated for ${SiParser.formatFixed(maxRatedC, 1)}C! Severe thermal runaway and rapid degradation risk!"
            ))
        }

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(cRate, 2)} C",
            primaryLabel = "Discharge / Charge C-Rate",
            formula = "C_rate = Current (A) / Capacity (Ah)",
            substitution = "$currentA A / $capacityAh Ah = ${SiParser.formatFixed(cRate, 2)} C",
            intermediateSteps = listOf(
                "Calculated C-Rate" to "${SiParser.formatFixed(cRate, 2)} C",
                "Theoretical Full Discharge Time" to if (theoreticalDischargeHours >= 1.0)
                    "${SiParser.formatFixed(theoreticalDischargeHours, 2)} hours"
                else
                    "${SiParser.formatFixed(theoreticalDischargeMinutes, 1)} minutes",
                "Equivalent 1C Current" to "$capacityAh A"
            ),
            warnings = warnings,
            notes = listOf(
                "1C means the battery is fully discharged from 100% to 0% in exactly 1 hour.",
                "High discharge rates (> 2C) experience Peukert's capacity loss and heating."
            )
        )
    }
}

object ChargingTimeCalculator {
    fun calculate(
        capacityMah: Double,
        chargeCurrentMa: Double,
        initialSocPercent: Double = 10.0,
        chargerEfficiencyPct: Double = 90.0
    ): CalculationOutput {
        if (capacityMah <= 0 || chargeCurrentMa <= 0) return CalculationOutput.error("Capacity and charge current must be positive.")

        val socDelta = (100.0 - initialSocPercent).coerceIn(0.0, 100.0) / 100.0
        val neededMah = capacityMah * socDelta
        val eff = (chargerEfficiencyPct / 100.0).coerceIn(0.5, 1.0)

        // Standard Li-ion CC/CV profile adds ~20-30% extra time in CV saturation phase
        val ccHours = (neededMah / chargeCurrentMa) / eff
        val totalEstimatedHours = ccHours * 1.25
        val totalMinutes = totalEstimatedHours * 60.0

        return CalculationOutput(
            primaryValue = "${(totalMinutes / 60).toInt()} h ${(totalMinutes % 60).toInt()} min",
            primaryLabel = "Estimated Charging Time (CC/CV)",
            formula = "t_charge ≈ (Capacity × ΔSoC) / (I_charge × η) × 1.25 (CV tail factor)",
            substitution = "t ≈ ($capacityMah × $socDelta) / ($chargeCurrentMa × $eff) × 1.25 = ${SiParser.formatFixed(totalEstimatedHours, 2)} h",
            intermediateSteps = listOf(
                "Target Capacity to Replenish" to "${SiParser.formatFixed(neededMah, 0)} mAh",
                "Charge Current" to "$chargeCurrentMa mA (${SiParser.formatFixed(chargeCurrentMa / capacityMah, 2)}C)",
                "Constant Current (CC) Stage Duration" to "${SiParser.formatFixed(ccHours, 2)} h",
                "Constant Voltage (CV) Stage Estimate" to "+${SiParser.formatFixed(totalEstimatedHours - ccHours, 2)} h",
                "Total Charge Time" to "${(totalMinutes / 60).toInt()} hours ${(totalMinutes % 60).toInt()} minutes"
            ),
            warnings = listOf(
                CalculationWarning(
                    WarningLevel.WARNING,
                    "DISCLAIMER: Approximate estimation. Real charging curve is governed by ambient temperature, cell chemistry, internal resistance, and BMS balancing cutoff."
                )
            ),
            notes = listOf(
                "Standard recommended charge rate for standard 18650/21700 cells is 0.5C."
            )
        )
    }
}

object VoltageSagCalculator {
    fun calculate(
        cellInternalResistanceMohm: Double,
        seriesS: Int,
        parallelP: Int,
        loadCurrentA: Double,
        cellOpenCircuitV: Double = 3.8
    ): CalculationOutput {
        if (cellInternalResistanceMohm <= 0 || seriesS < 1 || parallelP < 1 || loadCurrentA <= 0) {
            return CalculationOutput.error("All parameters must be positive.")
        }

        val rCellOhms = cellInternalResistanceMohm * 1e-3
        val packResistanceOhms = (seriesS.toDouble() / parallelP.toDouble()) * rCellOhms
        val vSag = loadCurrentA * packResistanceOhms
        val packOcv = seriesS * cellOpenCircuitV
        val vLoaded = packOcv - vSag
        val powerLossWatts = loadCurrentA * loadCurrentA * packResistanceOhms
        val lossPerCellWatts = powerLossWatts / (seriesS * parallelP)

        val warnings = mutableListOf<CalculationWarning>()
        if (vLoaded < seriesS * 3.0) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "LOW VOLTAGE CUTOFF: Loaded voltage (${SiParser.formatFixed(vLoaded, 2)} V) falls below standard safe discharge cutoff (~3.0V/cell). Pack is heavily overloaded!"
            ))
        }
        if (lossPerCellWatts > 2.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "HIGH INTERNAL HEATING: Dissipating ${SiParser.formatFixed(lossPerCellWatts, 2)} W inside each individual cell! Cells will heat rapidly."
            ))
        }

        return CalculationOutput(
            primaryValue = "-${SiParser.formatWithSi(vSag, "V")} sag (${SiParser.formatFixed(vLoaded, 2)} V under load)",
            primaryLabel = "Pack Voltage Sag",
            formula = "R_pack = (S / P) × R_cell,  ΔV = I_load × R_pack,  P_loss = I² × R_pack",
            substitution = "ΔV = $loadCurrentA A × ${SiParser.formatFixed(packResistanceOhms * 1000, 2)} mΩ = ${SiParser.formatWithSi(vSag, "V")}",
            intermediateSteps = listOf(
                "Total Pack Internal Resistance" to "${SiParser.formatFixed(packResistanceOhms * 1000.0, 2)} mΩ",
                "Open Circuit Voltage (OCV)" to "${SiParser.formatFixed(packOcv, 2)} V",
                "Voltage Sag under Load" to "-${SiParser.formatWithSi(vSag, "V")} (${SiParser.formatFixed((vSag / packOcv) * 100.0, 1)}%)",
                "Terminal Voltage Under Load" to "${SiParser.formatFixed(vLoaded, 2)} V",
                "Internal Heat Dissipation" to SiParser.formatWithSi(powerLossWatts, "W"),
                "Heat Loss per Cell" to "${SiParser.formatFixed(lossPerCellWatts, 2)} W / cell"
            ),
            warnings = warnings,
            notes = listOf(
                "Fresh high-drain 18650/21700 cells typically have 12–25 mΩ internal resistance (AC 1kHz)."
            )
        )
    }
}

object BmsCheckCalculator {
    fun calculate(
        seriesS: Int,
        maxContinuousCurrentA: Double,
        peakCurrentA: Double,
        chargeCurrentA: Double,
        bmsSeries: Int,
        bmsContinuousA: Double,
        bmsPeakA: Double,
        bmsChargeA: Double
    ): CalculationOutput {
        if (seriesS <= 0 || maxContinuousCurrentA <= 0) return CalculationOutput.error("Pack parameters must be positive.")

        val warnings = mutableListOf<CalculationWarning>()
        var isCompatible = true

        if (seriesS != bmsSeries) {
            isCompatible = false
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "SERIES MISMATCH: Battery is ${seriesS}S but BMS is designed for ${bmsSeries}S! Never connect mismatched S-count BMS!"
            ))
        }

        if (maxContinuousCurrentA > bmsContinuousA) {
            isCompatible = false
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "BMS OVERCURRENT: Pack load (${maxContinuousCurrentA} A) exceeds BMS continuous rating (${bmsContinuousA} A)! BMS MOSFETs will overheat or trip."
            ))
        }

        if (peakCurrentA > bmsPeakA) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "PEAK CURRENT RISK: Peak load (${peakCurrentA} A) exceeds BMS peak limit (${bmsPeakA} A). Motor starting inrush may trip BMS overcurrent protection."
            ))
        }

        if (chargeCurrentA > bmsChargeA) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "CHARGING CURRENT EXCEEDED: Charger current (${chargeCurrentA} A) exceeds BMS charge port rating (${bmsChargeA} A)!"
            ))
        }

        warnings.add(CalculationWarning(
            WarningLevel.WARNING,
            "CRITICAL SAFETY WARNING: BMS is the primary safety barrier preventing lithium battery fire. Always verify balance leads wiring with a multimeter before plugging into the BMS connector!"
        ))

        val currentMargin = ((bmsContinuousA - maxContinuousCurrentA) / bmsContinuousA) * 100.0

        return CalculationOutput(
            primaryValue = if (isCompatible) "COMPATIBLE (${SiParser.formatFixed(currentMargin, 0)}% safety margin)" else "INCOMPATIBLE / HAZARDOUS",
            primaryLabel = "BMS Suitability",
            formula = "BMS_S == Pack_S  AND  BMS_Continuous_A ≥ Pack_Continuous_A × 1.25",
            substitution = "${bmsSeries}S BMS with ${bmsContinuousA}A rating vs ${seriesS}S pack with ${maxContinuousCurrentA}A load",
            intermediateSteps = listOf(
                "Series Match" to if (seriesS == bmsSeries) "MATCH (${seriesS}S)" else "MISMATCH (${seriesS}S vs ${bmsSeries}S)",
                "Continuous Current Headroom" to "${SiParser.formatFixed(bmsContinuousA - maxContinuousCurrentA, 1)} A",
                "Peak Current Headroom" to "${SiParser.formatFixed(bmsPeakA - peakCurrentA, 1)} A",
                "Charge Current Headroom" to "${SiParser.formatFixed(bmsChargeA - chargeCurrentA, 1)} A"
            ),
            warnings = warnings,
            notes = listOf(
                "Choose a BMS with temperature sensor (NTC) cutoffs to prevent charging below 0°C or discharging above 60°C."
            )
        )
    }
}

object CellBalancingCalculator {
    fun calculate(
        capacityDeltaMah: Double,
        balanceCurrentMa: Double,
        cellVoltage: Double = 4.0
    ): CalculationOutput {
        if (capacityDeltaMah <= 0 || balanceCurrentMa <= 0) {
            return CalculationOutput.error("Imbalance capacity and balancing current must be positive.")
        }

        val hours = capacityDeltaMah / balanceCurrentMa
        val minutes = hours * 60.0
        val dissipationWatts = cellVoltage * (balanceCurrentMa * 1e-3)

        return CalculationOutput(
            primaryValue = "${(minutes / 60).toInt()} h ${(minutes % 60).toInt()} min",
            primaryLabel = "Estimated Cell Balancing Time",
            formula = "t_balance = ΔCapacity / I_balance,  P_dissipated = V_cell × I_balance",
            substitution = "$capacityDeltaMah mAh / $balanceCurrentMa mA = ${SiParser.formatFixed(hours, 1)} hours",
            intermediateSteps = listOf(
                "Capacity Difference" to "$capacityDeltaMah mAh",
                "Balancing Current" to "$balanceCurrentMa mA",
                "Bleed Resistor Heat Dissipation" to SiParser.formatWithSi(dissipationWatts, "W"),
                "Estimated Duration" to "${(minutes / 60).toInt()} hours ${(minutes % 60).toInt()} mins"
            ),
            warnings = listOf(
                CalculationWarning(
                    WarningLevel.INFO,
                    "Passive balancing bleeds excess charge as heat through resistors (typically 30mA - 80mA on standard BMS boards)."
                )
            ),
            notes = listOf(
                "For large cell packs (> 50Ah), consider an active inductor/capacitor equalizer (1A - 5A transfer current)."
            )
        )
    }
}

object NickelStripCalculator {
    enum class StripMaterial(val resistivity: Double, val label: String) {
        PURE_NICKEL(6.84e-8, "Pure Nickel (Ni 99.6%)"),
        NICKEL_PLATED_STEEL(14.0e-8, "Nickel-Plated Steel (SPCC)")
    }

    fun calculate(
        currentA: Double,
        widthMm: Double,
        thicknessMm: Double,
        lengthCm: Double = 5.0,
        material: StripMaterial = StripMaterial.PURE_NICKEL
    ): CalculationOutput {
        if (currentA <= 0 || widthMm <= 0 || thicknessMm <= 0 || lengthCm <= 0) {
            return CalculationOutput.error("All parameters must be positive.")
        }

        val areaM2 = (widthMm * 1e-3) * (thicknessMm * 1e-3)
        val lengthM = lengthCm * 1e-2
        val resistanceOhms = (material.resistivity * lengthM) / areaM2
        val vDrop = currentA * resistanceOhms
        val powerLossWatts = currentA * currentA * resistanceOhms

        // Ampacity heuristic:
        // Pure nickel 0.15mm x 8mm is rated around 8-10A continuous.
        // Area mm² = width * thickness
        val areaMm2 = widthMm * thicknessMm
        val maxRecommendedCurrent = if (material == StripMaterial.PURE_NICKEL) areaMm2 * 8.0 else areaMm2 * 4.0

        val warnings = mutableListOf<CalculationWarning>()
        if (currentA > maxRecommendedCurrent) {
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "OVERCURRENT ON STRIP: Current ($currentA A) exceeds safe ampacity (${SiParser.formatFixed(maxRecommendedCurrent, 1)} A)! Strip will glow, melt cell wraps, or cause fire! Layer multiple strips or use copper sandwich."
            ))
        }
        if (material == StripMaterial.NICKEL_PLATED_STEEL) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "Nickel-plated steel has more than 2× the electrical resistance of pure nickel. High heating under heavy current loads."
            ))
        }
        warnings.add(CalculationWarning(
            WarningLevel.WARNING,
            "PRELIMINARY ESTIMATE: Weld joint quality, contact resistance, and thermal airflow heavily affect real performance. Test under thermal camera!"
        ))

        return CalculationOutput(
            primaryValue = "${SiParser.formatFixed(powerLossWatts * 1000.0, 1)} mW loss | ${SiParser.formatWithSi(vDrop, "V")} drop",
            primaryLabel = "Strip Electrical & Thermal Loss",
            formula = "R = ρ × L / A,  V_drop = I × R,  P = I² × R",
            substitution = "R = (${material.label}) × ${lengthCm}cm / (${widthMm}mm × ${thicknessMm}mm) = ${SiParser.formatFixed(resistanceOhms * 1000.0, 2)} mΩ",
            intermediateSteps = listOf(
                "Cross-Sectional Area" to "${SiParser.formatFixed(areaMm2, 3)} mm²",
                "Strip Electrical Resistance" to "${SiParser.formatFixed(resistanceOhms * 1000.0, 3)} mΩ",
                "Voltage Drop" to "${SiParser.formatFixed(vDrop * 1000.0, 2)} mV",
                "Heat Dissipation" to "${SiParser.formatFixed(powerLossWatts, 3)} W",
                "Safe Ampacity Guideline" to "${SiParser.formatFixed(maxRecommendedCurrent, 1)} A"
            ),
            warnings = warnings,
            notes = listOf(
                "Spot welding requires proper energy pulses. Insufficient welds have high contact resistance."
            )
        )
    }
}

object CellCompatibilityCalculator {
    fun checkCompatibility(
        chemistry1: BatteryChemistry,
        chemistry2: BatteryChemistry,
        capacityDeltaPct: Double,
        irDeltaPct: Double,
        voltageDeltaMv: Double
    ): CalculationOutput {
        val warnings = mutableListOf<CalculationWarning>()
        var isSafe = true

        if (chemistry1 != chemistry2) {
            isSafe = false
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "NEVER MIX CHEMISTRIES! Mixing ${chemistry1.label} with ${chemistry2.label} leads to severe overcharge, overdischarge, and violent thermal runaway!"
            ))
        }

        if (voltageDeltaMv > 50.0) {
            isSafe = false
            warnings.add(CalculationWarning(
                WarningLevel.DANGER,
                "HIGH VOLTAGE DIFFERENCE (${SiParser.formatFixed(voltageDeltaMv, 0)} mV)! Paralleling cells with > 50mV delta causes massive uncontrolled inrush currents that can melt strips or rupture cells. Balance them individually before connecting!"
            ))
        }

        if (capacityDeltaPct > 5.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "Capacity mismatch is ${SiParser.formatFixed(capacityDeltaPct, 1)}% (> 5%). In series, weaker cells will reverse-charge or hit cutoff prematurely."
            ))
        }

        if (irDeltaPct > 15.0) {
            warnings.add(CalculationWarning(
                WarningLevel.WARNING,
                "Internal resistance mismatch is ${SiParser.formatFixed(irDeltaPct, 1)}% (> 15%). Unequal current sharing will overload lower-resistance cells in parallel groups."
            ))
        }

        return CalculationOutput(
            primaryValue = if (isSafe) "SAFE TO ASSEMBLE (Matched Cells)" else "UNSAFE / REJECT COMBINATION",
            primaryLabel = "Cell Compatibility Verdict",
            formula = "ΔChem == 0,  ΔV < 50mV,  ΔCap < 5%,  ΔIR < 15%",
            substitution = "ΔV = ${voltageDeltaMv}mV, ΔCap = $capacityDeltaPct%, ΔIR = $irDeltaPct%",
            intermediateSteps = listOf(
                "Chemistry Compatibility" to if (chemistry1 == chemistry2) "IDENTICAL" else "CRITICAL MISMATCH",
                "Voltage Delta (ΔV)" to "${SiParser.formatFixed(voltageDeltaMv, 1)} mV (Max: 50mV)",
                "Capacity Delta (ΔCap)" to "${SiParser.formatFixed(capacityDeltaPct, 1)} % (Max: 5%)",
                "Internal Resistance Delta (ΔIR)" to "${SiParser.formatFixed(irDeltaPct, 1)} % (Max: 15%)"
            ),
            warnings = warnings,
            notes = listOf(
                "Grade-A matched cell batches from the same production lot ensure maximum pack life and safety."
            )
        )
    }
}
