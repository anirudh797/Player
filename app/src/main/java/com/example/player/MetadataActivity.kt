package com.example.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class MetadataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MetadataScreen()
            }
        }
    }
}

@Composable
fun MetadataScreen() {
    var htmlContent by remember { mutableStateOf<String?>(null) }
    var metadata by remember { mutableStateOf<ShowMetadata?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.mxplayer.in/") // Use root base URL
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                val api = retrofit.create(ShowApiService::class.java)
                val headers = mapOf(
                    "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                    "user-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"
                )
                htmlContent = api.getShowMetadata("comedy-khatam-ladai-chalu",headers)
                htmlContent?.let { it->
                    val content =  extractInnerHtmlJson(it)
                    val contentDetails = content.getJSONObject(2)
                     metadata = ShowMetadata(
                        showName = content.getJSONObject(3).getString("headline"),
                        releaseYear = contentDetails.getString("dateCreated"),
                        genre = contentDetails.getString("genre"),
                        author = content.getJSONObject(3).getString("author"),
                        episodeDescription = contentDetails.getString("description"),
                        episodeNumber = contentDetails.getString("episodeNumber"),
                    )
                }

            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            metadata != null -> metadata?.let {
                MetadataContent(it)
            }
            error != null -> Text("Error: $error")
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
fun MetadataContent(metadata: ShowMetadata) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Show Name: ${metadata.showName}", style = MaterialTheme.typography.titleLarge)
        Text(text = "Release Year: ${metadata.releaseYear}")
        Text(text = "Genre: ${metadata.genre}")
        Text(text = "Author: ${metadata.author}")
        Text(text = "Episode Number : ${metadata.episodeNumber}")
        Text(text = "Description: ${metadata.episodeDescription}")

    }
}
