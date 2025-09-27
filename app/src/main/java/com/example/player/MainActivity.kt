package com.example.player
import VideoPlayerComposable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.player.ui.theme.PlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayerTheme {
                VideoPlayerComposable(
                    context = this,
                    contentUri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11\n" +
                            "331.mpd",
                    licenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth",
                    licenseRequestHeaders = mapOf(/* "Authorization" to "Bearer YOUR_TOKEN" */)
                )
            }
        }
    }
}