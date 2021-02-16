package com.example.jetsetfood



import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonLayer

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt


class PrototypeMap : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var textView:TextView
    private val produceUtil=ProduceUtil()
    private val database=DatabaseUtil(produceUtil)




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

    override fun onMarkerClick(marker: Marker?): Boolean {
        Log.d("Marker", "click")
        Log.e("Marker", "click ${marker?.tag}")
        if(marker?.tag!="GER") {
            runBlocking {
                val geoJSON = async {
                    database.getGeoJson(listOf(marker?.tag as String))
                }
                addOutlineFromJSON(mMap, geoJSON.await(), this@PrototypeMap)

            }
            Log.d("Marker", "click ${marker?.tag}")
        }
            return false
        }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        /*mMap.let {
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
        }*/

        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
        mMap.addMarker(MarkerOptions().position(LatLng(0.0,0.0))).tag="ECU"
        mMap.setOnMarkerClickListener(this)
        runBlocking {

            val produce= async{database.getProduce(intent.extras?.getString("input")!!)}.await()
            when {
                produce != null -> {
                    val origin = produceUtil.inSeason(/*res.getOrNull()*/produce, produceUtil.currentMonth)
                    val countries = async {
                        database.getOrigin(origin)
                    }
                    /*val geoJSONs = async {
                         getGeoJson(origin)

                    }*/
                    var displayCountries =
                        countries.await()?.first?.map { it.land }
                    Log.d("countries", displayCountries.toString())
                    //addLabel(mMap, countries.await()?.first, this@PrototypeMap)
                    if (!countries.await()?.second.isNullOrEmpty()) {
                        addFarmingMethod(mMap, this@PrototypeMap, countries.await()?.second!!)
                        displayCountries = displayCountries?.plus(listOf("Deutschland"))
                    } else addOrigin(mMap, "Deutschland", germany, R.raw.germany, this@PrototypeMap)
                    //addOutlineFromJSON(mMap, geoJSONs.await(),this@PrototypeMap)
                    addRoutes(mMap, countries.await()?.first, this@PrototypeMap, germany)

                    textView.text =
                        "Im ${produceUtil.monthNames[produceUtil.currentMonth]} kann man ${produceUtil.convertUmlaut(produce.name, false)} aus ${produceUtil.makeString(
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
