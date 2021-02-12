package com.example.jetsetfood



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


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
        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())

        //addOrigin(mMap, "Deutschland", germany, R.raw.germany, this)

        runBlocking {
            val res = fetchDeferred(intent.extras?.getString("input")!!).await()
            when {
                res.isSuccess -> {
                    val origin = inSeason(res.getOrNull(), currentMonth)
                    val countries = async {
                        getOrigin(origin)
                    }
                    val geoJSONs = async {
                         getGeoJson(origin)

                    }
                    addLabel(mMap, countries.await()?.first, this@PrototypeMap)
                    if(!countries.await()?.second.isNullOrEmpty())  addFarmingMethod(mMap, this@PrototypeMap, countries.await()?.second!!)
                    addOutlineFromJSON(mMap, geoJSONs.await(),this@PrototypeMap)
                    textView.text="Im ${monthNames[currentMonth]} kann man ${res.getOrNull()?.name} aus ${countries.await()?.first?.map{it.laendercode}} kaufen"
                //TODO: DEutschland als herkunft fixen
                }
                else -> Log.e("api", "nebenläufigkeit kaputt ", res.exceptionOrNull())
            }
        }
        mMap.setOnMarkerClickListener(MarkerClick(mMap, this, germany))
        }/*
        runBlocking {
            val res = fetchDeferred1(intent.extras?.getString("input")!!).await()
            when {
                res.isSuccess -> addToMap(res.getOrNull(),mMap, this@PrototypeMap, textView)
                res.isFailure -> Log.e("api", "nebenläufigkeit kaputt ",res.exceptionOrNull())
                else -> Log.e("wtf", "wieso bin ich hier")
            }
        }*/
        //TODO: Herausfinden wie das Schöner geht (add to map nicht in der funktion aufrufen)


}
