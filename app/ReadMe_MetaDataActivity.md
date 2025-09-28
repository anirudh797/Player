# MetadataActivity Documentation
https://github.com/anirudh797/Player

## Overview

`MetadataActivity` is responsible for fetching and displaying metadata for episodes of a show from MX Player APIs. It uses Retrofit for network requests and Jetpack Compose for UI.

## Workflow

1. **Fetch Episode Content IDs**  
   On launch, the activity requests episode data from  
   `https://api.mxplayer.in/v1/web/detail/tab/aroundcurrentepisodes`  
   and parses the JSON response to extract episode numbers and content IDs.

2. **Fetch Episode Metadata**  
   When an episode is selected, it makes a request to  
   `https://www.mxplayer.in/show/watch-rise-and-fall/season-1/{showName}-online-{contentId}`  
   with custom headers, expecting an HTML response.  
   The HTML is parsed to extract metadata such as show name, release year, genre, author, episode number, and description.

3. **Display Metadata**  
   The extracted metadata is shown in a Compose UI, allowing users to select episodes and view their details.

## Technologies Used

- **Kotlin**
- **Retrofit** (with ScalarsConverterFactory for HTML responses)
- **Jetpack Compose**
- **Coroutines**
- **org.json** for JSON parsing

## Key Methods

- `getContentId(episodeNumber, itemsMap)`: Returns content ID for a given episode.
- `MetadataContent(metadata)`: Composable to display episode metadata.

## Error Handling

Any network or parsing errors are caught and displayed in the UI.

## Custom Headers

Some requests require custom headers (e.g., `accept`, `user-agent`) to mimic browser requests.

## Usage

- Launch `MetadataActivity` to view and select episodes.
- Episode metadata is fetched and displayed automatically.