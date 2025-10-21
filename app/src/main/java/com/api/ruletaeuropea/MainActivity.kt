package com.api.ruletaeuropea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.api.ruletaeuropea.pantallas.PantallaApuestas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Text("¡Bienvenida a RuleEuropa!")
            }
            PantallaApuestas()
        }
    }
}
