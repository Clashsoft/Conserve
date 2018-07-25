package com.gemini.energy.service

import android.content.Context
import android.os.Environment
import android.util.Log
import com.gemini.energy.domain.entity.Computable
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class DataHolder {
    var header: MutableList<String>? = mutableListOf()
    var rows: MutableList<Map<String, String>>? = mutableListOf()
    lateinit var computable: Computable<*>

    fun path() = StringBuilder()
            .append("${computable.auditName.toLowerCase().replace("\\s+".toRegex(), "_")}/")
            .append("${computable.zoneName.toLowerCase().replace("\\s+".toRegex(), "_")}/")
            .append("${computable.auditScopeType?.value?.toLowerCase()}_")
            .append("${computable.auditScopeSubType?.toString()?.toLowerCase()}_")
            .append("${computable.auditScopeName.toLowerCase().replace("[^a-zA-Z0-9]".toRegex(), "_")}/")
            .toString()
}

class OutgoingRows(private val context: Context) {

    private lateinit var filePath: File
    lateinit var computable: Computable<*>

    lateinit var dataHolder: MutableList<DataHolder>

    /**
     * Saves the Data to the Internal File System
     * */
    fun saveFile() {

        // Step 1: Loop over through the Data Holder
        // Step 2: Extract out the individual components
        // Step 3: Feed those components to the data method
        // Step 4: Collect those data string in a list
        // Step 5: Concatenate the list and write that to the file

        Log.d(TAG, dataHolder.count().toString())
        dataHolder.forEach { eachData ->

            val outgoing = StringBuilder()
            val header = eachData.header
            val rows = eachData.rows

            Log.d(TAG, header.toString())
            Log.d(TAG, rows.toString())
            outgoing.append(data(header, rows))

            setFilePath(eachData.path(), "${Date().time}.csv")

            val data = outgoing.toString()

            try {
                val inputStream = ByteArrayInputStream(data.toByteArray())
                val outputStream = BufferedOutputStream(FileOutputStream(filePath))

                val buffer = ByteArray(1024)
                var bytesRead = inputStream.read(buffer, 0, buffer.size)

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer, 0, buffer.size)
                }

                inputStream.close()
                outputStream.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Utility Methods
     * */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    private fun parseFilenameFromPath(filePath: String): String {
        val index = filePath.lastIndexOf('/') + 1
        return filePath.substring(index)
    }



    private fun getDocumentFolderPath(subFolderPath: String? = null): File? {

        val folderDir: File?

        if (isExternalStorageWritable()) {
            Log.d(TAG, "External - Storage :: Writable")
            if (subFolderPath == null) {
                folderDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).path)
            } else {
                folderDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).path + "/$subFolderPath")
            }
        } else {
            Log.d(TAG, "External - Storage :: Not Writable")
            if (subFolderPath == null) {
                folderDir = File(context.filesDir.path + "/Documents")
            } else {
                folderDir = File(context.filesDir.path + "/Documents/$subFolderPath")
            }
        }

        if (!folderDir.isDirectory) {
            Log.d(TAG, "****** Creating Directory *******")
            Log.d(TAG, folderDir.toString())

            folderDir.mkdirs()
        }

        Log.d(TAG, folderDir.toString())

        return folderDir
    }


    private fun setFilePath(path: String, fileName: String) {
        val directory = getDocumentFolderPath("gemini/$path")!!
        this.filePath = File(directory.toString(), fileName)

        if (this.filePath.exists()) {
            Log.d(TAG, "File Exists")
        } else {
            Log.d(TAG, "File Does Not Exists")
        }
    }

    private fun data(header: List<String>?, rows: MutableList<Map<String, String>>?): String {
        val buffer = StringBuilder()
        buffer.append(header?.joinToString())
        buffer.append("\r\n")

        for (row in rows!!) {
            val tmp: MutableList<String> = mutableListOf()
            header?.forEach { item ->
                tmp.add(row[item] ?: "")
            }
            buffer.append(tmp.joinToString())
            buffer.append("\r\n")
        }

        return buffer.toString()
    }

    companion object {
        private const val TAG = "OutgoingRows"
    }

}