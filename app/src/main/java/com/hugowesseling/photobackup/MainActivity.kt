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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hugowesseling.photobackup.ui.theme.PhotoBackupTheme

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
    viewModel: ConfigurationViewModel = viewModel()
) {
    var configuration by remember { mutableStateOf<Configuration?>(null) }

    LaunchedEffect(configId) {
        if (configId != -1) {
            configuration = viewModel.getById(configId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup Process") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (configuration != null) {
                Text(text = "Backing up: ${configuration?.name}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Server: ${configuration?.server}")
                Text(text = "Remote Path: ${configuration?.backupFolder}")
                Text(text = "Local Path: ${configuration?.localFolder}")
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { /* Start backup logic */ }) {
                    Text("Start Backup")
                }
            } else {
                Text(text = "No configuration selected or found.")
            }
        }
    }
}
