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

/**
 * Objekt zum Handling aller Interaktionen mit Produkten
 *
 */

object ProduceUtil {

    //Types der Klassen um die JSONStrings zu kovertieren
    val produceType = object : TypeToken<Produce>() {}.type!!
    val countryType = object : TypeToken<Center>() {}.type!!

    //Bei jedem Aufruf wird der aktuelle Monat als Int erhalten
     val currentMonth
         get() = Calendar.getInstance().get(Calendar.MONTH)

    //Um den Int currentMonth in einen String umzuwandeln
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
    //Eine Liste der unterstützen Anbaumethoden
     val farmingMethod = listOf("la", "gg", "ug", "ga", "fr")

    /***
    * Extrahiert den Saisonkalender des Produkts.
    * @param produce: das Produkt zu dem der Saisonkalender abgerufen werden soll
    * @return Ein String der die Namen der Monate, in denen das Produkt in Deutschland Saison hat
     * @return Einen leeren String falls das Produkt nie Saison in Deutschland hat oder leer ist
    */
    fun getSeasonGer(produce:Produce?)
        = produce?.season?.fold(listOf<Int>()) { acc, origin -> if(origin.land.any{it=="DEU"}) acc+produce.season.indexOf(origin) else acc }
        ?.map { monthNames[it] }
        ?: listOf()


    /***
     * Extrahiert die Herkunftsliste des Produkts.
     * @param produce: das Produkt zu dem die Herkunftsliste abgerufen werden soll
     * @param month: der Monat für den die Herkunftsliste des Produkts abgerufen werden soll
     * @return Einne Liste in dem die Länderkürzel der Herkünfte stehen
     * @return Einen leeren String falls das Produkt leer ist
     */
     val inSeason: (Produce?, Int) -> List<String> = { produce, month ->
         produce?.season?.get(month)?.land ?: listOf()
     } //Falls Produce null ist, wird eine leere liste zurückgegeben

    /***
     * Konvertiert die Umlaute
     * @param wort: String in welchem die Umlaute konvertiert werden sollen
     * @param removeUmlaut: Boolean der festlegt ob die Umlaute entfernt oder hinzugefügt werden sollen
     * @return String mit konvertierten Umlauten
     */
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

    /***
     * Erzeugt aus der Liste einen String
     * @param inputList: Liste die Konvertiert werden soll
     * @param spacer: String mit dem die einzelnen Listenobjekte verknüft werden sollen
     * @param extra: String mit dem die letzten beiden Objekte verknüft werden sollen, falls null dann spacer
     * @return aus inputList Objekten, spacer und extra verknüfter String
     */
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

