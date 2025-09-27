import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.C
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerComposable(
    context: Context,
    contentUri: String,
    licenseUrl: String,
    licenseRequestHeaders: Map<String, String> = emptyMap()
) {
    // Remember player across recompositions
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
        }
    }

// State for available tracks
    var availableVideoQualities by remember { mutableStateOf<List<Pair<String, TrackGroup>>>(emptyList()) }
    var showQualityDialog by remember { mutableStateOf(false) }

// Prepare media item with DRM
    LaunchedEffect(contentUri, licenseUrl, licenseRequestHeaders) {
        val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .setLicenseUri(licenseUrl)
            .setLicenseRequestHeaders(licenseRequestHeaders)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(contentUri.toUri())
            .setDrmConfiguration(drmConfig)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        // Listen for tracks
        exoPlayer.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val videoQualities = mutableListOf<Pair<String, TrackGroup>>()

                tracks.groups.forEach { trackGroup ->
                    if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                        trackGroup.mediaTrackGroup.let { group ->
                            for (i in 0 until group.length) {
                                val format = group.getFormat(i)
                                val quality = "${format.width}x${format.height}"
                                videoQualities.add(quality to group)
                            }
                        }
                    }
                }
                availableVideoQualities = videoQualities
            }
        })
    }

// Release player when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = exoPlayer
            }
        )

        // Quality selection button
        IconButton(
            onClick = { showQualityDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Quality Settings"
            )
        }
    }

// Quality selection dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Select Quality") },
            text = {
                LazyColumn {
                    items(availableVideoQualities.size) { track  ->
                        val quality = availableVideoQualities[track].first
                        val group = availableVideoQualities[track].second
                        TextButton(
                            onClick = {
                                val trackSelector = DefaultTrackSelector(context)
                                trackSelector.setParameters(
                                    trackSelector.buildUponParameters()
                                        .setMaxVideoSize(group.getFormat(track).width, group.getFormat(track).height)
                                )
                                val parameters = trackSelector.parameters.buildUpon()
                                    .setMaxVideoSize(group.getFormat(track).width, group.getFormat(track).height)
                                    .setMaxVideoBitrate(group.getFormat(track).bitrate)
                                    .build()
                                exoPlayer.trackSelectionParameters = parameters
                                showQualityDialog = false
                            }
                        ) {
                            Text(quality)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
