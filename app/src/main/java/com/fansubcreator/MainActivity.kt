package com.fansubcreator

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fansubcreator.ui.screens.MainScreen
import com.fansubcreator.ui.screens.VideoPlayerScreen
import com.fansubcreator.ui.theme.FansubCreatorTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FansubCreatorTheme {
                val navController = rememberNavController()
                
                // Permission handling
                val permissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                
                LaunchedEffect(Unit) {
                    permissionsState.launchMultiplePermissionRequest()
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                onVideoSelected = { uri ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set("video_uri", uri)
                                    navController.navigate("video_player")
                                }
                            )
                        }
                        composable("video_player") { backStackEntry ->
                            val videoUri = navController.previousBackStackEntry?.savedStateHandle?.get<Uri>("video_uri")
                            videoUri?.let { uri ->
                                VideoPlayerScreen(
                                    videoUri = uri,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}