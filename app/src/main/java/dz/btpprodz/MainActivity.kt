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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

                Spacer(modifier = Modifier.height(24.dp))
                if (showConcreteCalculator) {
                    OutlinedButton(onClick = { showConcreteCalculator = false }) {
                        Text(if (isArabic) "رجوع إلى الأقسام" else "Retour aux rubriques")
                    }
                    ConcreteCalculator(isArabic = isArabic)
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
    var heightText by remember { mutableStateOf("3.00") }

    val typesAr = listOf("القواعد", "الأعمدة", "الجسور", "البلاطات")
    val typesFr = listOf("Semelles", "Poteaux", "Poutres", "Dalles")
    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val length = lengthText.toDoubleOrNull() ?: 0.0
    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val totalVolume = if (quantity > 0 && length > 0 && width > 0 && height > 0) quantity * length * width * height else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (isArabic) "حساب عدد وحجم العناصر الخرسانية" else "Nombre et volume des éléments en béton", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (isArabic) "اختر نوع العنصر" else "Choisissez le type d’élément")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            typesAr.forEachIndexed { index, ar ->
                if (elementType == index) Button(onClick = { elementType = index }) { Text(if (isArabic) ar else typesFr[index]) }
                else OutlinedButton(onClick = { elementType = index }) { Text(if (isArabic) ar else typesFr[index]) }
            }
        }
        NumberField(if (isArabic) "العدد" else "Quantité", quantityText) { quantityText = it }
        NumberField(if (isArabic) "الطول (متر)" else "Longueur (m)", lengthText) { lengthText = it }
        NumberField(if (isArabic) "العرض (متر)" else "Largeur (m)", widthText) { widthText = it }
        NumberField(if (isArabic) "الارتفاع / السماكة (متر)" else "Hauteur / épaisseur (m)", heightText) { heightText = it }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(if (isArabic) "عدد العناصر: ${quantity.toInt()}" else "Nombre d’éléments : ${quantity.toInt()}")
                Text(if (isArabic) "الحجم الإجمالي: ${"%.3f".format(totalVolume)} م³" else "Volume total : ${"%.3f".format(totalVolume)} m³", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Text(if (isArabic) "الحساب: العدد × الطول × العرض × الارتفاع/السماكة" else "Calcul : quantité × longueur × largeur × hauteur/épaisseur", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> if (input.all { it.isDigit() || it == '.' || it == ',' }) onValueChange(input.replace(',', '.')) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
