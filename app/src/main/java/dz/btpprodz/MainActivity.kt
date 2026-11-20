package dz.btpprodz

import android.os.Bundle
import androidx.room.withTransaction
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dz.btpprodz.calculations.concrete.ConcreteMaterialCalculator
import dz.btpprodz.calculations.concrete.ConcreteDosage
import dz.btpprodz.calculations.concrete.ConcreteMaterialInput
import dz.btpprodz.calculations.concrete.ConcreteVolumeCalculator
import dz.btpprodz.calculations.concrete.ConcreteVolumeInput
import dz.btpprodz.domain.calculations.DevisCostLine
import dz.btpprodz.domain.calculations.ProjectDevisCalculator
import dz.btpprodz.domain.calculations.EarthworkCalculator
import dz.btpprodz.domain.calculations.FoundationVolumeCalculator
import dz.btpprodz.domain.calculations.LinearElementVolumeCalculator
import dz.btpprodz.domain.calculations.SurfaceAreaCalculator
import dz.btpprodz.domain.calculations.SlabVolumeCalculator
import dz.btpprodz.domain.calculations.StairVolumeCalculator
import dz.btpprodz.export.ProjectReportExporter
import dz.btpprodz.export.ProjectReportRow
import java.math.BigDecimal
import dz.btpprodz.data.AppSettingEntity
import dz.btpprodz.data.BtpDatabase
import dz.btpprodz.data.CustomUnitEntity
import dz.btpprodz.data.CalculationCostEntity
import dz.btpprodz.data.MaterialEntity
import dz.btpprodz.data.MaterialPriceHistoryEntity
import dz.btpprodz.data.LaborRateEntity
import dz.btpprodz.data.LaborCostEntity
import dz.btpprodz.data.ProjectEntity
import dz.btpprodz.data.DevisDocumentEntity
import dz.btpprodz.data.DevisLineEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BtpProDzApp() }
    }
}

private data class WorkSection(val ar: String, val fr: String, val descriptionAr: String, val descriptionFr: String)

private val workSections = listOf(
    WorkSection("الخرسانة", "Béton", "حساب عدد العناصر الخرسانية وأحجامها وكميات المواد", "Nombre d’éléments en béton, volumes et quantités des matériaux"),
    WorkSection("الأساسات", "Fondations", "القواعد والسملات والحفر", "Semelles, longrines et fouilles"),
    WorkSection("الأعمدة والجسور", "Poteaux et poutres", "الأبعاد والكميات", "Dimensions et quantités"),
    WorkSection("الجدران والبناء", "Maçonnerie", "مساحات الجدران والآجر والمونة", "Surfaces, briques et mortier"),
    WorkSection("السقوف والبلاطات", "Planchers et dalles", "حساب البلاطات والكميات", "Calcul des dalles et quantités"),
    WorkSection("التلبيس والطلاء", "Enduit et peinture", "المساحات والمواد اللازمة", "Surfaces et matériaux nécessaires"),
    WorkSection("الأرضيات والتبليط", "Revêtements", "البلاط والسيراميك والرخام", "Carrelage, céramique et marbre"),
    WorkSection("الحفر والردم", "Terrassement", "حساب الحفر والردم", "Calcul des déblais et remblais"),
    WorkSection("السلالم", "Escaliers", "الأبعاد والكميات", "Dimensions et quantités"),
    WorkSection("اليد العاملة", "Main-d’œuvre", "حساب الأجور حسب نوع الأشغال", "Calcul des coûts par type de travaux")
)

private val constructionStages = listOf(
    "تهيئة الأرض والحفر" to "Préparation du terrain et terrassement",
    "الأساسات" to "Fondations",
    "الأعمدة والجسور" to "Poteaux et poutres",
    "الجدران والبناء" to "Maçonnerie",
    "السقف والبلاطة" to "Planchers et dalles",
    "التلبيس والطلاء" to "Enduit et peinture",
    "الأرضيات والتبليط" to "Revêtements",
    "الأشغال النهائية" to "Finitions"
)

