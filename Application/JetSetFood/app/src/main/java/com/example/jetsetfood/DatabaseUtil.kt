package com.example.jetsetfood

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL

val url=""//TODO Add url

data class DBResponse(val produce:Produce, val countries:List<Country>, val geojsons:List<JSONObject>, val regional:List<String>?)

//Datenbsnkcalls

//eine funktion die auf die antwort der API wartet
suspend fun getDataforProduce(produce:String,map:GoogleMap, context: Context,textView: TextView?,displayOnMap:(DBResponse?, GoogleMap, Context, TextView?)->Unit){
     try{
        var regional:List<String>?=null
        val produceString = GlobalScope.async{
            callDatabase(url, produce)
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
            processResponse(it, countryType) as Country
        }
        //Mittelpunktliste wird benötigt um Marker dort zu setzen
        val geoJSONs= makeQuery(origins,"geojson").map{
            GlobalScope.async{ callDatabase(url, it)}.await()
            }.map{JSONObject(it)}
        //Wird benötigt zur Markierung der Länder

         displayOnMap(DBResponse(curProduce, curCountries, geoJSONs, regional), map, context,textView)

    }catch (e:Exception){
        Log.e("API", "Query failed", e)

    }

}

//der eigentliche call an die API
private fun callDatabase(apiUrl:String, query:String):String?{
    var result:String?=""
    val url= URL(apiUrl+query)
    try {
        val connection = url.openConnection() as HttpURLConnection

        //TODO: HEADER, HOST NAME, API KEY mit connection.setRequestProperty() setten falls benötigt

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


val makeQuery: (List<String>, String)->List<String> ={ countryList, listenres->

    countryList.fold(listOf()){acc, cur -> if(farmingMethod.contains(cur)) {
        //TODO: Sonderfall, es kommt aus Deutschland und in der Liste steht die Anbauart funktionsaufruf
        acc
    }else
        acc+"$listenres/$cur"

    }
}