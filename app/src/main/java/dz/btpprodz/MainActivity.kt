package dz.btpprodz

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import dz.btpprodz.data.AppSettingEntity
import dz.btpprodz.data.BtpDatabase
import dz.btpprodz.data.CustomUnitEntity
import dz.btpprodz.data.MaterialEntity
import dz.btpprodz.data.MaterialPriceHistoryEntity
import kotlinx.coroutines.launch

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
                if (!showConcreteCalculator && !showFoundationCalculator && !showEarthworksCalculator && !showColumnBeamCalculator && selectedOtherSection == null && !showSettings) {
                    OutlinedButton(onClick = { showSettings = true }) {
                        Text(if (isArabic) "الإعدادات" else "Paramètres")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (showConcreteCalculator || showFoundationCalculator || showEarthworksCalculator || showColumnBeamCalculator || selectedOtherSection != null || showSettings) {
                    OutlinedButton(onClick = { showConcreteCalculator = false; showFoundationCalculator = false; showEarthworksCalculator = false; showColumnBeamCalculator = false; selectedOtherSection = null; showSettings = false }) {
                        Text(if (isArabic) "رجوع إلى الأقسام" else "Retour aux rubriques")
                    }
                    if (showConcreteCalculator) ConcreteCalculator(isArabic = isArabic)
                    if (showFoundationCalculator) FoundationCalculator(isArabic = isArabic)
                    if (showEarthworksCalculator) EarthworksCalculator(isArabic = isArabic)
                    if (showColumnBeamCalculator) ColumnBeamCalculator(isArabic = isArabic)
                    if (selectedOtherSection != null) OtherWorkCalculator(section = selectedOtherSection!!, isArabic = isArabic)
                    if (showSettings) SettingsScreen(isArabic = isArabic)
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

    val typesAr = listOf("القواعد", "الأعمدة", "الجسور", "البلاطات")
    val typesFr = listOf("Semelles", "Poteaux", "Poutres", "Dalles")
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val valid = quantity > 0 && length > 0 && width > 0 && height > 0
    val totalVolume = if (valid) quantity * length * width * height else 0.0
    val perElementVolume = if (length > 0 && width > 0 && height > 0) length * width * height else 0.0

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

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "عدد العناصر: ${if (quantity >= 0) quantity.toInt() else 0}" else "Nombre d’éléments : ${if (quantity >= 0) quantity.toInt() else 0}")
                Text(if (isArabic) "حجم العنصر الواحد: ${"%.3f".format(perElementVolume)} م³" else "Volume unitaire : ${"%.3f".format(perElementVolume)} m³")
                Text(if (isArabic) "الحجم الإجمالي: ${"%.3f".format(totalVolume)} م³" else "Volume total : ${"%.3f".format(totalVolume)} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    val valid = quantity > 0 && quantity % 1.0 == 0.0 && length > 0 && width > 0 && depth > 0
    val unitVolume = length * width * depth
    val totalVolume = if (valid) quantity * unitVolume else 0.0
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
                Text(if (isArabic) "عدد العناصر: ${if (quantity >= 0) quantity.toInt() else 0}" else "Quantité : ${if (quantity >= 0) quantity.toInt() else 0}")
                Text(if (isArabic) "حجم العنصر الواحد: ${"%.3f".format(unitVolume)} م³" else "Volume unitaire : ${"%.3f".format(unitVolume)} m³")
                Text(
                    if (isArabic) "الحجم الإجمالي: ${"%.3f".format(totalVolume)} م³" else "Volume total : ${"%.3f".format(totalVolume)} m³",
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

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val depth = depthText.toDoubleOrNull() ?: 0.0
    val valid = quantity > 0 && quantity % 1.0 == 0.0 && length > 0 && width > 0 && depth > 0
    val unitVolume = length * width * depth
    val totalVolume = if (valid) quantity * unitVolume else 0.0

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

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isArabic) "النتيجة" else "Résultat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (isArabic) "العملية: ${if (operation == 0) "حفر" else "ردم"}" else "Opération : ${if (operation == 0) "Déblai" else "Remblai"}")
                Text(if (isArabic) "عدد المناطق: ${if (quantity >= 0) quantity.toInt() else 0}" else "Nombre de zones : ${if (quantity >= 0) quantity.toInt() else 0}")
                Text(if (isArabic) "حجم المنطقة الواحدة: ${"%.3f".format(unitVolume)} م³" else "Volume par zone : ${"%.3f".format(unitVolume)} m³")
                Text(
                    if (isArabic) "الحجم الإجمالي: ${"%.3f".format(totalVolume)} م³" else "Volume total : ${"%.3f".format(totalVolume)} m³",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!valid) Text(
                    if (isArabic) "أدخل عدداً صحيحاً وأبعاداً أكبر من الصفر." else "Saisissez un nombre entier et des dimensions positives.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            if (isArabic) "الحساب الهندسي المبسط: العدد × الطول × العرض × العمق/الارتفاع. لا يشمل معامل انتفاش التربة أو الدمك." else "Calcul géométrique simple : nombre × longueur × largeur × profondeur/hauteur. Hors foisonnement et compactage.",
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
    val valid = quantity > 0 && quantity % 1.0 == 0.0 && length > 0 && width > 0 && height > 0
    val unitVolume = length * width * height
    val totalVolume = if (valid) quantity * unitVolume else 0.0

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
                Text(if (isArabic) "عدد العناصر: ${if (quantity >= 0) quantity.toInt() else 0}" else "Nombre d’éléments : ${if (quantity >= 0) quantity.toInt() else 0}")
                Text(if (isArabic) "حجم العنصر الواحد: ${"%.3f".format(unitVolume)} م³" else "Volume unitaire : ${"%.3f".format(unitVolume)} m³")
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
    var widthText by remember(section) { mutableStateOf("3") }
    var heightText by remember(section) { mutableStateOf("3") }
    var unitPriceText by remember(section) { mutableStateOf("0") }
    var openingAreaText by remember(section) { mutableStateOf("0") }
    var tileLengthText by remember(section) { mutableStateOf("0.60") }
    var tileWidthText by remember(section) { mutableStateOf("0.60") }

    val count = countText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val openings = openingAreaText.toDoubleOrNull() ?: 0.0
    val tileLength = tileLengthText.toDoubleOrNull() ?: 0.0
    val tileWidth = tileWidthText.toDoubleOrNull() ?: 0.0

    val isWall = section == "الجدران والبناء"
    val isRoof = section == "السقوف والبلاطات"
    val isFinish = section == "التلبيس والطلاء"
    val isFloor = section == "الأرضيات والتبليط"
    val isStairs = section == "السلالم"
    val isLabor = section == "اليد العاملة"
    val area = when {
        isWall -> count * length * height - openings
        isRoof || isFloor -> count * length * width
        isFinish -> count * length * height
        isStairs -> count * length * width
        else -> 0.0
    }
    val volume = if (isRoof) area * height else 0.0
    val tileCount = if (isFloor && tileLength > 0 && tileWidth > 0) area / (tileLength * tileWidth) else 0.0
    val laborTotal = if (isLabor && count >= 0 && unitPrice >= 0) count * unitPrice else 0.0
    val valid = count > 0 && length > 0 && width > 0 && height > 0 &&
        openings >= 0 && (!isFloor || (tileLength > 0 && tileWidth > 0)) &&
        (!isLabor || unitPrice >= 0) && (!isWall || area >= 0)

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
            NumberField(if (isArabic) "الطول (م)" else "Longueur (m)", lengthText) { lengthText = it }
            NumberField(if (isArabic) if (isWall || isFinish) "الارتفاع (م)" else "العرض (م)" else if (isWall || isFinish) "Hauteur (m)" else "Largeur (m)", widthText) { widthText = it }
            if (isWall || isFinish) {
                NumberField(if (isArabic) "المساحة الإجمالية للفتحات (م²)" else "Surface totale des ouvertures (m²)", openingAreaText) { openingAreaText = it }
            } else if (!isStairs && !isRoof) {
                NumberField(if (isArabic) "البعد الإضافي (م)" else "Dimension supplémentaire (m)", heightText) { heightText = it }
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
                        Text(if (isArabic) "إجمالي وحدات العمل: ${"%.2f".format(count)}" else "Total des unités : ${"%.2f".format(count)}")
                        Text(if (isArabic) "التكلفة الإجمالية: ${"%.2f".format(laborTotal)} دج" else "Coût total : ${"%.2f".format(laborTotal)} DA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    isRoof -> {
                        Text(if (isArabic) "المساحة: ${"%.3f".format(area)} م²" else "Surface : ${"%.3f".format(area)} m²")
                        Text(if (isArabic) "حجم الخرسانة: ${"%.3f".format(volume)} م³" else "Volume de béton : ${"%.3f".format(volume)} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    isFloor -> {
                        Text(if (isArabic) "المساحة الإجمالية: ${"%.3f".format(area)} م²" else "Surface totale : ${"%.3f".format(area)} m²")
                        Text(if (isArabic) "عدد البلاطات النظري: ${"%.0f".format(kotlin.math.ceil(tileCount))}" else "Nombre théorique de carreaux : ${"%.0f".format(kotlin.math.ceil(tileCount))}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    else -> Text(if (isArabic) "المساحة الصافية: ${"%.3f".format(area)} م²" else "Surface nette : ${"%.3f".format(area)} m²", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
private fun SettingsScreen(isArabic: Boolean) {
    val context = LocalContext.current
    val dao = remember { BtpDatabase.getInstance(context).appDataDao() }
    val scope = rememberCoroutineScope()
    val units by dao.observeUnits().collectAsState(initial = emptyList())
    val materials by dao.observeMaterials().collectAsState(initial = emptyList())
    val history by dao.observePriceHistory().collectAsState(initial = emptyList())

    var tvaText by remember { mutableStateOf("19") }
    var unitName by remember { mutableStateOf("") }
    var unitSymbol by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    var materialPrice by remember { mutableStateOf("") }
    var materialUnit by remember { mutableStateOf("وحدة") }
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
            if (price != null && price >= 0 && materialName.isNotBlank() && materialUnit.isNotBlank()) {
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
        }, enabled = materialName.isNotBlank() && materialUnit.isNotBlank() && (materialPrice.toDoubleOrNull()?.let { it >= 0 } == true)) {
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
        Text(
            if (isArabic) "تُحفظ هذه البيانات محلياً على الهاتف دون الحاجة إلى الإنترنت. ربطها بالحسابات وDevis سيكون في المرحلة التالية." else "Ces données sont enregistrées localement sur le téléphone, sans Internet. Leur liaison aux calculs et aux devis viendra à l’étape suivante.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> if (input.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(input.replace(',', '.')) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
