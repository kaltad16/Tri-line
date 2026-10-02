package com.triline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.triline.game.GameViewModel
import com.triline.ui.TriLineScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val gameViewModel: GameViewModel = viewModel()
            TriLineScreen(gameViewModel)
        }
    }
}
