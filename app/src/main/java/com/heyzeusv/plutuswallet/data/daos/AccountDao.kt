package com.heyzeusv.plutuswallet.data.daos

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.data.model.Account
import kotlinx.coroutines.flow.Flow

/**
 *  Queries that can be applied to Account table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class AccountDao : BaseDao<Account>() {

    /**
     *  Returns a list of Account names in alphabetical order.
     */
    @Query("""SELECT name
              FROM `account`
              ORDER BY name ASC""")
    abstract fun getAccountNames(): Flow<List<String>>

    /**
     *  Returns a list of Accounts used by a Transaction
     */
    @Query("""SELECT DISTINCT `account`.id, `account`.name
              FROM `account`
              INNER JOIN `transaction` ON `transaction`.account = `account`.name
              ORDER BY name ASC""")
    abstract fun getAccountsUsed(): Flow<List<Account>>

    /**
     *  Returns the size of table.
     */
    @Query("""SELECT COUNT(*)
              FROM `account`""")
    abstract suspend fun getAccountSize(): Int

    /**
     *  Returns flow that emits list of all Accounts in order of name.
     */
    @Query("""SELECT *
              FROM `account`
              ORDER BY name ASC""")
    abstract fun getAccounts(): Flow<List<Account>>
}