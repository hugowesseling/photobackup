package com.hugowesseling.photobackup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hugowesseling.photobackup.ui.theme.PhotoBackupTheme

class ConfigurationListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoBackupTheme {
                ConfigurationListScreen(
                    onAddClick = {
                        startActivity(Intent(this, AddEditConfigurationActivity::class.java))
                    },
                    onEditClick = { config ->
                        val intent = Intent(this, AddEditConfigurationActivity::class.java).apply {
                            putExtra("config_id", config.id)
                        }
                        startActivity(intent)
                    },
                    onConfigClick = { config ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("config_id", config.id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationListScreen(
    onAddClick: () -> Unit,
    onEditClick: (Configuration) -> Unit,
    onConfigClick: (Configuration) -> Unit,
    viewModel: ConfigurationViewModel = viewModel()
) {
    val configurations by viewModel.allConfigurations.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurations") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Configuration")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(configurations) { config ->
                ConfigurationItem(
                    config = config,
                    onClick = { onConfigClick(config) },
                    onEdit = { onEditClick(config) },
                    onDelete = { viewModel.delete(config) }
                )
            }
        }
    }
}

@Composable
fun ConfigurationItem(
    config: Configuration,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = config.name, style = MaterialTheme.typography.titleMedium)
                Text(text = config.server, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
