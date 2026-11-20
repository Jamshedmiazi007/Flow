package com.example.openflow

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {

    private var incomingLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingLink = extractLink(intent)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(incomingLink = incomingLink)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incomingLink = extractLink(intent)
    }

    private fun extractLink(intent: Intent?): String? = when (intent?.action) {
        Intent.ACTION_SEND -> {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            Patterns.WEB_URL.matcher(text).let { if (it.find()) it.group() else null }
        }
        Intent.ACTION_VIEW -> intent.data?.toString()
        else -> null
    }
}

@Composable
fun PlayerScreen(incomingLink: String?) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    var link by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun play(uri: Uri) {
        error = null
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.playWhenReady = true
    }

    DisposableEffect(player) {
        val activity = context as? ComponentActivity
        val errorListener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                error = "Can't play this link (${e.errorCodeName}). " +
                    "Use a direct video link, such as .mp4 or .m3u8."
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        player.addListener(errorListener)
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            activity?.lifecycle?.removeObserver(observer)
            player.removeListener(errorListener)
            player.release()
        }
    }

    LaunchedEffect(incomingLink) {
        incomingLink?.let {
            link = it
            play(Uri.parse(it))
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { play(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Video link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val pasted = cm.primaryClip
                    ?.takeIf { it.itemCount > 0 }
                    ?.getItemAt(0)?.text?.toString()
                if (!pasted.isNullOrBlank()) link = pasted.trim()
            }) { Text("Paste") }

            Button(onClick = {
                if (link.isNotBlank()) play(Uri.parse(link.trim()))
            }) { Text("Play") }

            OutlinedButton(onClick = { filePicker.launch(arrayOf("video/*")) }) {
                Text("Open file")
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
