package dz.btpprodz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class MainActivityUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenShowsConstructionSections() {
        composeRule.onNodeWithText("Btp Pro Dz").assertIsDisplayed()
        composeRule.onNodeWithText("ماذا تريد أن تحسب؟").assertIsDisplayed()
        composeRule.onNodeWithText("الخرسانة").assertIsDisplayed()
        composeRule.onNodeWithText("الأساسات").assertIsDisplayed()
    }

    @Test
    fun concreteCalculatorOpensAndShowsCalculatedResult() {
        composeRule.onNodeWithText("الخرسانة").performClick()
        composeRule.onNodeWithText("حساب عدد وحجم العناصر الخرسانية").assertIsDisplayed()

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
        composeRule.onNodeWithText("حاسبة الأساسات").assertIsDisplayed()
        val resultLabel = composeRule.onNodeWithText("النتيجة")
        resultLabel.performScrollTo()
        resultLabel.assertIsDisplayed()
        composeRule.onNodeWithText("الحجم الإجمالي:", substring = true)
            .performScrollTo()
    }

    @Test
    fun userCanSwitchLanguageAndReturnToSections() {
        composeRule.onNodeWithText("FR").performClick()
        composeRule.onNodeWithText("Calculs de construction et d’ingénierie").assertIsDisplayed()
        composeRule.onNodeWithText("Béton").performClick()
        composeRule.onNodeWithText("Retour aux rubriques").assertIsDisplayed()
        composeRule.onNodeWithText("Retour aux rubriques").performClick()
        composeRule.onNodeWithText("Que voulez-vous calculer ?").assertIsDisplayed()
    }

    @Test
    fun earthworksExcavationDisplaysExpectedVolume() {
        val earthworksSection = composeRule.onNodeWithText("الحفر والردم")
        earthworksSection.performScrollTo()
        earthworksSection.assertIsDisplayed()
        earthworksSection.performClick()
        composeRule.onNodeWithText("حاسبة الحفر والردم").assertIsDisplayed()
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 50.000 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

    @Test
    fun earthworksResultUpdatesWhenDimensionChanges() {
        val earthworksSection = composeRule.onNodeWithText("الحفر والردم")
        earthworksSection.performScrollTo()
        earthworksSection.assertIsDisplayed()
        earthworksSection.performClick()
        val length = composeRule.onNodeWithText("الطول (م)")
        length.performScrollTo()
        length.performTextReplacement("12")
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 60.000 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

    @Test
    fun columnsAndBeamsDisplaysExpectedConcreteVolume() {
        composeRule.onNodeWithText("الأعمدة والجسور").performClick()
        composeRule.onNodeWithText("حاسبة الأعمدة والجسور").assertIsDisplayed()
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 1.200 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

}
