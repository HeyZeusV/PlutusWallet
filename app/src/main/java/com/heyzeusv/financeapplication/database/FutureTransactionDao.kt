package com.heyzeusv.financeapplication.database

import androidx.room.Dao
import com.heyzeusv.financeapplication.FutureTransaction

/*
    All the queries that can be applied to the database
    Can have as many as needed, make returnType nullable in case table is empty!
    Using LiveData signals Room to run on background thread, LiveData object will handle sending
        the crime data over to the main thread and notify any observers
*/

@Dao
interface FutureTransactionDao : BaseDao<FutureTransaction> {
}