package com.example.jetsetfood

import android.content.Context
import android.util.Log
import java.io.IOException

val produceListe=listOf("avocado", "erdbeere", "feige", "himbeere", "kartoffel", "mango", "okra", "paprika", "tomate", "zucchini")

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

/*data class Country(val abbr:String, val loc:Int){
     fun equals(other: Country) = other.abbr==abbr

}*/

data class Origin(val month:String, val land:List<String>)

data class Produce(val type:String,val id:String , val name:String, val origin: List<Origin> )


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

