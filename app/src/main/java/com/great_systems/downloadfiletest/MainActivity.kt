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
                downloadFile("http://gsys.ru/test.exe", "test.exe")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun download(link: String, path: String, progress: ((Long, Long) -> Unit)? = null): Long {
        val url = URL(link)
        val connection = url.openConnection()
        connection.connect()
        val length = connection.contentLengthLong
        url.openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead = input.read(buffer)
                var bytesCopied = 0L
                while (bytesRead >= 0) {
                    output.write(buffer, 0, bytesRead)
                    bytesCopied += bytesRead
                    progress?.invoke(bytesCopied, length)
                    bytesRead = input.read(buffer)
                }
                return bytesCopied
            }
        }
    }


    fun DownloadFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val u = URL("http://gsys.ru/test.exe")
            val inpStream: InputStream = u.openStream()
            val dis = DataInputStream(inpStream)
            val buffer = ByteArray(1024)
            var length: Int
            val fileToSave = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/test.kml"

            val outFile = File(fileToSave)
            if (!outFile.exists()) {
               val rez = outFile.mkdirs()
                Log.d("dlk", rez.toString())
                // val answ = Files.createFile(outFile.toPath())
            }

            val fos = FileOutputStream(outFile)
            while (dis.read(buffer).also { length = it } > 0) {
                fos.write(buffer, 0, length)
            }



        } catch (mue: MalformedURLException) {
            Log.e("SYNC getUpdate", "malformed url error", mue)
        } catch (ioe: IOException) {
            Log.e("SYNC getUpdate", "io error", ioe)
        } catch (se: SecurityException) {
            Log.e("SYNC getUpdate", "security error", se)
        }
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