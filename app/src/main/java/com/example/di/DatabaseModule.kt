package com.example.di

import android.content.Context
import com.example.data.AgentDao
import com.example.data.AppDatabase

object DatabaseModule {

    @Volatile
    private var databaseInstance: AppDatabase? = null

    fun provideAppDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            databaseInstance = instance
            instance
        }
    }

    fun provideAgentDao(context: Context): AgentDao {
        return provideAppDatabase(context).agentDao()
    }
}
