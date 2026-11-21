package dz.btpprodz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class MainActivityUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenShowsConstructionSections() {
        composeRule.onNodeWithText("Btp Pro Dz").assertIsDisplayed()
        composeRule.onNodeWithText("ماذا تريد أن تحسب؟").assertExists()
        composeRule.onNodeWithText("الخرسانة").assertExists()
        composeRule.onNodeWithText("الأساسات").assertExists()
    }

    @Test
    fun concreteCalculatorOpensAndShowsCalculatedResult() {
        composeRule.onNodeWithText("الخرسانة").performClick()
        composeRule.onNodeWithText("حساب عدد وحجم العناصر الخرسانية").assertExists()

        val resultLabel = composeRule.onNodeWithText("النتيجة")
        resultLabel.performScrollTo()
        resultLabel.assertIsDisplayed()

        val totalVolume = composeRule.onNodeWithText(
            "الحجم الإجمالي: 0.027 م³",
            substring = true
        )
        totalVolume.performScrollTo()
        totalVolume.assertIsDisplayed()
    }

    @Test
    fun foundationCalculatorOpensAndShowsResultCard() {
        composeRule.onNodeWithText("الأساسات").performClick()
        composeRule.onNodeWithText("حاسبة الأساسات").assertExists()
        val resultLabel = composeRule.onNodeWithText("النتيجة")
        resultLabel.performScrollTo()
        resultLabel.assertIsDisplayed()
        composeRule.onNodeWithText("الحجم الإجمالي:", substring = true)
            .performScrollTo()
    }

    @Test
    fun userCanSwitchLanguageAndReturnToSections() {
        composeRule.onNodeWithText("FR").performClick()
        composeRule.onNodeWithText("Calculs de construction et d’ingénierie").assertExists()
        composeRule.onNodeWithText("Béton").performClick()
        composeRule.onNodeWithText("Retour aux rubriques").assertExists()
        composeRule.onNodeWithText("Retour aux rubriques").performClick()
        composeRule.onNodeWithText("Que voulez-vous calculer ?").assertExists()
    }
}
