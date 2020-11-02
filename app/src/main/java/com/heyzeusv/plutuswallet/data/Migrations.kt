package com.heyzeusv.plutuswallet.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 *  Migrations used to update Room database to newer schemas.
 */
object Migrations {

    val migration22to23: Migration = object : Migration(22, 23) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("""DROP TABLE ExpenseCategory""")
            database.execSQL("""DROP TABLE IncomeCategory""")
        }
    }

    val migration16to22: Migration = object : Migration(16, 22) {

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
                                            ON UPDATE CASCADE 
                                            ON DELETE NO ACTION , 
                                    FOREIGN KEY(`category`, `type`) 
                                        REFERENCES `Category`(`category`, `type`)
                                            ON UPDATE CASCADE 
                                            ON DELETE NO ACTION )""")
            database.execSQL("""INSERT INTO `Transaction_new` SELECT * FROM `Transaction`""")
            database.execSQL("""DROP TABLE `Transaction`""")
            database.execSQL("""ALTER TABLE `Transaction_new` RENAME TO `Transaction`""")
            database.execSQL("""CREATE INDEX IF NOT EXISTS index_cat_name_type
                                    ON `Transaction` (category, type)""")
            database.execSQL("""CREATE INDEX IF NOT EXISTS `index_account_name` 
                                    ON `Transaction` (`account`)""")
        }
    }
}