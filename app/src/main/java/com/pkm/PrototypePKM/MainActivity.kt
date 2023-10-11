package com.pkm.PrototypePKM

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pkm.PrototypePKM.ui.navGraph.RootNavGraph
import com.pkm.PrototypePKM.ui.theme.PrototypePKMTheme

class MainActivity : ComponentActivity() {
    private val permissionsToRequest = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value

                if (isGranted) {
                    // Izin diberikan, lakukan tindakan yang diperlukan
                } else {
                    // Izin ditolak, tangani dengan baik
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Meminta izin-izin yang diperlukan saat aplikasi pertama kali dijalankan
        requestPermissions()

        setContent {
            PrototypePKMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavGraph(navController = rememberNavController())
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequestList = mutableListOf<String>()
        for (permission in permissionsToRequest) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequestList.add(permission)
            }
        }
        if (permissionsToRequestList.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequestList.toTypedArray())
        }
    }
}
