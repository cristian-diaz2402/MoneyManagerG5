package com.example.moneymanagerg5.ui.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {
    private val _text = MutableStateFlow("Esta es la pantalla de estadísticas")
    val text: StateFlow<String> = _text
}