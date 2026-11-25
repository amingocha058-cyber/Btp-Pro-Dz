package dz.btpprodz

import android.os.Bundle
import android.app.Activity
import android.net.Uri
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import dz.btpprodz.calculations.concrete.ConcreteMaterialCalculator
import dz.btpprodz.calculations.concrete.ConcreteDosage
import dz.btpprodz.calculations.concrete.ConcreteMaterialInput
import dz.btpprodz.calculations.concrete.ConcreteVolumeCalculator
import dz.btpprodz.calculations.concrete.ConcreteVolumeInput
import dz.btpprodz.domain.calculations.DevisCostLine
import dz.btpprodz.domain.calculations.FormworkCalculator
import dz.btpprodz.domain.calculations.RebarCalculator
import dz.btpprodz.domain.calculations.ProjectDevisCalculator
import dz.btpprodz.domain.calculations.EarthworkCalculator
import dz.btpprodz.domain.calculations.FoundationVolumeCalculator
import dz.btpprodz.domain.calculations.LinearElementVolumeCalculator
import dz.btpprodz.domain.calculations.SurfaceAreaCalculator
import dz.btpprodz.domain.calculations.SlabVolumeCalculator
import dz.btpprodz.domain.calculations.StairVolumeCalculator
import dz.btpprodz.export.ProjectReportExporter
import dz.btpprodz.export.ProjectReportRow
import dz.btpprodz.export.ProjectReportSummary
import java.math.BigDecimal
import dz.btpprodz.data.AppSettingEntity
import dz.btpprodz.data.BtpDatabase
import dz.btpprodz.data.DatabaseBackupManager
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

private data class WorkSection(
    val ar: String,
    val fr: String,
    val descriptionAr: String,
    val descriptionFr: String,
    val icon: String
)

private val workSections = listOf(
    WorkSection("الخرسانة", "Béton", "حساب حجم الخرسانة وكميات مكوناتها", "Volume de béton et quantités de ses composants", "🧱"),
    WorkSection("قواعد منفردة", "Semelles isolées", "حساب حجم القواعد المنفردة", "Volume des semelles isolées", "🏗️"),
    WorkSection("قواعد شريطية", "Semelles filantes", "حساب حجم القواعد الشريطية", "Volume des semelles filantes", "🏗️"),
    WorkSection("السملات والميدات", "Longrines", "حساب حجم السملات والميدات", "Volume des longrines", "🏗️"),
    WorkSection("الأعمدة", "Poteaux", "حساب حجم خرسانة الأعمدة", "Volume de béton des poteaux", "🏛️"),
    WorkSection("الجسور", "Poutres", "حساب حجم خرسانة الجسور", "Volume de béton des poutres", "🏛️"),
    WorkSection("الجدران والبناء", "Maçonnerie", "مساحات الجدران وخصم الفتحات", "Surfaces des murs et ouvertures", "🧱"),
    WorkSection("السقوف والبلاطات", "Planchers et dalles", "مساحة البلاطة وحجم الخرسانة", "Surface et volume des dalles", "▰"),
    WorkSection("التلبيس والطلاء", "Enduit et peinture", "مساحات التلبيس والطلاء", "Surfaces d’enduit et de peinture", "🖌️"),
    WorkSection("الأرضيات والتبليط", "Revêtements", "مساحة الأرضية وعدد البلاطات", "Surface du sol et nombre de carreaux", "🔲"),
    WorkSection("القوالب", "Coffrage", "مساحة ألواح قوالب العناصر", "Surface de coffrage des éléments", "🪚"),
    WorkSection("الحديد", "Ferraillage", "وزن قضبان التسليح", "Poids des barres d’armature", "🔩"),
    WorkSection("الشبكات", "Réseaux", "أطوال الأنابيب أو الكابلات", "Longueurs des conduites ou câbles", "🔌"),
    WorkSection("التشطيبات", "Finitions", "مساحات التشطيبات وخصم الفتحات", "Surfaces de finition et ouvertures", "🎨"),
    WorkSection("الحفر والردم", "Terrassement", "حساب أحجام الحفر والردم", "Volumes de déblais et remblais", "🚜"),
    WorkSection("السلالم", "Escaliers", "أبعاد وحجم خرسانة السلالم", "Dimensions et volume des escaliers", "🪜"),
    WorkSection("اليد العاملة", "Main-d’œuvre", "حساب تكلفة العمال حسب الوحدات", "Coût de la main-d’œuvre par unité", "👷")
)

