package com.example.service

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.work.WorkManager
import com.example.data.AppDatabase
import com.example.data.CouncilMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class PanicTileService : TileService() {
    
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "DEPRECATION")
    override fun onClick() {
        super.onClick()
        val context = applicationContext
        
        // Access the database directly to perform immediate lockdown
        val dao = AppDatabase.getDatabase(context).colonyDao()
        
        // Cancel all WorkManager jobs
        WorkManager.getInstance(context).cancelAllWork()
        
        // Stop Ktor server
        com.example.utils.ApiGateway.stopServer()
        
        CoroutineScope(Dispatchers.IO).launch {
            dao.haltAllSystems()
            dao.insertMessage(
                CouncilMessage(
                    role = "system",
                    content = "EMERGENCY LOCKDOWN: Wszystkie aktywne zadania zostały natychmiast przerwane za pomocą systemowego kafelka Szybkich Ustawień."
                )
            )
        }
        
        // Collapse notification panel/quick settings
        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(closeIntent)
    }

    
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        if (tile != null) {
            tile.state = android.service.quicksettings.Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }
}
