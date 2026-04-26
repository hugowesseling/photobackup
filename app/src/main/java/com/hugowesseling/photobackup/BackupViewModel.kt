package com.hugowesseling.photobackup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

data class FileMetadata(val size: Long, val path: String)

class BackupViewModel : ViewModel() {
    var localTotalSize by mutableStateOf(0L)
    var localToBackupSize by mutableStateOf(0L)
    var serverTotalSize by mutableStateOf(0L)
    var serverBackedUpSize by mutableStateOf(0L)
    var isRefreshing by mutableStateOf(false)
    var isBackingUp by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    
    var filesToUpload by mutableStateOf(listOf<FileMetadata>())
        private set

    private var localFiles = listOf<FileMetadata>()
    private var serverFiles = listOf<FileMetadata>()

    fun refresh(config: Configuration) {
        viewModelScope.launch {
            isRefreshing = true
            statusMessage = "Refreshing..."
            try {
                localFiles = getLocalFiles(config.localFolder)
                serverFiles = getServerFiles(config)
                
                localTotalSize = localFiles.sumOf { it.size }
                serverTotalSize = serverFiles.sumOf { it.size }
                
                val serverFileMap = serverFiles.associateBy { it.path }
                filesToUpload = localFiles.filter { local ->
                    val remote = serverFileMap[local.path]
                    remote == null || remote.size != local.size
                }
                
                localToBackupSize = filesToUpload.sumOf { it.size }
                serverBackedUpSize = localTotalSize - localToBackupSize
                
                statusMessage = "Refresh complete. ${filesToUpload.size} files to backup."
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun startBackup(config: Configuration) {
        viewModelScope.launch {
            if (filesToUpload.isEmpty()) {
                statusMessage = "Nothing to backup."
                return@launch
            }
            isBackingUp = true
            statusMessage = "Starting backup..."
            try {
                uploadFiles(config, filesToUpload)
                statusMessage = "Backup complete!"
                refresh(config) // Refresh after backup
            } catch (e: Exception) {
                statusMessage = "Backup failed: ${e.message}"
                e.printStackTrace()
            } finally {
                isBackingUp = false
            }
        }
    }

    private suspend fun getLocalFiles(rootPath: String): List<FileMetadata> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) return@withContext emptyList<FileMetadata>()

        root.walkTopDown()
            .filter { file ->
                file.isFile && !file.name.startsWith(".trashed")
            }
            .map { file ->
                FileMetadata(
                    size = file.length(),
                    path = file.absolutePath.removePrefix(root.absolutePath).trimStart('/')
                )
            }.toList()
    }

    private suspend fun getServerFiles(config: Configuration): List<FileMetadata> = withContext(Dispatchers.IO) {
        val jsch = JSch()
        var session: Session? = null
        try {
            session = jsch.getSession(config.username, config.server, 22)
            session.setPassword(config.password)
            val properties = Properties()
            properties["StrictHostKeyChecking"] = "no"
            session.setConfig(properties)
            session.connect()

            val channel = session.openChannel("exec") as ChannelExec
            val command = "find \"${config.backupFolder}\" -type f -printf \"%s %p\\n\""
            channel.setCommand(command)
            val inputStream = channel.inputStream
            channel.connect()

            val result = inputStream.bufferedReader().use { it.readText() }
            channel.disconnect()

            result.lines().filter { it.isNotBlank() }.mapNotNull { line ->
                val parts = line.split(" ", limit = 2)
                if (parts.size == 2) {
                    val size = parts[0].toLongOrNull() ?: 0L
                    val fullPath = parts[1]
                    val relativePath = fullPath.removePrefix(config.backupFolder).trimStart('/')
                    FileMetadata(size, relativePath)
                } else null
            }
        } finally {
            session?.disconnect()
        }
    }

    private suspend fun uploadFiles(config: Configuration, files: List<FileMetadata>) = withContext(Dispatchers.IO) {
        val jsch = JSch()
        var session: Session? = null
        try {
            session = jsch.getSession(config.username, config.server, 22)
            session.setPassword(config.password)
            val properties = Properties()
            properties["StrictHostKeyChecking"] = "no"
            session.setConfig(properties)
            session.connect()

            val channelSftp = session.openChannel("sftp") as ChannelSftp
            channelSftp.connect()

            files.forEachIndexed { index, fileMetadata ->
                val localFile = File(config.localFolder, fileMetadata.path)
                val remoteFilePath = "${config.backupFolder}/${fileMetadata.path}"
                
                // Create remote directories if they don't exist
                val remoteDir = remoteFilePath.substringBeforeLast('/')
                createRemoteDirs(channelSftp, remoteDir)
                
                statusMessage = "Uploading (${index + 1}/${files.size}): ${fileMetadata.path}"
                channelSftp.put(localFile.absolutePath, remoteFilePath)
                
                // Update progress dynamically
                serverBackedUpSize += fileMetadata.size
                localToBackupSize -= fileMetadata.size
            }
            channelSftp.disconnect()
        } finally {
            session?.disconnect()
        }
    }

    private fun createRemoteDirs(sftp: ChannelSftp, path: String) {
        val dirs = path.split("/").filter { it.isNotEmpty() }
        var currentPath = ""
        for (dir in dirs) {
            currentPath += "/$dir"
            try {
                sftp.mkdir(currentPath)
            } catch (e: Exception) {
                // Directory likely already exists
            }
        }
    }
}
