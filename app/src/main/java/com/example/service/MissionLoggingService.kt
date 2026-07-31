package com.example.service

import com.example.data.ColonyDao
import com.example.data.MissionStateLog
import kotlinx.coroutines.flow.Flow

class MissionLoggingService(private val colonyDao: ColonyDao) {

    suspend fun logStateTransition(
        missionId: Int,
        agentName: String,
        previousState: String,
        newState: String,
        message: String
    ) {
        val log = MissionStateLog(
            missionId = missionId,
            agentName = agentName,
            previousState = previousState,
            newState = newState,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        colonyDao.insertMissionStateLog(log)
    }

    fun getAllLogs(): Flow<List<MissionStateLog>> = colonyDao.getAllMissionStateLogs()

    fun getLogsForMission(missionId: Int): Flow<List<MissionStateLog>> =
        colonyDao.getMissionStateLogsForMission(missionId)
}
