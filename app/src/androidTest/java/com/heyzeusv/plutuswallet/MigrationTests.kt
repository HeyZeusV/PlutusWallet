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
class MigrationTests {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper : MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TransactionDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate16to21() {

        val MIGRATION_16_21 = object : Migration(16, 21) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `Category` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL)""")
                database.execSQL("""CREATE UNIQUE INDEX index_cat_name_type
                                        ON `Category` (category, type)""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `Account` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            `account` TEXT NOT NULL)""")
                database.execSQL("""CREATE UNIQUE INDEX index_account
                                        ON `Account` (account)""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `title` TEXT NOT NULL, 
                                            `date` INTEGER NOT NULL,    
                                            `total` TEXT NOT NULL, 
                                            `account` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `memo` TEXT NOT NULL, 
                                            `repeating` INTEGER NOT NULL, 
                                            `frequency` INTEGER NOT NULL, 
                                            `period` INTEGER NOT NULL, 
                                            `futureDate` INTEGER NOT NULL,
                                            `futureTCreated` INTEGER NOT NULL,
                                            FOREIGN KEY(`account`) 
                                                REFERENCES `Account`(`account`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION , 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`)
                                                ON UPDATE CASCADE ON DELETE NO ACTION )""")
                database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
                database.execSQL("""DROP TABLE `Transaction`""")
                database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_trans_name_type` 
                                            ON `Transaction` (category, type)""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_account_name` 
                                            ON `Transaction` (`account`)""")
            }
        }

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 16).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 21, true, MIGRATION_16_21)
    }

    @Test
    @Throws(IOException::class)
    fun migrate20to21() {

        val MIGRATION_20_21 = object : Migration(20, 21) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `Account` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            `account` TEXT NOT NULL)""")
                database.execSQL("""CREATE UNIQUE INDEX index_account
                                        ON `Account` (account)""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `title` TEXT NOT NULL, 
                                            `date` INTEGER NOT NULL,    
                                            `total` TEXT NOT NULL, 
                                            `account` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `memo` TEXT NOT NULL, 
                                            `repeating` INTEGER NOT NULL, 
                                            `frequency` INTEGER NOT NULL, 
                                            `period` INTEGER NOT NULL, 
                                            `futureDate` INTEGER NOT NULL,
                                            `futureTCreated` INTEGER NOT NULL,
                                            FOREIGN KEY(`account`) 
                                                REFERENCES `Account`(`account`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION , 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`)
                                                ON UPDATE CASCADE ON DELETE NO ACTION )""")
                database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
                database.execSQL("""DROP TABLE `Transaction`""")
                database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_trans_name_type` 
                                            ON `Transaction` (category, type)""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_account_name` 
                                            ON `Transaction` (`account`)""")
            }
        }

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 20).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 21, true, MIGRATION_20_21)
    }

    @Test
    @Throws(IOException::class)
    fun migrate19to20() {

        val MIGRATION_19_20 = object : Migration(19, 20) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `title` TEXT NOT NULL, 
                                            `date` INTEGER NOT NULL, 
                                            `total` TEXT NOT NULL, 
                                            `account` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `memo` TEXT NOT NULL, 
                                            `repeating` INTEGER NOT NULL, 
                                            `frequency` INTEGER NOT NULL, 
                                            `period` INTEGER NOT NULL, 
                                            `futureDate` INTEGER NOT NULL, 
                                            `futureTCreated` INTEGER NOT NULL, 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION)""")
                database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
                database.execSQL("""DROP TABLE `Transaction`""")
                database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_trans_name_type` 
                                            ON `Transaction` (category, type)""")
            }
        }

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 19).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 20, true, MIGRATION_19_20)
    }

    @Test
    @Throws(IOException::class)
    fun migrate18to19() {

        val MIGRATION_18_19 = object : Migration(18, 19) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `title` TEXT NOT NULL, 
                                            `date` INTEGER NOT NULL, 
                                            `total` TEXT NOT NULL, 
                                            `account` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `memo` TEXT NOT NULL, 
                                            `repeating` INTEGER NOT NULL, 
                                            `frequency` INTEGER NOT NULL, 
                                            `period` INTEGER NOT NULL, 
                                            `futureDate` INTEGER NOT NULL, 
                                            `futureTCreated` INTEGER NOT NULL, 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION)""")
                database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
                database.execSQL("""DROP TABLE `Transaction`""")
                database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
                database.execSQL("""CREATE INDEX index_trans_name_type
                                        ON `Transaction` (category, type)""")
            }
        }

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 18).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 19, true, MIGRATION_18_19)
    }

    @Test
    @Throws(IOException::class)
    fun migrate17to18() {

        val MIGRATION_17_18 = object : Migration(17, 18) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE UNIQUE INDEX index_cat_name_type
                                        ON `Category` (category, type)""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `Transaction_new` (
                                            `id` INTEGER NOT NULL, 
                                            `title` TEXT NOT NULL, 
                                            `date` INTEGER NOT NULL, 
                                            `total` TEXT NOT NULL, 
                                            `account` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `memo` TEXT NOT NULL, 
                                            `repeating` INTEGER NOT NULL, 
                                            `frequency` INTEGER NOT NULL, 
                                            `period` INTEGER NOT NULL, 
                                            `futureDate` INTEGER NOT NULL, 
                                            `futureTCreated` INTEGER NOT NULL, 
                                            PRIMARY KEY(`id`), 
                                            FOREIGN KEY(`category`, `type`) 
                                                REFERENCES `Category`(`category`, `type`) 
                                                ON UPDATE CASCADE ON DELETE NO ACTION )""")
                database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
                database.execSQL("""DROP TABLE `Transaction`""")
                database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
                database.execSQL("""CREATE UNIQUE INDEX index_trans_name_type
                                        ON `Transaction` (category, type)""")
            }
        }

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 17).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 18, true, MIGRATION_17_18)
    }

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

        var db : SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 16).apply {

            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 17, true, MIGRATION_16_17)
    }
}