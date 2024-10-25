package com.great_systems.downloadfiletest

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Start download in a background thread to avoid blocking the main thread
        Thread {
            try {
                downloadFile("http://gsys.ru/test.zip", "downloaded_test.zip")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun downloadFile(fileUrl: String, fileName: String) {
        val url = URL(fileUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.connect()  // Connect to the URL

        // Check for successful response code (200)
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("DownloadActivity", "Server returned HTTP ${connection.responseCode}")
            return
        }

        // Path to save the downloaded file
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (!filePath.exists()) {
            val rez = filePath.mkdirs()
            Log.d("dlk", rez.toString())
            // val answ = Files.createFile(outFile.toPath())
        }

        // Input and output streams for downloading and saving the file
        val inputStream: InputStream = connection.inputStream
        val outputStream: OutputStream = FileOutputStream(filePath)

        val buffer = ByteArray(4096) // Buffer size for reading
        var bytesRead: Int

        // Read data from InputStream into buffer and write it to OutputStream
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        // Close streams
        outputStream.flush()
        outputStream.close()
        inputStream.close()

        Log.d("DownloadActivity", "File downloaded to ${filePath.absolutePath}")
    }

}