@Composable
private fun BtpProDzApp() {
    var isArabic by remember { mutableStateOf(true) }
    var calculatorSearch by remember { mutableStateOf("") }
    var showConcreteCalculator by remember { mutableStateOf(false) }
    var selectedFoundationType by remember { mutableStateOf<Int?>(null) }
    var showEarthworksCalculator by remember { mutableStateOf(false) }
    var selectedColumnBeamType by remember { mutableStateOf<Int?>(null) }
    var selectedOtherSection by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var activeProjectId by remember { mutableStateOf<Long?>(null) }

    val detailOpen = showConcreteCalculator || selectedFoundationType != null ||
        showEarthworksCalculator || selectedColumnBeamType != null ||
        selectedOtherSection != null || showSettings || showProjects

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp).then(
                    if (detailOpen) Modifier.verticalScroll(rememberScrollState()) else Modifier
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                Spacer(modifier = Modifier.height(12.dp))
                if (detailOpen) {
                    OutlinedButton(
                        onClick = {
                            showConcreteCalculator = false
                            selectedFoundationType = null
                            showEarthworksCalculator = false
                            selectedColumnBeamType = null
                            selectedOtherSection = null
                            showSettings = false
                            showProjects = false
                        }
                    ) {
                        Text(if (isArabic) "رجوع إلى الحاسبات" else "Retour aux calculateurs")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when {
                        showConcreteCalculator -> {
                            ConcreteCalculator(isArabic = isArabic, projectId = activeProjectId)
                            CostEstimatorSection(section = "الخرسانة", isArabic = isArabic, projectId = activeProjectId)
                            LaborCostSection(section = "الخرسانة", isArabic = isArabic, projectId = activeProjectId)
                        }
                        selectedFoundationType != null -> {
                            val foundationSection = listOf("قواعد منفردة", "قواعد شريطية", "السملات والميدات")[selectedFoundationType!!]
                            FoundationCalculator(isArabic = isArabic, projectId = activeProjectId, foundationType = selectedFoundationType!!)
                            CostEstimatorSection(section = foundationSection, isArabic = isArabic, projectId = activeProjectId)
                            LaborCostSection(section = foundationSection, isArabic = isArabic, projectId = activeProjectId)
                        }
                        showEarthworksCalculator -> {
                            EarthworksCalculator(isArabic = isArabic, projectId = activeProjectId)
                            CostEstimatorSection(section = "الحفر والردم", isArabic = isArabic, projectId = activeProjectId)
                            LaborCostSection(section = "الحفر والردم", isArabic = isArabic, projectId = activeProjectId)
                        }
                        selectedColumnBeamType != null -> {
                            val columnBeamSection = if (selectedColumnBeamType == 0) "الأعمدة" else "الجسور"
                            ColumnBeamCalculator(isArabic = isArabic, projectId = activeProjectId, elementType = selectedColumnBeamType!!)
                            CostEstimatorSection(section = columnBeamSection, isArabic = isArabic, projectId = activeProjectId)
                            LaborCostSection(section = columnBeamSection, isArabic = isArabic, projectId = activeProjectId)
                        }
                        selectedOtherSection != null -> {
                            OtherWorkCalculator(section = selectedOtherSection!!, isArabic = isArabic, projectId = activeProjectId)
                            if (selectedOtherSection != "اليد العاملة") {
                                CostEstimatorSection(section = selectedOtherSection!!, isArabic = isArabic, projectId = activeProjectId)
                                LaborCostSection(section = selectedOtherSection!!, isArabic = isArabic, projectId = activeProjectId)
                            }
                        }
                        showSettings -> SettingsScreen(isArabic = isArabic)
                        showProjects -> ProjectsScreen(isArabic = isArabic, onProjectSelected = { activeProjectId = it })
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showSettings = true }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "الإعدادات" else "Paramètres")
                        }
                        Button(onClick = { showProjects = true }, modifier = Modifier.weight(1f)) {
                            Text(if (isArabic) "المشاريع و Devis" else "Projets et devis")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isArabic) "اختر الحاسبة" else "Choisissez un calculateur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = calculatorSearch,
                        onValueChange = { calculatorSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(if (isArabic) "ابحث عن حاسبة" else "Rechercher un calculateur") },
                        placeholder = { Text(if (isArabic) "مثال: خرسانة، حديد، طلاء..." else "Ex. béton, ferraillage, peinture...") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val query = calculatorSearch.trim()
                    val filteredSections = workSections.filter { section ->
                        query.isBlank() || listOf(section.ar, section.fr, section.descriptionAr, section.descriptionFr)
                            .any { it.contains(query, ignoreCase = true) }
                    }
                    if (filteredSections.isEmpty()) {
                        Text(
                            if (isArabic) "لا توجد حاسبة مطابقة. جرّب كلمة أخرى." else "Aucun calculateur correspondant. Essayez un autre terme.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredSections, key = { it.ar }) { section ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .testTag("work-section-${section.ar}")
                                        .clickable {
                                            when (section.ar) {
                                                "الخرسانة" -> showConcreteCalculator = true
                                                "قواعد منفردة" -> selectedFoundationType = 0
                                                "قواعد شريطية" -> selectedFoundationType = 1
                                                "السملات والميدات" -> selectedFoundationType = 2
                                                "الأعمدة" -> selectedColumnBeamType = 0
                                                "الجسور" -> selectedColumnBeamType = 1
                                                "الحفر والردم" -> showEarthworksCalculator = true
                                                else -> selectedOtherSection = section.ar
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(section.icon, fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            if (isArabic) section.ar else section.fr,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isArabic) section.descriptionAr else section.descriptionFr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
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
private fun CalculationExplanation(
    isArabic: Boolean,
    formula: String,
    steps: String,
    unit: String,
    note: String
) {
    var expanded by remember(formula, steps) { mutableStateOf(true) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isArabic) "كيف تم الحساب؟" else "Détail du calcul", fontWeight = FontWeight.Bold)
                Text(if (expanded) if (isArabic) "إخفاء" else "Masquer" else if (isArabic) "عرض" else "Afficher",
                    modifier = Modifier.clickable { expanded = !expanded }, color = MaterialTheme.colorScheme.primary)
            }
            if (expanded) {
                Text((if (isArabic) "المعادلة: " else "Formule : ") + formula, fontWeight = FontWeight.SemiBold)
                Text(if (isArabic) "خطوات الحساب:" else "Étapes du calcul :", fontWeight = FontWeight.Medium)
                Text(steps)
                Text((if (isArabic) "الوحدة: " else "Unité : ") + unit)
                Divider()
                Text((if (isArabic) "ملاحظة: " else "Note : ") + note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ConcreteCalculator(isArabic: Boolean, projectId: Long?) {
    var quantityText by remember { mutableStateOf("") }
    var lengthText by remember { mutableStateOf("") }
    var widthText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var cementDosageText by remember { mutableStateOf("") }
    var sandDosageText by remember { mutableStateOf("") }
    var gravelDosageText by remember { mutableStateOf("") }
    var wasteText by remember { mutableStateOf("0") }

    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val length = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val width = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val height = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
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
    val cementDosage = cementDosageText.replace(',', '.').toDoubleOrNull()
    val sandDosage = sandDosageText.replace(',', '.').toDoubleOrNull()
    val gravelDosage = gravelDosageText.replace(',', '.').toDoubleOrNull()
    val waste = wasteText.replace(',', '.').toDoubleOrNull()
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

    val dimensionLabels = if (isArabic)
        listOf("الطول (م)", "العرض (م)", "الارتفاع / السماكة (م)")
    else listOf("Longueur (m)", "Largeur (m)", "Hauteur / épaisseur (m)")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) "حساب عدد وحجم العناصر الخرسانية" else "Nombre et volume des éléments en béton", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        NumberField(if (isArabic) "عدد العناصر" else "Nombre d’éléments", quantityText) { quantityText = it }
        NumberField(dimensionLabels[0], lengthText) { lengthText = it }
        NumberField(dimensionLabels[1], widthText) { widthText = it }
        NumberField(dimensionLabels[2], heightText) { heightText = it }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "عدد العناصر: ${if (valid) quantity.toInt().toString() else "—"}" else "Nombre d’éléments : ${if (valid) quantity.toInt().toString() else "—"}")
                Text(if (isArabic) "حجم العنصر الواحد: ${if (valid) "%.3f".format(perElementVolume) else "—"} م³" else "Volume unitaire : ${if (valid) "%.3f".format(perElementVolume) else "—"} m³")
                Text(if (isArabic) "الحجم الإجمالي: ${if (valid) "%.3f".format(totalVolume) else "—"} م³" else "Volume total : ${if (valid) "%.3f".format(totalVolume) else "—"} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!valid) Text(if (isArabic) "أدخل أعداداً وأبعاداً أكبر من الصفر لعرض نتيجة صحيحة." else "Saisissez des quantités et dimensions supérieures à zéro.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

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
        } else if (cementDosageText.isNotBlank() || sandDosageText.isNotBlank() || gravelDosageText.isNotBlank() || wasteText.isNotBlank()) {
            Text(if (isArabic) "تحقق من إدخال الجرعات الثلاث ونسبة الهدر ضمن المجال الصحيح." else "Vérifiez les trois dosages et le pourcentage de pertes.")
        }

        CalculatedQuantityCostEntry("الخرسانة", projectId, if (valid) totalVolume else 0.0, "م³", isArabic)
        Text(if (isArabic) "الحساب: عدد العناصر × الطول × العرض × الارتفاع أو السماكة" else "Calcul : nombre × longueur × largeur × hauteur ou épaisseur", style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
private fun FoundationCalculator(isArabic: Boolean, projectId: Long?, foundationType: Int) {
    var quantityText by remember { mutableStateOf("4") }
    var lengthText by remember { mutableStateOf("1.20") }
    var widthText by remember { mutableStateOf("1.20") }
    var depthText by remember { mutableStateOf("0.40") }

    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val length = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val width = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val depth = depthText.replace(',', '.').toDoubleOrNull() ?: 0.0
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
    val selectedTypeAr = typesAr[foundationType]
    val selectedTypeFr = typesFr[foundationType]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (isArabic) "حاسبة $selectedTypeAr" else "Calculateur : $selectedTypeFr",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
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
        CalculatedQuantityCostEntry("الأساسات", projectId, if (valid) totalVolume else 0.0, "م³", isArabic)
        Text(
            if (isArabic) "الحساب: العدد × الطول × العرض × الارتفاع. هذه حاسبة حجم أولية، ويجب إدخال الأبعاد حسب شكل العنصر." else "Calcul : quantité × longueur × largeur × hauteur. Calcul volumique simple, selon les dimensions de l’élément.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun EarthworksCalculator(isArabic: Boolean, projectId: Long?) {
    var operation by remember { mutableStateOf(0) }
    var quantityText by remember { mutableStateOf("1") }
    var lengthText by remember { mutableStateOf("10") }
    var widthText by remember { mutableStateOf("5") }
    var depthText by remember { mutableStateOf("1") }
    var occupiedVolumeText by remember { mutableStateOf("0") }

    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val length = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val width = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val depth = depthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val occupiedVolume = occupiedVolumeText.replace(',', '.').toBigDecimalOrNull()
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
        CalculatedQuantityCostEntry(if (operation == 0) "الحفر والردم - حفر" else "الحفر والردم - ردم", projectId, if (valid) totalVolume else 0.0, "م³", isArabic)
        Text(
            if (isArabic) "الحفر = الطول × العرض × العمق. الردم الصافي = حجم الحفر − حجم المنشأ المشغول لكل منطقة. لا يشمل الحساب انتفاش التربة أو الدمك." else "Déblai = longueur × largeur × profondeur. Remblai net = volume excavé − volume occupé par la structure. Hors foisonnement et compactage.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun ColumnBeamCalculator(isArabic: Boolean, projectId: Long?, elementType: Int) {
    var quantityText by remember { mutableStateOf("4") }
    var lengthText by remember { mutableStateOf("3.00") }
    var widthText by remember { mutableStateOf("0.25") }
    var heightText by remember { mutableStateOf("0.40") }

    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val length = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val width = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val height = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val elementLabelAr = if (elementType == 0) "الأعمدة" else "الجسور"
    val elementLabelFr = if (elementType == 0) "Poteaux" else "Poutres"
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
            if (isArabic) "حاسبة $elementLabelAr" else "Calculateur : $elementLabelFr",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
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
        CalculatedQuantityCostEntry(elementLabelAr, projectId, if (valid) totalVolume else 0.0, "م³", isArabic)
        Text(
            if (isArabic) "الحساب: عدد العناصر × الطول × عرض المقطع × ارتفاع المقطع. هذه حاسبة حجم الخرسانة فقط وليست حساباً إنشائياً للتسليح أو المقاومة." else "Calcul : nombre × longueur × largeur de section × hauteur de section. Volume de béton uniquement, sans dimensionnement structurel ni calcul des armatures.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun OtherWorkCalculator(section: String, isArabic: Boolean, projectId: Long?) {
    var countText by remember(section) { mutableStateOf("1") }
    var lengthText by remember(section) { mutableStateOf("5") }
    var widthText by remember(section) { mutableStateOf(if (section == "السلالم") "1.2" else "3") }
    var heightText by remember(section) { mutableStateOf(if (section == "السلالم" || section == "السقوف والبلاطات") "0.15" else "3") }
    var openingAreaText by remember(section) { mutableStateOf("0") }
    var unitPriceText by remember(section) { mutableStateOf("0") }
    var tileLengthText by remember(section) { mutableStateOf("0.60") }
    var tileWidthText by remember(section) { mutableStateOf("0.60") }
    var stairStepCountText by remember(section) { mutableStateOf("10") }
    var stairTreadText by remember(section) { mutableStateOf("0.30") }
    var stairRiserText by remember(section) { mutableStateOf("0.17") }
    var steelDiameterText by remember(section) { mutableStateOf("12") }

    val count = countText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val length = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val width = widthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val height = heightText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val openings = openingAreaText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val tileLength = tileLengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val tileWidth = tileWidthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val stairStepCount = stairStepCountText.toIntOrNull() ?: 0
    val stairTread = stairTreadText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val stairRiser = stairRiserText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val steelDiameterMm = steelDiameterText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val isWall = section == "الجدران والبناء"
    val isRoof = section == "السقوف والبلاطات"
    val isFinish = section == "التلبيس والطلاء"
    val isGeneralFinishes = section == "التشطيبات"
    val isFloor = section == "الأرضيات والتبليط"
    val isStairs = section == "السلالم"
    val isLabor = section == "اليد العاملة"
    val isFormwork = section == "القوالب"
    val isSteel = section == "الحديد"
    val isNetworks = section == "الشبكات"

    // Dedicated quantity modules: column side shutters and straight longitudinal bars.
    val formworkArea = runCatching {
        FormworkCalculator.columns(
            count.toInt(),
            BigDecimal.valueOf(length),
            BigDecimal.valueOf(width),
            BigDecimal.valueOf(height)
        ).totalAreaM2.toDouble()
    }.getOrDefault(-1.0)
    val steelWeightKg = runCatching {
        RebarCalculator.straightBars(
            count.toInt(),
            BigDecimal.valueOf(length),
            BigDecimal.valueOf(steelDiameterMm)
        ).totalMassKg.toDouble()
    }.getOrDefault(-1.0)
    val networkLengthM = count * length
    val laborTotal = count * unitPrice

    val area = when {
        isFormwork -> formworkArea
        isWall || isFinish || isGeneralFinishes -> runCatching {
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
    val inputsFinite = listOf(count, length, width, height, openings, unitPrice, tileLength, tileWidth, stairTread, stairRiser, steelDiameterMm).all { it.isFinite() }
    val outputsFinite = area.isFinite() && volume.isFinite() && tileCount.isFinite() && steelWeightKg.isFinite() && networkLengthM.isFinite() && laborTotal.isFinite()
    val integerCountRequired = !isLabor
    val valid = inputsFinite && outputsFinite && count > 0.0 &&
        (!integerCountRequired || (count % 1.0 == 0.0 && count <= Int.MAX_VALUE)) &&
        (isLabor || isNetworks || isSteel || (length > 0.0 && width > 0.0 && height > 0.0)) &&
        (isLabor || isNetworks || isSteel || openings >= 0.0) &&
        (!isFloor || (tileLength > 0.0 && tileWidth > 0.0)) &&
        (!isSteel || (length > 0.0 && steelDiameterMm > 0.0)) &&
        (!isNetworks || length > 0.0) &&
        (!isLabor || unitPrice >= 0.0) &&
        (!(isWall || isFinish || isGeneralFinishes || isFloor || isRoof || isStairs || isFormwork) || area >= 0.0) &&
        (!(isRoof || isStairs) || volume >= 0.0) &&
        (!isStairs || (stairStepCount > 0 && stairTread > 0.0 && stairRiser > 0.0))

    val titleFr = when(section) {
        "الجدران والبناء" -> "Maçonnerie"
        "السقوف والبلاطات" -> "Planchers et dalles"
        "التلبيس والطلاء" -> "Enduit et peinture"
        "الأرضيات والتبليط" -> "Revêtements"
        "القوالب" -> "Coffrage"
        "الحديد" -> "Ferraillage"
        "الشبكات" -> "Réseaux"
        "التشطيبات" -> "Finitions"
        "السلالم" -> "Escaliers"
        else -> "Main-d’œuvre"
    }

    val calculatedQuantity = when {
        !valid -> 0.0
        isSteel -> steelWeightKg
        isNetworks -> networkLengthM
        isRoof || isStairs -> volume
        isWall || isFinish || isGeneralFinishes || isFloor || isFormwork -> area
        else -> 0.0
    }
    val calculatedUnit = when {
        isSteel -> "كغ"
        isNetworks -> "م"
        isRoof || isStairs -> "م³"
        else -> "م²"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) section else titleFr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (!isLabor) {
            NumberField(
                if (isArabic) when {
                    isSteel -> "عدد قضبان الحديد"
                    isNetworks -> "عدد المسارات / الخطوط"
                    isFormwork -> "عدد العناصر"
                    else -> "عدد العناصر / الغرف / المساحات"
                } else when {
                    isSteel -> "Nombre de barres"
                    isNetworks -> "Nombre de parcours / lignes"
                    isFormwork -> "Nombre d’éléments"
                    else -> "Nombre d’éléments / pièces / surfaces"
                },
                countText
            ) { countText = it }
        }

        if (isLabor) {
            NumberField(if (isArabic) "عدد وحدات العمل / الأيام / الساعات" else "Nombre d’unités / jours / heures", countText) { countText = it }
            NumberField(if (isArabic) "سعر الوحدة (دج)" else "Prix unitaire (DA)", unitPriceText) { unitPriceText = it }
        } else {
            NumberField(if (isArabic) when { isSteel -> "طول القضيب الواحد (م)"; isNetworks -> "طول المسار (م)"; isStairs -> "طول الميل (م)"; isFormwork -> "عرض العمود A (م)"; else -> "الطول (م)" } else when { isSteel -> "Longueur d’une barre (m)"; isNetworks -> "Longueur du parcours (m)"; isStairs -> "Longueur inclinée (m)"; isFormwork -> "Côté A du poteau (m)"; else -> "Longueur (m)" }, lengthText) { lengthText = it }
            if (isSteel) {
                NumberField(if (isArabic) "قطر القضيب (مم)" else "Diamètre de la barre (mm)", steelDiameterText) { steelDiameterText = it }
            } else if (isFormwork) {
                NumberField(if (isArabic) "عرض العمود B (م)" else "Côté B du poteau (m)", widthText) { widthText = it }
                NumberField(if (isArabic) "ارتفاع العمود (م)" else "Hauteur du poteau (m)", heightText) { heightText = it }
            } else if (isNetworks) {
                Text(if (isArabic) "أدخل الطول الإجمالي لكل مسار؛ نوع الشبكة يحدد في وصف بند التكلفة." else "Saisissez la longueur de chaque parcours ; précisez le type de réseau dans le coût.")
            } else {
                NumberField(if (isArabic) if (isWall || isFinish || isGeneralFinishes) "الارتفاع (م)" else "العرض (م)" else if (isWall || isFinish || isGeneralFinishes) "Hauteur (m)" else "Largeur (m)", widthText) { widthText = it }
                if (isWall || isFinish || isGeneralFinishes) {
                    NumberField(if (isArabic) "المساحة الإجمالية للفتحات (م²)" else "Surface totale des ouvertures (m²)", openingAreaText) { openingAreaText = it }
                }
                if (isRoof || isStairs) {
                    NumberField(if (isArabic) if (isRoof) "السماكة (م)" else "سماكة بطن السلم (م)" else if (isRoof) "Épaisseur (m)" else "Épaisseur de paillasse (m)", heightText) { heightText = it }
                }
                if (isStairs) {
                    NumberField(if (isArabic) "عدد الدرجات" else "Nombre de marches", stairStepCountText) { stairStepCountText = it }
                    NumberField(if (isArabic) "عمق الدرجة (م)" else "Giron (m)", stairTreadText) { stairTreadText = it }
                    NumberField(if (isArabic) "ارتفاع الدرجة (م)" else "Hauteur de marche (m)", stairRiserText) { stairRiserText = it }
                }
                if (isFloor) {
                    NumberField(if (isArabic) "طول البلاطة الواحدة (م)" else "Longueur d’un carreau (m)", tileLengthText) { tileLengthText = it }
                    NumberField(if (isArabic) "عرض البلاطة الواحدة (م)" else "Largeur d’un carreau (m)", tileWidthText) { tileWidthText = it }
                }
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
                    isSteel -> Text(if (isArabic) "وزن الحديد التقريبي: ${if (valid) "%.2f".format(steelWeightKg) else "—"} كغ" else "Poids estimé : ${if (valid) "%.2f".format(steelWeightKg) else "—"} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    isNetworks -> Text(if (isArabic) "الطول الإجمالي للشبكة: ${if (valid) "%.2f".format(networkLengthM) else "—"} م" else "Longueur totale du réseau : ${if (valid) "%.2f".format(networkLengthM) else "—"} m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    isFormwork -> Text(if (isArabic) "مساحة القوالب التقديرية: ${if (valid) "%.3f".format(area) else "—"} م²" else "Surface estimée du coffrage : ${if (valid) "%.3f".format(area) else "—"} m²", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    isStairs -> Text(if (isArabic) "حجم خرسانة السلم التقديري: ${if (valid) "%.3f".format(volume) else "—"} م³" else "Volume estimé de béton de l’escalier : ${if (valid) "%.3f".format(volume) else "—"} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    if (isArabic) "راجع المدخلات: تأكد من أن الأعداد والأبعاد والفتحات وقطر الحديد صالحة." else "Vérifiez les quantités, dimensions, ouvertures et diamètre des barres.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (!isLabor) CalculatedQuantityCostEntry(section, projectId, calculatedQuantity, calculatedUnit, isArabic)
        Text(
            if (isArabic) when {
                isSteel -> "وزن تقريبي باستعمال d²/162 كغ لكل متر؛ لا يغني عن جدول الثني والتفاصيل الإنشائية."
                isFormwork -> "قوالب الأعمدة: محيط المقطع × ارتفاع العمود × العدد. لا يشمل القاعدة أو رأس العمود أو الهدر أو التدعيم."
                isNetworks -> "الكمية طولية تقديرية؛ حدّد نوع الشبكة ومواصفاتها في وصف بند التكلفة."
                else -> "هذه نتائج كميات أولية. لا تشمل نسب الهدر أو مواصفات المواد أو تفاصيل التنفيذ."
            } else when {
                isSteel -> "Poids indicatif selon d²/162 kg par mètre ; ne remplace pas le plan de façonnage."
                isFormwork -> "Poteaux : périmètre de section × hauteur × nombre. Hors base, tête, pertes et étaiement."
                isNetworks -> "Quantité linéaire estimative ; précisez le type et les spécifications du réseau."
                else -> "Estimations de quantités, hors pertes et détails d’exécution."
            },
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
    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
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
            "قواعد منفردة" -> "Semelles isolées"
            "قواعد شريطية" -> "Semelles filantes"
            "السملات والميدات" -> "Longrines"
            "الأعمدة" -> "Poteaux"
            "الجسور" -> "Poutres"
            "الجدران والبناء" -> "Maçonnerie"
            "السقوف والبلاطات" -> "Planchers et dalles"
            "التلبيس والطلاء" -> "Enduit et peinture"
            "الأرضيات والتبليط" -> "Revêtements"
            "القوالب" -> "Coffrage"
            "الحديد" -> "Ferraillage"
            "الشبكات" -> "Réseaux"
            "التشطيبات" -> "Finitions"
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
    val quantity = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val calculatedTotal = quantity * (rate?.unitPrice ?: 0.0)
    val validLaborEntry = rate != null && quantity.isFinite() && quantity > 0.0 &&
        rate.unitPrice.isFinite() && rate.unitPrice >= 0.0 && calculatedTotal.isFinite() &&
        projectId != null
    val total = costs.sumOf { it.totalCost }
    val sectionLabelFr = when (section) {
        "قواعد منفردة" -> "Semelles isolées"
        "قواعد شريطية" -> "Semelles filantes"
        "السملات والميدات" -> "Longrines"
        "الأعمدة" -> "Poteaux"
        "الجسور" -> "Poutres"
        "الخرسانة" -> "Béton"
        "الجدران والبناء" -> "Maçonnerie"
        "السقوف والبلاطات" -> "Planchers et dalles"
        "التلبيس والطلاء" -> "Enduit et peinture"
        "الأرضيات والتبليط" -> "Revêtements"
        "القوالب" -> "Coffrage"
        "الحديد" -> "Ferraillage"
        "الشبكات" -> "Réseaux"
        "التشطيبات" -> "Finitions"
        "الحفر والردم" -> "Terrassement"
        "السلالم" -> "Escaliers"
        else -> section
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (projectId == null) Text(if (isArabic) "اختر مشروعاً من شاشة المشاريع قبل تسجيل التكاليف." else "Sélectionnez un projet dans l’écran Projets avant d’enregistrer des coûts.", color = MaterialTheme.colorScheme.error)
        Divider()
        Text(if (isArabic) "تكلفة اليد العاملة — " + section else "Coût de main-d’œuvre — " + sectionLabelFr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
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
        pendingRestoreUri = uri
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
        Text(if (isArabic) "تنبيه: ملف النسخة يحتوي بيانات المشاريع والأسعار والزبائن؛ احفظه في مكان آمن." else "Attention: the backup contains project, price and client data. Store it securely.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        pendingRestoreUri?.let { uri ->
            AlertDialog(
                onDismissRequest = { pendingRestoreUri = null },
                title = { Text(if (isArabic) "تأكيد استرجاع النسخة" else "Confirm database restore") },
                text = { Text(if (isArabic) "سيتم استبدال قاعدة البيانات الحالية بالنسخة المختارة. تأكد من وجود نسخة احتياطية حديثة قبل المتابعة." else "The current database will be replaced by the selected backup. Ensure you have a recent backup before continuing.") },
                confirmButton = {
                    Button(onClick = {
                        pendingRestoreUri = null
                        scope.launch {
                            backupMessage = runCatching {
                                withContext(Dispatchers.IO) {
                                    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open selected backup.")
                                    input.use { DatabaseBackupManager.restore(context, it) }
                                }
                                (context as? Activity)?.recreate()
                                if (isArabic) "تم التحقق من النسخة واسترجاع قاعدة البيانات." else "Backup validated and restored."
                            }.getOrElse { (if (isArabic) "فشل الاسترجاع: " else "Restore failed: ") + (it.message ?: "Unknown error") }
                        }
                    }) { Text(if (isArabic) "استرجاع" else "Restore") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { pendingRestoreUri = null }) { Text(if (isArabic) "إلغاء" else "Cancel") }
                }
            )
        }
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
    val allMaterialCostsFlow = remember(selectedProjectId) {
        selectedProjectId?.let { dao.observeAllCalculationCostsForProject(it) } ?: flowOf(emptyList())
    }
    val laborCostsFlow = remember(selectedProjectId) {
        selectedProjectId?.let { dao.observeAllLaborCostsForProject(it) } ?: flowOf(emptyList())
    }
    val materialCosts by allMaterialCostsFlow.collectAsState(initial = emptyList())
    val laborCosts by laborCostsFlow.collectAsState(initial = emptyList())
    var showSavedResults by remember { mutableStateOf(true) }
    var selectedDevisId by remember { mutableStateOf<Long?>(null) }
    var devisMessage by remember { mutableStateOf<String?>(null) }
    var editingDevisLine by remember { mutableStateOf<DevisLineEntity?>(null) }
    var editQuantityText by remember { mutableStateOf("") }
    var editUnitPriceText by remember { mutableStateOf("") }
    var pendingDeleteDevisId by remember { mutableStateOf<Long?>(null) }

    val selectedLinesFlow = remember(selectedDevisId) {
        selectedDevisId?.let { dao.observeDevisLines(it) } ?: flowOf(emptyList())
    }
    val selectedLines by selectedLinesFlow.collectAsState(initial = emptyList())
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val selectedDocument = devisList.firstOrNull { it.id == selectedDevisId }
    val reportTitle = selectedDocument?.let { document ->
        val client = document.clientNameSnapshot.takeIf { it.isNotBlank() }?.let {
            (if (isArabic) "الزبون: " else "Client: ") + it + " - "
        }.orEmpty()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT)
            .format(java.util.Date(document.createdAt))
        "${document.projectNameSnapshot} - ${client}${if (isArabic) "عرض سعر" else "Devis"} ${document.id} - $date"
    } ?: selectedProject?.let { it.name + if (isArabic) " - النتائج" else " - Résultats" }
        ?: "Btp Pro Dz"
    val reportSummary = selectedDocument?.let { document ->
        ProjectReportSummary(
            materialsLabel = if (isArabic) "مجموع المواد HT" else "Matériaux HT",
            materialsHt = document.totalMaterials,
            laborLabel = if (isArabic) "مجموع اليد العاملة HT" else "Main-d’œuvre HT",
            laborHt = document.totalLabor,
            totalHtLabel = if (isArabic) "المجموع HT" else "Total HT",
            totalHt = document.totalHt,
            tvaLabel = "TVA",
            tvaPercent = document.tvaPercent,
            tvaAmount = document.tvaAmount,
            totalTtcLabel = if (isArabic) "المجموع TTC" else "Total TTC",
            totalTtc = document.totalTtc
        )
    }
    val reportRows = remember(selectedLines, selectedDocument, materialCosts, laborCosts, selectedProject, isArabic) {
        buildList {
            if (selectedDocument != null) {
                selectedLines.forEach { line ->
                    add(ProjectReportRow(line.description, line.quantity, line.unit, line.unitPrice, line.total))
                }

            } else {
                materialCosts.forEach { line ->
                    add(ProjectReportRow("${line.section} — ${line.materialName}", line.quantity, line.unit, line.unitPriceSnapshot, line.totalCost))
                }
                laborCosts.forEach { line ->
                    add(ProjectReportRow("${line.section} — ${line.tradeName}", line.quantity, line.unit, line.unitPriceSnapshot, line.totalCost))
                }
            }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) scope.launch {
            exportMessage = runCatching {
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri) ?: error("Cannot open PDF destination.")
                    stream.use { ProjectReportExporter.writePdf(reportTitle, reportRows, it, reportSummary) }
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
                    stream.use { ProjectReportExporter.writeExcel(reportTitle, reportRows, it, reportSummary) }
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
                exportMessage = null
                showSavedResults = true
                onProjectSelected(id)
                projectName = ""
                clientName = ""
            }
        }, enabled = projectName.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(if (isArabic) "إنشاء مشروع" else "Créer le projet")
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (isArabic) "كيف تستخدم Devis؟" else "Comment utiliser le devis ?", fontWeight = FontWeight.Bold)
                Text(if (isArabic) "١. أنشئ مشروعاً واختره. ٢. أضف كميات وأسعار المواد أو اليد العاملة من أقسام الأشغال. ٣. راجع النتائج المحفوظة هنا. ٤. أنشئ الوثيقة ثم صدّر PDF أو Excel." else "1. Créez et sélectionnez un projet. 2. Ajoutez quantités et prix dans les rubriques. 3. Vérifiez les résultats enregistrés ici. 4. Créez le devis puis exportez en PDF ou Excel.")
            }
        }
        Divider()
        Text(if (isArabic) "قائمة المشاريع" else "Liste des projets", fontWeight = FontWeight.SemiBold)
        if (projects.isEmpty()) Text(if (isArabic) "لا توجد مشاريع بعد." else "Aucun projet pour le moment.")
        projects.forEach { project ->
            val label = project.name + if (project.clientName.isNotBlank()) " — " + project.clientName else ""
            if (selectedProjectId == project.id) Button(onClick = { selectedProjectId = project.id; selectedDevisId = null; devisMessage = null; exportMessage = null; onProjectSelected(project.id) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
            else OutlinedButton(onClick = { selectedProjectId = project.id; showSavedResults = true; selectedDevisId = null; devisMessage = null; exportMessage = null; onProjectSelected(project.id) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
        if (selectedProject != null) {
            Divider()
            Text(if (isArabic) "المشروع المحدد: " + selectedProject.name else "Projet sélectionné : " + selectedProject.name, fontWeight = FontWeight.Bold)
            Text(if (isArabic) "اختر المشروع أولاً من هذه القائمة، ثم أضف تكاليف المواد واليد العاملة من أقسام الأشغال. لن تُنقل البنود القديمة غير المرتبطة تلقائياً حتى لا تختلط تكاليف المشاريع." else "Sélectionnez d’abord ce projet, puis ajoutez les coûts depuis les rubriques. Les anciennes lignes non associées ne sont pas déplacées automatiquement afin d’éviter tout mélange.")
            OutlinedButton(
                onClick = { showSavedResults = !showSavedResults },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (showSavedResults) {
                        if (isArabic) "إخفاء النتائج المحفوظة" else "Masquer les résultats enregistrés"
                    } else {
                        if (isArabic) "عرض النتائج المحفوظة (${materialCosts.size + laborCosts.size})"
                        else "Voir les résultats enregistrés (${materialCosts.size + laborCosts.size})"
                    }
                )
            }
            if (showSavedResults) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isArabic) "النتائج المحفوظة لهذا المشروع" else "Résultats enregistrés pour ce projet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isArabic) "المواد: ${materialCosts.size} بند — ${"%.2f".format(materialCosts.sumOf { it.totalCost })} دج"
                            else "Matériaux : ${materialCosts.size} lignes — ${"%.2f".format(materialCosts.sumOf { it.totalCost })} DA"
                        )
                        materialCosts.forEach { line ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${line.section} — ${line.materialName}", fontWeight = FontWeight.SemiBold)
                                Text("${line.quantity} ${line.unit} × ${"%.2f".format(line.unitPriceSnapshot)} = ${"%.2f".format(line.totalCost)} DA")
                            }
                            Divider()
                        }
                        Text(
                            if (isArabic) "اليد العاملة: ${laborCosts.size} بند — ${"%.2f".format(laborCosts.sumOf { it.totalCost })} دج"
                            else "Main-d’œuvre : ${laborCosts.size} lignes — ${"%.2f".format(laborCosts.sumOf { it.totalCost })} DA"
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (isArabic) "الإجمالي المحفوظ للمشروع" else "Total enregistré du projet", fontWeight = FontWeight.Bold)
                                Text(
                                    "%.2f".format(materialCosts.sumOf { it.totalCost } + laborCosts.sumOf { it.totalCost }) +
                                        if (isArabic) " دج" else " DA",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isArabic) "عدد البنود: ${materialCosts.size + laborCosts.size}" else "Nombre de lignes : ${materialCosts.size + laborCosts.size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        laborCosts.forEach { line ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${line.section} — ${line.tradeName}", fontWeight = FontWeight.SemiBold)
                                Text("${line.quantity} ${line.unit} × ${"%.2f".format(line.unitPriceSnapshot)} = ${"%.2f".format(line.totalCost)} DA")
                            }
                            Divider()
                        }
                        if (materialCosts.isNotEmpty() || laborCosts.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { pdfLauncher.launch("BtpProDz_Resultats.pdf") }, modifier = Modifier.weight(1f)) {
                                    Text(if (isArabic) "PDF للنتائج" else "Résultats PDF")
                                }
                                OutlinedButton(onClick = { excelLauncher.launch("BtpProDz_Resultats.xlsx") }, modifier = Modifier.weight(1f)) {
                                    Text(if (isArabic) "Excel للنتائج" else "Résultats Excel")
                                }
                            }
                            Text(if (isArabic) "يمكنك حفظ النتائج مباشرة، أو إنشاء وثيقة Devis رسمية لاحقاً." else "Vous pouvez exporter ces résultats directement ou créer un devis officiel ensuite.", style = MaterialTheme.typography.bodySmall)
                        }
                        exportMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        Text(
                            if (materialCosts.isEmpty() && laborCosts.isEmpty()) {
                                if (isArabic) "لا توجد نتائج محفوظة لهذا المشروع بعد. افتح قسم الأشغال، اختر المشروع الحالي، ثم احفظ تكلفة المواد أو اليد العاملة."
                                else "Aucun résultat enregistré. Ouvrez une rubrique, sélectionnez ce projet, puis enregistrez un coût de matériau ou de main-d’œuvre."
                            } else {
                                if (isArabic) "هذه البنود هي التي ستُدرج عند إنشاء Devis." else "Ces lignes seront incluses lors de la création du devis."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
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
                            val documentId = dao.insertDevisWithLines(
                                document = DevisDocumentEntity(
                                    projectId = selectedProject.id,
                                    projectNameSnapshot = selectedProject.name,
                                    clientNameSnapshot = selectedProject.clientName,
                                    totalMaterials = totals.materialsHt.toDouble(),
                                    totalLabor = totals.laborHt.toDouble(),
                                    totalHt = totals.totalHt.toDouble(),
                                    tvaPercent = totals.tvaPercent.toDouble(),
                                    tvaAmount = totals.tvaAmount.toDouble(),
                                    totalTtc = totals.totalTtc.toDouble()
                                ),
                                lines = rows
                            )
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { selectedDevisId = if (selectedDevisId == doc.id) null else doc.id }) {
                                Text(if (selectedDevisId == doc.id) { if (isArabic) "إخفاء البنود" else "Masquer les lignes" } else { if (isArabic) "عرض تفاصيل البنود" else "Voir les lignes" })
                            }
                            OutlinedButton(onClick = { pendingDeleteDevisId = doc.id }) {
                                Text(if (isArabic) "حذف الوثيقة" else "Supprimer")
                            }
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    editingDevisLine = line
                                    editQuantityText = line.quantity.toString()
                                    editUnitPriceText = line.unitPrice.toString()
                                }) { Text(if (isArabic) "تعديل" else "Modifier") }
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        runCatching {
                                            database.withTransaction {
                                                dao.deleteDevisLine(line.id, line.devisId)
                                                dao.recalculateDevisDocument(line.devisId)
                                            }
                                        }.onFailure { devisMessage = (if (isArabic) "تعذر حذف البند: " else "Suppression impossible : ") + (it.message ?: "Erreur") }
                                    }
                                }) { Text(if (isArabic) "حذف" else "Supprimer") }
                            }
                        }
                    }
                }
            }
        }
        if (editingDevisLine != null) {
            val line = editingDevisLine!!
            val quantity = editQuantityText.replace(',', '.').toBigDecimalOrNull()
            val unitPrice = editUnitPriceText.replace(',', '.').toBigDecimalOrNull()
            val validEdit = quantity != null && unitPrice != null && quantity > BigDecimal.ZERO &&
                unitPrice >= BigDecimal.ZERO && quantity.toDouble().isFinite() && unitPrice.toDouble().isFinite() &&
                quantity.multiply(unitPrice).toDouble().isFinite()
            AlertDialog(
                onDismissRequest = { editingDevisLine = null },
                title = { Text(if (isArabic) "تعديل بند Devis" else "Modifier la ligne") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(line.description)
                        NumberField(if (isArabic) "الكمية" else "Quantité", editQuantityText) { editQuantityText = it }
                        NumberField(if (isArabic) "سعر الوحدة (دج)" else "Prix unitaire (DA)", editUnitPriceText) { editUnitPriceText = it }
                        Text((if (isArabic) "المجموع الجديد: " else "Nouveau total : ") + (quantity?.multiply(unitPrice ?: BigDecimal.ZERO)?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "—") + if (isArabic) " دج" else " DA")
                    }
                },
                confirmButton = {
                    Button(enabled = validEdit, onClick = {
                        val q = quantity!!
                        val p = unitPrice!!
                        val total = q.multiply(p).setScale(2, java.math.RoundingMode.HALF_UP)
                        scope.launch {
                            runCatching {
                                database.withTransaction {
                                    dao.updateDevisLine(line.copy(quantity = q.toDouble(), unitPrice = p.toDouble(), total = total.toDouble()))
                                    dao.recalculateDevisDocument(line.devisId)
                                }
                            }.onSuccess {
                                editingDevisLine = null
                                devisMessage = if (isArabic) "تم تعديل البند وتحديث المجاميع." else "Ligne modifiée et totaux actualisés."
                            }.onFailure { devisMessage = (if (isArabic) "تعذر التعديل: " else "Modification impossible : ") + (it.message ?: "Erreur") }
                        }
                    }) { Text(if (isArabic) "حفظ التعديل" else "Enregistrer") }
                },
                dismissButton = { OutlinedButton(onClick = { editingDevisLine = null }) { Text(if (isArabic) "إلغاء" else "Annuler") } }
            )
        }
        pendingDeleteDevisId?.let { id ->
            AlertDialog(
                onDismissRequest = { pendingDeleteDevisId = null },
                title = { Text(if (isArabic) "حذف وثيقة Devis؟" else "Supprimer ce devis ?") },
                text = { Text(if (isArabic) "سيتم حذف الوثيقة وبنودها المحفوظة. لا يمكن التراجع عن هذا الإجراء." else "Le document et toutes ses lignes seront supprimés. Cette action est irréversible.") },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            runCatching {
                                database.withTransaction {
                                    dao.deleteAllDevisLines(id)
                                    dao.deleteDevisDocument(id)
                                }
                            }.onSuccess {
                                if (selectedDevisId == id) selectedDevisId = null
                                pendingDeleteDevisId = null
                                devisMessage = if (isArabic) "تم حذف وثيقة Devis." else "Devis supprimé."
                            }.onFailure { devisMessage = (if (isArabic) "تعذر الحذف: " else "Suppression impossible : ") + (it.message ?: "Erreur") }
                        }
                    }) { Text(if (isArabic) "تأكيد الحذف" else "Confirmer") }
                },
                dismissButton = { OutlinedButton(onClick = { pendingDeleteDevisId = null }) { Text(if (isArabic) "إلغاء" else "Annuler") } }
            )
        }
        Text(if (isArabic) "تُحفظ تفاصيل كل Devis بصورة مستقلة؛ التعديلات اللاحقة على أسعار المواد أو أجور العمال لا تغيّر الوثائق السابقة." else "Chaque devis conserve ses propres lignes ; les modifications ultérieures des tarifs ne changent pas les documents existants.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun CalculatedQuantityCostEntry(
    section: String,
    projectId: Long?,
    quantity: Double,
    unit: String,
    isArabic: Boolean
) {
    val context = LocalContext.current
    val dao = remember { BtpDatabase.getInstance(context).appDataDao() }
    val scope = rememberCoroutineScope()
    var unitPriceText by remember(section) { mutableStateOf("") }
    var message by remember(section) { mutableStateOf<String?>(null) }
    val unitPrice = unitPriceText.toDoubleOrNull()
    val total = unitPrice?.let { quantity * it } ?: Double.NaN
    val canSave = projectId != null && quantity.isFinite() && quantity > 0.0 &&
        unitPrice != null && unitPrice.isFinite() && unitPrice >= 0.0 && total.isFinite()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Divider()
        Text(
            if (isArabic) "تسجيل الكمية والتكلفة في المشروع" else "Enregistrer la quantité et le coût dans le projet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (projectId == null) {
            Text(
                if (isArabic) "اختر مشروعاً أولاً من شاشة المشاريع حتى تتمكن من إدراج هذه الكمية في Devis." else "Sélectionnez d’abord un projet pour ajouter cette quantité au devis.",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(if (isArabic) "الكمية المحسوبة التي ستُسجّل: ${"%.3f".format(quantity)} $unit" else "Quantité calculée à enregistrer : ${"%.3f".format(quantity)} $unit")
            Text(
                if (isArabic) "الخطوة 1: أدخل سعر وحدة واحدة بالدينار. سيحسب التطبيق التكلفة تلقائياً." else "Étape 1 : saisissez le prix d’une unité en DA. Le coût sera calculé automatiquement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NumberField(if (isArabic) "سعر الوحدة (دج / $unit)" else "Prix unitaire (DA / $unit)", unitPriceText) { unitPriceText = it }
            Text(
                if (isArabic) "تكلفة هذا البند: ${if (total.isFinite()) "%.2f".format(total) else "أدخل سعر الوحدة"} دج" else "Coût de cette ligne : ${if (total.isFinite()) "%.2f".format(total) else "Saisissez le prix unitaire"} DA",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (isArabic) "الخطوة 2: اضغط حفظ لتسجيل الكمية والتكلفة في المشروع فقط. الخطوة 3: افتح «المشاريع وDevis» ثم أنشئ Devis لإدراج البنود المسجلة." else "Étape 2 : enregistrez cette quantité et son coût dans le projet. Étape 3 : ouvrez « Projets et devis » puis créez un devis pour y inclure les lignes enregistrées.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val price = unitPrice ?: return@Button
                    scope.launch {
                        runCatching {
                            dao.insertCalculationCost(
                                CalculationCostEntity(
                                    section = section,
                                    materialName = "كمية محسوبة - $section",
                                    quantity = quantity,
                                    unit = unit,
                                    unitPriceSnapshot = price,
                                    totalCost = total,
                                    projectId = projectId
                                )
                            )
                        }.onSuccess {
                            message = if (isArabic) "تم الحفظ في تكاليف المشروع فقط. انتقل إلى «المشاريع وDevis» ثم اضغط «إنشاء Devis من التكاليف المسجلة»." else "Enregistré dans les coûts du projet. Ouvrez « Projets et devis », puis appuyez sur « Créer un devis à partir des coûts enregistrés »."
                            unitPriceText = ""
                        }.onFailure {
                            message = (if (isArabic) "تعذر الحفظ: " else "Échec : ") + (it.message ?: "Erreur")
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isArabic) "حفظ بند التكلفة في المشروع" else "Enregistrer le coût dans le projet") }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> if (input.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(input.replace(',', '.')) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
