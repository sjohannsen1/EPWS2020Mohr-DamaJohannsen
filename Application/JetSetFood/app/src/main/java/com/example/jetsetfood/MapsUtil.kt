package com.example.jetsetfood

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.Feature
import com.google.maps.android.data.Layer
import com.google.maps.android.data.geojson.GeoJsonLayer
import org.json.JSONObject
import kotlin.math.roundToInt

//markiert die in herkunft spezifizierten Länder
fun addOutlineFromJSON(map: GoogleMap, herkunft:List<JSONObject>, context: Context){
    if(herkunft.isEmpty()){
        Toast.makeText(context, "Zu diesem Produkt existieren keine Herkunftsinformationen", Toast.LENGTH_LONG).show()
        //Gibt Fehlermeldung aus, das keine Herkunftsinfos vorliegen
    }
    herkunft.forEach{
        //Überprüft ob Objekt in der Liste eine GEOJson Datei im richtigen Format ist
        runCatching {
            GeoJsonLayer(map, it)
            //Wenn nicht wird ein Toast angezeigt
        }.onFailure {
            Toast.makeText(context, "Land kann nicht markiert werden", Toast.LENGTH_LONG)
                .show()
            Log.e("GeoJson", "konnte nicht eingelesen werden", it)
        }
            //Wenn schon werden die Daten angezeigt und das Land kann markiert werden
            .onSuccess {layer ->
                layer.defaultPolygonStyle.strokeWidth = 0.0f
                layer.defaultPolygonStyle.fillColor = Color.DKGRAY
                layer.addLayerToMap()

                if (!layer.isLayerOnMap) {
                    Toast.makeText(context, "Land kann nicht markiert werden", Toast.LENGTH_SHORT).show()
                    Log.e("GeoJson", "Layer konnte nicht zur Karte hinzugefügt werden")
                    //Gibt Fehlermeldung aus, das Länder nicht markiert werden können
                }

                //wenn Land angeklickt wird sollen routen angezeigt werden
                //layer.setOnFeatureClickListener(onClick)
            }


    }
}

fun addFarmingMethod(map: GoogleMap, context: Context, farmingMethods:List<String>){
    val blurb=farmingMethods.fold("")
    { acc, cur -> when (cur) {
        "FR"-> acc +"\n"+ context.getString(R.string.FR)
        "UG"-> acc +"\n"+ context.getString(R.string.UG)
        "GG"-> acc +"\n"+ context.getString(R.string.GG)
        "LA"-> acc +"\n"+ context.getString(R.string.LA)
        "GA"-> acc +"\n"+ context.getString(R.string.GA)
        else -> acc
    } }
    map.addMarker(MarkerOptions()
        .position(germany)
        .title("Deutschland")
        .snippet(blurb)
    )
    val layerGermany= GeoJsonLayer(map, germanyArea, context)
    when {
        farmingMethods.contains("GG") -> layerGermany.defaultPolygonStyle.fillColor=Color.RED
        farmingMethods.contains("UG") || farmingMethods.contains("LA") || farmingMethods.contains("GA") -> layerGermany.defaultPolygonStyle.fillColor=context.getColor(R.color.orange)
        farmingMethods.contains("FR") -> layerGermany.defaultPolygonStyle.fillColor=Color.GREEN
    }

}
fun addLabel(map: GoogleMap, herkunft: List<Country>, context: Context){
    herkunft.forEach {
        map.addMarker(MarkerOptions()
            .position(LatLng(it.mittelpunkt.first().latitude, it.mittelpunkt.first().longitude))
            .title(it.land))
    }
}
//Markiert Deutschland als Heimatland, kann durch Geolocating geändert werden
fun addOrigin(map: GoogleMap, name:String, position: LatLng, flaeche:Int , context: Context){
    map.addMarker(MarkerOptions()
        .position(position)
        .title(name))
    val layerGermany= GeoJsonLayer(map, flaeche, context)
    layerGermany.defaultPolygonStyle.strokeWidth=0.0f
    layerGermany.defaultPolygonStyle.fillColor=Color.LTGRAY
    layerGermany.addLayerToMap()
}

//fügt Polylines zwischen Deutschland und den Herkunftsländern
//geodesic bedeutet dass die Linie nicht einfach grade ist, sondern sich an der Krümmung der Erdkugel orientiert
//fügt routen hinzu und fügt auch nen clickable marker hinzu der es ermöglicht, das die distanz angezeigt wird
fun addRoutes(mMap: GoogleMap, herkunft:List<Country>, context: Context, initPos:LatLng, origin:LatLng){
    var centre=initPos
    herkunft.forEach{country->
        val coords=LatLng(country.mittelpunkt.first().latitude, country.mittelpunkt.first().longitude)
        runCatching {  mMap.addMarker(MarkerOptions().position(coords).title(country.land))
        }.onFailure {
            Toast.makeText(context, "Markierung konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
            Log.e("Marker", "konnte nicht hinzugefügt werden", it)
        }
        runCatching {
            mMap.addPolyline(
                PolylineOptions().add(origin).add(coords).width(10f).color(
                    Color.DKGRAY).geodesic(true).clickable(true).zIndex(1.0f))
            mMap.addMarker(MarkerOptions()
                .position(SphericalUtil.interpolate(origin,coords,0.5))
                .flat(true)
                .title("Distanz:")
                .snippet("${((SphericalUtil.computeDistanceBetween(origin, coords)) / 10).roundToInt().toDouble() / 100} km"))
        }.onFailure {
            Toast.makeText(context, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
            Log.e("Route", "konnte nicht hinzugefügt werden", it)
        }

        //bewegt die kamera zwischen die einzelnen marker
        centre= SphericalUtil.interpolate(centre,coords,0.3)
    }
    mMap.moveCamera(CameraUpdateFactory.newLatLng(centre))

}
