package com.example.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var itemsMap by remember { mutableStateOf<HashMap<Int, String>>(hashMapOf()) }
    var episodeNumber by remember { mutableIntStateOf(itemsMap.keys.firstOrNull() ?: 19) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.mxplayer.in/")
                    .addConverterFactory(ScalarsConverterFactory.create())
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
                // Assuming response is a JSON string and itemsArray is a JSONArray inside it
                val jsonObj = org.json.JSONObject(response)
                val itemsArray = jsonObj.getJSONArray("items")
                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.getJSONObject(i)
                    val sequence = item.optInt("sequence")
                    val contentId = item.optString("id")
                    itemsMap = HashMap(itemsMap).apply { put(sequence, contentId) }
                    episodeNumber = itemsMap.keys.first()
                }
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
                htmlContent = api.getShowMetadata(getShowName(episodeNumber), getContentId(episodeNumber,itemsMap),headers)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(itemsMap.keys.toList()) { epNum ->
                    Button(
                        onClick = { episodeNumber = epNum },
                        modifier = Modifier, // Staggered effect
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (episodeNumber == epNum) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column {
                            Text("Episode $epNum")
                            Text("ContentId ${itemsMap[epNum]}")
                        }
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


/* These contentIds are fetched at the activity launch with another api call */
fun getContentId(episodeNumber: Int, itemsMap: Map<Int, String>): String {
    return itemsMap[episodeNumber] ?: ""

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
        Text(text = "Description: ${metadata.episodeDescription}",
            maxLines = 5,
            )

    }
}
