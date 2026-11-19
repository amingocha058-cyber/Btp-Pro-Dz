package dz.btpprodz.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_settings")
data class AppSettingEntity(@PrimaryKey val key: String, val value: String)

@Entity(tableName = "custom_units", indices = [Index(value = ["name", "symbol"], unique = true)])
data class CustomUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val symbol: String
)

@Entity(tableName = "materials", indices = [Index(value = ["name"], unique = true)])
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentPrice: Double,
    val unit: String = "وحدة",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "material_price_history", indices = [Index("materialId")])
data class MaterialPriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materialId: Long,
    val materialName: String,
    val price: Double,
    val unit: String,
    val recordedAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "calculation_costs", indices = [Index("recordedAt"), Index("section")])
data class CalculationCostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val section: String,
    val materialName: String,
    val quantity: Double,
    val unit: String,
    val unitPriceSnapshot: Double,
    val totalCost: Double,
    val projectId: Long? = null,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "labor_rates", indices = [Index(value = ["tradeName"], unique = true)])
data class LaborRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tradeName: String,
    val unit: String,
    val unitPrice: Double,
    val updatedAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "projects", indices = [Index("createdAt")])
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clientName: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "devis_documents", indices = [Index("projectId"), Index("createdAt")])
data class DevisDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val projectNameSnapshot: String,
    val clientNameSnapshot: String,
    val totalMaterials: Double,
    val totalLabor: Double,
    val totalHt: Double,
    val tvaPercent: Double,
    val tvaAmount: Double,
    val totalTtc: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "devis_lines", indices = [Index("devisId"), Index("section")])
data class DevisLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val devisId: Long,
    val section: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val total: Double,
    val category: String
)

@Entity(tableName = "labor_costs", indices = [Index("section"), Index("recordedAt")])
data class LaborCostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val section: String,
    val tradeName: String,
    val quantity: Double,
    val unit: String,
    val unitPriceSnapshot: Double,
    val totalCost: Double,
    val projectId: Long? = null,
    val recordedAt: Long = System.currentTimeMillis()
)

@Dao
interface AppDataDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Upsert
    suspend fun saveSetting(setting: AppSettingEntity)

    @Query("SELECT * FROM custom_units ORDER BY name COLLATE NOCASE")
    fun observeUnits(): Flow<List<CustomUnitEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnit(unit: CustomUnitEntity): Long

    @Query("SELECT * FROM materials ORDER BY name COLLATE NOCASE")
    fun observeMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findMaterial(name: String): MaterialEntity?

    @Upsert
    suspend fun saveMaterial(material: MaterialEntity)

    @Insert
    suspend fun addPriceHistory(entry: MaterialPriceHistoryEntity)

    @Query("SELECT * FROM material_price_history ORDER BY recordedAt DESC")
    fun observePriceHistory(): Flow<List<MaterialPriceHistoryEntity>>

    @Insert
    suspend fun insertCalculationCost(cost: CalculationCostEntity)

    @Query("SELECT * FROM calculation_costs WHERE section = :section ORDER BY recordedAt DESC")
    fun observeCalculationCosts(section: String): Flow<List<CalculationCostEntity>>

    @Query("SELECT * FROM labor_rates ORDER BY tradeName COLLATE NOCASE")
    fun observeLaborRates(): Flow<List<LaborRateEntity>>

    @Query("SELECT * FROM labor_rates WHERE tradeName = :name COLLATE NOCASE LIMIT 1")
    suspend fun findLaborRate(name: String): LaborRateEntity?

    @Upsert
    suspend fun saveLaborRate(rate: LaborRateEntity)

    @Insert
    suspend fun insertLaborCost(cost: LaborCostEntity)

    @Query("SELECT * FROM labor_costs WHERE section = :section ORDER BY recordedAt DESC")
    fun observeLaborCosts(section: String): Flow<List<LaborCostEntity>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert
    suspend fun insertDevis(document: DevisDocumentEntity): Long

    @Insert
    suspend fun insertDevisLines(lines: List<DevisLineEntity>)

    @Query("SELECT * FROM devis_documents WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun observeDevis(projectId: Long): Flow<List<DevisDocumentEntity>>

    @Query("SELECT * FROM devis_lines WHERE devisId = :devisId ORDER BY id")
    fun observeDevisLines(devisId: Long): Flow<List<DevisLineEntity>>

    @Query("SELECT * FROM calculation_costs WHERE projectId = :projectId ORDER BY section, recordedAt")
    suspend fun materialCostLinesForProject(projectId: Long): List<CalculationCostEntity>

    @Query("SELECT * FROM labor_costs WHERE projectId = :projectId ORDER BY section, recordedAt")
    suspend fun laborCostLinesForProject(projectId: Long): List<LaborCostEntity>

    @Query("UPDATE calculation_costs SET projectId = :projectId WHERE projectId IS NULL")
    suspend fun assignUnassignedMaterialCosts(projectId: Long): Int

    @Query("UPDATE labor_costs SET projectId = :projectId WHERE projectId IS NULL")
    suspend fun assignUnassignedLaborCosts(projectId: Long): Int
}

