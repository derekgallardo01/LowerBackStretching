package com.lowerbackstretching.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import com.lowerbackstretching.core.model.WatchRoutine
import com.lowerbackstretching.wear.ui.WearPlayerScreen
import com.lowerbackstretching.wear.ui.WearRoutineListScreen

/**
 * Single activity hosting the Wear OS app.
 * Provides navigation between the Routine selection menu and the Player.
 */
class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    var selectedRoutine by remember { mutableStateOf<WatchRoutine?>(null) }

                    if (selectedRoutine == null) {
                        WearRoutineListScreen(
                            onSelectRoutine = { routine ->
                                selectedRoutine = routine
                            },
                        )
                    } else {
                        BackHandler {
                            selectedRoutine = null
                        }
                        WearPlayerScreen(
                            routine = selectedRoutine,
                            onExit = {
                                selectedRoutine = null
                            },
                        )
                    }
                }
            }
        }
    }
}
