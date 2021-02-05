package com.example.jetsetfood

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt




class RouteActivity : AppCompatActivity(), OnMapReadyCallback {

    class Input(val name:String, lat:Double,  lng:Double){
        //normiert die koordinaten
        val coords = LatLng(lat%90, lng%180)
        val distance:Double
            get(){
                val dis =SphericalUtil.computeDistanceBetween(coords, germany)
                return dis
            }
        override fun toString(): String {
            //Gibt distanz zu deutschland aus

            return "${(distance/10).roundToInt().toDouble()/100} km"
        }
    }
    private lateinit var mMap: GoogleMap

    var centre=germany

    private val origin=listOf(Input("brazil", -14.235004, -51.92528), Input("greece",39.074208, 21.824312))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //inits map & camera on germany
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Karte reduzieren auf das wichtigste
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
        addOrigin(mMap, "Deutschland", germany, R.raw.germany, this)
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
       // addRoutes1(mMap, origin,this, centre)


    }


}
