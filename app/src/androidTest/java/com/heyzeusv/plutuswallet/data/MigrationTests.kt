package com.heyzeusv.plutuswallet.data

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class MigrationTests {

    private val testDB = "migration-test"

    @get:Rule
    val helper : MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PWDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate23to24() {

        helper.createDatabase(testDB, 23).apply {

            close()
        }

        helper.runMigrationsAndValidate(testDB, 24, true, Migrations.migration23to24)
    }

    @Test
    @Throws(IOException::class)
    fun migrate22to23() {

        val migration22to23 = object : Migration(22, 23) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""DROP TABLE ExpenseCategory""")
                database.execSQL("""DROP TABLE IncomeCategory""")
            }
        }

        helper.createDatabase(testDB, 22).apply {

            close()
        }

        helper.runMigrationsAndValidate(testDB, 23, true, migration22to23)
    }

    @Test
    @Throws(IOException::class)
    fun migrate21to22() {

        val migration21to22 = object : Migration(21, 22) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""DROP INDEX index_cat_name_type""")
                database.execSQL("""DROP INDEX index_trans_name_type""")
                database.execSQL("""CREATE UNIQUE INDEX index_cat_type
                                        ON `Category` (category, type)""")
                database.execSQL("""CREATE INDEX index_cat_name_type
                                        ON `Transaction` (category, type)""")

            }
        }

        helper.createDatabase(testDB, 21).apply {

            close()
        }

        helper.runMigrationsAndValidate(testDB, 22, true, migration21to22)
    }

    @Test
    @Throws(IOException::class)
    fun migrate16to21() {

        val migration16to21 = object : Migration(16, 22) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `Category` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                            `category` TEXT NOT NULL, 
                                            `type` TEXT NOT NULL)""")
                database.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS index_cat_type
                                        ON `Category` (category, type)""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `Account` (
                                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                            `account` TEXT NOT NULL)""")
                database.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS index_account
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
                database.execSQL("""CREATE INDEX IF NOT EXISTS index_cat_name_type
                                        ON `Transaction` (category, type)""")
                database.execSQL("""CREATE INDEX IF NOT EXISTS `index_account_name` 
                                            ON `Transaction` (`account`)""")
            }
        }

        helper.createDatabase(testDB, 16).apply {

            close()
        }

        helper.runMigrationsAndValidate(testDB, 22, true, migration16to21)
    }

    @Test
    @Throws(IOException::class)
    fun migrate20to21() {

        val migration20to21 = object : Migration(20, 21) {

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

        helper.createDatabase(testDB, 20).apply { close() }

        helper.runMigrationsAndValidate(testDB, 21, true, migration20to21)
    }

    @Test
    @Throws(IOException::class)
    fun migrate19to20() {

        val migration19to20 = object : Migration(19, 20) {

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

        helper.createDatabase(testDB, 19).apply { close() }
        helper.runMigrationsAndValidate(testDB, 20, true, migration19to20)
    }

    @Test
    @Throws(IOException::class)
    fun migrate18to19() {

        val migration18to19 = object : Migration(18, 19) {

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

        helper.createDatabase(testDB, 18).apply { close() }
        helper.runMigrationsAndValidate(testDB, 19, true, migration18to19)
    }

    @Test
    @Throws(IOException::class)
    fun migrate17to18() {

        val migration17to18 = object : Migration(17, 18) {

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

        helper.createDatabase(testDB, 17).apply { close() }
        helper.runMigrationsAndValidate(testDB, 18, true, migration17to18)
    }

    @Test
    @Throws(IOException::class)
    fun migrate16to17() {

        val migration16to17 = object : Migration(16, 17) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE `Category` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `category` TEXT NOT NULL, 
                    `type` TEXT NOT NULL)""")
            }
        }

        helper.createDatabase(testDB, 16).apply { close() }
        helper.runMigrationsAndValidate(testDB, 17, true, migration16to17)
    }
}