@Database(
    entities = [AppSettingEntity::class, CustomUnitEntity::class, MaterialEntity::class, MaterialPriceHistoryEntity::class, CalculationCostEntity::class, LaborRateEntity::class, LaborCostEntity::class, ProjectEntity::class, DevisDocumentEntity::class, DevisLineEntity::class],
    version = 5,
    exportSchema = false
)
abstract class BtpDatabase : RoomDatabase() {
    abstract fun appDataDao(): AppDataDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS calculation_costs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, section TEXT NOT NULL, materialName TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, unitPriceSnapshot REAL NOT NULL, totalCost REAL NOT NULL, recordedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calculation_costs_recordedAt ON calculation_costs(recordedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calculation_costs_section ON calculation_costs(section)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS labor_rates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tradeName TEXT NOT NULL, unit TEXT NOT NULL, unitPrice REAL NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_labor_rates_tradeName ON labor_rates(tradeName)")
                db.execSQL("CREATE TABLE IF NOT EXISTS labor_costs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, section TEXT NOT NULL, tradeName TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, unitPriceSnapshot REAL NOT NULL, totalCost REAL NOT NULL, recordedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_labor_costs_section ON labor_costs(section)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_labor_costs_recordedAt ON labor_costs(recordedAt)")
            }
        }


        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, clientName TEXT NOT NULL, address TEXT NOT NULL, notes TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_createdAt ON projects(createdAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS devis_documents (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, projectId INTEGER NOT NULL, projectNameSnapshot TEXT NOT NULL, clientNameSnapshot TEXT NOT NULL, totalMaterials REAL NOT NULL, totalLabor REAL NOT NULL, totalHt REAL NOT NULL, tvaPercent REAL NOT NULL, tvaAmount REAL NOT NULL, totalTtc REAL NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devis_documents_projectId ON devis_documents(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devis_documents_createdAt ON devis_documents(createdAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS devis_lines (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, devisId INTEGER NOT NULL, section TEXT NOT NULL, description TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, unitPrice REAL NOT NULL, total REAL NOT NULL, category TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devis_lines_devisId ON devis_lines(devisId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_devis_lines_section ON devis_lines(section)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE calculation_costs ADD COLUMN projectId INTEGER")
                db.execSQL("ALTER TABLE labor_costs ADD COLUMN projectId INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calculation_costs_projectId ON calculation_costs(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_labor_costs_projectId ON labor_costs(projectId)")
            }
        }

        @Volatile private var INSTANCE: BtpDatabase? = null

        fun getInstance(context: Context): BtpDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BtpDatabase::class.java,
                    "btp_pro_dz.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
    }
}