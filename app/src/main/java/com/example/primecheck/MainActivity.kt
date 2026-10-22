package com.example.primecheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.primecheck.ui.PrimeCheckApp
import com.example.primecheck.ui.PrimeCheckViewModel
import com.example.primecheck.ui.theme.PrimeCheckTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PrimeCheckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            PrimeCheckTheme(
                appTheme = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                PrimeCheckApp(viewModel = viewModel)
            }
        }
    }
}
