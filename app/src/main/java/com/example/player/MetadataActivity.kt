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
import retrofit2.converter.moshi.MoshiConverterFactory
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
    var episodeNumber by remember { mutableStateOf<String>("19") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.mxplayer.in/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                val api = retrofit.create(ShowApiService::class.java)
                val response = api.getAroundCurrentEpisodes(
                    type = "season",
                    id = "1a56e25fb21d2f42d03051dc247103dd",
                    filterId = "434701b23e00e63d13e216e4d70f8a6f",
                    deviceDensity = 3,
                    userId = "a4add1e5-cc1d-40d0-9537-a4cf05bbaf8a",
                    platform = "com.mxplay.desktop",
                    contentLanguages = "hi,en",
                    kidsModeEnabled = false
                )
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    LaunchedEffect(episodeNumber) {
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
                htmlContent = api.getShowMetadata(getShowName(episodeNumber), "",headers)
                htmlContent?.let { it ->
                    val content = extractInnerHtmlJson(it)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("19", "20", "21").forEach { epNum ->
                    Button(
                        onClick = { episodeNumber = epNum },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (episodeNumber == epNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Episode $epNum")
                    }
                }
            }
            when {
                metadata != null -> metadata?.let {
                    MetadataContent(it)
                }

                error != null -> Text("Error: $error")
                else -> CircularProgressIndicator()
            }
        }
    }
}


fun getShowName(episodeNumber: String): String {
    return when (episodeNumber) {
        "19" -> "comedy-khatam-ladai-chalu"
        "20" -> "gira-gira-kaun-gira"
        "21" -> "kya-hoga-takhta-palat"
        else -> {
            ""
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
        Text(
            text = "Show Name: ${metadata.showName}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = "Release Year: ${metadata.releaseYear}")
        Text(text = "Genre: ${metadata.genre}")
        Text(text = "Author: ${metadata.author}")
        Text(text = "Episode Number : ${metadata.episodeNumber}")
        Text(text = "Description: ${metadata.episodeDescription}")

    }
}
