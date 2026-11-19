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
}

@Database(
    entities = [AppSettingEntity::class, CustomUnitEntity::class, MaterialEntity::class, MaterialPriceHistoryEntity::class, CalculationCostEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BtpDatabase : RoomDatabase() {
    abstract fun appDataDao(): AppDataDao

    companion object {
        @Volatile private var INSTANCE: BtpDatabase? = null

        fun getInstance(context: Context): BtpDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BtpDatabase::class.java,
                    "btp_pro_dz.db"
                ).build().also { INSTANCE = it }
            }
    }
}