package com.example.moneymanagerg5.ui.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationsViewModel : ViewModel() {
    private val _text = MutableStateFlow("Esta es la pantalla de ajustes")
    val text: StateFlow<String> = _text
}