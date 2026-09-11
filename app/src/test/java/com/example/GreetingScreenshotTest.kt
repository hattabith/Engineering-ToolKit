package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.calculations.model.CalculatorCategory
import com.example.calculations.registry.CalculatorRegistry
import com.example.ui.screens.CalculatorCard
import com.example.ui.theme.EngineeringToolkitTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun calculator_card_screenshot() {
    val sampleItem = CalculatorRegistry.items.first()
    composeTestRule.setContent {
      EngineeringToolkitTheme {
        CalculatorCard(
          item = sampleItem,
          isFavorite = true,
          onToggleFavorite = {},
          onClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/calculator_card.png")
  }
}
