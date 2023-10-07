package com.pkm.PrototypePKM.ui.screen.feedback

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.pkm.PrototypePKM.BottomBarItem
import com.pkm.PrototypePKM.R
import com.pkm.PrototypePKM.ui.theme.PrototypePKMTheme


@Composable
fun FeedbackScreen(navController: NavHostController) {
    val context = LocalContext.current
    var textResult by remember { mutableStateOf("") }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            navController.navigate(BottomBarItem.Beranda.route)
        } else {
            textResult = result.contents
        }
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, do nothing here
            } else {
                Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    val showCamera = remember { mutableStateOf(false) }

    // Menggunakan LaunchedEffect untuk memulai pemindaian QR code secara otomatis saat showCamera berubah menjadi true
    LaunchedEffect(showCamera.value) {
        if (showCamera.value) {
            val option = ScanOptions()
            // option.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setCaptureActivity(AnyOrientationCaptureActivity.class);
            option.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            option.setPrompt("Scan a QR code")
            option.setCameraId(0)
            option.setBeepEnabled(false)
            option.setOrientationLocked(false)
            barcodeLauncher.launch(option)
        } else {
            // Memeriksa izin kamera saat showCamera berubah menjadi true
            checkCamPermission(
                activity = (context as Activity),
                requestPermissionLauncher = requestPermissionLauncher,
                permission = android.Manifest.permission.CAMERA,
                showCamera = showCamera
            )
        }
    }

    BackHandler {
        // Lakukan perutean kembali ke tampilan awal (halaman Beranda)
        navController.navigate(BottomBarItem.Beranda.route)
    }

    PrototypePKMTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // ...

                    // Hapus tombol "Scan QR Code"

                    Text(
                        text = textResult,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


class CaptureActivityPortrait {

}

private fun checkCamPermission(
    activity: Activity,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    permission: String,
    showCamera: MutableState<Boolean>
) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
        showCamera.value = true
    } else if (androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

    } else {
        requestPermissionLauncher.launch(permission)
    }
}

@Preview
@Composable
fun FBSPrev() {
    val navController = rememberNavController() // Inisialisasi NavController
    MaterialTheme {
        Surface {
            FeedbackScreen(navController) // Kirim navController sebagai parameter
        }
    }
}
