package dz.btpprodz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onFirst
import org.junit.Rule
import org.junit.Test

class MainActivityUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenShowsFlatTwoColumnCalculatorGrid() {
        composeRule.onNodeWithText("Btp Pro Dz").assertIsDisplayed()
        composeRule.onNodeWithText("اختر الحاسبة").assertIsDisplayed()
        composeRule.onNodeWithText("الخرسانة").assertIsDisplayed()
        composeRule.onNodeWithText("قواعد منفردة").assertIsDisplayed()
        composeRule.onAllNodesWithText("حسب الأشغال").assertCountEquals(0)
        composeRule.onAllNodesWithText("مراحل البناء").assertCountEquals(0)
    }

    @Test
    fun calculatorSearchFiltersConstructionSections() {
        composeRule.onNodeWithText("ابحث عن حاسبة").assertIsDisplayed()
        composeRule.onNodeWithText("ابحث عن حاسبة").performTextInput("الحفر")
        composeRule.onNodeWithText("الحفر والردم").assertIsDisplayed()
        composeRule.onAllNodesWithText("الخرسانة").assertCountEquals(0)
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
        composeRule.onNodeWithText("قواعد منفردة").performClick()
        composeRule.onNodeWithText("حاسبة قواعد منفردة").assertIsDisplayed()
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
        composeRule.onNodeWithText("Retour aux calculateurs").assertIsDisplayed()
        composeRule.onNodeWithText("Retour aux calculateurs").performClick()
        composeRule.onNodeWithText("Choisissez un calculateur").assertIsDisplayed()
    }

    @Test
    fun earthworksExcavationDisplaysExpectedVolume() {
        // The section is below the initial LazyColumn viewport; filter it into the
        // composed list before locating and opening it.
        composeRule.onNodeWithText("ابحث عن حاسبة")
            .performTextInput("الحفر")
        val earthworksSection = composeRule.onNodeWithText("الحفر والردم")
        earthworksSection.assertIsDisplayed()
        earthworksSection.performClick()
        composeRule.onNodeWithText("حاسبة الحفر والردم").assertIsDisplayed()
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 50.000 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

    @Test
    fun earthworksResultUpdatesWhenDimensionChanges() {
        // Search first so LazyColumn composes the off-screen section.
        composeRule.onNodeWithText("ابحث عن حاسبة")
            .performTextInput("الحفر")
        val earthworksSection = composeRule.onNodeWithText("الحفر والردم")
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
    fun columnsCalculatorDisplaysExpectedConcreteVolume() {
        composeRule.onNodeWithText("الأعمدة").performClick()
        composeRule.onNodeWithText("حاسبة الأعمدة").assertIsDisplayed()
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 1.200 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

    @Test
    fun beamsCalculatorDisplaysExpectedConcreteVolume() {
        composeRule.onNodeWithText("الجسور").performClick()
        composeRule.onNodeWithText("حاسبة الجسور").assertIsDisplayed()
        val result = composeRule.onNodeWithText("الحجم الإجمالي: 1.200 م³", substring = true)
        result.performScrollTo()
        result.assertIsDisplayed()
    }

    @Test
    fun masonryCalculatorOpensAndShowsNetArea() {
        openSection("الجدران والبناء")
        composeRule.onNodeWithText("المساحة الصافية:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun slabCalculatorOpensAndShowsConcreteVolume() {
        openSection("السقوف والبلاطات")
        composeRule.onNodeWithText("حجم الخرسانة:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun plasterAndPaintingCalculatorOpensAndShowsNetArea() {
        openSection("التلبيس والطلاء")
        composeRule.onNodeWithText("المساحة الصافية:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun flooringCalculatorOpensAndShowsTileEstimate() {
        openSection("الأرضيات والتبليط")
        composeRule.onNodeWithText("عدد البلاطات النظري:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun stairsCalculatorOpensAndShowsConcreteVolume() {
        openSection("السلالم")
        composeRule.onNodeWithText("حجم خرسانة السلم التقديري:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun laborCalculatorOpensAndShowsCostEstimate() {
        openSection("اليد العاملة")
        composeRule.onNodeWithText("التكلفة الإجمالية:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun formworkCalculatorShowsEstimatedArea() {
        openSection("القوالب")
        composeRule.onNodeWithText("مساحة القوالب التقديرية:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun steelCalculatorShowsEstimatedRebarWeight() {
        openSection("الحديد")
        composeRule.onNodeWithText("وزن الحديد التقريبي:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun networksCalculatorShowsTotalLength() {
        openSection("الشبكات")
        composeRule.onNodeWithText("الطول الإجمالي للشبكة:", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun finishesCalculatorShowsNetArea() {
        openSection("التشطيبات")
        composeRule.onNodeWithText("المساحة الصافية:", substring = true).performScrollTo().assertIsDisplayed()
    }

    private fun openSection(section: String) {
        composeRule.onNodeWithText("ابحث عن حاسبة")
            .performTextInput(section)
        val sectionCard = composeRule.onNodeWithTag("work-section-$section")
        sectionCard.assertIsDisplayed()
        // Click the actual Card node rather than its child Text semantics node.
        sectionCard.performClick()
        // Wait for the calculator screen itself, not a shared results heading.
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("رجوع إلى الحاسبات").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("رجوع إلى الحاسبات").assertIsDisplayed()
    }

    @Test
    fun projectCreationAndEmptyDevisGuardWorkInUi() {
        composeRule.onNodeWithText("المشاريع و Devis").performClick()
        val projectName = composeRule.onNodeWithText("اسم المشروع")
        projectName.performTextInput("اختبار Devis UI")
        composeRule.onNodeWithText("إنشاء مشروع").performScrollTo().performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("المشروع المحدد: اختبار Devis UI", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("المشروع المحدد: اختبار Devis UI", substring = true)
            .performScrollTo().assertIsDisplayed()

        val createDevis = composeRule.onNodeWithText("إنشاء Devis من التكاليف المسجلة")
        createDevis.performScrollTo().performClick()
        composeRule.onNodeWithText("لا يمكن إنشاء Devis فارغ", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("لم تُنشأ وثائق لهذا المشروع بعد.")
            .performScrollTo().assertIsDisplayed()
    }

}
