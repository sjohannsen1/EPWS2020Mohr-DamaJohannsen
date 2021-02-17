package com.example.jetsetfood

import android.content.Context
import android.util.Log
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.util.*

//Dataclasses für Datenbankobjekte

data class Origin(val month:String, val land:List<String>)
data class Produce(val type:String, val name:String, val season: List<Origin>)
data class Center(val mittelpunkt: List<Country>)
data class Country(val laendercode: String, val land:String, val latitude: Double, val longitude: Double)


object ProduceUtil {

    //Types der Klassen um die JSONStrings zu kovertieren
    val produceType = object : TypeToken<Produce>() {}.type!!
    val countryType = object : TypeToken<Center>() {}.type!!

     val currentMonth
         get() = Calendar.getInstance().get(Calendar.MONTH)

     val monthNames = listOf(
         "Januar",
         "Februar",
         "März",
         "April",
         "Mai",
         "Juni",
         "Juli",
         "August",
         "September",
         "Oktober",
         "November",
         "Dezember"
     )
     val farmingMethod = listOf("la", "gg", "ug", "ga", "fr")

     val inSeason: (Produce?, Int) -> List<String> = { produce, month ->
         produce?.season?.get(month)?.land ?: listOf()
     } //Falls Produce null ist, wird eine leere liste zurückgegeben

     fun convertUmlaut(wort: String, removeUmlaut: Boolean): String =
         when (removeUmlaut) {
             true -> when {
                 wort.contains("ü") -> wort.replace("ü", "ue")
                 wort.contains("ö") -> wort.replace("ö", "oe")
                 wort.contains("ä") -> wort.replace("ä", "ae")
                 wort.contains("ß") -> wort.replace("ß", "ss")
                 else -> wort
             }
             false -> when {
                 wort.contains("ue") -> wort.replace("ue", "ü")
                 wort.contains("oe") -> wort.replace("oe", "ö")
                 wort.contains("ae") -> wort.replace("ae", "ä")
                 wort.contains("ss") -> wort.replace("ss", "ß")
                 else -> wort
             }
         }

     val makeString: (List<String>?, String, String?) -> String = { inputList, spacer, extra ->
         val last = extra ?: spacer
         when (inputList?.size) {
             1 -> inputList.first()
             2 -> "${inputList.first()}$last${inputList.last()}"
             else -> {
                 inputList?.subList(1, inputList.lastIndex)
                     ?.fold(inputList.first()) { acc, cur -> "$acc$spacer$cur" }
                     .plus("$last${inputList?.last()}")
             }
         }
     }
 }



//Alt
fun getJsonDataFromAsset(context:Context, dateiName:String):String{
    /*val res=
    runCatching { context.assets.open(dateiName).bufferedReader().use { it.readText() } }
        .onSuccess {
            return it
        }.onFailure {
            Toast.makeText(context, "Ein Fehler bei der Abfrage der Datenbank ist aufgetreten", Toast.LENGTH_SHORT).show()
            Log.e("JSON Data","nicht einlesbar ",it)
            return ""
        }
    return res*/
    val json:String
    try {
        json=context.assets.open(dateiName).bufferedReader().use { it.readText() }
    }catch (ioException: IOException){
        Log.e("JSON Data","nicht einlesbar ",ioException)
        return ""
    }
    return json
}