@Composable
private fun BtpProDzApp() {
    var isArabic by remember { mutableStateOf(true) }
    var selectedView by remember { mutableStateOf(0) }
    var showConcreteCalculator by remember { mutableStateOf(false) }
    var showFoundationCalculator by remember { mutableStateOf(false) }
    var showEarthworksCalculator by remember { mutableStateOf(false) }
    var showColumnBeamCalculator by remember { mutableStateOf(false) }
    var selectedOtherSection by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var activeProjectId by remember { mutableStateOf<Long?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Btp Pro Dz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (isArabic) "حسابات البناء والهندسة" else "Calculs de construction et d’ingénierie",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { isArabic = !isArabic }) {
                        Text(if (isArabic) "FR" else "عربي")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (!showConcreteCalculator && !showFoundationCalculator && !showEarthworksCalculator && !showColumnBeamCalculator && selectedOtherSection == null && !showSettings && !showProjects) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showSettings = true }) {
                            Text(if (isArabic) "الإعدادات" else "Paramètres")
                        }
                        Button(onClick = { showProjects = true }) {
                            Text(if (isArabic) "المشاريع و Devis" else "Projets et devis")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (showConcreteCalculator || showFoundationCalculator || showEarthworksCalculator || showColumnBeamCalculator || selectedOtherSection != null || showSettings || showProjects) {
                    OutlinedButton(onClick = { showConcreteCalculator = false; showFoundationCalculator = false; showEarthworksCalculator = false; showColumnBeamCalculator = false; selectedOtherSection = null; showSettings = false; showProjects = false }) {
                        Text(if (isArabic) "رجوع إلى الأقسام" else "Retour aux rubriques")
                    }
                    if (showConcreteCalculator) { ConcreteCalculator(isArabic = isArabic); CostEstimatorSection(section = "الخرسانة", isArabic = isArabic, projectId = activeProjectId); LaborCostSection(section = "الخرسانة", isArabic = isArabic, projectId = activeProjectId) }
                    if (showFoundationCalculator) { FoundationCalculator(isArabic = isArabic); CostEstimatorSection(section = "الأساسات", isArabic = isArabic, projectId = activeProjectId); LaborCostSection(section = "الأساسات", isArabic = isArabic, projectId = activeProjectId) }
                    if (showEarthworksCalculator) { EarthworksCalculator(isArabic = isArabic); CostEstimatorSection(section = "الحفر والردم", isArabic = isArabic, projectId = activeProjectId); LaborCostSection(section = "الحفر والردم", isArabic = isArabic, projectId = activeProjectId) }
                    if (showColumnBeamCalculator) { ColumnBeamCalculator(isArabic = isArabic); CostEstimatorSection(section = "الأعمدة والجسور", isArabic = isArabic, projectId = activeProjectId); LaborCostSection(section = "الأعمدة والجسور", isArabic = isArabic, projectId = activeProjectId) }
                    if (selectedOtherSection != null) { OtherWorkCalculator(section = selectedOtherSection!!, isArabic = isArabic); if (selectedOtherSection != "اليد العاملة") { CostEstimatorSection(section = selectedOtherSection!!, isArabic = isArabic, projectId = activeProjectId); LaborCostSection(section = selectedOtherSection!!, isArabic = isArabic, projectId = activeProjectId) } }
                    if (showSettings) SettingsScreen(isArabic = isArabic)
                    if (showProjects) ProjectsScreen(isArabic = isArabic, onProjectSelected = { activeProjectId = it })
                } else {
                Text(
                    if (isArabic) "ماذا تريد أن تحسب؟" else "Que voulez-vous calculer ?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedView == 0) {
                        Button(onClick = { selectedView = 0 }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "حسب الأشغال" else "Par travaux")
                        }
                    } else {
                        OutlinedButton(onClick = { selectedView = 0 }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "حسب الأشغال" else "Par travaux")
                        }
                    }
                    if (selectedView == 1) {
                        Button(onClick = { selectedView = 1 }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "مراحل البناء" else "Étapes")
                        }
                    } else {
                        OutlinedButton(onClick = { selectedView = 1 }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "مراحل البناء" else "Étapes")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selectedView == 0) {
                        items(workSections) { section ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (section.ar == "الخرسانة") showConcreteCalculator = true
                                    if (section.ar == "الأساسات") showFoundationCalculator = true
                                    if (section.ar == "الحفر والردم") showEarthworksCalculator = true
                                    if (section.ar == "الأعمدة والجسور") showColumnBeamCalculator = true
                                    if (section.ar !in listOf("الخرسانة", "الأساسات", "الحفر والردم", "الأعمدة والجسور")) selectedOtherSection = section.ar
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        if (isArabic) section.ar else section.fr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (isArabic) section.descriptionAr else section.descriptionFr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(constructionStages) { stage ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        if (isArabic) stage.first else stage.second,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun ConcreteCalculator(isArabic: Boolean) {
    var elementType by remember { mutableStateOf(0) }
    var quantityText by remember { mutableStateOf("1") }
    var lengthText by remember { mutableStateOf("0.30") }
    var widthText by remember { mutableStateOf("0.30") }
    var heightText by remember { mutableStateOf("0.30") }
    var cementDosageText by remember { mutableStateOf("") }
    var sandDosageText by remember { mutableStateOf("") }
    var gravelDosageText by remember { mutableStateOf("") }
    var wasteText by remember { mutableStateOf("0") }

    val typesAr = listOf("القواعد", "الأعمدة", "الجسور", "البلاطات")
    val typesFr = listOf("Semelles", "Poteaux", "Poutres", "Dalles")
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val countValid = quantity.isFinite() && quantity > 0.0 &&
        quantity % 1.0 == 0.0 && quantity <= Int.MAX_VALUE
    val dimensionsValid = length.isFinite() && length > 0.0 &&
        width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0
    val volumeResult = if (countValid && dimensionsValid) runCatching {
        ConcreteVolumeCalculator.calculate(
            ConcreteVolumeInput(
                lengthM = length,
                widthM = width,
                heightM = height,
                quantity = quantity.toInt()
            )
        )
    }.getOrNull() else null
    val valid = volumeResult != null
    val perElementVolume = volumeResult?.volumePerUnitM3 ?: 0.0
    val totalVolume = volumeResult?.totalVolumeM3 ?: 0.0
    val cementDosage = cementDosageText.toDoubleOrNull()
    val sandDosage = sandDosageText.toDoubleOrNull()
    val gravelDosage = gravelDosageText.toDoubleOrNull()
    val waste = wasteText.toDoubleOrNull()
    val materialResult = if (valid && cementDosage != null && sandDosage != null &&
        gravelDosage != null && waste != null) runCatching {
        ConcreteMaterialCalculator.calculate(
            ConcreteMaterialInput(
                concreteVolumeM3 = totalVolume,
                dosage = ConcreteDosage(cementDosage, sandDosage, gravelDosage),
                wastePercent = waste
            )
        )
    }.getOrNull() else null

    val dimensionLabels = when (elementType) {
        0 -> if (isArabic) listOf("الطول (م)", "العرض (م)", "الارتفاع / العمق (م)") else listOf("Longueur (m)", "Largeur (m)", "Hauteur / profondeur (m)")
        1 -> if (isArabic) listOf("عرض المقطع (م)", "عمق المقطع (م)", "الارتفاع (م)") else listOf("Largeur (m)", "Profondeur (m)", "Hauteur (m)")
        2 -> if (isArabic) listOf("الطول (م)", "العرض (م)", "الارتفاع (م)") else listOf("Longueur (m)", "Largeur (m)", "Hauteur (m)")
        else -> if (isArabic) listOf("الطول (م)", "العرض (م)", "السماكة (م)") else listOf("Longueur (m)", "Largeur (m)", "Épaisseur (m)")
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) "حساب عدد وحجم العناصر الخرسانية" else "Nombre et volume des éléments en béton", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (isArabic) "اختر نوع العنصر" else "Choisissez le type d’élément")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            typesAr.forEachIndexed { index, ar ->
                if (elementType == index) Button(onClick = { elementType = index }) { Text(if (isArabic) ar else typesFr[index]) }
                else OutlinedButton(onClick = { elementType = index }) { Text(if (isArabic) ar else typesFr[index]) }
            }
        }
        NumberField(if (isArabic) "عدد العناصر" else "Nombre d’éléments", quantityText) { quantityText = it }
        NumberField(dimensionLabels[0], lengthText) { lengthText = it }
        NumberField(dimensionLabels[1], widthText) { widthText = it }
        NumberField(dimensionLabels[2], heightText) { heightText = it }

        Text(if (isArabic) "تقدير مواد الخرسانة (اختياري)" else "Estimation des matériaux (facultatif)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(if (isArabic) "أدخل جرعة المشروع لكل 1 م³ كما حددها المهندس أو مواصفات المشروع. لا توجد خلطة افتراضية." else "Saisissez le dosage du projet par 1 m³ selon les spécifications. Aucun dosage par défaut.")
        NumberField(if (isArabic) "الإسمنت (كغ / م³)" else "Ciment (kg / m³)", cementDosageText) { cementDosageText = it }
        NumberField(if (isArabic) "الرمل (م³ / م³)" else "Sable (m³ / m³)", sandDosageText) { sandDosageText = it }
        NumberField(if (isArabic) "الحصى (م³ / م³)" else "Gravier (m³ / m³)", gravelDosageText) { gravelDosageText = it }
        NumberField(if (isArabic) "نسبة الهدر (%)" else "Pertes (%)", wasteText) { wasteText = it }
        if (materialResult != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (isArabic) "تقدير المواد حسب الجرعات المدخلة" else "Matériaux selon le dosage saisi", fontWeight = FontWeight.Bold)
                    Text(if (isArabic) "الإسمنت: %.1f كغ".format(materialResult.cementKg) else "Ciment : %.1f kg".format(materialResult.cementKg))
                    Text(if (isArabic) "الرمل: %.3f م³".format(materialResult.sandM3) else "Sable : %.3f m³".format(materialResult.sandM3))
                    Text(if (isArabic) "الحصى: %.3f م³".format(materialResult.gravelM3) else "Gravier : %.3f m³".format(materialResult.gravelM3))
                    Text(if (isArabic) "الحجم بعد الهدر: %.3f م³".format(materialResult.appliedVolumeM3) else "Volume avec pertes : %.3f m³".format(materialResult.appliedVolumeM3))
                }
            }
        } else if (cementDosageText.isNotBlank() || sandDosageText.isNotBlank() || gravelDosageText.isNotBlank()) {
            Text(if (isArabic) "تحقق من إدخال الجرعات الثلاث ونسبة الهدر ضمن المجال الصحيح." else "Vérifiez les trois dosages et le pourcentage de pertes.")
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "عدد العناصر: ${if (valid) quantity.toInt().toString() else "—"}" else "Nombre d’éléments : ${if (valid) quantity.toInt().toString() else "—"}")
                Text(if (isArabic) "حجم العنصر الواحد: ${if (valid) "%.3f".format(perElementVolume) else "—"} م³" else "Volume unitaire : ${if (valid) "%.3f".format(perElementVolume) else "—"} m³")
                Text(if (isArabic) "الحجم الإجمالي: ${if (valid) "%.3f".format(totalVolume) else "—"} م³" else "Volume total : ${if (valid) "%.3f".format(totalVolume) else "—"} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!valid) Text(if (isArabic) "أدخل أعداداً وأبعاداً أكبر من الصفر لعرض نتيجة صحيحة." else "Saisissez des quantités et dimensions supérieures à zéro.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(if (isArabic) "الحساب: عدد العناصر × الطول × العرض × الارتفاع أو السماكة" else "Calcul : nombre × longueur × largeur × hauteur ou épaisseur", style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
private fun FoundationCalculator(isArabic: Boolean) {
    var foundationType by remember { mutableStateOf(0) }
    var quantityText by remember { mutableStateOf("4") }
    var lengthText by remember { mutableStateOf("1.20") }
    var widthText by remember { mutableStateOf("1.20") }
    var depthText by remember { mutableStateOf("0.40") }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val depth = depthText.toDoubleOrNull() ?: 0.0
    val countValid = quantity.isFinite() && quantity > 0.0 &&
        quantity % 1.0 == 0.0 && quantity <= Int.MAX_VALUE
    val dimensionsValid = length.isFinite() && length > 0.0 &&
        width.isFinite() && width > 0.0 && depth.isFinite() && depth > 0.0
    val foundationResult = if (countValid && dimensionsValid) runCatching {
        val count = quantity.toInt()
        val l = BigDecimal.valueOf(length)
        val w = BigDecimal.valueOf(width)
        val d = BigDecimal.valueOf(depth)
        when (foundationType) {
            0 -> FoundationVolumeCalculator.isolatedFootings(count, l, w, d)
            1 -> FoundationVolumeCalculator.stripFootingRuns(count, l, w, d)
            else -> FoundationVolumeCalculator.gradeBeamRuns(count, l, w, d)
        }
    }.getOrNull() else null
    val valid = foundationResult != null
    val unitVolume = foundationResult?.volumePerElementM3?.toDouble() ?: 0.0
    val totalVolume = foundationResult?.totalVolumeM3?.toDouble() ?: 0.0
    val typesAr = listOf("قواعد منفردة", "قواعد شريطية", "سملات / ميدات")
    val typesFr = listOf("Semelles isolées", "Semelles filantes", "Longrines")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (isArabic) "حاسبة الأساسات" else "Calculateur des fondations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(if (isArabic) "نوع الأساس" else "Type de fondation")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            typesAr.forEachIndexed { index, ar ->
                if (foundationType == index) {
                    Button(onClick = { foundationType = index }) {
                        Text(if (isArabic) ar else typesFr[index])
                    }
                } else {
                    OutlinedButton(onClick = { foundationType = index }) {
                        Text(if (isArabic) ar else typesFr[index])
                    }
                }
            }
        }
        NumberField(if (isArabic) "العدد" else "Quantité", quantityText) { quantityText = it }
        NumberField(if (isArabic) "الطول (م)" else "Longueur (m)", lengthText) { lengthText = it }
        NumberField(if (isArabic) "العرض (م)" else "Largeur (m)", widthText) { widthText = it }
        NumberField(if (isArabic) "الارتفاع / العمق (م)" else "Hauteur / profondeur (m)", depthText) { depthText = it }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "نوع الأساس: ${typesAr[foundationType]}" else "Type : ${typesFr[foundationType]}")
                Text(if (isArabic) "عدد العناصر: ${if (valid) quantity.toInt().toString() else "—"}" else "Quantité : ${if (valid) quantity.toInt().toString() else "—"}")
                Text(if (isArabic) "حجم العنصر الواحد: ${if (valid) "%.3f".format(unitVolume) else "—"} م³" else "Volume unitaire : ${if (valid) "%.3f".format(unitVolume) else "—"} m³")
                Text(
                    if (isArabic) "الحجم الإجمالي: ${if (valid) "%.3f".format(totalVolume) else "—"} م³" else "Volume total : ${if (valid) "%.3f".format(totalVolume) else "—"} m³",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!valid) {
                    Text(
                        if (isArabic) "أدخل عدداً صحيحاً وأبعاداً أكبر من الصفر." else "Saisissez une quantité entière et des dimensions supérieures à zéro.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Text(
            if (isArabic) "الحساب: العدد × الطول × العرض × الارتفاع. هذه حاسبة حجم أولية، ويجب إدخال الأبعاد حسب شكل العنصر." else "Calcul : quantité × longueur × largeur × hauteur. Calcul volumique simple, selon les dimensions de l’élément.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun EarthworksCalculator(isArabic: Boolean) {
    var operation by remember { mutableStateOf(0) }
    var quantityText by remember { mutableStateOf("1") }
    var lengthText by remember { mutableStateOf("10") }
    var widthText by remember { mutableStateOf("5") }
    var depthText by remember { mutableStateOf("1") }
    var occupiedVolumeText by remember { mutableStateOf("0") }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val depth = depthText.toDoubleOrNull() ?: 0.0
    val occupiedVolume = occupiedVolumeText.toBigDecimalOrNull()
    val excavation = runCatching {
        EarthworkCalculator.rectangularExcavation(
            length.toBigDecimal(), width.toBigDecimal(), depth.toBigDecimal()
        ).volumeCubicMeters
    }.getOrNull()
    val backfillPerZone = if (operation == 1 && excavation != null && occupiedVolume != null) {
        runCatching { EarthworkCalculator.backfill(excavation, occupiedVolume).netVolumeCubicMeters }.getOrNull()
    } else null
    val unitVolume = when (operation) {
        0 -> excavation?.toDouble() ?: 0.0
        else -> backfillPerZone?.toDouble() ?: 0.0
    }
    val totalVolume = quantity * unitVolume
    val valid = quantity.isFinite() && quantity > 0.0 && quantity % 1.0 == 0.0 &&
        quantity <= Int.MAX_VALUE && length.isFinite() && width.isFinite() && depth.isFinite() &&
        length > 0.0 && width > 0.0 && depth > 0.0 && unitVolume.isFinite() &&
        totalVolume.isFinite() && (operation == 0 || backfillPerZone != null)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (isArabic) "حاسبة الحفر والردم" else "Calculateur de terrassement",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (operation == 0) Button(onClick = { operation = 0 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الحفر" else "Déblai")
            } else OutlinedButton(onClick = { operation = 0 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الحفر" else "Déblai")
            }
            if (operation == 1) Button(onClick = { operation = 1 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الردم" else "Remblai")
            } else OutlinedButton(onClick = { operation = 1 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الردم" else "Remblai")
            }
        }
        NumberField(if (isArabic) "عدد الحفريات / المساحات" else "Nombre de zones", quantityText) { quantityText = it }
        NumberField(if (isArabic) "الطول (م)" else "Longueur (m)", lengthText) { lengthText = it }
        NumberField(if (isArabic) "العرض (م)" else "Largeur (m)", widthText) { widthText = it }
        NumberField(if (isArabic) "العمق أو الارتفاع (م)" else "Profondeur ou hauteur (m)", depthText) { depthText = it }
        if (operation == 1) {
            NumberField(if (isArabic) "حجم المنشأ داخل الحفر لكل منطقة (م³)" else "Volume de structure occupé par zone (m³)", occupiedVolumeText) { occupiedVolumeText = it }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "العملية: ${if (operation == 0) "حفر" else "ردم"}" else "Opération : ${if (operation == 0) "Déblai" else "Remblai"}")
                Text(if (isArabic) "عدد المناطق: ${if (valid) quantity.toInt().toString() else "—"}" else "Nombre de zones : ${if (valid) quantity.toInt().toString() else "—"}")
                Text(if (valid) {
                    if (isArabic) "الحجم الصافي للمنطقة الواحدة: ${"%.3f".format(unitVolume)} م³" else "Volume net par zone : ${"%.3f".format(unitVolume)} m³"
                } else {
                    if (isArabic) "حجم المنطقة الواحدة: —" else "Volume par zone : —"
                })
                Text(
                    if (isArabic) "الحجم الإجمالي: ${if (valid) "%.3f".format(totalVolume) else "—"} م³" else "Volume total : ${if (valid) "%.3f".format(totalVolume) else "—"} m³",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!valid) Text(
                    if (operation == 1) {
                        if (isArabic) "تحقق من الأبعاد والعدد، وأن حجم المنشأ بين صفر وحجم الحفر." else "Vérifiez les dimensions, la quantité et un volume occupé compris entre zéro et le volume excavé."
                    } else {
                        if (isArabic) "أدخل عدداً صحيحاً وأبعاداً أكبر من الصفر." else "Saisissez un nombre entier et des dimensions positives."
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            if (isArabic) "الحفر = الطول × العرض × العمق. الردم الصافي = حجم الحفر − حجم المنشأ المشغول لكل منطقة. لا يشمل الحساب انتفاش التربة أو الدمك." else "Déblai = longueur × largeur × profondeur. Remblai net = volume excavé − volume occupé par la structure. Hors foisonnement et compactage.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun ColumnBeamCalculator(isArabic: Boolean) {
    var elementType by remember { mutableStateOf(0) }
    var quantityText by remember { mutableStateOf("4") }
    var lengthText by remember { mutableStateOf("3.00") }
    var widthText by remember { mutableStateOf("0.25") }
    var heightText by remember { mutableStateOf("0.40") }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val countValid = quantity.isFinite() && quantity > 0.0 &&
        quantity % 1.0 == 0.0 && quantity <= Int.MAX_VALUE
    val dimensionsValid = length.isFinite() && length > 0.0 &&
        width.isFinite() && width > 0.0 && height.isFinite() && height > 0.0
    val volumeResult = if (countValid && dimensionsValid) runCatching {
        LinearElementVolumeCalculator.calculate(
            quantity.toInt(),
            BigDecimal.valueOf(length),
            BigDecimal.valueOf(width),
            BigDecimal.valueOf(height)
        )
    }.getOrNull() else null
    val valid = volumeResult != null
    val unitVolume = volumeResult?.volumePerElementM3?.toDouble() ?: 0.0
    val totalVolume = volumeResult?.totalVolumeM3?.toDouble() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (isArabic) "حاسبة الأعمدة والجسور" else "Calculateur des poteaux et poutres",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(if (isArabic) "اختر نوع العنصر" else "Type d’élément")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (elementType == 0) Button(onClick = { elementType = 0 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الأعمدة" else "Poteaux")
            } else OutlinedButton(onClick = { elementType = 0 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الأعمدة" else "Poteaux")
            }
            if (elementType == 1) Button(onClick = { elementType = 1 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الجسور" else "Poutres")
            } else OutlinedButton(onClick = { elementType = 1 }, modifier = Modifier.weight(1f)) {
                Text(if (isArabic) "الجسور" else "Poutres")
            }
        }
        NumberField(if (isArabic) "عدد العناصر" else "Nombre d’éléments", quantityText) { quantityText = it }
        NumberField(if (isArabic) "الطول (م)" else "Longueur (m)", lengthText) { lengthText = it }
        NumberField(if (isArabic) "عرض المقطع (م)" else "Largeur de section (m)", widthText) { widthText = it }
        NumberField(if (isArabic) "ارتفاع المقطع (م)" else "Hauteur de section (m)", heightText) { heightText = it }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "نوع العنصر: ${if (elementType == 0) "عمود" else "جسر"}" else "Élément : ${if (elementType == 0) "Poteau" else "Poutre"}")
                Text(if (isArabic) "عدد العناصر: ${if (valid) quantity.toInt().toString() else "—"}" else "Nombre d’éléments : ${if (valid) quantity.toInt().toString() else "—"}")
                Text(if (isArabic) "حجم العنصر الواحد: ${if (valid) "%.3f".format(unitVolume) else "—"} م³" else "Volume unitaire : ${if (valid) "%.3f".format(unitVolume) else "—"} m³")
                Text(
                    if (isArabic) "الحجم الإجمالي: ${"%.3f".format(totalVolume)} م³" else "Volume total : ${"%.3f".format(totalVolume)} m³",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!valid) Text(
                    if (isArabic) "أدخل عدداً صحيحاً وأبعاداً أكبر من الصفر." else "Saisissez une quantité entière et des dimensions positives.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            if (isArabic) "الحساب: عدد العناصر × الطول × عرض المقطع × ارتفاع المقطع. هذه حاسبة حجم الخرسانة فقط وليست حساباً إنشائياً للتسليح أو المقاومة." else "Calcul : nombre × longueur × largeur de section × hauteur de section. Volume de béton uniquement, sans dimensionnement structurel ni calcul des armatures.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun OtherWorkCalculator(section: String, isArabic: Boolean) {
    var countText by remember(section) { mutableStateOf("1") }
    var lengthText by remember(section) { mutableStateOf("5") }
    var widthText by remember(section) { mutableStateOf(if (section == "السلالم") "1.2" else "3") }
    var heightText by remember(section) { mutableStateOf(if (section == "السلالم" || section == "السقوف والبلاطات") "0.15" else "3") }
    var unitPriceText by remember(section) { mutableStateOf("0") }
    var openingAreaText by remember(section) { mutableStateOf("0") }
    var tileLengthText by remember(section) { mutableStateOf("0.60") }
    var tileWidthText by remember(section) { mutableStateOf("0.60") }
    var stairStepCountText by remember(section) { mutableStateOf("10") }
    var stairTreadText by remember(section) { mutableStateOf("0.30") }
    var stairRiserText by remember(section) { mutableStateOf("0.17") }

    val count = countText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val openings = openingAreaText.toDoubleOrNull() ?: 0.0
    val tileLength = tileLengthText.toDoubleOrNull() ?: 0.0
    val tileWidth = tileWidthText.toDoubleOrNull() ?: 0.0
    val stairStepCount = stairStepCountText.toIntOrNull() ?: 0
    val stairTread = stairTreadText.toDoubleOrNull() ?: 0.0
    val stairRiser = stairRiserText.toDoubleOrNull() ?: 0.0

    val isWall = section == "الجدران والبناء"
    val isRoof = section == "السقوف والبلاطات"
    val isFinish = section == "التلبيس والطلاء"
    val isFloor = section == "الأرضيات والتبليط"
    val isStairs = section == "السلالم"
    val isLabor = section == "اليد العاملة"
    val area = when {
        isWall || isFinish -> runCatching {
            SurfaceAreaCalculator.netRectangularArea(
                count = count.toInt(),
                lengthM = BigDecimal.valueOf(length),
                heightM = BigDecimal.valueOf(width),
                totalOpeningsM2 = BigDecimal.valueOf(openings)
            ).toDouble()
        }.getOrDefault(-1.0)
        isRoof || isFloor || isStairs -> count * length * width
        else -> 0.0
    }
    val volume = when {
        isRoof -> runCatching {
            SlabVolumeCalculator.calculate(
                count.toInt(), BigDecimal.valueOf(length), BigDecimal.valueOf(width),
                BigDecimal.valueOf(height)
            ).toDouble()
        }.getOrDefault(-1.0)
        isStairs -> runCatching {
            StairVolumeCalculator.calculate(
                count = count.toInt(),
                stairWidth = BigDecimal.valueOf(width),
                slopingLength = BigDecimal.valueOf(length),
                waistThickness = BigDecimal.valueOf(height),
                stepCount = stairStepCount,
                treadDepth = BigDecimal.valueOf(stairTread),
                riserHeight = BigDecimal.valueOf(stairRiser)
            ).totalVolumeM3.toDouble()
        }.getOrDefault(-1.0)
        else -> 0.0
    }
    val tileCount = if (isFloor && tileLength > 0 && tileWidth > 0) area / (tileLength * tileWidth) else 0.0
    val laborTotal = if (isLabor && count >= 0 && unitPrice >= 0) count * unitPrice else 0.0
    val inputsFinite = listOf(count, length, width, height, unitPrice, openings, tileLength, tileWidth, stairTread, stairRiser).all { it.isFinite() }
    val outputsFinite = area.isFinite() && volume.isFinite() && tileCount.isFinite() && laborTotal.isFinite()
    val valid = inputsFinite && outputsFinite && count > 0.0 &&
        (isLabor || (count % 1.0 == 0.0 && count <= Int.MAX_VALUE)) &&
        (isLabor || (length > 0.0 && width > 0.0 && height > 0.0)) &&
        (isLabor || openings >= 0.0) &&
        (!isFloor || (tileLength > 0.0 && tileWidth > 0.0)) &&
        (!isLabor || unitPrice >= 0.0) && (!(isWall || isFinish) || area >= 0.0) &&
        (!isStairs || (stairStepCount > 0 && stairTread > 0.0 && stairRiser > 0.0))

    val titleAr = section
    val titleFr = when(section) {
        "الجدران والبناء" -> "Maçonnerie"
        "السقوف والبلاطات" -> "Planchers et dalles"
        "التلبيس والطلاء" -> "Enduit et peinture"
        "الأرضيات والتبليط" -> "Revêtements"
        "السلالم" -> "Escaliers"
        else -> "Main-d’œuvre"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) titleAr else titleFr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (isLabor) {
            NumberField(if (isArabic) "عدد وحدات العمل / الأيام / الساعات" else "Nombre d’unités / jours / heures", countText) { countText = it }
            NumberField(if (isArabic) "سعر الوحدة (دج)" else "Prix unitaire (DA)", unitPriceText) { unitPriceText = it }
        } else {
            NumberField(if (isArabic) "عدد العناصر / الغرف / المساحات" else "Nombre d’éléments / pièces / surfaces", countText) { countText = it }
            NumberField(if (isArabic) if (isStairs) "طول الميل (م)" else "الطول (م)" else if (isStairs) "Longueur inclinée (m)" else "Longueur (m)", lengthText) { lengthText = it }
            NumberField(if (isArabic) if (isWall || isFinish) "الارتفاع (م)" else "العرض (م)" else if (isWall || isFinish) "Hauteur (m)" else "Largeur (m)", widthText) { widthText = it }
            if (isWall || isFinish) {
                NumberField(if (isArabic) "المساحة الإجمالية للفتحات (م²)" else "Surface totale des ouvertures (m²)", openingAreaText) { openingAreaText = it }
            } else if (!isStairs && !isRoof) {
                NumberField(if (isArabic) "البعد الإضافي (م)" else "Dimension supplémentaire (m)", heightText) { heightText = it }
            }
            if (isStairs) {
                NumberField(if (isArabic) "سماكة بطن السلم (م)" else "Épaisseur de paillasse (m)", heightText) { heightText = it }
                NumberField(if (isArabic) "عدد الدرجات" else "Nombre de marches", stairStepCountText) { stairStepCountText = it }
                NumberField(if (isArabic) "عمق الدرجة (م)" else "Giron (m)", stairTreadText) { stairTreadText = it }
                NumberField(if (isArabic) "ارتفاع الدرجة (م)" else "Hauteur de marche (m)", stairRiserText) { stairRiserText = it }
            }
            if (isFloor) {
                NumberField(if (isArabic) "طول البلاطة الواحدة (م)" else "Longueur d’un carreau (m)", tileLengthText) { tileLengthText = it }
                NumberField(if (isArabic) "عرض البلاطة الواحدة (م)" else "Largeur d’un carreau (m)", tileWidthText) { tileWidthText = it }
            }
            if (isRoof) {
                NumberField(if (isArabic) "السماكة (م)" else "Épaisseur (m)", heightText) { heightText = it }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (isArabic) "النتائج التقديرية" else "Résultats estimatifs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                when {
                    isLabor -> {
                        Text(if (isArabic) "إجمالي وحدات العمل: ${if (valid) "%.2f".format(count) else "—"}" else "Total des unités : ${if (valid) "%.2f".format(count) else "—"}")
                        Text(if (isArabic) "التكلفة الإجمالية: ${if (valid) "%.2f".format(laborTotal) else "—"} دج" else "Coût total : ${if (valid) "%.2f".format(laborTotal) else "—"} DA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    isStairs -> {
                        Text(if (isArabic) "حجم خرسانة السلم التقديري: ${if (valid) "%.3f".format(volume) else "—"} م³" else "Volume estimé de béton de l’escalier : ${if (valid) "%.3f".format(volume) else "—"} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    isRoof -> {
                        Text(if (isArabic) "المساحة: ${if (valid) "%.3f".format(area) else "—"} م²" else "Surface : ${if (valid) "%.3f".format(area) else "—"} m²")
                        Text(if (isArabic) "حجم الخرسانة: ${if (valid) "%.3f".format(volume) else "—"} م³" else "Volume de béton : ${if (valid) "%.3f".format(volume) else "—"} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    isFloor -> {
                        Text(if (isArabic) "المساحة الإجمالية: ${if (valid) "%.3f".format(area) else "—"} م²" else "Surface totale : ${if (valid) "%.3f".format(area) else "—"} m²")
                        Text(if (isArabic) "عدد البلاطات النظري: ${if (valid) "%.0f".format(kotlin.math.ceil(tileCount)) else "—"}" else "Nombre théorique de carreaux : ${if (valid) "%.0f".format(kotlin.math.ceil(tileCount)) else "—"}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    else -> Text(if (isArabic) "المساحة الصافية: ${if (valid) "%.3f".format(area) else "—"} م²" else "Surface nette : ${if (valid) "%.3f".format(area) else "—"} m²", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (!valid) Text(
                    if (isArabic) "راجع المدخلات: يجب أن تكون الأبعاد والأعداد موجبة، وألا تتجاوز الفتحات مساحة الجدار." else "Vérifiez les données : valeurs positives et ouvertures inférieures à la surface du mur.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            if (isArabic) "هذه نتائج كميات أولية. لا تشمل نسب الهدر أو مواصفات المواد أو تفاصيل التنفيذ. في حساب اليد العاملة، أدخل عدد الوحدات وسعر الوحدة المناسب." else "Estimations de quantités. Hors pertes, spécifications des matériaux et détails d’exécution. Pour la main-d’œuvre, saisissez le nombre d’unités et le prix unitaire.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}



@Composable
private fun CostEstimatorSection(section: String, isArabic: Boolean, projectId: Long?) {
    val context = LocalContext.current
    val dao = remember { BtpDatabase.getInstance(context).appDataDao() }
    val scope = rememberCoroutineScope()
    val materials by dao.observeMaterials().collectAsState(initial = emptyList())
    val costsFlow = remember(section, projectId) { projectId?.let { dao.observeCalculationCostsForProject(section, it) } ?: flowOf(emptyList()) }
    val costs by costsFlow.collectAsState(initial = emptyList())
    var selectedMaterialId by remember(section) { mutableStateOf<Long?>(null) }
    var quantityText by remember(section) { mutableStateOf("1") }

    val selectedMaterial = materials.firstOrNull { it.id == selectedMaterialId }
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val estimatedCostCandidate = quantity * (selectedMaterial?.currentPrice ?: 0.0)
    val valid = selectedMaterial != null && quantity.isFinite() && quantity > 0.0 &&
        selectedMaterial.currentPrice.isFinite() && selectedMaterial.currentPrice >= 0.0 &&
        estimatedCostCandidate.isFinite() && projectId != null
    val estimatedCost = if (valid) estimatedCostCandidate else 0.0
    val totalRecorded = costs.sumOf { it.totalCost }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (projectId == null) Text(if (isArabic) "اختر مشروعاً من شاشة المشاريع قبل تسجيل التكاليف." else "Sélectionnez un projet dans l’écran Projets avant d’enregistrer des coûts.", color = MaterialTheme.colorScheme.error)
        Divider()
        Text(if (isArabic) "تكلفة المواد — $section" else "Coût des matériaux — ${when(section) {
            "الخرسانة" -> "Béton"
            "الأساسات" -> "Fondations"
            "الأعمدة والجسور" -> "Poteaux et poutres"
            "الجدران والبناء" -> "Maçonnerie"
            "السقوف والبلاطات" -> "Planchers et dalles"
            "التلبيس والطلاء" -> "Enduit et peinture"
            "الأرضيات والتبليط" -> "Revêtements"
            "الحفر والردم" -> "Terrassement"
            "السلالم" -> "Escaliers"
            else -> "Main-d’œuvre"
        }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (materials.isEmpty()) {
            Text(if (isArabic) "أضف أسعار المواد أولاً من الإعدادات حتى تتمكن من حساب التكلفة." else "Ajoutez d’abord les prix des matériaux dans les paramètres.")
        } else {
            Text(if (isArabic) "اختر المادة" else "Choisir un matériau")
            materials.forEach { material ->
                val label = "${material.name} — ${material.currentPrice} دج / ${material.unit}"
                if (selectedMaterialId == material.id) {
                    Button(onClick = { selectedMaterialId = material.id }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                } else {
                    OutlinedButton(onClick = { selectedMaterialId = material.id }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            }
            NumberField(
                if (isArabic) "الكمية المستهلكة (${selectedMaterial?.unit ?: "الوحدة"})" else "Quantité consommée (${selectedMaterial?.unit ?: "unité"})",
                quantityText
            ) { quantityText = it }
            Text(
                if (isArabic) "التكلفة التقديرية: ${"%.2f".format(estimatedCost)} دج" else "Coût estimé : ${"%.2f".format(estimatedCost)} DA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    val material = selectedMaterial
                    if (material != null && quantity.isFinite() && quantity > 0.0 &&
                        material.currentPrice.isFinite() && material.currentPrice >= 0.0 &&
                        (quantity * material.currentPrice).isFinite()) {
                        scope.launch {
                            dao.insertCalculationCost(
                                CalculationCostEntity(
                                    section = section,
                                    materialName = material.name,
                                    quantity = quantity,
                                    unit = material.unit,
                                    unitPriceSnapshot = material.currentPrice,
                                    totalCost = quantity * material.currentPrice,
                                    projectId = projectId
                                )
                            )
                            quantityText = "1"
                        }
                    }
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isArabic) "حفظ بند التكلفة" else "Enregistrer le coût") }
        }
        Text(
            if (isArabic) "مجموع بنود التكلفة المسجلة: ${"%.2f".format(totalRecorded)} دج" else "Total des coûts enregistrés : ${"%.2f".format(totalRecorded)} DA",
            fontWeight = FontWeight.SemiBold
        )
        costs.forEach { cost ->
            Text(
                "• ${cost.materialName}: ${cost.quantity} ${cost.unit} × ${cost.unitPriceSnapshot} = ${"%.2f".format(cost.totalCost)} DA",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            if (isArabic) "يُحفظ سعر الوحدة المستخدم في هذا البند كنسخة ثابتة؛ تحديث سعر المادة لاحقاً لا يغيّر هذا السجل. أدخل كمية الاستهلاك الفعلية، فالتطبيق لا يفترض نسب خلط أو هدر تلقائياً." else "Le prix unitaire utilisé est figé dans chaque ligne. Une mise à jour ultérieure du matériau ne modifie pas cet enregistrement. Saisissez la consommation réelle : aucun dosage ni taux de perte n’est supposé.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun LaborCostSection(section: String, isArabic: Boolean, projectId: Long?) {
    val context = LocalContext.current
    val dao = remember { BtpDatabase.getInstance(context).appDataDao() }
    val scope = rememberCoroutineScope()
    val rates by dao.observeLaborRates().collectAsState(initial = emptyList())
    val costsFlow = remember(section, projectId) { projectId?.let { dao.observeLaborCostsForProject(section, it) } ?: flowOf(emptyList()) }
    val costs by costsFlow.collectAsState(initial = emptyList())
    var selectedId by remember(section) { mutableStateOf<Long?>(null) }
    var quantityText by remember(section) { mutableStateOf("1") }
    val rate = rates.firstOrNull { it.id == selectedId }
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val calculatedTotal = quantity * (rate?.unitPrice ?: 0.0)
    val validLaborEntry = rate != null && quantity.isFinite() && quantity > 0.0 &&
        rate.unitPrice.isFinite() && rate.unitPrice >= 0.0 && calculatedTotal.isFinite() &&
        projectId != null
    val total = costs.sumOf { it.totalCost }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (projectId == null) Text(if (isArabic) "اختر مشروعاً من شاشة المشاريع قبل تسجيل التكاليف." else "Sélectionnez un projet dans l’écran Projets avant d’enregistrer des coûts.", color = MaterialTheme.colorScheme.error)
        Divider()
        Text(if (isArabic) "تكلفة اليد العاملة — " + section else "Coût de main-d’œuvre — " + section, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (rates.isEmpty()) Text(if (isArabic) "أضف أسعار المهن من الإعدادات أولاً." else "Ajoutez les tarifs des métiers dans les paramètres.")
        rates.forEach { item ->
            val label = item.tradeName + " — " + item.unitPrice + " دج / " + item.unit
            if (selectedId == item.id) Button(onClick = { selectedId = item.id }, modifier = Modifier.fillMaxWidth()) { Text(label) }
            else OutlinedButton(onClick = { selectedId = item.id }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        if (rate != null) {
            NumberField(if (isArabic) "الكمية (" + rate.unit + ")" else "Quantité (" + rate.unit + ")", quantityText) { quantityText = it }
            Text(if (isArabic) "التكلفة التقديرية: " + "%.2f".format(if (validLaborEntry) calculatedTotal else 0.0) + " دج" else "Coût estimé : " + "%.2f".format(if (validLaborEntry) calculatedTotal else 0.0) + " DA")
            Button(onClick = {
                if (validLaborEntry && rate != null) scope.launch {
                    dao.insertLaborCost(LaborCostEntity(section = section, tradeName = rate.tradeName, quantity = quantity, unit = rate.unit, unitPriceSnapshot = rate.unitPrice, totalCost = quantity * rate.unitPrice, projectId = projectId))
                    quantityText = "1"
                }
            }, enabled = validLaborEntry, modifier = Modifier.fillMaxWidth()) { Text(if (isArabic) "حفظ بند اليد العاملة" else "Enregistrer la main-d’œuvre") }
        }
        Text(if (isArabic) "مجموع اليد العاملة المسجل: " + "%.2f".format(total) + " دج" else "Total main-d’œuvre : " + "%.2f".format(total) + " DA", fontWeight = FontWeight.Bold)
        costs.forEach { item -> Text("• " + item.tradeName + ": " + item.quantity + " " + item.unit + " × " + item.unitPriceSnapshot + " = " + "%.2f".format(item.totalCost) + " DA", style = MaterialTheme.typography.bodySmall) }
        Text(if (isArabic) "يُحفظ سعر المهنة وقت تسجيل البند ولا يتغير عند تحديث السعر." else "Le tarif est figé au moment de l’enregistrement.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SettingsScreen(isArabic: Boolean) {
    val context = LocalContext.current
    val dao = remember { BtpDatabase.getInstance(context).appDataDao() }
    val scope = rememberCoroutineScope()

    val backupDatabase = remember { BtpDatabase.getInstance(context) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) scope.launch {
            backupMessage = runCatching {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri) ?: error("Cannot open backup destination.")
                    output.use { DatabaseBackupManager.backup(context, backupDatabase, it) }
                }
                (context as? Activity)?.recreate()
                if (isArabic) "تم إنشاء النسخة الاحتياطية بنجاح." else "Backup created successfully."
            }.getOrElse { (if (isArabic) "فشل النسخ الاحتياطي: " else "Backup failed: ") + (it.message ?: "Unknown error") }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            backupMessage = runCatching {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open selected backup.")
                    input.use { DatabaseBackupManager.restore(context, it) }
                }
                (context as? Activity)?.recreate()
                if (isArabic) "تم التحقق من النسخة واسترجاع قاعدة البيانات." else "Backup validated and restored."
            }.getOrElse { (if (isArabic) "فشل الاسترجاع: " else "Restore failed: ") + (it.message ?: "Unknown error") }
        }
    }
    val units by dao.observeUnits().collectAsState(initial = emptyList())
    val materials by dao.observeMaterials().collectAsState(initial = emptyList())
    val history by dao.observePriceHistory().collectAsState(initial = emptyList())

    var tvaText by remember { mutableStateOf("19") }
    var unitName by remember { mutableStateOf("") }
    var unitSymbol by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    var materialPrice by remember { mutableStateOf("") }
    var materialUnit by remember { mutableStateOf("وحدة") }
    var laborTrade by remember { mutableStateOf("") }
    var laborUnit by remember { mutableStateOf("يوم") }
    var laborPrice by remember { mutableStateOf("") }
    val laborRates by dao.observeLaborRates().collectAsState(initial = emptyList())
    var tvaLoaded by remember { mutableStateOf(false) }
    val tva = tvaText.toDoubleOrNull() ?: -1.0
    val validTva = tva in 0.0..100.0

    LaunchedEffect(Unit) {
        val saved = dao.getSetting("tva_percent")
        if (saved != null) tvaText = saved.value
        tvaLoaded = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) "إعدادات التطبيق" else "Paramètres de l’application", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Divider()
        Text(if (isArabic) "النسخ الاحتياطي والاسترجاع" else "Sauvegarde et restauration", fontWeight = FontWeight.SemiBold)
        Text(if (isArabic) "احفظ نسخة من قاعدة بيانات مشاريعك وأسعارك ووثائق Devis في ملف تختار مكانه. الاسترجاع يستبدل قاعدة البيانات الحالية بعد فحص سلامة الملف." else "Exportez la base locale (projets, tarifs et devis). La restauration remplace la base actuelle après vérification d’intégrité.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { backupLauncher.launch("BtpProDz_Backup.db") }) {
                Text(if (isArabic) "إنشاء نسخة" else "Sauvegarder")
            }
            OutlinedButton(onClick = { restoreLauncher.launch(arrayOf("*/*")) }) {
                Text(if (isArabic) "استرجاع نسخة" else "Restaurer")
            }
        }
        backupMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Text(if (isArabic) "الضريبة على القيمة المضافة TVA (%)" else "TVA (%)", fontWeight = FontWeight.SemiBold)
        NumberField(if (isArabic) "نسبة TVA" else "Taux de TVA", tvaText) { tvaText = it }
        Text(
            if (validTva) {
                if (isArabic) "معامل السعر شامل الضريبة: ${"%.2f".format(1 + tva / 100)}" else "Coefficient TTC : ${"%.2f".format(1 + tva / 100)}"
            } else {
                if (isArabic) "أدخل نسبة بين 0 و100." else "Saisissez un taux entre 0 et 100."
            },
            color = if (validTva) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
        )
        Button(onClick = { scope.launch { dao.saveSetting(AppSettingEntity("tva_percent", tvaText)) } }, enabled = validTva && tvaLoaded) {
            Text(if (isArabic) "حفظ نسبة TVA" else "Enregistrer la TVA")
        }

        Divider()
        Text(if (isArabic) "إضافة وحدة قياس" else "Ajouter une unité", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = unitName, onValueChange = { unitName = it }, label = { Text(if (isArabic) "اسم الوحدة" else "Nom de l’unité") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = unitSymbol, onValueChange = { unitSymbol = it }, label = { Text(if (isArabic) "رمز الوحدة" else "Symbole de l’unité") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            scope.launch {
                dao.insertUnit(CustomUnitEntity(name = unitName.trim(), symbol = unitSymbol.trim()))
                unitName = ""
                unitSymbol = ""
            }
        }, enabled = unitName.isNotBlank() && unitSymbol.isNotBlank()) {
            Text(if (isArabic) "إضافة الوحدة" else "Ajouter l’unité")
        }
        if (units.isEmpty()) Text(if (isArabic) "لا توجد وحدات مخصصة بعد." else "Aucune unité personnalisée.")
        units.forEach { Text("• ${it.name} (${it.symbol})") }

        Divider()
        Text(if (isArabic) "أسعار المواد وسجل الأسعار" else "Prix des matériaux et historique", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = materialName, onValueChange = { materialName = it }, label = { Text(if (isArabic) "اسم المادة" else "Nom du matériau") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = materialUnit, onValueChange = { materialUnit = it }, label = { Text(if (isArabic) "وحدة التسعير" else "Unité de prix") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        NumberField(if (isArabic) "السعر بالدينار الجزائري (دج)" else "Prix en dinars algériens (DA)", materialPrice) { materialPrice = it }
        Button(onClick = {
            val price = materialPrice.toDoubleOrNull()
            if (price != null && price.isFinite() && price >= 0 && materialName.isNotBlank() && materialUnit.isNotBlank()) {
                scope.launch {
                    val name = materialName.trim()
                    val unit = materialUnit.trim()
                    val previous = dao.findMaterial(name)
                    dao.saveMaterial(MaterialEntity(
                        id = previous?.id ?: 0,
                        name = name,
                        currentPrice = price,
                        unit = unit,
                        updatedAt = System.currentTimeMillis()
                    ))
                    val saved = dao.findMaterial(name)
                    if (saved != null) dao.addPriceHistory(MaterialPriceHistoryEntity(
                        materialId = saved.id,
                        materialName = saved.name,
                        price = price,
                        unit = unit
                    ))
                    materialName = ""
                    materialPrice = ""
                }
            }
        }, enabled = materialName.isNotBlank() && materialUnit.isNotBlank() && (materialPrice.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true)) {
            Text(if (isArabic) "حفظ / تحديث السعر" else "Enregistrer / actualiser le prix")
        }

        Text(if (isArabic) "المواد المسجلة والأسعار الحالية" else "Matériaux et prix actuels", fontWeight = FontWeight.SemiBold)
        if (materials.isEmpty()) Text(if (isArabic) "لا توجد مواد مسجلة بعد." else "Aucun matériau enregistré.")
        materials.forEach { Text("• ${it.name}: ${it.currentPrice} DA / ${it.unit}") }

        Text(if (isArabic) "سجل تغيّر الأسعار" else "Historique des prix", fontWeight = FontWeight.SemiBold)
        if (history.isEmpty()) Text(if (isArabic) "لا يوجد سجل أسعار بعد." else "Aucun historique pour le moment.")
        history.forEach { entry ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ROOT).format(java.util.Date(entry.recordedAt))
            Text("• ${entry.materialName}: ${entry.price} DA / ${entry.unit} — $date")
        }
        Divider()
        Text(if (isArabic) "أسعار اليد العاملة حسب المهنة" else "Tarifs de main-d’œuvre par métier", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = laborTrade, onValueChange = { laborTrade = it }, label = { Text(if (isArabic) "المهنة" else "Métier") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = laborUnit, onValueChange = { laborUnit = it }, label = { Text(if (isArabic) "وحدة الحساب: يوم أو ساعة أو وحدة" else "Unité : jour, heure ou unité") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        NumberField(if (isArabic) "السعر بالدينار الجزائري" else "Tarif en DA", laborPrice) { laborPrice = it }
        Button(onClick = {
            val price = laborPrice.toDoubleOrNull()
            if (price != null && price.isFinite() && price >= 0 && laborTrade.isNotBlank() && laborUnit.isNotBlank()) scope.launch {
                val oldRate = dao.findLaborRate(laborTrade.trim())
                dao.saveLaborRate(LaborRateEntity(id = oldRate?.id ?: 0, tradeName = laborTrade.trim(), unit = laborUnit.trim(), unitPrice = price))
                laborTrade = ""
                laborPrice = ""
            }
        }, enabled = laborTrade.isNotBlank() && laborUnit.isNotBlank() && (laborPrice.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true)) {
            Text(if (isArabic) "حفظ / تحديث سعر المهنة" else "Enregistrer / actualiser le tarif")
        }
        if (laborRates.isEmpty()) Text(if (isArabic) "لا توجد أسعار مهن مسجلة." else "Aucun tarif enregistré.")
        laborRates.forEach { rate -> Text("• " + rate.tradeName + ": " + rate.unitPrice + " دج / " + rate.unit) }

        Text(
            if (isArabic) "تُحفظ هذه البيانات محلياً على الهاتف دون الحاجة إلى الإنترنت. أصبحت بنود تكلفة المواد قابلة للإضافة من داخل أقسام الأشغال، وسيأتي ربطها بكميات الحاسبات ومشاريع Devis في المراحل التالية." else "Ces données sont enregistrées localement sur le téléphone, sans Internet. Les lignes de coûts des matériaux peuvent maintenant être ajoutées depuis les rubriques. Leur liaison automatique aux quantités calculées et aux devis viendra ensuite.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun ProjectsScreen(isArabic: Boolean, onProjectSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val database = remember { BtpDatabase.getInstance(context) }
    val dao = remember { database.appDataDao() }
    val scope = rememberCoroutineScope()
    val projects by dao.observeProjects().collectAsState(initial = emptyList())
    var projectName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    val selectedProject = projects.firstOrNull { it.id == selectedProjectId }
    val devisFlow = remember(selectedProjectId) {
        selectedProjectId?.let { dao.observeDevis(it) } ?: flowOf(emptyList())
    }
    val devisList by devisFlow.collectAsState(initial = emptyList())
    var selectedDevisId by remember { mutableStateOf<Long?>(null) }
    var devisMessage by remember { mutableStateOf<String?>(null) }

    val selectedLinesFlow = remember(selectedDevisId) {
        selectedDevisId?.let { dao.observeDevisLines(it) } ?: flowOf(emptyList())
    }
    val selectedLines by selectedLinesFlow.collectAsState(initial = emptyList())
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val selectedDocument = devisList.firstOrNull { it.id == selectedDevisId }
    val reportTitle = selectedDocument?.let { "${it.projectNameSnapshot} - Devis ${it.id}" } ?: "Btp Pro Dz"
    val reportRows = remember(selectedLines, selectedDocument, isArabic) {
        buildList {
            selectedLines.forEach { line ->
                add(ProjectReportRow(line.description, line.quantity, line.unit, line.unitPrice, line.total))
            }
            selectedDocument?.let { document ->
                add(ProjectReportRow(if (isArabic) "المجموع HT" else "Total HT", 1.0, "DA", document.totalHt, document.totalHt))
                add(ProjectReportRow(if (isArabic) "TVA ${document.tvaPercent}%" else "TVA ${document.tvaPercent}%", 1.0, "DA", document.tvaAmount, document.tvaAmount))
                add(ProjectReportRow(if (isArabic) "المجموع TTC" else "Total TTC", 1.0, "DA", document.totalTtc, document.totalTtc))
            }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) scope.launch {
            exportMessage = runCatching {
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri) ?: error("Cannot open PDF destination.")
                    stream.use { ProjectReportExporter.writePdf(reportTitle, reportRows, it) }
                }
                if (isArabic) "تم حفظ PDF بنجاح." else "PDF export saved."
            }.getOrElse { (if (isArabic) "فشل تصدير PDF: " else "PDF export failed: ") + (it.message ?: "Unknown error") }
        }
    }
    val excelLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) scope.launch {
            exportMessage = runCatching {
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri) ?: error("Cannot open Excel destination.")
                    stream.use { ProjectReportExporter.writeExcel(reportTitle, reportRows, it) }
                }
                if (isArabic) "تم حفظ Excel بنجاح." else "Excel export saved."
            }.getOrElse { (if (isArabic) "فشل تصدير Excel: " else "Excel export failed: ") + (it.message ?: "Unknown error") }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) "المشاريع والـ Devis" else "Projets et devis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (isArabic) "أنشئ مشروعاً، ثم أنشئ وثيقة Devis تحفظ نسخة ثابتة من التكاليف والضريبة." else "Créez un projet puis un devis avec un instantané des coûts et de la TVA.")
        OutlinedTextField(value = projectName, onValueChange = { projectName = it }, label = { Text(if (isArabic) "اسم المشروع" else "Nom du projet") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text(if (isArabic) "اسم الزبون (اختياري)" else "Client (facultatif)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            if (projectName.isNotBlank()) scope.launch {
                val id = dao.insertProject(ProjectEntity(name = projectName.trim(), clientName = clientName.trim()))
                selectedProjectId = id
                selectedDevisId = null
                devisMessage = null
                onProjectSelected(id)
                projectName = ""
                clientName = ""
            }
        }, enabled = projectName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(if (isArabic) "إنشاء مشروع" else "Créer le projet")
        }
        Divider()
        Text(if (isArabic) "قائمة المشاريع" else "Liste des projets", fontWeight = FontWeight.SemiBold)
        if (projects.isEmpty()) Text(if (isArabic) "لا توجد مشاريع بعد." else "Aucun projet pour le moment.")
        projects.forEach { project ->
            val label = project.name + if (project.clientName.isNotBlank()) " — " + project.clientName else ""
            if (selectedProjectId == project.id) Button(onClick = { selectedProjectId = project.id; selectedDevisId = null; devisMessage = null; onProjectSelected(project.id) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
            else OutlinedButton(onClick = { selectedProjectId = project.id; onProjectSelected(project.id) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        if (selectedProject != null) {
            Divider()
            Text(if (isArabic) "المشروع المحدد: " + selectedProject.name else "Projet sélectionné : " + selectedProject.name, fontWeight = FontWeight.Bold)
            Text(if (isArabic) "اختر المشروع أولاً من هذه القائمة، ثم أضف تكاليف المواد واليد العاملة من أقسام الأشغال. لن تُنقل البنود القديمة غير المرتبطة تلقائياً حتى لا تختلط تكاليف المشاريع." else "Sélectionnez d’abord ce projet, puis ajoutez les coûts depuis les rubriques. Les anciennes lignes non associées ne sont pas déplacées automatiquement afin d’éviter tout mélange.")
            Button(onClick = {
                scope.launch {
                    database.withTransaction {
                    val materialLines = dao.materialCostLinesForProject(selectedProject.id)
                    val laborLines = dao.laborCostLinesForProject(selectedProject.id)
                    val rows = materialLines.map { line ->
                        DevisLineEntity(devisId = 0, section = line.section, description = line.materialName, quantity = line.quantity, unit = line.unit, unitPrice = line.unitPriceSnapshot, total = line.totalCost, category = "MATERIAL")
                    } + laborLines.map { line ->
                        DevisLineEntity(devisId = 0, section = line.section, description = line.tradeName, quantity = line.quantity, unit = line.unit, unitPrice = line.unitPriceSnapshot, total = line.totalCost, category = "LABOR")
                    }
                    if (rows.isEmpty()) {
                        devisMessage = if (isArabic) "لا يمكن إنشاء Devis فارغ. أضف تكاليف المواد أو اليد العاملة لهذا المشروع أولاً." else "Impossible de créer un devis vide. Ajoutez d’abord des coûts au projet."
                    } else {
                        val storedTva = dao.getSetting("tva_percent")?.value
                        val totals = runCatching {
                            val tva = storedTva?.toBigDecimalOrNull()
                                ?: throw IllegalArgumentException("TVA rate is missing or invalid.")
                            ProjectDevisCalculator.calculate(
                                materialLines = materialLines.map {
                                    DevisCostLine(it.quantity, it.unitPriceSnapshot, it.totalCost)
                                },
                                laborLines = laborLines.map {
                                    DevisCostLine(it.quantity, it.unitPriceSnapshot, it.totalCost)
                                },
                                tvaPercent = tva
                            )
                        }.getOrNull()
                        if (totals == null) {
                            devisMessage = if (isArabic) "تعذر إنشاء Devis: توجد كميات أو أسعار غير صالحة. راجع البنود ونسبة TVA." else "Devis impossible : quantités, prix ou TVA invalides."
                        } else {
                            val documentId = dao.insertDevis(
                                DevisDocumentEntity(
                                    projectId = selectedProject.id,
                                    projectNameSnapshot = selectedProject.name,
                                    clientNameSnapshot = selectedProject.clientName,
                                    totalMaterials = totals.materialsHt.toDouble(),
                                    totalLabor = totals.laborHt.toDouble(),
                                    totalHt = totals.totalHt.toDouble(),
                                    tvaPercent = totals.tvaPercent.toDouble(),
                                    tvaAmount = totals.tvaAmount.toDouble(),
                                    totalTtc = totals.totalTtc.toDouble()
                                )
                            )
                            dao.insertDevisLines(rows.map { it.copy(devisId = documentId) })
                            selectedDevisId = documentId
                            devisMessage = if (isArabic) "تم إنشاء وثيقة Devis وحفظ تفاصيلها." else "Le devis et ses lignes ont été enregistrés."
                        }
                    }
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isArabic) "إنشاء Devis من التكاليف المسجلة" else "Créer un devis à partir des coûts")
            }
            devisMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Text(if (isArabic) "الوثائق المحفوظة" else "Documents enregistrés", fontWeight = FontWeight.SemiBold)
            if (devisList.isEmpty()) Text(if (isArabic) "لم تُنشأ وثائق لهذا المشروع بعد." else "Aucun devis pour ce projet.")
            devisList.forEach { doc ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (isArabic) "Devis رقم " + doc.id else "Devis n° " + doc.id, fontWeight = FontWeight.Bold)
                        Text(if (isArabic) "المواد: " + "%.2f".format(doc.totalMaterials) + " دج" else "Matériaux : " + "%.2f".format(doc.totalMaterials) + " DA")
                        Text(if (isArabic) "اليد العاملة: " + "%.2f".format(doc.totalLabor) + " دج" else "Main-d’œuvre : " + "%.2f".format(doc.totalLabor) + " DA")
                        Text("HT : " + "%.2f".format(doc.totalHt) + " DA")
                        Text("TVA (" + doc.tvaPercent + "%) : " + "%.2f".format(doc.tvaAmount) + " DA")
                        Text("TTC : " + "%.2f".format(doc.totalTtc) + " DA", fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { selectedDevisId = if (selectedDevisId == doc.id) null else doc.id }) {
                            Text(if (selectedDevisId == doc.id) { if (isArabic) "إخفاء البنود" else "Masquer les lignes" } else { if (isArabic) "عرض تفاصيل البنود" else "Voir les lignes" })
                        }
                    }
                }
            }
            if (selectedDevisId != null) {
                Divider()
                Text(if (isArabic) "تفاصيل وثيقة Devis" else "Détail du devis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { pdfLauncher.launch("BtpProDz_Devis_${selectedDevisId}.pdf") }, enabled = selectedLines.isNotEmpty()) {
                        Text(if (isArabic) "تصدير PDF" else "Exporter PDF")
                    }
                    OutlinedButton(onClick = { excelLauncher.launch("BtpProDz_Devis_${selectedDevisId}.xlsx") }, enabled = selectedLines.isNotEmpty()) {
                        Text(if (isArabic) "تصدير Excel" else "Exporter Excel")
                    }
                }
                exportMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                if (selectedLines.isEmpty()) Text(if (isArabic) "لا توجد بنود محفوظة في هذه الوثيقة." else "Aucune ligne enregistrée dans ce devis.")
                selectedLines.forEach { line ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text((if (line.category == "LABOR") { if (isArabic) "اليد العاملة" else "Main-d’œuvre" } else { if (isArabic) "المواد" else "Matériaux" }) + " — " + line.section, fontWeight = FontWeight.SemiBold)
                            Text(line.description)
                            Text(line.quantity.toString() + " " + line.unit + " × " + "%.2f".format(line.unitPrice) + " = " + "%.2f".format(line.total) + " DA")
                        }
                    }
                }
            }
        }
        Text(if (isArabic) "تُحفظ تفاصيل كل Devis بصورة مستقلة؛ التعديلات اللاحقة على أسعار المواد أو أجور العمال لا تغيّر الوثائق السابقة." else "Chaque devis conserve ses propres lignes ; les modifications ultérieures des tarifs ne changent pas les documents existants.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> if (input.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(input.replace(',', '.')) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
