package dz.btpprodz.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DevisPersistenceInstrumentedTest {
    private lateinit var database: BtpDatabase
    private lateinit var dao: AppDataDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.appDataDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun savedDevisLinesCanBeEditedAndReadBack() = runBlocking {
        val documentId = dao.insertDevisWithLines(
            document = DevisDocumentEntity(
                projectId = 1L,
                projectNameSnapshot = "اختبار المشروع",
                clientNameSnapshot = "الزبون",
                totalMaterials = 200.0,
                totalLabor = 0.0,
                totalHt = 200.0,
                tvaPercent = 19.0,
                tvaAmount = 38.0,
                totalTtc = 238.0
            ),
            lines = listOf(
                DevisLineEntity(
                    devisId = 0L, // Replaced atomically with the newly inserted document ID.
                    section = "الخرسانة",
                    description = "خرسانة C25",
                    quantity = 2.0,
                    unit = "م³",
                    unitPrice = 100.0,
                    total = 200.0,
                    category = "MATERIAL"
                )
            )
        )

        val saved = dao.getDevisLines(documentId).single()
        dao.updateDevisLine(saved.copy(quantity = 3.0, total = 300.0))
        val updated = dao.getDevisLines(documentId).single()

        assertEquals(3.0, updated.quantity, 0.0)
        assertEquals(300.0, updated.total, 0.0)
    }

    @Test
    fun deletingDevisLinesAndDocumentRemovesPersistedRecords() = runBlocking {
        val documentId = dao.insertDevis(
            DevisDocumentEntity(
                projectId = 2L,
                projectNameSnapshot = "Project",
                clientNameSnapshot = "",
                totalMaterials = 50.0,
                totalLabor = 0.0,
                totalHt = 50.0,
                tvaPercent = 0.0,
                tvaAmount = 0.0,
                totalTtc = 50.0
            )
        )
        dao.insertDevisLines(
            listOf(
                DevisLineEntity(
                    devisId = documentId,
                    section = "Test",
                    description = "Line",
                    quantity = 1.0,
                    unit = "u",
                    unitPrice = 50.0,
                    total = 50.0,
                    category = "MATERIAL"
                )
            )
        )

        assertEquals(1, dao.deleteDevisLine(dao.getDevisLines(documentId).single().id, documentId))
        assertEquals(0, dao.getDevisLines(documentId).size)
        dao.deleteAllDevisLines(documentId)
        assertEquals(1, dao.deleteDevisDocument(documentId))
        assertNull(dao.getDevisDocument(documentId))
    }

    @Test
    fun updatedDocumentFinancialSnapshotIsPersisted() = runBlocking {
        val documentId = dao.insertDevis(
            DevisDocumentEntity(
                projectId = 3L,
                projectNameSnapshot = "Project totals",
                clientNameSnapshot = "",
                totalMaterials = 100.0,
                totalLabor = 50.0,
                totalHt = 150.0,
                tvaPercent = 19.0,
                tvaAmount = 28.5,
                totalTtc = 178.5
            )
        )
        val document = dao.getDevisDocument(documentId)!!
        dao.updateDevisDocument(
            document.copy(
                totalMaterials = 200.0,
                totalLabor = 75.0,
                totalHt = 275.0,
                tvaAmount = 52.25,
                totalTtc = 327.25
            )
        )
        val updated = dao.getDevisDocument(documentId)!!
        assertEquals(275.0, updated.totalHt, 0.0)
        assertEquals(52.25, updated.tvaAmount, 0.0)
        assertEquals(327.25, updated.totalTtc, 0.0)
    }

}
