package com.example.core.units

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Standard SI prefixes for electronics and engineering.
 */
enum class SiPrefix(val symbol: String, val exponent: Int, val multiplier: Double) {
    TERA("T", 12, 1e12),
    GIGA("G", 9, 1e9),
    MEGA("M", 6, 1e6),
    KILO("k", 3, 1e3),
    NONE("", 0, 1.0),
    MILLI("m", -3, 1e-3),
    MICRO("µ", -6, 1e-6),
    NANO("n", -9, 1e-9),
    PICO("p", -12, 1e-12),
    FEMTO("f", -15, 1e-15);

    companion object {
        fun fromSymbol(sym: String): SiPrefix {
            val normalized = sym.trim().lowercase()
            return when (normalized) {
                "t" -> TERA
                "g" -> GIGA
                "m", "meg" -> MEGA
                "k" -> KILO
                "milli" -> MILLI
                "u", "µ", "micro" -> MICRO
                "n" -> NANO
                "p" -> PICO
                "f" -> FEMTO
                else -> NONE
            }
        }
    }
}

/**
 * SI Unit parsing and formatting engine.
 * Parses strings like "4.7k", "4k7", "100nF", "3.3V", "2200mAh", "10uH", "0.05R", "2R2".
 */
object SiParser {
    private val regex = Regex("""^([+-]?\d*(?:\.\d+)?(?:[eE][+-]?\d+)?)\s*([TGMkmµunpf]?)(?:[ΩRrvVaAhHwWfFzZsS%]|Hz|mAh|Wh|Ohm|ohm)?$""")
    private val inlinePrefixRegex = Regex("""^([+-]?\d+)([RkMmunpf])(\d+)$""")

    /**
     * Parses a string representation into a Double in standard base units.
     * Returns null if invalid.
     */
    fun parse(input: String): Double? {
        val trimmed = input.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null

        // Try direct double parse first
        trimmed.toDoubleOrNull()?.let { return it }

        // Check for inline notation: 4k7 -> 4.7k, 2R2 -> 2.2, 1n5 -> 1.5n
        val inlineMatch = inlinePrefixRegex.find(trimmed)
        val normalizedInput = if (inlineMatch != null) {
            val intPart = inlineMatch.groupValues[1]
            val prefix = inlineMatch.groupValues[2]
            val fracPart = inlineMatch.groupValues[3]
            if (prefix.equals("r", ignoreCase = true)) {
                "$intPart.$fracPart"
            } else {
                "$intPart.${fracPart}$prefix"
            }
        } else {
            trimmed
        }

        normalizedInput.toDoubleOrNull()?.let { return it }

        val match = regex.find(normalizedInput) ?: return null
        val numStr = match.groupValues[1]
        val prefixStr = match.groupValues[2]

        val baseNum = if (numStr.isEmpty() || numStr == "+" || numStr == "-") {
            if (numStr == "-") -1.0 else 1.0
        } else {
            numStr.toDoubleOrNull() ?: return null
        }

        val multiplier = when (prefixStr) {
            "T" -> 1e12
            "G" -> 1e9
            "M" -> 1e6
            "k" -> 1e3
            "m" -> 1e-3
            "µ", "u" -> 1e-6
            "n" -> 1e-9
            "p" -> 1e-12
            "f" -> 1e-15
            else -> 1.0
        }

        return baseNum * multiplier
    }

    /**
     * Formats a double with best-matching SI prefix and given unit string.
     * e.g., formatWithSi(4700.0, "Ω") -> "4.70 kΩ"
     */
    fun formatWithSi(value: Double, unit: String = "", decimals: Int = 3): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        if (value == 0.0) return "0 $unit".trim()

        val absVal = kotlin.math.abs(value)
        val prefix = when {
            absVal >= 1e12 -> SiPrefix.TERA
            absVal >= 1e9 -> SiPrefix.GIGA
            absVal >= 1e6 -> SiPrefix.MEGA
            absVal >= 1e3 -> SiPrefix.KILO
            absVal >= 1.0 -> SiPrefix.NONE
            absVal >= 1e-3 -> SiPrefix.MILLI
            absVal >= 1e-6 -> SiPrefix.MICRO
            absVal >= 1e-9 -> SiPrefix.NANO
            absVal >= 1e-12 -> SiPrefix.PICO
            absVal >= 1e-15 -> SiPrefix.FEMTO
            else -> SiPrefix.NONE
        }

        val scaled = value / prefix.multiplier
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0")
            if (decimals > 0) {
                append(".")
                repeat(decimals) { append("#") }
            }
        }
        val df = DecimalFormat(pattern, symbols)
        val formatted = df.format(scaled)
        val prefixSymbol = prefix.symbol
        return if (prefixSymbol.isEmpty() && unit.isEmpty()) {
            formatted
        } else if (prefixSymbol.isEmpty()) {
            "$formatted $unit"
        } else {
            "$formatted $prefixSymbol$unit"
        }.trim()
    }

    /**
     * Formats number to a fixed decimal precision without engineering prefix.
     */
    fun formatFixed(value: Double, decimals: Int = 2): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (decimals > 0) "#,##0." + "0".repeat(decimals) else "#,##0"
        val df = DecimalFormat(pattern, symbols)
        return df.format(value)
    }
}
