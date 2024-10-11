package com.example.installerapp

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import eu.chainfire.libsuperuser.Shell


class MainActivity : AppCompatActivity() {

    private lateinit var downloadFdroidButton: Button
    private lateinit var downloadTermuxButton: Button
    private lateinit var executeTermuxCommandsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos los botones
        downloadFdroidButton = findViewById(R.id.downloadFdroidButton)
        downloadTermuxButton = findViewById(R.id.downloadTermuxButton)
        executeTermuxCommandsButton = findViewById(R.id.executeTermuxCommandsButton)

        // Evento para descargar F-Droid
        downloadFdroidButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                downloadAndInstallApk("https://f-droid.org/F-Droid.apk", "F-Droid.apk") {
                    withContext(Dispatchers.Main) {
                        installApk("F-Droid.apk")
                        downloadTermuxButton.isEnabled = true  // Habilitar botón de Termux
                    }
                }
            }
        }

        // Evento para descargar Termux (una vez F-Droid esté instalado)
        downloadTermuxButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                downloadAndInstallApk("https://f-droid.org/repo/com.termux_118.apk", "Termux.apk") {
                    withContext(Dispatchers.Main) {
                        installApk("Termux.apk")
                        executeTermuxCommandsButton.isEnabled = true  // Habilitar el botón de comandos
                    }
                }
            }
        }

        // Evento para ejecutar comandos en Termux
        executeTermuxCommandsButton.setOnClickListener {
            executeTermuxCommands()
        }
    }

    // Función para descargar e instalar el APK
    private suspend fun downloadAndInstallApk(url: String, fileName: String, onDownloadComplete: suspend () -> Unit) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, "Descargando $fileName...", Toast.LENGTH_SHORT).show()
        }

        // Descargar el archivo
        val downloaded = downloadApk(url, fileName)

        if (downloaded) {
            onDownloadComplete()  // Llamar al callback solo si la descarga fue exitosa
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error al descargar $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para descargar el APK
    private fun downloadApk(url: String, apkName: String): Boolean {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading $apkName")
            .setDescription("Please wait...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)
            .setAllowedOverMetered(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                // Verificamos si las columnas existen
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        return true
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                        return false
                    }
                }
            }
            cursor.close()
        }

        return false
    }

    // Función para instalar el APK
    private fun installApk(apkName: String) {
        val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName)
        val uri = FileProvider.getUriForFile(this, "com.example.installerapp.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // Función para ejecutar comandos en Termux (lógica a definir después)
    private fun executeTermuxCommands() {
        // Comandos que deseas ejecutar

        val commands = listOf(
            "pkg update -y",
            "pkg install git -y",
            "git clone https://github.com/AxlJoan/Extractor.git",
            "cd Extractor",
            "chmod +x main.sh sacarCombinado.sh",
            "./main.sh"
        )

        // Ejecutar comandos usando libsuperuser
        //val shell = Shell.SU
        //val output = shell.run(commands)

        //if (output.isSuccessful) {
        //    Toast.makeText(this, "Comandos ejecutados correctamente", Toast.LENGTH_SHORT).show()
        //} else {
        //    Toast.makeText(this, "Error al ejecutar comandos", Toast.LENGTH_SHORT).show()
        //}
    }

}
