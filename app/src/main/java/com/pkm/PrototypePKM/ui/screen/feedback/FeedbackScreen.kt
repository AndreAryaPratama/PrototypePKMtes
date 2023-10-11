package com.pkm.PrototypePKM.ui.screen.feedback

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.pkm.PrototypePKM.BottomBarItem
import com.pkm.PrototypePKM.R
import com.pkm.PrototypePKM.ui.theme.PrototypePKMTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavHostController) {
    // State untuk menyimpan tanggal yang dipilih oleh pengguna


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
            option.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
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

    if(showCamera.value){
        if(textResult.isNotEmpty()){
            if (textResult == "Miaw") {
                FeedbackContent(textResult, navController)
                Toast.makeText(context, "QR Code Valid", Toast.LENGTH_SHORT).show()
            } else {
                navController.navigate(BottomBarItem.Beranda.route)
                Toast.makeText(context, "Invalid QR Code", Toast.LENGTH_SHORT).show()
            }
        }else{
            //Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
        }
    }else{
        Toast.makeText(context, "Scanning", Toast.LENGTH_SHORT).show()
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackContent(textResult: String, navController: NavHostController) {
    PrototypePKMTheme {
        val context = LocalContext.current
        var text by remember { mutableStateOf(TextFieldValue()) }
        val selectedDateText = remember { mutableStateOf("Pilih tanggal") }
        val selectedFile = remember { mutableStateOf<String?>(null) }
        val showDialog = remember { mutableStateOf(false) }
        var showOptionsDialog by remember { mutableStateOf(false) }
        val contentResolver = context.contentResolver
        val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        })
        val selectedDate = datePickerState.selectedDateMillis?.let {
            convertMillisToDate(it)
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedImageUri = data?.data
                selectedFile.value = selectedImageUri?.toString()

                if (selectedImageUri != null) {
                    // Jika gambar dipilih dari galeri, dapatkan nama file dari URI
                    val fileName = getFileNameFromUri(contentResolver, selectedImageUri)
                    selectedFile.value = fileName
                }
            }
        }

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Izin diberikan, Anda sekarang dapat mengakses gambar
                val galleryIntent = Intent(Intent.ACTION_PICK)
                galleryIntent.type = "image/*"
                launcher.launch(galleryIntent)
            } else {
                // Izin ditolak, tangani dengan baik
                // Anda dapat menampilkan pesan kepada pengguna atau mengambil tindakan yang sesuai
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(1.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp) // Sesuaikan ukuran ikon
                )
                Text(
                    text = textResult, // Teks yang ingin Anda tambahkan
                    modifier = Modifier.padding(start = 8.dp), // Sesuaikan jarak antara ikon dan teks
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Divider(
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxSize()
                    .border(width = 1.dp, color = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState()),
                value = text.text,
                onValueChange = {
                    text = text.copy(text = it)
                },
                textStyle = TextStyle(color = Color.Black),
                placeholder = {
                    Text("Input Feedback")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {

                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxSize()){
                Row(modifier = Modifier.padding(8.dp)){

                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(0.1f))

                    Text(
                        text = selectedDateText.value,
                        color = if (selectedDate.isNullOrEmpty()) {
                            Color.Gray
                        } else {
                            Color.Black
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { showDialog.value = true } // Update the value of showDialog
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Check if the dialog should be shown
                    if (showDialog.value) { // Access the value property
                        DatePickerDialog(
                            onDismissRequest = {
                                // Dismiss the dialog when requested
                                showDialog.value = false // Update the value of showDialog
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Handle date selection
                                        selectedDate?.let {
                                            // Perbarui teks label dengan tanggal yang terpilih
                                            selectedDateText.value = it
                                        }
                                        showDialog.value = false // Update the value of showDialog to dismiss the dialog
                                    }
                                ) {
                                    Text(text = "OK")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        showDialog.value = false // Update the value of showDialog to dismiss the dialog
                                    }
                                ) {
                                    Text(text = "Cancel")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerState
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxSize()){
                Row(modifier = Modifier.padding(8.dp)) {
                    Icon(
                        contentDescription = null,
                        painter = painterResource(id = R.drawable.ic_cam),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.weight(1f)
                            .align(Alignment.CenterVertically)
                    ){
                        Text(
                            text = selectedFile.value ?: "Foto",
                            color = if (selectedFile.value.isNullOrEmpty()) {
                                Color.Gray
                            } else {
                                Color.Black
                            },
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.1f))

                    Button(
                        onClick = {
                            showOptionsDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cam),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                }
            ) {
                Text("Kirim")
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }

            if (showOptionsDialog) {
                AlertDialog(
                    onDismissRequest = { showOptionsDialog = false },
                    title = {
                        Text(text = "Pilih Sumber Gambar")
                    },
                    text = {
                        val fileName = selectedFile.value?.substringAfterLast('%')
                        Text(text = fileName ?: "Pilih sumber gambar untuk mengunggah")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (PermissionChecker.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    ) == PermissionChecker.PERMISSION_GRANTED
                                ) {
                                    val galleryIntent = Intent(Intent.ACTION_PICK)
                                    galleryIntent.type = "image/*"
                                    launcher.launch(galleryIntent)
                                } else {
                                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                                showOptionsDialog = false
                            }
                        ) {
                            Text(text = "Dari Galeri")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                launcher.launch(cameraIntent)
                                showOptionsDialog = false
                            }
                        ) {
                            Text(text = "Ambil Foto")
                        }
                    }
                )
            }
        }
    }
}
private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    return formatter.format(Date(millis))
}

private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)

    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                val displayName = it.getString(displayNameIndex)
                return displayName
            }
        }
    }

    return null
}
