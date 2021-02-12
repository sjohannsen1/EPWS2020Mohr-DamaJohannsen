package com.example.jetsetfood

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL

const val url ="https://sheltered-river-33488.herokuapp.com/"

data class DBResponse(val produce:Produce, val countries:List<Country>, val geojsons:List<JSONObject>, val regional:List<String>?)

fun fetchDeferred(produce: String) : Deferred<Result<Produce?>> {
    return GlobalScope.async {
        runCatching {
            getProduce(produce)
        }
    }
}

//Datenbankcalls

suspend fun getProduce(produce: String):Produce?{
   try {
       val produceString = GlobalScope.async {
           callDatabase(url, makeQuery(listOf(produce), "produce").first())
       }.await()
       //resultat in Objekt konvertieren
       return processResponse(produceString, produceType) as Produce

   } catch (e:Exception){
       Log.e("API", "Produce Query failed", e)
       return null
   }
}
suspend fun getOrigin(origins: List<String>):Pair<List<Country>,List<String>>?{
    try {
        var regional= listOf<String>()
        //val origins = inSeason(curProduce, currentMonth)
        if (origins.contains("DEU")) {
            regional =
                origins.fold(listOf()) { acc, cur -> if (farmingMethod.contains(cur.toLowerCase())) acc + cur.toLowerCase() else acc }
        }
        val countryStrings = makeQuery(origins, "mittelpunkt").map {
            GlobalScope.async { callDatabase(url, it) }.await()
        }
        val curCountries = countryStrings.map {
            (processResponse(it, countryType) as Mittelpunkt).mittelpunkt[0]
        }
        return Pair(curCountries,regional)
    }catch (e:Exception){
        Log.e("API", "Country Query failed", e)
        return null
    }
}

suspend fun getGeoJson(origins:List<String>):List<JSONObject>?{
    try {

     val geoJSONs= makeQuery(origins,"geo_daten").map{
        GlobalScope.async{ callDatabase(url, it)}.await()
    }.map{JSONObject(it)}
    //Wird benötigt zur Markierung der Länder

    return geoJSONs

}catch (e:Exception){
    Log.e("API", "GeoJson Query failed", e)
    return null
}
}


//eine funktion die auf die antwort der API wartet
suspend fun getDataforProduce(produce:String):DBResponse?{
     try{
        var regional:List<String>?=null
        val produceString = GlobalScope.async{
            callDatabase(url, "produce/$produce")
        }.await()
       //resultat in Objekt konvertieren
        val curProduce=processResponse(produceString, produceType) as Produce
        val origins=inSeason(curProduce, currentMonth)
         if(origins.contains("GER")){
             regional=origins.fold(listOf()){acc, cur -> if(farmingMethod.contains(cur)) acc+cur else acc}
         }
        val countryStrings=makeQuery(origins, "mittelpunkt").map{
            GlobalScope.async { callDatabase(url,it)}.await()
        }
        val curCountries= countryStrings.map{
            (processResponse(it, countryType) as Mittelpunkt).mittelpunkt[0]
        }
        //Mittelpunktliste wird benötigt um Marker dort zu setzen
        val geoJSONs= makeQuery(origins,"geo_daten").map{
            GlobalScope.async{ callDatabase(url, it)}.await()
            }.map{JSONObject(it)}
        //Wird benötigt zur Markierung der Länder

        return DBResponse(curProduce, curCountries, geoJSONs, regional)

    }catch (e:Exception){
        Log.e("API", "Query failed", e)
        return null
    }

}

//der eigentliche call an die API
private fun callDatabase(apiUrl:String, query:String):String?{
    var result:String?=""
    val url= URL(apiUrl+query)
    try {
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        val `in` = connection.inputStream
        val reader = InputStreamReader(`in`)
        var data = reader.read()
        while (data!=-1){
            val current=data.toChar()
            result+=current
            data=reader.read()
        }
        return result
    } catch(e:Exception){
        Log.e("API", "API Abfrage fehlgeschlagen", e)
    }
    //falls Datenabfrage fehlgeschlagen ist, return null
    return null
}

fun processResponse(result:String?, type: Type):Any?{
    try{
        //String zu JSON convertieren
        return gson.fromJson(result, type)
    }
    catch(e:Exception){
        Log.e("API", "Probleme bei der Konvertierung des JSON strings zu Object", e)
    }
    return null
}


val makeQuery: (List<String>, String)->List<String> ={ inputList, listenres->
    inputList.fold(listOf()) { acc, cur ->
        when {
            farmingMethod.contains(cur.toLowerCase()) || cur == "DEU" ->
                //Sonderfall, es kommt aus Deutschland und in der Liste steht die Anbauart funktionsaufruf
                acc
            cur.contains("ü") -> acc + "$listenres/${cur.replace("ü", "ue")}"
            cur.contains("ö") -> acc + "$listenres/${cur.replace("ö", "oe")}"
            cur.contains("ä") -> acc + "$listenres/${cur.replace("ä", "ae")}"
            cur.contains("ß") -> acc + "$listenres/${cur.replace("ß", "ss")}"
            else -> acc + "$listenres/$cur"
        }
    }
}