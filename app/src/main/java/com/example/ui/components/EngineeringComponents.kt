package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculations.model.CalculationOutput
import com.example.calculations.model.CalculationWarning
import com.example.calculations.model.WarningLevel
import com.example.core.units.SiParser

@Composable
fun EngineeringInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    unit: String = "",
    placeholder: String = "e.g. 4.7k, 100n",
    prefixShortcuts: List<String> = listOf("p", "n", "µ", "m", "k", "M"),
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String = "eng_input_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) },
            trailingIcon = {
                if (unit.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp)
        )

        // Live preview of parsed SI value
        val parsed = remember(value) { SiParser.parse(value) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (errorMessage != null && isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            } else if (parsed != null && value.isNotBlank()) {
                Text(
                    text = "Parsed: ${SiParser.formatWithSi(parsed, unit)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Quick SI prefix insert buttons
            if (prefixShortcuts.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    prefixShortcuts.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val clean = value.trimEnd { it.isLetter() || it == 'Ω' || it == '%' }
                                    onValueChange(clean + p)
                                }
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDisplayCard(
    output: CalculationOutput,
    modifier: Modifier = Modifier,
    onSaveToHistory: (() -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_display_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Primary Result & Copy Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = output.primaryLabel.ifEmpty { "Result" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = output.primaryValue,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val textToCopy = buildString {
                                append("${output.primaryLabel}: ${output.primaryValue}\n")
                                if (output.formula.isNotEmpty()) append("Formula: ${output.formula}\n")
                                if (output.substitution.isNotEmpty()) append("Substitution: ${output.substitution}\n")
                                output.intermediateSteps.forEach { (k, v) -> append("$k: $v\n") }
                            }
                            clipboard.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (onSaveToHistory != null) {
                        IconButton(
                            onClick = onSaveToHistory,
                            modifier = Modifier.testTag("save_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save to History",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Formula Box
            if (output.formula.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "FORMULA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = output.formula,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (output.substitution.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = output.substitution,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Intermediate Steps / Breakdown
            if (output.intermediateSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CALCULATION BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                output.intermediateSteps.forEach { (stepName, stepVal) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stepName,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = stepVal,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Safety Warnings
            if (output.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                output.warnings.forEach { warning ->
                    WarningBanner(warning = warning)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Engineering Notes
            if (output.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                output.notes.forEach { note ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarningBanner(warning: CalculationWarning, modifier: Modifier = Modifier) {
    val (bgColor, contentColor, icon) = when (warning.level) {
        WarningLevel.DANGER -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )
        WarningLevel.WARNING -> Triple(
            Color(0xFFFFF3CD),
            Color(0xFF664D03),
            Icons.Default.PriorityHigh
        )
        WarningLevel.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Info
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = warning.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
