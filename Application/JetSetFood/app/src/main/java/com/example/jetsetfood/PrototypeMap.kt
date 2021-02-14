package com.example.jetsetfood



import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonLayer

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt


class PrototypeMap : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.let {
            it.setOnMarkerClickListener { marker ->
                Log.d("Marker", "click ${marker?.tag}")
                runBlocking {
                    val geoJSON = async {
                        getGeoJson(listOf(marker.tag as String))
                    }
                    addOutlineFromJSON(mMap, geoJSON.await(), this@PrototypeMap)

                }
                Log.d("Marker", "click ${marker?.tag}")
                false
                //TODO: WIESO KLAPPT DAS NICHT
            }
        }
        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())

        runBlocking {

            val produce= async{getProduce(intent.extras?.getString("input")!!)}.await()
            when {
                produce != null -> {
                    val origin = inSeason(/*res.getOrNull()*/produce, currentMonth)
                    val countries = async {
                        getOrigin(origin)
                    }
                    /*val geoJSONs = async {
                         getGeoJson(origin)

                    }*/
                    var displayCountries =
                        countries.await()?.first?.map { it.laendercode }//TODO: Wenn voll ländernamen geadded wurden ändern
                    //addLabel(mMap, countries.await()?.first, this@PrototypeMap)
                    if (!countries.await()?.second.isNullOrEmpty()) {
                        addFarmingMethod(mMap, this@PrototypeMap, countries.await()?.second!!)
                        displayCountries = displayCountries?.plus(listOf("DEU"))
                    } else addOrigin(mMap, "Deutschland", germany, R.raw.germany, this@PrototypeMap)
                    //addOutlineFromJSON(mMap, geoJSONs.await(),this@PrototypeMap)
                    addRoutes(mMap, countries.await()?.first, this@PrototypeMap, germany)

                    textView.text =
                        "Im ${monthNames[currentMonth]} kann man ${produce.name} aus ${makeString(
                            displayCountries,
                            ", ",
                            " oder "
                        )} kaufen"
                }
                else -> Log.e("api", "nebenläufigkeit kaputt ")
            }

        }
    }

}
