package com.example.calculations.model

enum class WarningLevel {
    INFO,
    WARNING,
    DANGER
}

data class CalculationWarning(
    val level: WarningLevel,
    val message: String
)

data class CalculationOutput(
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val primaryValue: String = "",
    val primaryLabel: String = "",
    val formula: String = "",
    val substitution: String = "",
    val intermediateSteps: List<Pair<String, String>> = emptyList(),
    val warnings: List<CalculationWarning> = emptyList(),
    val notes: List<String> = emptyList()
) {
    companion object {
        fun error(message: String): CalculationOutput =
            CalculationOutput(isSuccess = false, errorMessage = message)
    }
}

enum class CalculatorCategory(val id: String, val displayName: String, val displayNameUk: String) {
    BASIC_ELECTRONICS("basic_electronics", "Basic Electronics", "Базова електроніка"),
    POWER_EMBEDDED("power_embedded", "Power & Embedded", "Живлення та Embedded"),
    BATTERIES("batteries", "Batteries & Packs", "Акумулятори та батареї")
}

data class CalculatorMetadata(
    val id: String,
    val category: CalculatorCategory,
    val titleResKey: String,
    val descriptionResKey: String,
    val iconName: String
)
