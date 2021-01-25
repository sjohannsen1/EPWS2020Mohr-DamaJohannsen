package com.example.jetsetfood

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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

data class Origin(val month:String, val land:List<String>)
data class Produce(val type:String,val id:String , val name:String, val origin: List<Origin> )

data class GEOStructure(
    val features: List<Feature>,
    val type: String
)

data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)

data class Geometry(
    val coordinates: List<List<List<Double>>>,
    val type: String
)

data class Properties(
    val abbrev: String,
    val abbrev_len: Int,
    val adm0_a3: String,
    val adm0_a3_is: String,
    val adm0_a3_un: Int,
    val adm0_a3_us: String,
    val adm0_a3_wb: Int,
    val adm0_dif: Int,
    val admin: String,
    val brk_a3: String,
    val brk_diff: Int,
    val brk_group: Any,
    val brk_name: String,
    val continent: String,
    val economy: String,
    val featurecla: String,
    val filename: String,
    val fips_10: Any,
    val formal_en: String,
    val formal_fr: Any,
    val gdp_md_est: Int,
    val gdp_year: Int,
    val geou_dif: Int,
    val geounit: String,
    val gu_a3: String,
    val homepart: Int,
    val income_grp: String,
    val iso_a2: String,
    val iso_a3: String,
    val iso_n3: String,
    val labelrank: Int,
    val lastcensus: Int,
    val level: Int,
    val long_len: Int,
    val mapcolor13: Int,
    val mapcolor7: Int,
    val mapcolor8: Int,
    val mapcolor9: Int,
    val name: String,
    val name_alt: Any,
    val name_len: Int,
    val name_long: String,
    val name_sort: String,
    val note_adm0: Any,
    val note_brk: Any,
    val pop_est: Int,
    val pop_year: Int,
    val postal: String,
    val region_un: String,
    val region_wb: String,
    val scalerank: Int,
    val sov_a3: String,
    val sovereignt: String,
    val su_a3: String,
    val su_dif: Int,
    val subregion: String,
    val subunit: String,
    val tiny: Int,
    val type: String,
    val un_a3: String,
    val wb_a2: String,
    val wb_a3: String,
    val wikipedia: Int,
    val woe_id: Int
)



fun getCountryName(produce: Produce, currentMonth:Int):String{
    var res=""
    produce.origin[currentMonth].land.forEach{land ->
        countryListe.forEach{
            if(it.first==land)
                res+="${it.third}, "
        }
    }
    return res
}

val gson= Gson()
val produceType=object: TypeToken<Produce>(){}.type
val countryType=object: TypeToken<GEOStructure>(){}.type

fun getCountryJSONs(countries:List<String>):List<JSONObject>{
    var res= listOf<JSONObject>()
    countries.forEach{
        res+=JSONObject(it)
    }
    return res
}

fun getCountriesAsObjects(countries: List<String>):List<GEOStructure>{
    var res= listOf<GEOStructure>()
    countries.forEach{
        res+=gson.fromJson(it, countryType) as GEOStructure
    }
    return res
}

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

fun getFromAPI(url:URL, query:String):Int{
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    val `in` = connection.inputStream
    val reader = InputStreamReader(`in`)
    var data = reader.read()

    return data
}

