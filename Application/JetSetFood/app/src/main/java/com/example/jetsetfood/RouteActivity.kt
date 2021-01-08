package com.example.jetsetfood

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlin.math.roundToInt


class Input(val name:String, lat:Double,  lng:Double){
    val coords= LatLng(lat, lng)
    val distance=SphericalUtil.computeDistanceBetween(coords, germany)
    override fun toString(): String {
        return "${(distance/10).roundToInt().toDouble()/100} km"
    }

}

class RouteActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private lateinit var mMap: GoogleMap

    var centre=germany



    private val origin=listOf(Input("brazil", -14.235004, -51.92528), Input("greece",39.074208, 21.824312)/*, Input("Australia", -25.274398, 133.775136 )*/)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //wird aufgerufen, wenn die Polyline angeklickt wird
    override fun onPolylineClick(line: Polyline){
        line.color= Color.RED

        //zeigt distanz an
        mMap.addMarker(MarkerOptions()
            .position(SphericalUtil.interpolate(line.points.first(),line.points[1],0.5))
            .alpha(0.0F)
            .flat(true)
            .title("Distanz:")
            .snippet(line.tag.toString()))
            .showInfoWindow()
        //Toast.makeText(this, line.tag.toString(), Toast.LENGTH_SHORT).show()
        val layer= GeoJsonLayer(mMap, R.raw.brazil, this)
        layer.addLayerToMap()

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //fügt Polylines zwischen Deutschland und den Herkunftsländern
    //geodesic bedeutet dass die Linie nicht einfach grade ist, sondern sich an der Krümmung der Erdkugel orientiert
    fun addRoutes(mMap: GoogleMap){
        origin.forEach{country->
            mMap.addMarker(MarkerOptions().position(country.coords).title(country.name))
            mMap.addPolyline(
                PolylineOptions().add(germany).add(country.coords).width(10f).color(
                    Color.DKGRAY).geodesic(true).clickable(true)).tag=country

            //bewegt die kamera zwischen die einzelnen marker
            centre= SphericalUtil.interpolate(centre,country.coords,0.3)


        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centre))
    }
    //inits map & camera on germany
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(germany).title("Marker in Germany"))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
        addRoutes(mMap)
        mMap.setOnPolylineClickListener(this)
    }


}
