package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

data class AppPermissionInfo(
    val packageName: String,
    val appName: String,
    val riskScore: Int,
    val grantedDangerousPermissions: List<String>,
    val isSystemApp: Boolean
)

object PermissionScannerAgent {

    fun scanDevicePermissions(context: Context): List<AppPermissionInfo> {
        val pm = context.packageManager
        val results = mutableListOf<AppPermissionInfo>()
        
        try {
            @Suppress("DEPRECATION")
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkg in packages) {
                if (pkg.packageName == context.packageName) continue
                
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val appLabel = pm.getApplicationLabel(appInfo).toString()
                
                val requestedPermissions = pkg.requestedPermissions ?: continue
                val requestedFlags = pkg.requestedPermissionsFlags
                
                val grantedDangerous = mutableListOf<String>()
                var score = 0
                
                for (i in requestedPermissions.indices) {
                    val permission = requestedPermissions[i]
                    val isGranted = if (requestedFlags != null && i < requestedFlags.size) {
                        (requestedFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                    } else {
                        pm.checkPermission(permission, pkg.packageName) == PackageManager.PERMISSION_GRANTED
                    }
                    
                    if (isGranted) {
                        when (permission) {
                            android.Manifest.permission.RECORD_AUDIO -> {
                                if (!grantedDangerous.contains("Microphone")) {
                                    grantedDangerous.add("Microphone")
                                    score += 25
                                }
                            }
                            android.Manifest.permission.CAMERA -> {
                                if (!grantedDangerous.contains("Camera")) {
                                    grantedDangerous.add("Camera")
                                    score += 25
                                }
                            }
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                if (!grantedDangerous.contains("Location")) {
                                    grantedDangerous.add("Location")
                                    score += 20
                                }
                            }
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.WRITE_CONTACTS -> {
                                if (!grantedDangerous.contains("Contacts")) {
                                    grantedDangerous.add("Contacts")
                                    score += 15
                                }
                            }
                            android.Manifest.permission.READ_CALENDAR,
                            android.Manifest.permission.WRITE_CALENDAR -> {
                                if (!grantedDangerous.contains("Calendar")) {
                                    grantedDangerous.add("Calendar")
                                    score += 15
                                }
                            }
                        }
                    }
                }
                
                val finalScore = score.coerceAtMost(100)
                
                if (grantedDangerous.isNotEmpty()) {
                    results.add(
                        AppPermissionInfo(
                            packageName = pkg.packageName,
                            appName = appLabel,
                            riskScore = finalScore,
                            grantedDangerousPermissions = grantedDangerous,
                            isSystemApp = isSystem
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return results.sortedByDescending { it.riskScore }
    }
}
