package com.example.moneymanagerg5.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _text = MutableStateFlow("Esta es la pantalla de inicio")
    val text: StateFlow<String> = _text
}