package com.marcoassociation.dicho

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.marcoassociation.dicho.presentation.navigation.DichoNavHost
import com.marcoassociation.dicho.ui.theme.DichoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DichoTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    DichoNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        onMicTap = {
                            Toast.makeText(
                                context,
                                "TODO: Iniciar reconocimiento de voz",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    DichoTheme {
        val navController = rememberNavController()
        DichoNavHost(navController = navController, onMicTap = {})
    }
}
