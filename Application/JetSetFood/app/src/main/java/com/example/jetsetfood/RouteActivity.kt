package com.example.jetsetfood

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlin.math.roundToInt




class RouteActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

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

    //wird aufgerufen, wenn die Polyline angeklickt wird
    override fun onPolylineClick(line: Polyline){
        Log.i("Polyline", "Click")
        line.color= Color.RED
        //zeigt distanz an
        mMap.addMarker(MarkerOptions()
            .position(SphericalUtil.interpolate(line.points.first(),line.points[1],0.5))
            .alpha(0.0F)
            .flat(true)
            .title("Distanz:")
            .snippet(line.tag.toString()))
            .showInfoWindow()
    }

   /* //fügt Polylines zwischen Deutschland und den Herkunftsländern
    //geodesic bedeutet dass die Linie nicht einfach grade ist, sondern sich an der Krümmung der Erdkugel orientiert
    fun addRoutes(mMap: GoogleMap, herkunft:List<Input>){
        herkunft.forEach{country->
           runCatching {  mMap.addMarker(MarkerOptions().position(country.coords).title(country.name)) }
               .onFailure {
                   Toast.makeText(this, "Markierung konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
                   Log.e("Marker", "konnte nicht hinzugefügt werden", it)
               }
          runCatching { mMap.addPolyline(
              PolylineOptions().add(germany).add(country.coords).width(10f).color(
                  Color.DKGRAY).geodesic(true).clickable(true).zIndex(1.0f)) }
              .onSuccess {
                  it.tag=country

              }

              .onFailure {
                  Toast.makeText(this, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
                  Log.e("Route", "konnte nicht hinzugefügt werden", it)
              }

            //bewegt die kamera zwischen die einzelnen marker
            centre= SphericalUtil.interpolate(centre,country.coords,0.3)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centre))

    }



    //Markiert Deutschland als Heimatland, kann durch Geolocating geändert werden
    fun addOrigin(map: GoogleMap,name:String, position:LatLng, flaeche:Int ){
        map.addMarker(MarkerOptions().position(position).title(name))
    }
*/

    //inits map & camera on germany
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Karte reduzieren auf das wichtigste
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
        addOrigin(mMap, "Deutschland", germany, R.raw.germany, this)
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
        addRoutes(mMap, origin,this, centre)
        mMap.setOnPolylineClickListener(this)

    }


}
