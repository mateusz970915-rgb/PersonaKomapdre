package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Agent::class,
        CouncilMessage::class,
        ColonyMemory::class,
        AgentDecision::class,
        Mission::class,
        SubTask::class,
        DataAccessRequest::class,
        CalendarEvent::class,
        Badge::class,
        InterAgentMessage::class,
        AgentMilestone::class,
        RuleNodeEntity::class,
        RuleConnectionEntity::class,
        MissionStateLog::class,
        AgentNegotiationProposal::class,
        AgentMeshTelemetry::class,
        AgentKnowledgeEdge::class,
        AgentHeuristicRule::class,
        LlmCallTelemetry::class,
        FinanceTransaction::class,
        CustomAgentDefinition::class,
        Flashcard::class,
        Subscription::class,
        SleepRecord::class,
        AgentSentimentLog::class,
        ChartAnnotation::class,
        AgentInteractionFtsContent::class,
        SelfHealingProposal::class,
        WorkflowDag::class,
        ColonyProfile::class,
        VectorEmbeddingLog::class,
        HallucinationAuditLog::class,
        AgentPersona::class,
        UserAgentMessage::class,
        PersonaAgent::class
    ],
    version = 33,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun colonyDao(): ColonyDao
    abstract fun agentDao(): AgentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure sub_tasks table retains completedAt column or existing schema without dropping tables
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sub_tasks_assignedAgent` ON `sub_tasks` (`assignedAgent`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mission_state_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `missionId` INTEGER NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `previousState` TEXT NOT NULL,
                        `newState` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`missionId`) REFERENCES `missions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mission_state_logs_missionId` ON `mission_state_logs` (`missionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mission_state_logs_agentName` ON `mission_state_logs` (`agentName`)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `statusNotes` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_negotiations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `missionId` INTEGER NOT NULL,
                        `proposerAgent` TEXT NOT NULL,
                        `targetAgent` TEXT NOT NULL,
                        `proposedAction` TEXT NOT NULL,
                        `counterProposal` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `conflictTopic` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_mesh_telemetry` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `agentId` INTEGER NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `latencyMs` INTEGER NOT NULL,
                        `cpuLoadPct` REAL NOT NULL,
                        `memoryUsageMb` REAL NOT NULL,
                        `activeConnectionsCount` INTEGER NOT NULL,
                        `healthStatus` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_knowledge_edges` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sourceLabel` TEXT NOT NULL,
                        `sourceType` TEXT NOT NULL,
                        `targetLabel` TEXT NOT NULL,
                        `targetType` TEXT NOT NULL,
                        `relationType` TEXT NOT NULL,
                        `weight` REAL NOT NULL,
                        `creatorAgent` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_heuristics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `heuristicKey` TEXT NOT NULL,
                        `patternTarget` TEXT NOT NULL,
                        `confidenceScore` REAL NOT NULL,
                        `successCount` INTEGER NOT NULL,
                        `failureCount` INTEGER NOT NULL,
                        `adaptedPolicy` TEXT NOT NULL,
                        `generation` INTEGER NOT NULL,
                        `lastEvolvedTimestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `llm_call_telemetry` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `provider` TEXT NOT NULL,
                        `model` TEXT NOT NULL,
                        `promptLength` INTEGER NOT NULL,
                        `responseLength` INTEGER NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `errorMessage` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `data_access_requests` ADD COLUMN `requiresUserApproval` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `data_access_requests` ADD COLUMN `approvalStatus` TEXT NOT NULL DEFAULT 'Approved'")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `finance_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `category` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_agent_definitions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `systemPrompt` TEXT NOT NULL,
                        `temperature` REAL NOT NULL,
                        `toolsAccess` TEXT NOT NULL,
                        `autonomyLevel` TEXT NOT NULL DEFAULT 'Medium',
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `flashcards` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `question` TEXT NOT NULL,
                        `answer` TEXT NOT NULL,
                        `interval` INTEGER NOT NULL DEFAULT 1,
                        `repetition` INTEGER NOT NULL DEFAULT 0,
                        `easinessFactor` REAL NOT NULL DEFAULT 2.5,
                        `nextReviewTime` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subscriptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `category` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL DEFAULT 'Monthly',
                        `isCancelled` INTEGER NOT NULL DEFAULT 0,
                        `nextBillingDate` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sleep_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `sleepDurationHours` REAL NOT NULL,
                        `deepSleepMinutes` INTEGER NOT NULL,
                        `remSleepMinutes` INTEGER NOT NULL,
                        `lightSleepMinutes` INTEGER NOT NULL,
                        `recoveryScore` INTEGER NOT NULL,
                        `heartRateAvg` INTEGER NOT NULL,
                        `recommendation` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `personaDescription` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `lastActiveTimestamp` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `avatarUrl` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `configurationJson` TEXT NOT NULL DEFAULT '{}'")
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_sentiment_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `emoji` TEXT NOT NULL,
                        `moodTitle` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_sentiment_logs_agentName` ON `agent_sentiment_logs` (`agentName`)")
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'General'")
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `failoverAgentId` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `agents` ADD COLUMN `maxLatencyThresholdMs` INTEGER NOT NULL DEFAULT 5000")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chart_annotations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `tag` TEXT NOT NULL,
                        `agentId` INTEGER,
                        `colorHex` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_interaction_fts_content` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `snippet` TEXT NOT NULL,
                        `modelUsed` TEXT NOT NULL,
                        `tag` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS `agent_interaction_fts` USING fts5(
                        `agentName`,
                        `snippet`,
                        `modelUsed`,
                        `tag`,
                        content=`agent_interaction_fts_content`,
                        content_rowid=`id`
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_28_29: Migration = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 29 was added destructively previously, creating the missing tables
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `SelfHealingProposal` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `missionId` INTEGER NOT NULL,
                        `errorLogSnippet` TEXT NOT NULL,
                        `rootCauseAnalysis` TEXT NOT NULL,
                        `proposedCodeFix` TEXT NOT NULL,
                        `architectAgentName` TEXT NOT NULL,
                        `status` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `WorkflowDag` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `nodesJson` TEXT NOT NULL,
                        `edgesJson` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ColonyProfile` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `isCurrentActive` INTEGER NOT NULL,
                        `agentCount` INTEGER NOT NULL,
                        `createdTimestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `VectorEmbeddingLog` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sourceText` TEXT NOT NULL,
                        `tag` TEXT NOT NULL,
                        `embeddingVectorJson` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `HallucinationAuditLog` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `promptText` TEXT NOT NULL,
                        `responseText` TEXT NOT NULL,
                        `factCheckScore` REAL NOT NULL,
                        `verdict` TEXT NOT NULL,
                        `checkedClaimsJson` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_29_30: Migration = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `ColonyProfile` ADD COLUMN `metadataJson` TEXT NOT NULL DEFAULT '{}'")
            }
        }

        val MIGRATION_30_31: Migration = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_personas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `characterTraits` TEXT NOT NULL,
                        `operationalRole` TEXT NOT NULL,
                        `communicationStyle` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "colony_database"
                )
                .addMigrations(
                    MIGRATION_9_10, 
                    MIGRATION_10_11, 
                    MIGRATION_11_12, 
                    MIGRATION_12_13, 
                    MIGRATION_13_14, 
                    MIGRATION_14_15, 
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

