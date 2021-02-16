package com.example.jetsetfood

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.IOException
import java.util.*

//Dataclasses für Datenbankobjekte
data class Origin(val month:String, val land:List<String>)
//Beim Fall Deutschland: Eintrag in land, bei verschiedenen Anbauarten das schlechtere
data class Produce(val type:String, val name:String, val season: List<Origin>)
data class Mittelpunkt(val mittelpunkt: List<Country>)
data class Country(val laendercode: String, val land:String, val latitude: Double, val longitude: Double)
val gson = Gson()

 class ProduceUtil {
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


     //Types der Klassen um die JSONStrings zu kovertieren
     val produceType = object : TypeToken<Produce>() {}.type
     val countryType = object : TypeToken<Mittelpunkt>() {}.type


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
val produceListe=listOf("avocado", "erdbeere", "feige", "himbeere", "kartoffel", "mango", "okra", "paprika", "tomate", "zucchini", "lauch", "salatgurke", "grünkohl")

val produceString= produceListe.fold(""){acc, it -> "$acc\n${it.capitalize()} "}

val countryListe=listOf(
    Triple("AUS", R.raw.australia, "Australien"),
    Triple("BEL", R.raw.belgium, "Belgien"),
    Triple("BRA", R.raw.australia, "Australien"),
    Triple("CHL", R.raw.chile, "Chile"),
    Triple("KEN", R.raw.kenya, "Kenia"),
    Triple("THA", R.raw.thailand, "Thailand"),
    Triple("CRI", R.raw.costarica, "Costa Rica"),
    Triple("CYP", R.raw.cyprus, "Zypern"),
    Triple("ECU", R.raw.ecuador, "Ecuador"),
    Triple("EGY", R.raw.egypt, "Ägypten"),
    Triple("GER", R.raw.germany, "Deutschland"),
    Triple("GRC", R.raw.greece, "Griechenland"),
    Triple("GTM", R.raw.guatemala, "Guatemala"),
    Triple("ISR", R.raw.israel, "Israel"),
    Triple("ITA", R.raw.italy, "Italien"),
    Triple("CIV", R.raw.ivorycoast, "Elfenbeinküste"),
    Triple("LUX", R.raw.louxemburg, "Luxemburg"),
    Triple("MEX", R.raw.mexico, "Mexiko"),
    Triple("MAR", R.raw.morroco, "Marokko"),
    Triple("NLD", R.raw.netherlands, "Niederlande"),
    Triple("NZL", R.raw.newzealand, "Neuseeland"),
    Triple("PAK", R.raw.pakistan, "Pakistan"),
    Triple("PER", R.raw.peru, "Peru"),
    Triple("PRI", R.raw.puertorico, "Puerto Rico"),
    Triple("ZAF", R.raw.southafrica, "Südafrika"),
    Triple("ESP", R.raw.spain, "Spanien"),
    Triple("THA", R.raw.thailand, "Thailand"),
    Triple("TUN", R.raw.tunisia, "Tunesien"),
    Triple("TUR", R.raw.turkey, "Türkei"),
    Triple("USA", R.raw.usa, "USA")
)


//Alt
fun getCountryName(produce: Produce, currentMonth:Int):String{
    var res=""
    produce.season[currentMonth].land.forEach{ land ->
        countryListe.forEach{
            if(it.first==land)
                res+="${it.third}, "
        }
    }
    return res
}

//Alr
fun getCountryJSONs(countries:List<String>):List<JSONObject>{
    var res= listOf<JSONObject>()
    countries.forEach{
        res+=JSONObject(it)
    }
    return res
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

