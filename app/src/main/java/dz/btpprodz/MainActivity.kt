package dz.btpprodz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    WorkSection("الخرسانة", "Béton", "حساب الأحجام وكميات المواد", "Volumes et quantités des matériaux"),
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
                                modifier = Modifier.fillMaxWidth(),
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
