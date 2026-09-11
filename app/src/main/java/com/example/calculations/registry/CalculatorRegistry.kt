package com.example.calculations.registry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.calculations.model.CalculatorCategory

data class CalculatorItem(
    val id: String,
    val category: CalculatorCategory,
    val title: String,
    val titleUk: String,
    val description: String,
    val descriptionUk: String,
    val icon: ImageVector,
    val formulaBadge: String,
    val tags: List<String>
)

object CalculatorRegistry {
    val items: List<CalculatorItem> = listOf(
        // --- 1. BASIC ELECTRONICS (13) ---
        CalculatorItem(
            id = "ohms_law",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Ohm's Law",
            titleUk = "Закон Ома",
            description = "Calculate Voltage, Current, Resistance, and Power from any 2 known values.",
            descriptionUk = "Розрахунок V, I, R, P за будь-якими двома відомими величинами.",
            icon = Icons.Default.Bolt,
            formulaBadge = "V = I × R",
            tags = listOf("ohm", "voltage", "current", "power", "resistance", "напруга", "струм", "опір", "потужність")
        ),
        CalculatorItem(
            id = "power_dissipation",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Power & Dissipation",
            titleUk = "Потужність і розсіювання",
            description = "P = VI = I²R = V²/R with component power rating & safety margin advice.",
            descriptionUk = "P = VI = I²R = V²/R та рекомендований запас номіналу резистора.",
            icon = Icons.Default.Whatshot,
            formulaBadge = "P = I²R",
            tags = listOf("power", "heat", "resistor", "margin", "потужність", "ват", "розсіювання")
        ),
        CalculatorItem(
            id = "series_parallel_resistors",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Series & Parallel Resistors",
            titleUk = "Послідовні й паралельні резистори",
            description = "Equivalent resistance, per-resistor voltage drop & power for arbitrary counts.",
            descriptionUk = "Еквівалентний опір, напруга й потужність на кожному резисторі.",
            icon = Icons.Default.LinearScale,
            formulaBadge = "R_eq",
            tags = listOf("series", "parallel", "resistor", "послідовне", "паралельне", "опір")
        ),
        CalculatorItem(
            id = "series_parallel_capacitors",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Series & Parallel Capacitors",
            titleUk = "Послідовні й паралельні конденсатори",
            description = "Equivalent capacitance, assembly voltage rating & series balancing warnings.",
            descriptionUk = "Еквівалентна ємність, напруга збірки та застереження про балансування.",
            icon = Icons.Default.CallSplit,
            formulaBadge = "C_eq",
            tags = listOf("capacitor", "capacitance", "конденсатор", "ємність")
        ),
        CalculatorItem(
            id = "voltage_divider",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Voltage Divider",
            titleUk = "Подільник напруги",
            description = "Loaded & unloaded Vout, divider current, resistor power & sag analysis.",
            descriptionUk = "Vout, R1, R2, ненавантажений і навантажений режими, струм і втрати.",
            icon = Icons.Default.Tune,
            formulaBadge = "Vout = Vin × R2/(R1+R2)",
            tags = listOf("divider", "voltage", "подільник", "дільник", "напруга")
        ),
        CalculatorItem(
            id = "led_resistor",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "LED Series Resistor",
            titleUk = "LED-калькулятор",
            description = "Current limiting resistor for single or multiple series LEDs with power rating.",
            descriptionUk = "Струмообмежувальний резистор, кількість LED, потужність і запас напруги.",
            icon = Icons.Default.Lightbulb,
            formulaBadge = "R = (Vs - N×Vf)/I",
            tags = listOf("led", "diode", "resistor", "світлодіод", "резистор")
        ),
        CalculatorItem(
            id = "rc_filter",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "RC Circuit & Filter",
            titleUk = "RC-ланцюги та фільтри",
            description = "Time constant τ, cutoff frequency fc = 1/(2πRC), rise time & charge/discharge.",
            descriptionUk = "Стала часу τ, частота зрізу fc, час наростання та заряд/розряд.",
            icon = Icons.Default.ShowChart,
            formulaBadge = "fc = 1 / (2πRC)",
            tags = listOf("rc", "filter", "cutoff", "фільтр", "частота", "зріз")
        ),
        CalculatorItem(
            id = "rl_rlc_resonance",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "RL & RLC Resonance",
            titleUk = "RL та RLC резонанс",
            description = "Resonance f0 = 1/(2π√LC), Q factor, bandwidth, and inductive/capacitive reactance.",
            descriptionUk = "Резонансна частота f0, добротність Q, смуга пропускання та реактивні опори.",
            icon = Icons.Default.GraphicEq,
            formulaBadge = "f0 = 1 / (2π√LC)",
            tags = listOf("rlc", "resonance", "inductor", "q-factor", "індуктивність", "резонанс")
        ),
        CalculatorItem(
            id = "op_amp",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Operational Amplifier",
            titleUk = "Операційні підсилювачі",
            description = "Inverting & non-inverting gain Av, input impedance, and output voltage.",
            descriptionUk = "Інвертувальний та неінвертувальний підсилювачі, Av та резистори.",
            icon = Icons.Default.ChangeHistory,
            formulaBadge = "Av = 1 + Rf/R1",
            tags = listOf("opamp", "amplifier", "підсилювач", "оу")
        ),
        CalculatorItem(
            id = "db_calculator",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Decibel (dB / dBm / dBV)",
            titleUk = "dB-калькулятор",
            description = "Convert dB ↔ Voltage ratio (20log), dB ↔ Power ratio (10log), dBm ↔ mW, dBV ↔ V.",
            descriptionUk = "Перетворення dB ↔ напруга, dB ↔ потужність, dBm ↔ mW, dBV ↔ V.",
            icon = Icons.Default.VolumeUp,
            formulaBadge = "dB = 20 log(V2/V1)",
            tags = listOf("db", "dbm", "dbv", "decibel", "децибел", "потужність")
        ),
        CalculatorItem(
            id = "timer_555",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "555 Timer",
            titleUk = "555 Таймер",
            description = "Astable mode (frequency, duty cycle, t_high, t_low) & Monostable pulse width.",
            descriptionUk = "Astable режим (частота, duty cycle, імпульси) та Monostable тривалість.",
            icon = Icons.Default.Timer,
            formulaBadge = "f = 1.44/((R1+2R2)C)",
            tags = listOf("555", "ne555", "timer", "astable", "monostable", "таймер", "частота")
        ),
        CalculatorItem(
            id = "resistor_codes",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Resistor SMD & E-Series",
            titleUk = "Резисторні номінали та SMD",
            description = "Find nearest E12/E24 standard values, decode 3-digit, 4-digit & R-notation SMD codes.",
            descriptionUk = "Найближчі значення E12, E24, декодування SMD-маркування (3-digit, 4-digit, R).",
            icon = Icons.Default.QrCode,
            formulaBadge = "E12 / E24 / SMD",
            tags = listOf("smd", "resistor", "e24", "e12", "color", "маркування", "резистор")
        ),
        CalculatorItem(
            id = "tolerance_analysis",
            category = CalculatorCategory.BASIC_ELECTRONICS,
            title = "Tolerance Worst-Case Analysis",
            titleUk = "Аналіз допусків (Worst-Case)",
            description = "Worst-case minimum, typical, and maximum output for voltage divider & RC networks.",
            descriptionUk = "Розрахунок min/typ/max за допусками для подільника напруги та RC.",
            icon = Icons.Default.CompareArrows,
            formulaBadge = "Min / Typ / Max",
            tags = listOf("tolerance", "worst-case", "допуск", "похибка", "точність")
        ),

        // --- 2. POWER & EMBEDDED (15) ---
        CalculatorItem(
            id = "power_budget",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Power Budget & Profiler",
            titleUk = "Бюджет енергоспоживання",
            description = "Weighted average current across Active, Sleep, Transmit modes & battery life forecast.",
            descriptionUk = "Середній струм у режимах active/sleep/tx, добове mAh та прогноз автономності.",
            icon = Icons.Default.PieChart,
            formulaBadge = "I_avg = Σ (I_i × Duty_i)",
            tags = listOf("power", "budget", "sleep", "iot", "струм", "енергоспоживання", "автономність")
        ),
        CalculatorItem(
            id = "battery_runtime",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Battery Runtime Estimator",
            titleUk = "Розрахунок автономного часу",
            description = "Calculate battery runtime in hours, days, and months with converter efficiency and derating.",
            descriptionUk = "Час автономної роботи в годинах, днях і місяцях з урахуванням ККД і деградації.",
            icon = Icons.Default.AccessTime,
            formulaBadge = "t = (Cap × Derating) / I_avg",
            tags = listOf("runtime", "battery", "hours", "days", "час", "робота", "автономність")
        ),
        CalculatorItem(
            id = "dcdc_converter",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "DC/DC Buck/Boost Converter",
            titleUk = "DC/DC перетворювач",
            description = "Input current, output power, losses, and heat dissipation for switching converters.",
            descriptionUk = "Вхідний струм, вихідна потужність, втрати та теплове виділення імпульсника.",
            icon = Icons.Default.SyncAlt,
            formulaBadge = "Pin = Pout / η",
            tags = listOf("dcdc", "buck", "boost", "efficiency", "перетворювач", "ккд")
        ),
        CalculatorItem(
            id = "ldo_regulator",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "LDO Regulator & Thermal",
            titleUk = "LDO регулятор і тепловиділення",
            description = "Power dissipation Pd = (Vin - Vout)×Iout, thermal rise via θJA & overheating warnings.",
            descriptionUk = "Втрати потужності, оцінка нагріву за θJA та попередження про перегрів.",
            icon = Icons.Default.Thermostat,
            formulaBadge = "Pd = (Vin - Vout) × Iout",
            tags = listOf("ldo", "thermal", "heating", "sot223", "нагрів", "регулятор", "перегрів")
        ),
        CalculatorItem(
            id = "wire_voltage_drop",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Wire Voltage Drop & Gauge",
            titleUk = "Падіння напруги в проводах",
            description = "Voltage drop, drop %, wire resistance & power loss for Copper/Aluminum (AWG & mm²).",
            descriptionUk = "Падіння напруги, опір та втрати для міді й алюмінію (AWG та мм²).",
            icon = Icons.Default.Cable,
            formulaBadge = "V_drop = I × (ρL/A)",
            tags = listOf("wire", "cable", "awg", "drop", "провід", "кабель", "переріз", "падіння")
        ),
        CalculatorItem(
            id = "fuse_sizing",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Fuse Sizing & Inrush",
            titleUk = "Підбір запобіжника",
            description = "Recommended rating with safety factor, fast-acting vs slow-blow, time-current guidance.",
            descriptionUk = "Робочий і пусковий струм, рекомендований номінал із запасом та тип.",
            icon = Icons.Default.Security,
            formulaBadge = "I_fuse ≥ 1.25 × I_op",
            tags = listOf("fuse", "protection", "inrush", "запобіжник", "захист", "струм")
        ),
        CalculatorItem(
            id = "pcb_trace_width",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "PCB Trace Width (IPC-2221)",
            titleUk = "Ширина доріжки PCB",
            description = "Calculate minimum required PCB trace width (mm/mils) for external or internal layers.",
            descriptionUk = "Розрахунок мінімальної ширини доріжки за струмом і допустимим перегрівом.",
            icon = Icons.Default.Memory,
            formulaBadge = "IPC-2221 Standard",
            tags = listOf("pcb", "trace", "ipc2221", "width", "доріжка", "плата", "ширина")
        ),
        CalculatorItem(
            id = "decoupling_cap",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Decoupling & Bulk Capacitor",
            titleUk = "Блокувальний конденсатор",
            description = "Minimum capacitance to suppress transient current steps without exceeding allowable droop.",
            descriptionUk = "Стрибок струму, тривалість, допустима просадка та мінімальна ємність.",
            icon = Icons.Default.Layers,
            formulaBadge = "C = ΔI × Δt / ΔV",
            tags = listOf("decoupling", "bypass", "bulk", "конденсатор", "блокувальний", "імпульс")
        ),
        CalculatorItem(
            id = "mcu_timer",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "MCU Hardware Timer",
            titleUk = "Таймери MCU",
            description = "Prescaler (PSC) and Auto-Reload (ARR) overflow frequency and interrupt period.",
            descriptionUk = "Частота тактування, Prescaler, ARR та результуюча частота переривання.",
            icon = Icons.Default.AvTimer,
            formulaBadge = "f = f_clk / ((PSC+1)(ARR+1))",
            tags = listOf("mcu", "timer", "stm32", "avr", "arr", "psc", "таймер", "мікроконтролер")
        ),
        CalculatorItem(
            id = "uart_baud",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "UART Baud Rate & Error",
            titleUk = "UART baud-rate та похибка",
            description = "Divisor, actual baud rate, and percentage clock deviation error for UART peripherals.",
            descriptionUk = "Дільник, фактичний baud rate та абсолютна й відносна похибка.",
            icon = Icons.Default.SettingsEthernet,
            formulaBadge = "DIV = f_clk / (16 × Baud)",
            tags = listOf("uart", "baud", "serial", "usart", "похибка", "швидкість")
        ),
        CalculatorItem(
            id = "i2c_pullup",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "I²C Bus Pull-Up Resistor",
            titleUk = "Підтягувальні резистори I²C",
            description = "Calculate permissible R_min and R_max based on Vdd, bus capacitance (Cb), and speed.",
            descriptionUk = "Мінімальний і максимальний опір підтяжки за ємністю шини й швидкістю.",
            icon = Icons.Default.SwapHoriz,
            formulaBadge = "R_max = tr / (0.8473 × Cb)",
            tags = listOf("i2c", "pullup", "bus", "sda", "scl", "підтяжка", "шина")
        ),
        CalculatorItem(
            id = "can_termination",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "CAN Bus Termination",
            titleUk = "CAN-термінація шини",
            description = "Equivalent resistance verification for standard ISO 11898 dual 120 Ω bus termination.",
            descriptionUk = "Кількість термінаторів, еквівалентний опір та перевірка 60 Ω.",
            icon = Icons.Default.AltRoute,
            formulaBadge = "R_eq = 120 / N",
            tags = listOf("can", "termination", "iso11898", "термінація", "шина")
        ),
        CalculatorItem(
            id = "adc_calculator",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "ADC Code & Voltage Scaling",
            titleUk = "АЦП (ADC) та подільник",
            description = "LSB resolution step, ADC Code ↔ Voltage conversion, and voltage divider scaling.",
            descriptionUk = "LSB напруга, ADC code ↔ напруга та розрахунок вимірювання через дільник.",
            icon = Icons.Default.Speed,
            formulaBadge = "LSB = Vref / 2^bits",
            tags = listOf("adc", "analog", "lsb", "bits", "ацп", "напруга", "роздільність")
        ),
        CalculatorItem(
            id = "pwm_calculator",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "PWM Frequency & Duty Cycle",
            titleUk = "ШІМ (PWM) таймера",
            description = "PWM carrier frequency, compare register CCR value, and effective resolution.",
            descriptionUk = "Частота ШІМ, значення регістру порівняння CCR та роздільність.",
            icon = Icons.Default.Waves,
            formulaBadge = "CCR = (Duty/100) × (ARR+1)",
            tags = listOf("pwm", "ccr", "duty", "motor", "шим", "скважність", "частота")
        ),
        CalculatorItem(
            id = "unit_converter",
            category = CalculatorCategory.POWER_EMBEDDED,
            title = "Engineering Unit Converter",
            titleUk = "Конвертер інженерних одиниць",
            description = "Ah ↔ mAh ↔ Coulombs, Wh ↔ Joules, dBm ↔ mW, AWG ↔ mm², °C ↔ °F, Hz ↔ period.",
            descriptionUk = "Ah ↔ mAh ↔ Кулон, Wh ↔ Джоулі, dBm ↔ mW, AWG ↔ мм², °C ↔ °F, Hz ↔ період.",
            icon = Icons.Default.Transform,
            formulaBadge = "SI Conversion",
            tags = listOf("convert", "unit", "celsius", "joules", "конвертер", "одиниці")
        ),

        // --- 3. BATTERIES & BATTERY PACKS (9) ---
        CalculatorItem(
            id = "battery_pack_config",
            category = CalculatorCategory.BATTERIES,
            title = "Battery Pack S×P Configurator",
            titleUk = "Конфігуратор збірки S×P",
            description = "Calculate cell count, nominal/max/min voltages, capacity Ah, energy Wh & max current.",
            descriptionUk = "Кількість комірок, напруга збірки, ємність Ah, енергія Wh та максимальний струм.",
            icon = Icons.Default.BatteryChargingFull,
            formulaBadge = "S × V_cell,  P × Ah_cell",
            tags = listOf("pack", "series", "parallel", "18650", "21700", "збірка", "батарея", "акумулятор")
        ),
        CalculatorItem(
            id = "target_pack_finder",
            category = CalculatorCategory.BATTERIES,
            title = "Target S×P Pack Finder",
            titleUk = "Підбір S×P під вимоги",
            description = "Enter desired voltage, capacity, and current to get optimal S and P configurations.",
            descriptionUk = "Підбір конфігурації S та P під цільову напругу, ємність та струм навантаження.",
            icon = Icons.Default.FindInPage,
            formulaBadge = "S = V_tgt / V_cell",
            tags = listOf("target", "finder", "config", "підбір", "вимоги")
        ),
        CalculatorItem(
            id = "c_rate",
            category = CalculatorCategory.BATTERIES,
            title = "Battery C-Rate Calculator",
            titleUk = "C-Rate струмів заряду/розряду",
            description = "Convert between Current (A) and C-Rate, compare against rated limits & discharge time.",
            descriptionUk = "Перетворення між струмом та C-rate, порівняння з допустимим рейтингом.",
            icon = Icons.Default.FlashOn,
            formulaBadge = "C-Rate = I / Capacity",
            tags = listOf("c-rate", "discharge", "rate", "струм", "розряд")
        ),
        CalculatorItem(
            id = "charging_time",
            category = CalculatorCategory.BATTERIES,
            title = "Battery Charging Time (CC/CV)",
            titleUk = "Час заряджання батареї",
            description = "Estimate total charging duration with initial SoC and CC/CV saturation tail.",
            descriptionUk = "Оцінка часу заряджання за ємністю, струмом, початковим SoC та ККД.",
            icon = Icons.Default.HourglassTop,
            formulaBadge = "t ≈ (Cap × ΔSoC) / I_chg",
            tags = listOf("charge", "time", "soc", "заряд", "час", "заряджання")
        ),
        CalculatorItem(
            id = "voltage_sag",
            category = CalculatorCategory.BATTERIES,
            title = "Battery Voltage Sag Under Load",
            titleUk = "Просадка напруги під навантаженням",
            description = "Calculate pack internal resistance, voltage drop under load, and internal cell heat loss.",
            descriptionUk = "Внутрішній опір збірки, падіння напруги та теплові втрати всередині комірок.",
            icon = Icons.Default.TrendingDown,
            formulaBadge = "ΔV = I_load × R_pack",
            tags = listOf("sag", "drop", "internal resistance", "просадка", "навантаження", "опір")
        ),
        CalculatorItem(
            id = "bms_check",
            category = CalculatorCategory.BATTERIES,
            title = "BMS Compatibility Checker",
            titleUk = "BMS-перевірка сумісності",
            description = "Verify S-count, continuous, peak, and charging current compatibility against BMS ratings.",
            descriptionUk = "Кількість S, робочий, піковий та зарядний струм, перевірка номіналів BMS.",
            icon = Icons.Default.Shield,
            formulaBadge = "BMS Safety Audit",
            tags = listOf("bms", "protection", "mosfet", "бмс", "захист", "плата")
        ),
        CalculatorItem(
            id = "cell_balancing",
            category = CalculatorCategory.BATTERIES,
            title = "Cell Balancing Time",
            titleUk = "Час балансування комірок",
            description = "Calculate passive bleed resistor or active equalizer balancing duration and dissipation.",
            descriptionUk = "Різниця заряду, струм балансира, оцінка тривалості та тепловиділення.",
            icon = Icons.Default.Balance,
            formulaBadge = "t = ΔAh / I_bal",
            tags = listOf("balance", "equalizer", "bleed", "балансування", "вирівнювання")
        ),
        CalculatorItem(
            id = "nickel_strip",
            category = CalculatorCategory.BATTERIES,
            title = "Nickel Strip Current & Sizing",
            titleUk = "Нікелева стрічка для збірки",
            description = "Ampacity, resistance, voltage drop, and heat loss for Pure Nickel vs Nickel-Plated Steel.",
            descriptionUk = "Струм, опір, падіння напруги й нагрів для чистого нікелю та сталі.",
            icon = Icons.Default.LinearScale,
            formulaBadge = "R = ρ × L / (W × T)",
            tags = listOf("nickel", "strip", "spot welding", "нікель", "стрічка", "зварка")
        ),
        CalculatorItem(
            id = "cell_compatibility",
            category = CalculatorCategory.BATTERIES,
            title = "Cell Compatibility Validator",
            titleUk = "Перевірка сумісності комірок",
            description = "Form to verify chemistry, capacity, internal resistance, and resting voltage matching.",
            descriptionUk = "Перевірка хімії, ємності, опору й напруги перед збиранням збірки.",
            icon = Icons.Default.VerifiedUser,
            formulaBadge = "Cell Matching Audit",
            tags = listOf("matching", "compatibility", "grouping", "сумісність", "сортування", "підбір")
        )
    )

    fun getById(id: String): CalculatorItem? = items.find { it.id == id }

    fun filter(query: String, category: CalculatorCategory?, onlyFavorites: Boolean, favIds: Set<String>): List<CalculatorItem> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesFav = !onlyFavorites || favIds.contains(item.id)
            val matchesQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.titleUk.lowercase().contains(q) ||
                    item.description.lowercase().contains(q) ||
                    item.descriptionUk.lowercase().contains(q) ||
                    item.formulaBadge.lowercase().contains(q) ||
                    item.tags.any { it.contains(q) }

            matchesCategory && matchesFav && matchesQuery
        }
    }
}
