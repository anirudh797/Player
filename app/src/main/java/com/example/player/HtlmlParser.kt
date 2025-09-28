package com.example.player

import org.jsoup.Jsoup
import org.json.JSONObject
import org.json.JSONArray

fun extractInnerHtmlJson(html: String): JSONArray {
    // Parse the HTML
    val doc = Jsoup.parse(html)

    // Get all <script> tags
    val scripts = doc.getElementsByTag("script")

    var mxsJson: String? = null

    for (script in scripts) {
        val scriptData = script.data()
        if (scriptData.contains("window.__mxs__")) {
            // Remove "window.__mxs__ =" and ending semicolon
            mxsJson = scriptData.substringAfter("window.__mxs__ =").trim()
            if (mxsJson.endsWith(";")) {
                mxsJson = mxsJson.dropLast(1)
            }
            break
        }
    }

    // Parse the JSON
    val root = JSONObject(mxsJson!!)
    val metaData = root.getJSONObject("metaData")
    val scriptsArray = metaData.getJSONArray("script")

    // Find the innerHTML JSON string
    var innerHtmlJsonString: String? = null
    for (i in 0 until scriptsArray.length()) {
        val obj = scriptsArray.getJSONObject(i)
        if (obj.has("innerHTML")) {
            innerHtmlJsonString = obj.getString("innerHTML")
            break
        }
    }

    // Parse the innerHTML into JSONArray
    return JSONArray(innerHtmlJsonString!!)
}

/* Hardcode episdoeName as another api call was required to fetch these */
fun getShowName(episodeNumber: Int): String {
    return when (episodeNumber) {
        19 -> "comedy-khatam-ladai-chalu"
        20 -> "gira-gira-kaun-gira"
        21 -> "kya-hoga-takhta-palat"
        18 -> "dont-tell-me-shut-up"
        17 -> "arbaz-cancels-aarush"
        16->  "trustee-ka-beta"
        15 -> "kiku-ne-bajayi-band"
        14 -> "takhta-palat-duur-nahi"
        13 -> "arjun-bana-target"
        12 -> "akriti-ne-palta-game"
        11 -> "jab-pawanji-met-dhanashree"
        else -> {
            ""
        }
    }
}
