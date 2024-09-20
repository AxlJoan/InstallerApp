package com.example.installerapp

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.core.content.FileProvider



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
            downloadApk("https://f-droid.org/F-Droid.apk", "F-Droid.apk") {
                installApk("F-Droid.apk")
                downloadTermuxButton.isEnabled = true  // Habilitar botón de Termux
            }
        }

        // Evento para descargar Termux (una vez F-Droid esté instalado)
        downloadTermuxButton.setOnClickListener {
            downloadApk("https://f-droid.org/repo/com.termux_118.apk", "Termux.apk") {
                installApk("Termux.apk")
                executeTermuxCommandsButton.isEnabled = true  // Habilitar el botón de comandos
            }
        }

        // Evento para ejecutar comandos en Termux
        executeTermuxCommandsButton.setOnClickListener {
            executeTermuxCommands()  // Aquí ejecutarías tus comandos
        }
    }

    // Función para descargar el APK
    private fun downloadApk(url: String, apkName: String, onDownloadComplete: () -> Unit) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading $apkName")
            .setDescription("Please wait...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)
            .setAllowedOverMetered(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Downloading $apkName", Toast.LENGTH_SHORT).show()

        // Simulamos la finalización de la descarga para este ejemplo
        onDownloadComplete()  // Ejecuta el callback una vez descargado
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
        // Aquí irían los comandos que quieres ejecutar en Termux
        Toast.makeText(this, "Ejecutando comandos en Termux", Toast.LENGTH_SHORT).show()
        // Puedes interactuar con Termux usando Termux:API para ejecutar comandos específicos
    }
}
