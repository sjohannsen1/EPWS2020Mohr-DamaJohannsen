package com.example.jetsetfood

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonParser
import org.json.JSONObject
import kotlin.math.roundToInt
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import org.bson.types.ObjectId
import org.json.JSONException
import java.lang.RuntimeException


private val latGermany=51.5167
private val lngGermany=9.9167
val germany = LatLng(latGermany, lngGermany)
val germanyArea=R.raw.germany


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var countries= listOf( R.raw.italy, R.raw.usa, R.raw.brazil)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

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

    fun addLabel(map:GoogleMap, herkunft: List<Int>){
        herkunft.forEach({

        })
    }

    //Markiert Deutschland als Heimatland, kann durch Geolocating geändert werden
    fun addOrigin(map: GoogleMap,name:String, position:LatLng, flaeche:Int ){
        map.addMarker(MarkerOptions().position(position).title(name))
        val layerGermany= GeoJsonLayer(map, flaeche, this)
        layerGermany.defaultPolygonStyle.strokeWidth=0.0f
        layerGermany.defaultPolygonStyle.fillColor=Color.LTGRAY
        layerGermany.addLayerToMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))

        // Marker in Deutschland hinzufügen und Land markieren
        addOrigin(mMap, "Deutschland", germany, germanyArea)
        mMap.moveCamera(CameraUpdateFactory.zoomOut())

        //Länder markieren
        addOutline(mMap,countries)
    }
}
