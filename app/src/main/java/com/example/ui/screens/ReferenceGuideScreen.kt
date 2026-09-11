package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceGuideScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("E-Series", "Color Codes", "Batteries", "Protocols")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Engineering Quick Reference", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ref_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ESeriesSection()
                    1 -> ResistorColorCodeSection()
                    2 -> BatteryReferenceSection()
                    3 -> ProtocolsSection()
                }
            }
        }
    }
}

@Composable
private fun ESeriesSection() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("E24 Standard Resistor Values (±5%, ±1%)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1.0, 1.1, 1.2, 1.3, 1.5, 1.6, 1.8, 2.0, 2.2, 2.4, 2.7, 3.0, 3.3, 3.6, 3.9, 4.3, 4.7, 5.1, 5.6, 6.2, 6.8, 7.5, 8.2, 9.1",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 22.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text("E12 Standard Resistor Values (±10%)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 22.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Multiplied by 10^n to form values like 470 Ω, 4.7 kΩ, 47 kΩ, 470 kΩ, 4.7 MΩ.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun ResistorColorCodeSection() {
    val colors = listOf(
        Triple("Black", "0", "×1"),
        Triple("Brown", "1", "×10 (±1%)"),
        Triple("Red", "2", "×100 (±2%)"),
        Triple("Orange", "3", "×1k"),
        Triple("Yellow", "4", "×10k"),
        Triple("Green", "5", "×100k (±0.5%)"),
        Triple("Blue", "6", "×1M (±0.25%)"),
        Triple("Violet", "7", "×10M (±0.1%)"),
        Triple("Gray", "8", "×100M"),
        Triple("White", "9", "×1G"),
        Triple("Gold", "—", "×0.1 (±5%)"),
        Triple("Silver", "—", "×0.01 (±10%)")
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resistor Band Color Guide", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            colors.forEach { (name, digit, mult) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Digit: $digit", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    Text(mult, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun BatteryReferenceSection() {
    val chemistries = listOf(
        Triple("Li-ion (NMC/INR)", "3.6 - 3.7 V (Max 4.2V, Min 3.0V)", "500-1000 cycles, highest energy density, vulnerable to overcharge."),
        Triple("LiFePO4 (LFP)", "3.2 V (Max 3.65V, Min 2.5V)", "2000-5000+ cycles, non-combustible, very flat discharge curve."),
        Triple("LiPo (Polymer)", "3.7 V (Max 4.2V, Min 3.0V)", "High C-rate (30-100C), flexible pouch form factor, puncture sensitive."),
        Triple("LTO (Titanate)", "2.3 V (Max 2.8V, Min 1.5V)", "15000-20000 cycles, super-fast charging, operates down to -30°C.")
    )

    chemistries.forEach { (name, voltage, notes) ->
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Voltage: $voltage", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(notes, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
private fun ProtocolsSection() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Embedded Hardware Interfaces", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(10.dp))

            Text("I²C Bus:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("• Lines: SDA (Data), SCL (Clock), Open-Drain with pull-up resistors to VDD.", style = MaterialTheme.typography.bodySmall)
            Text("• Speeds: Standard (100 kHz), Fast (400 kHz), Fast-Plus (1 MHz).", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text("SPI Bus:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("• Lines: MOSI, MISO, SCK, CS/SS. Push-pull signals, high speed (10-50+ MHz).", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text("UART Serial:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("• Lines: TX, RX (cross-connected: TX→RX, RX←TX). 8N1 default framing.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text("CAN Bus (ISO 11898):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("• Lines: CAN_H, CAN_L differential twisted pair. Exactly two 120 Ω terminators at bus ends (60 Ω differential).", style = MaterialTheme.typography.bodySmall)
        }
    }
}
