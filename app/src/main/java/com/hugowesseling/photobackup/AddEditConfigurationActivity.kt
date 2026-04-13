package com.hugowesseling.photobackup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hugowesseling.photobackup.ui.theme.PhotoBackupTheme

class AddEditConfigurationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val configId = intent.getIntExtra("config_id", -1)

        setContent {
            PhotoBackupTheme {
                AddEditConfigurationScreen(
                    configId = if (configId == -1) null else configId,
                    onSave = { finish() },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditConfigurationScreen(
    configId: Int?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConfigurationViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var backupFolder by remember { mutableStateOf("") }
    var localFolder by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(configId) {
        if (configId != null) {
            val config = viewModel.getById(configId)
            if (config != null) {
                name = config.name
                server = config.server
                backupFolder = config.backupFolder
                localFolder = config.localFolder
                username = config.username
                password = config.password
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (configId == null) "Add Configuration" else "Edit Configuration") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = server,
                onValueChange = { server = it },
                label = { Text("Server") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = backupFolder,
                onValueChange = { backupFolder = it },
                label = { Text("Backup Folder (Server)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localFolder,
                onValueChange = { localFolder = it },
                label = { Text("Local Folder") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val config = Configuration(
                        id = configId ?: 0,
                        name = name,
                        server = server,
                        backupFolder = backupFolder,
                        localFolder = localFolder,
                        username = username,
                        password = password
                    )
                    if (configId == null) {
                        viewModel.insert(config)
                    } else {
                        viewModel.update(config)
                    }
                    onSave()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
