package com.example.jetsetfood

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.data.geojson.GeoJsonLayer
import java.util.*

class PrototypeMap : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val gson= Gson()
    val produceType=object: TypeToken<Produce>(){}.type
    private lateinit var textView:TextView



    private val latGermany=51.5167
    private val lngGermany=9.9167
    val germany = LatLng(latGermany, lngGermany)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype_map)
        textView=findViewById(R.id.info)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)



    }

    fun addOutline(map: GoogleMap,herkunft:List<Int>){
        if(herkunft.isEmpty()){
            Toast.makeText(this, "Zu diesem Produkt existieren keine Herkunftsinformationen", Toast.LENGTH_LONG).show()
            //Gibt Fehlermeldung aus, das keine Herkunftsinfos vorliegen
        }
        herkunft.forEach{
            //Überprüft ob Objekt in der Liste eine GEOJson Datei im richtigen Format ist
            runCatching {
                GeoJsonLayer(map, it, this)
                //Wenn nicht wird ein Toast angezeigt
            }.onFailure {
                Toast.makeText(this, "Land kann nicht markiert werden", Toast.LENGTH_LONG)
                    .show()
                Log.e("GeoJson", "konnte nicht eingelesen werden", it)
            }
                //Wenn schon werden die Daten angezeigt und das Land kann markiert werden
                .onSuccess {layer ->
                    layer.defaultPolygonStyle.strokeWidth = 0.0f
                    layer.defaultPolygonStyle.fillColor = Color.DKGRAY
                    layer.addLayerToMap()

                    if (!layer.isLayerOnMap) {
                        Toast.makeText(this, "Land kann nicht markiert werden", Toast.LENGTH_SHORT).show()
                        Log.e("GeoJson", "Layer konnte nicht zur Karte hinzugefügt werden")
                        //Gibt Fehlermeldung aus, das Länder nicht markiert werden können
                    }
                }


        }
    }

    fun addOrigin(map: GoogleMap,name:String, position:LatLng, flaeche:Int ){
        map.addMarker(MarkerOptions().position(position).title(name))
        val layerGermany= GeoJsonLayer(map, flaeche, this)
        layerGermany.defaultPolygonStyle.strokeWidth=0.0f
        layerGermany.defaultPolygonStyle.fillColor=Color.LTGRAY
        layerGermany.addLayerToMap()
    }

    fun getInfo(intent: Intent):Produce{
        val  input:String=intent.extras?.getString("input") ?: ""

        if(input=="")
            Log.e("Input", "ist leer")

        val jsonString = getJsonDataFromAsset(this, input.toLowerCase()+".json")

        Log.i("JSON",jsonString)

        return gson.fromJson(jsonString,produceType)
    }

    fun getCountryName(produce: Produce):String{
        var res=""
        produce.origin[currentMonth].land.forEach{land ->
            countryListe.forEach{
                if(it.first==land)
                    res+="${it.third}, "
            }
        }
        return res
    }
    fun getCountryPath(produce:Produce):List<Int>{

        var res= listOf<Int>()
         produce.origin[currentMonth].land.forEach{land ->
            countryListe.forEach{
                if(it.first==land)
                    res+=it.second
            }
        }
    return res
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
        addOrigin(mMap, "Deutschland", germany, R.raw.germany)
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
        val suche=getInfo(intent)
        addOutline(mMap,getCountryPath(suche))
        textView.text="Im ${currentMonth+1}. Monat im Jahr kann man ${suche.name} aus ${getCountryName(suche)} kaufen"


    }
}
