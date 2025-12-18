package com.api.ruletaeuropea.pantallas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color




@Composable
fun PantallaInfo(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // WebView con tu HTML
        WebViewScreen("file:///android_asset/info.html")

        // Botón flotante para volver al menú
        Button(
            onClick = {
                navController.navigate("menu") {
                    popUpTo("info") { inclusive = true } // elimina InfoScreen del backstack
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun WebViewScreen(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }
    )
}
