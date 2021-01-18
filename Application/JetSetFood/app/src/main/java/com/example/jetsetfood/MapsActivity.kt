package com.example.jetsetfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.Feature


private val latGermany=51.5167
private val lngGermany=9.9167
val germany = LatLng(latGermany, lngGermany)
val germanyArea=R.raw.germany
val cNames= listOf("australia","belgium","brazil","chile")


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var countries= listOf( R.raw.italy, R.raw.usa, R.raw.brazil)
    private val onCountryClick:(Feature)->Unit= {startActivity(Intent(this, Prototype::class.java))}

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
/*
//markiert die in herkunft spezifizierten Länder
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
                Toast.makeText(this@MapsActivity, "Land kann nicht markiert werden", Toast.LENGTH_LONG)
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

                //wenn Land angeklickt wird sollen routen angezeigt werden
                layer.setOnFeatureClickListener {
                    startActivity(Intent(this, Prototype::class.java))
                }


            }


                }
        }



    //Markiert Deutschland als Heimatland, kann durch Geolocating geändert werden
    fun addOrigin(map: GoogleMap,name:String, position:LatLng, flaeche:Int ){
        map.addMarker(MarkerOptions().position(position).title(name))
        val layerGermany= GeoJsonLayer(map, flaeche, this)
        layerGermany.defaultPolygonStyle.strokeWidth=0.0f
        layerGermany.defaultPolygonStyle.fillColor=Color.LTGRAY
        layerGermany.addLayerToMap()
    }*/






    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))

        // Marker in Deutschland hinzufügen und Land markieren
        addOrigin(mMap, "Deutschland", germany, germanyArea,this)
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
        val jsonStrings = cNames.map { getJsonDataFromAsset(this,it+".json")}
        //Länder markieren
        //addOutline(mMap,countries,this, onCountryClick)
        addOutlineFromJSON(mMap, getCountryJSONs(jsonStrings), this, onCountryClick)
        /*addLabel(mMap,getCountriesAsObjects(jsonStrings).map {
            Pair(LatLng(0.0,0.0), it.features[0].properties.name)
        }, this)*/



    }
}
