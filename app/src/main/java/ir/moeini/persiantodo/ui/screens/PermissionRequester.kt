package ir.moeini.persiantodo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

/**
 * Returns a function that, when called, requests [permission] if not already granted,
 * then invokes [onResult] with whether it ended up granted.
 */
@Composable
fun rememberPermissionRequester(permission: String, onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onResult(granted) }

    return {
        val alreadyGranted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) onResult(true) else launcher.launch(permission)
    }
}
