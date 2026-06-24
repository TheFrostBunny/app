package no.busswidget.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Departure(
    val line: String,
    val destination: String,
    val expectedTime: String,
    val minutesUntil: Long,
    val isRealtime: Boolean,
    val transportMode: String
)

data class StopPlace(
    val id: String,
    val name: String
)

object EnturApi {

    private const val GRAPHQL_URL = "https://api.entur.io/journey-planner/v3/graphql"
    private const val GEOCODER_URL = "https://api.entur.io/geocoder/v1/autocomplete"
    private const val CLIENT_NAME = "no.busswidget-android"

    private val client = OkHttpClient()

    fun searchStops(query: String): List<StopPlace> {
        val url = "$GEOCODER_URL?text=${query}&size=10&layers=venue"
        val request = Request.Builder()
            .url(url)
            .addHeader("ET-Client-Name", CLIENT_NAME)
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val features = json.getJSONArray("features")
            val results = mutableListOf<StopPlace>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val props = feature.getJSONObject("properties")
                val id = props.optString("id", "")
                val name = props.optString("label", "")
                if (id.isNotEmpty() && name.isNotEmpty()) {
                    results.add(StopPlace(id, name))
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getDepartures(stopId: String, maxResults: Int = 5): List<Departure> {
        val query = """
        {
          stopPlace(id: "$stopId") {
            name
            estimatedCalls(timeRange: 72100, numberOfDepartures: $maxResults) {
              expectedDepartureTime
              aimedDepartureTime
              realtime
              serviceJourney {
                journeyPattern {
                  line {
                    publicCode
                    transportMode
                  }
                }
              }
              destinationDisplay {
                frontText
              }
            }
          }
        }
        """.trimIndent()

        val payload = JSONObject().put("query", query).toString()
        val body = payload.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(GRAPHQL_URL)
            .addHeader("ET-Client-Name", CLIENT_NAME)
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return emptyList()
            val json = JSONObject(responseBody)
            val data = json.optJSONObject("data") ?: return emptyList()
            val stopPlace = data.optJSONObject("stopPlace") ?: return emptyList()
            val calls = stopPlace.optJSONArray("estimatedCalls") ?: return emptyList()

            val now = ZonedDateTime.now()
            val departures = mutableListOf<Departure>()

            for (i in 0 until calls.length()) {
                val call = calls.getJSONObject(i)
                val expectedTime = call.optString("expectedDepartureTime", "")
                val isRealtime = call.optBoolean("realtime", false)
                val destination = call.optJSONObject("destinationDisplay")
                    ?.optString("frontText", "Ukjent") ?: "Ukjent"

                val sj = call.optJSONObject("serviceJourney")
                val jp = sj?.optJSONObject("journeyPattern")
                val line = jp?.optJSONObject("line")
                val lineCode = line?.optString("publicCode", "?") ?: "?"
                val mode = line?.optString("transportMode", "bus") ?: "bus"

                val departureTime = try {
                    ZonedDateTime.parse(expectedTime)
                } catch (e: Exception) {
                    continue
                }

                val minutesUntil = java.time.Duration.between(now, departureTime).toMinutes()
                if (minutesUntil < -1) continue

                val displayTime = when {
                    minutesUntil <= 0 -> "Nå"
                    minutesUntil < 60 -> "${minutesUntil} min"
                    else -> departureTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                }

                departures.add(
                    Departure(
                        line = lineCode,
                        destination = destination,
                        expectedTime = displayTime,
                        minutesUntil = minutesUntil,
                        isRealtime = isRealtime,
                        transportMode = mode
                    )
                )
            }
            departures
        } catch (e: Exception) {
            emptyList()
        }
    }
}
