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

//markiert die in herkunft spezifizierten Länder
fun addOutline(map: GoogleMap, herkunft:List<Int>, context: Context, onClick: (Feature)->Unit){
    if(herkunft.isEmpty()){
        Toast.makeText(context, "Zu diesem Produkt existieren keine Herkunftsinformationen", Toast.LENGTH_LONG).show()
        //Gibt Fehlermeldung aus, das keine Herkunftsinfos vorliegen
    }
    herkunft.forEach{
        //Überprüft ob Objekt in der Liste eine GEOJson Datei im richtigen Format ist
        runCatching {
            GeoJsonLayer(map, it, context)
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
                layer.setOnFeatureClickListener(onClick)
            }


    }
}

fun addOutlineFromJSON(map: GoogleMap, herkunft:List<JSONObject>, context: Context, onClick: (Feature)->Unit){
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
                layer.setOnFeatureClickListener(onClick)
            }


    }
}
fun addLabel(map:GoogleMap, herkunft: List<Pair<LatLng,String>>, context: Context){
    herkunft.forEach{
        map.addMarker(MarkerOptions()
                .position(it.first)
                .title(it.second))
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
fun addRoutes(mMap: GoogleMap, herkunft:List<RouteActivity.Input>, context: Context, initPos:LatLng){
    var centre=initPos
    herkunft.forEach{country->
        runCatching {  mMap.addMarker(MarkerOptions().position(country.coords).title(country.name))
        }.onFailure {
                Toast.makeText(context, "Markierung konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
                Log.e("Marker", "konnte nicht hinzugefügt werden", it)
            }
        runCatching {
            mMap.addPolyline(
            PolylineOptions().add(germany).add(country.coords).width(10f).color(
                Color.DKGRAY).geodesic(true).clickable(true).zIndex(1.0f))
        }.onSuccess {
                it.tag=country

        }.onFailure {
                Toast.makeText(context, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG).show()
                Log.e("Route", "konnte nicht hinzugefügt werden", it)
            }

        //bewegt die kamera zwischen die einzelnen marker
        centre= SphericalUtil.interpolate(centre,country.coords,0.3)
    }
    mMap.moveCamera(CameraUpdateFactory.newLatLng(centre))

}

