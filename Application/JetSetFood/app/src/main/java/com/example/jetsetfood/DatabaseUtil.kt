package com.example.jetsetfood

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL


/**
 * Objekt zum Handling aller Interaktionen mit der Datenbank
 *
 */

object DatabaseUtil {
    private val gson = Gson()
    //Endpunkt der API
    private val URL ="https://sheltered-river-33488.herokuapp.com/"

    /***
     * Erfragt alle Informationen zum Produce aus der Datenbank
     * @param produce: enthält namen des gesuchten Produkts
     * @return Produce Objekt oder Null
     */
    suspend fun getProduce(produce: String): Produce? =
        try {
            val produceString = GlobalScope.async {
                callDatabase(makeQuery(listOf(produce), "produce").first())
            }.await()
            //resultat in Objekt konvertieren
            processResponse(produceString, ProduceUtil.produceType) as Produce

        } catch (e: Exception) {
            Log.e("API", "Produce Query failed", e)
            null
        }

    /***
     * Erfragt Länderinformationen aus der Datenbank, sammelt außerdem Anbauarten falls diese vorhanden sind
     * @param origins: Eine Liste gefüllt mit Ländercode-Strings und eventuell Anbauarten
     * @return Ein Paar. 1. Eine Liste mit zu den Ländercodestrings gehörigen Länderobjekten.
     *                   2. Eine Liste mit Anbauarten, falls vorhanden. Sonst null
     * @return null, bei Fehlern
     */
    suspend fun getOrigin(origins: List<String>): Pair<List<Country>, List<String>>? =
        try {
            var regional = listOf<String>()

            //Überprüft, ob Anbauarten in der Origins Liste vorhanden sind
            if (origins.contains("DEU")) {
                regional =
                    origins.fold(listOf()) { acc, cur -> if (ProduceUtil.farmingMethod.contains(cur.toLowerCase())) acc + cur.toLowerCase() else acc }
            }
            val countryStrings = makeQuery(origins, "mittelpunkt").map {
                GlobalScope.async { callDatabase(it) }.await()
            }
            val curCountries = countryStrings.map {
                (processResponse(it, ProduceUtil.countryType) as Center).mittelpunkt[0]
            }
            Pair(curCountries, regional)
        } catch (e: Exception) {
            Log.e("API", "Country Query failed", e)
            null
        }

    /***
     * Erfragt die zu den in der Liste stehenden Ländercode gehörige GeoJSONs
     * @param origins: Eine Liste gefüllt mit Ländercode-Strings
     * @return Eine Liste gefüllt mit JSON-Objekten
     * @return null, bei Fehlern
     */
    suspend fun getGeoJsonList(origins: List<String>): List<JSONObject>? =
        try {
             makeQuery(origins, "geo_daten").map {
                GlobalScope.async { callDatabase(it) }.await()
            }.map { JSONObject(it) }

        } catch (e: Exception) {
            Log.e("API", "GeoJson Query failed", e)
            null
        }

    /***
     * Erfragt die zu dem Ländercode gehörige GeoJSON
     * @param origin: Ein Ländercode-String
     * @return Ein JSON-Objekt
     * @return null, bei Fehlern
     */
    suspend fun getGeoJson(origin: String): JSONObject? =
        try {
            makeQuery(listOf(origin), "geo_daten").map {
                GlobalScope.async { callDatabase(it) }.await()
            }.map { JSONObject(it) }.first()

        } catch (e: Exception) {
            Log.e("API", "GeoJson Query failed", e)
           null
        }


    /***
     * Erfragt alle in der Datenbank vorhandenen Produkte
     * @return Eine liste mit allen Produkten
     */
    suspend fun getProduceList(): List<Produce> =
        try {
            processResponse(GlobalScope.async {
            callDatabase("produce")
            }.await(), object : TypeToken<List<Produce>>() {}.type) as List<Produce>

        } catch (e: Exception) {
            Log.e("API", "Produceliste konnte nicht erhalten werden", e)
            listOf()
    }

    /***
     * Die eigentliche Anfrage an die Datenbank
     * @param query: string mit der URI der erforderlichen Ressource
     * @return String, in dem die Antwort der Datenbank steht
     * @return null, bei Fehlern
     */
    private fun callDatabase(query: String): String? =
        try {
            var result: String? = ""
            val url = URL(URL + query)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            val `in` = connection.inputStream
            val reader = InputStreamReader(`in`)
            var data = reader.read()
            //solange weiterlesen, bis man am Ende der File ist
            while (data != -1) {
                val current = data.toChar()
                result += current
                data = reader.read()
            }
            result
        } catch (e: Exception) {
            Log.e("API", "API Abfrage fehlgeschlagen", e)
            //falls Datenabfrage fehlgeschlagen ist, return null
            null
        }

    /***
     * Konvertiert den String in ein Objekt des typs Type
     * @param result: String der konvertiert werden soll, die Antwort der Datenbank
     * @param type: Type der Klasse, in die der String konvertiert werden soll
     * @return Objekt der in Type spezifizierten Klasse.
     * Um die Omnipotenz der Funktion zu gewährleisten Any
     * @return null, bei Fehlern
     */
    private fun processResponse(result: String?, type: Type): Any? =
        try {
            //String zu JSON convertieren
            gson.fromJson(result, type)
        } catch (e: Exception) {
            Log.e("API", "Probleme bei der Konvertierung des JSON strings zu Object", e)
            null
        }

    /***
     * Setzt eine Liste mit Strings und einer Listenressource zu URIs zusammen. Filtert Anbaumethoden heraus
     * @param inputList: eine Liste mit Strings
     * @param listenres: die zugehörige Listenressource
     * @return Liste gefüllt mit URIs welche an die API geschickt werden können
     */
    private val makeQuery: (List<String>, String) -> List<String> = { inputList, listenres ->
        inputList.fold(listOf()) { acc, cur ->
            when {
                ProduceUtil.farmingMethod.contains(cur.toLowerCase()) || cur == "DEU" ->
                    //Sonderfall, es kommt aus Deutschland und in der Liste steht die Anbauart
                    acc

                else -> acc + "$listenres/${ProduceUtil.convertUmlaut(cur, true)}"
            }
        }
    }

}



