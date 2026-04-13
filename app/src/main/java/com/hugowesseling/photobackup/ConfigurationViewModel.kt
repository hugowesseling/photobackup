package com.hugowesseling.photobackup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).configurationDao()
    val allConfigurations: Flow<List<Configuration>> = dao.getAll()

    fun insert(configuration: Configuration) = viewModelScope.launch {
        dao.insert(configuration)
    }

    fun update(configuration: Configuration) = viewModelScope.launch {
        dao.update(configuration)
    }

    fun delete(configuration: Configuration) = viewModelScope.launch {
        dao.delete(configuration)
    }

    suspend fun getById(id: Int): Configuration? {
        return dao.getById(id)
    }
}
