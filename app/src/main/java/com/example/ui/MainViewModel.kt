package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculations.model.CalculationOutput
import com.example.calculations.model.CalculatorCategory
import com.example.calculations.registry.CalculatorItem
import com.example.calculations.registry.CalculatorRegistry
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationHistoryEntity
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val historyRepo = HistoryRepository(db.calculationHistoryDao())
    private val favRepo = FavoriteRepository(db.favoriteDao())
    private val prefsRepo = PreferencesRepository(application)

    // State flows
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CalculatorCategory?>(null)
    val selectedCategory: StateFlow<CalculatorCategory?> = _selectedCategory.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = favRepo.favoriteCalculatorIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val history: StateFlow<List<CalculationHistoryEntity>> = historyRepo.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<AppThemeMode> = prefsRepo.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)

    val language: StateFlow<AppLanguage> = prefsRepo.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.SYSTEM)

    // Filtered calculators list
    val calculators: StateFlow<List<CalculatorItem>> = combine(
        _searchQuery,
        _selectedCategory,
        _onlyFavorites,
        favoriteIds
    ) { query, category, onlyFavs, favs ->
        CalculatorRegistry.filter(query, category, onlyFavs, favs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculatorRegistry.items)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: CalculatorCategory?) {
        _selectedCategory.value = category
        if (category != null) _onlyFavorites.value = false
    }

    fun setOnlyFavorites(only: Boolean) {
        _onlyFavorites.value = only
        if (only) _selectedCategory.value = null
    }

    fun toggleFavorite(calculatorId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(calculatorId)
            favRepo.toggleFavorite(calculatorId, isFav)
        }
    }

    fun saveCalculationToHistory(
        calculatorId: String,
        calculatorTitle: String,
        inputSummary: String,
        resultSummary: String
    ) {
        viewModelScope.launch {
            historyRepo.recordCalculation(
                calculatorId = calculatorId,
                calculatorTitle = calculatorTitle,
                inputSummary = inputSummary,
                resultSummary = resultSummary
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepo.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            prefsRepo.setLanguage(lang)
        }
    }
}
