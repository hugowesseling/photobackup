package com.hugowesseling.photobackup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hugowesseling.photobackup.ui.theme.PhotoBackupTheme
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val configId = intent.getIntExtra("config_id", -1)
        
        setContent {
            PhotoBackupTheme {
                MainBackupScreen(
                    configId = configId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBackupScreen(
    configId: Int,
    onBack: () -> Unit,
    configViewModel: ConfigurationViewModel = viewModel(),
    backupViewModel: BackupViewModel = viewModel()
) {
    var configuration by remember { mutableStateOf<Configuration?>(null) }

    LaunchedEffect(configId) {
        if (configId != -1) {
            configuration = configViewModel.getById(configId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(configuration?.name ?: "Backup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (configuration == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Local Status (Top Left)
                    StatusSection(
                        title = "Local",
                        folder = configuration!!.localFolder,
                        totalSize = backupViewModel.localTotalSize,
                        secondarySize = backupViewModel.localToBackupSize,
                        secondaryLabel = "To backup",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Server Status (Top Right)
                    StatusSection(
                        title = "Server",
                        folder = configuration!!.backupFolder,
                        totalSize = backupViewModel.serverTotalSize,
                        secondarySize = backupViewModel.serverBackedUpSize,
                        secondaryLabel = "Backed up",
                        modifier = Modifier.weight(1f)
                    )
                }

                // General Things (Bottom)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = backupViewModel.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (backupViewModel.isRefreshing || backupViewModel.isBackingUp) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { backupViewModel.refresh(configuration!!) },
                            modifier = Modifier.weight(1f),
                            enabled = !backupViewModel.isRefreshing && !backupViewModel.isBackingUp
                        ) {
                            Text("Refresh")
                        }
                        Button(
                            onClick = { backupViewModel.startBackup(configuration!!) },
                            modifier = Modifier.weight(1f),
                            enabled = !backupViewModel.isRefreshing && !backupViewModel.isBackingUp
                        ) {
                            Text("Backup")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSection(
    title: String,
    folder: String,
    totalSize: Long,
    secondarySize: Long,
    secondaryLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = folder, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Total: ${formatFileSize(totalSize)}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "$secondaryLabel: ${formatFileSize(secondarySize)}", style = MaterialTheme.typography.bodyMedium)
    }
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
