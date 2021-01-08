package com.example.jetsetfood

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

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
//mongo
val  mongo = App(AppConfiguration.Builder("thkrealm-fxaij")
    .build())
private val latGermany=51.5167
private val lngGermany=9.9167
val germany = LatLng(latGermany, lngGermany)

enum class TaskStatus(val displayName: String) {
    Open("Open"),
    InProgress("In Progress"),
    Complete("Complete"),
}

open class Task(_name: String = "Task", project: String = "My Project") : RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = _name

    @Required
    var status: String = TaskStatus.Open.name
    var statusEnum: TaskStatus
        get() {
            // because status is actually a String and another client could assign an invalid value,
            // default the status to "Open" if the status is unreadable
            return try {
                TaskStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                TaskStatus.Open
            }
        }
        set(value) { status = value.name }
}
val credentials: Credentials = Credentials.anonymous()

fun authenticateUser() {
    mongo.loginAsync(credentials) {
        if (it.isSuccess) {
            Log.v("QUICKSTART", "Successfully authenticated anonymously.")
            val user: User? = mongo.currentUser()
            // interact with realm using your user object here
        } else {
            Log.e("QUICKSTART", "Failed to log in. Error: ${it.error}")
        }
    }
}
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {




    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        Realm.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poc__country)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    fun addOutline(geoJson: JSONObject){

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val layer=GeoJsonLayer(mMap, R.raw.brazil, this)
        layer.addLayerToMap()
        layer.setOnFeatureClickListener { startActivity(Intent(this, RouteActivity::class.java))}
        // Add a marker in Germany
        mMap.addMarker(MarkerOptions().position(germany).title("Marker in Germany"))
        mMap.moveCamera(CameraUpdateFactory.zoomOut())
    }
}
