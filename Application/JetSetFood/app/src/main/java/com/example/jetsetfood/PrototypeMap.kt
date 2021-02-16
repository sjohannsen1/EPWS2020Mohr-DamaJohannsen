package com.example.jetsetfood



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking



class PrototypeMap : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener {
    //lateinit vars werden in der onMapsReady gesetzt
    private lateinit var mMap: GoogleMap
    private lateinit var textView:TextView
    private lateinit var markerManager: MarkerManager
    private lateinit var markerCollection: MarkerManager.Collection
    private lateinit var polylineCollection: PolylineManager.Collection
    private lateinit var polylineManager: PolylineManager

    //Objekte für die Utilities
    private val produceUtil=ProduceUtil()
    private val database=DatabaseUtil(produceUtil)

    //um layer wieder entfernen zu können werden sie zwischengespeichert
    private var layerOnMap:GeoJsonLayer?=null

    //Um die Lesbarkeit zu verbessern
    private val context=this@PrototypeMap


    val germany = LatLng(51.5167, 9.9167)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype_map)
        textView=findViewById(R.id.info)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)


    }

    //Wenn ein Marker geklickt wird, soll das zugehörige Land markiert werden.
    //Außerdem soll falls ein anderes Land markiert ist, diese Markierung wieder entfernt werden
    override fun onMarkerClick(marker: Marker?): Boolean {
        if(marker?.tag!="GER") {
            if(layerOnMap!=null) {
                layerOnMap?.removeLayerFromMap()
            }
            runBlocking {
                    val geoJSON = async {
                        database.getGeoJson(marker?.tag as String)
                    }
                    layerOnMap=addOutlineFromJSONClustered(mMap, geoJSON.await(), context, markerManager)

                }
        }
        return false //signalisiert dass das standartverhalten von OnMarkerClickEvents noch durchgeführt werden soll, also das Infofenster gezeigt werden soll und die Karte auf dem Marker zentriert wird

    }

    //Wenn eine Polyline geklickt wird, soll das zugehörige Land markiert werden.
    //Außerdem soll falls ein anderes Land markiert ist, diese Markierung wieder entfernt werden
    override fun onPolylineClick(polyline: Polyline?) {
        Log.e("Polyline", "click ${polyline?.tag}")
        if(polyline?.tag!="GER") {
            runBlocking {
                val geoJSON = async {
                    database.getGeoJson(polyline?.tag as String)
                }
                addOutlineFromJSONClustered(mMap, geoJSON.await(), context, markerManager)

            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //lateinit vars setzen
        mMap = googleMap
        markerManager= MarkerManager(mMap)
        markerCollection=markerManager.Collection()
        polylineManager=PolylineManager(mMap)
        polylineCollection=polylineManager.Collection()


        //Karte reduzieren
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())

        runBlocking {

            //Die Nutzeranfrage aus Intent extrahieren und an die API senden
            val produce= async{
                database.getProduce(intent.extras?.getString("input")!!)
            }.await()

            //Wenn das Produceobjekt da ist, Saisonkalender extrahieren und Länder und Routen markieren
            when {
                produce != null -> {
                    val origin = produceUtil.inSeason(produce, produceUtil.currentMonth)
                    val countries = async {
                        database.getOrigin(origin)
                    }
                    var displayCountries =
                        countries.await()?.first?.map { it.land }
                    Log.d("countries", displayCountries.toString())

                    //Falls Anbaumethoden vorhanden sind, werden diese auf der Karte eingetragen
                    if (!countries.await()?.second.isNullOrEmpty()) {

                        addFarmingMethodClustered(mMap, context, countries.await()?.second!!,markerManager,polylineManager)
                        displayCountries = displayCountries?.plus(listOf("Deutschland"))

                    } else

                        //Sonst soll Deutschland nur als Heimatland markiert werden
                        addOriginClustered(mMap, "Deutschland", germany, R.raw.germany, context, markerManager, polylineManager)

                    //Routen und Marker werden hinzugefügt
                    addRoutesClustered(countries.await()?.first, context,germany,markerCollection, polylineCollection, onMarkerClick = context, onPolyClick = context)

                    //Saisonkalender wird im TextView angezeigt
                    textView.text=getString(R.string.saisonkalender,produceUtil.monthNames[produceUtil.currentMonth],produceUtil.convertUmlaut(produce.name, false),produceUtil.makeString(
                        displayCountries,
                        ", ",
                        " oder "
                    ))
                }
                else -> Log.e("api", "nebenläufigkeit kaputt ")
            }

        }

    }

}
