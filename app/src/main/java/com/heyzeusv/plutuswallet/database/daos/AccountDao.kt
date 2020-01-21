package com.heyzeusv.plutuswallet.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.database.entities.Account

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
     *  @return LiveData object holding list of all Accounts in order of account.
     */
    @Query("""SELECT *
              FROM account
              ORDER BY account ASC""")
    abstract fun getLDAccounts() : LiveData<List<Account>>

    /**
     *  @return size of table or null if empty.
     */
    @Query("""SELECT COUNT(*)
              FROM account""")
    abstract suspend fun getAccountSize() : Int?

    /**
     *  @return list holding names of Accounts.
     */
    @Query("""SELECT account
              FROM account
              ORDER BY account ASC""")
    abstract suspend fun getAccounts() : MutableList<String>
}