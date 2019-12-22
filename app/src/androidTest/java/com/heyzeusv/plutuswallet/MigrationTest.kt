package com.heyzeusv.plutuswallet

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.database.TransactionDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(androidx.test.runner.AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper : MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TransactionDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate16to17() {

        val MIGRATION_16_17 = object : Migration(16, 17) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE `Category` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `category` TEXT NOT NULL, 
                    `type` TEXT NOT NULL)""")
            }
        }

        var db = helper.createDatabase(TEST_DB, 16).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 17, true, MIGRATION_16_17)
    }
}