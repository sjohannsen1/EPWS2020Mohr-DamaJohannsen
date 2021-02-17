package com.example.jetsetfood

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.geojson.GeoJsonLayer
import org.json.JSONObject
import kotlin.math.roundToInt

object MapsUtil {

    val germany = LatLng(51.5167, 9.9167)
    const val germanyArea = R.raw.germany

    private fun vectorToBitmap(
        @DrawableRes id: Int,
        context: Context,
        scaling: Int = 11
    ): BitmapDescriptor {
        val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(context.resources, id, null)
        if (vectorDrawable == null) {
            Log.e("Markericon", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth / scaling,
            vectorDrawable.intrinsicHeight / scaling, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    //markiert die in herkunft spezifizierten Länder
    fun addOutlineFromJSONClustered(
        map: GoogleMap,
        herkunft: JSONObject?,
        context: Context,
        markerManager: MarkerManager,
        polylineManager: PolylineManager
    ): GeoJsonLayer? =
        if (herkunft == null) {
            Toast.makeText(
                context,
                "Zu diesem Produkt existieren keine Herkunftsinformationen",
                Toast.LENGTH_LONG
            ).show()
            //Gibt Fehlermeldung aus, das keine Herkunftsinfos vorliegen, und gibt null zurück
            null
        } else {
            //Überprüft ob Objekt eine GEOJson Datei im richtigen Format ist
            val layer = runCatching {
                GeoJsonLayer(
                    map,
                    herkunft,
                    markerManager,
                    PolygonManager(map),
                    polylineManager,
                    GroundOverlayManager(map)
                )

                //Wenn nicht wird ein Toast angezeigt
            }.onFailure {
                Toast.makeText(context, "Land kann nicht markiert werden", Toast.LENGTH_LONG)
                    .show()
                Log.e("GeoJson", "konnte nicht eingelesen werden", it)
            }
                //Wenn schon werden die Daten angezeigt und das Land kann markiert werden
                .onSuccess { layer ->
                    layer.defaultPolygonStyle.strokeWidth = 0.0f
                    layer.defaultPolygonStyle.fillColor = Color.DKGRAY
                    layer.addLayerToMap()

                    if (!layer.isLayerOnMap) {
                        Toast.makeText(
                            context,
                            "Land kann nicht markiert werden",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("GeoJson", "Layer konnte nicht zur Karte hinzugefügt werden")
                        //Gibt Fehlermeldung aus, das Länder nicht markiert werden können
                    }

                }
            layer.getOrNull()

        }


    fun addFarmingMethodClustered(
        map: GoogleMap,
        context: Context,
        farmingMethods: List<String>,
        markerManager: MarkerManager,
        polylineManager: PolylineManager
    ) {
        //Log.d("Laenderliste", farmingMethods.fold(""){acc, cur -> acc+""+cur})
        val blurb = ProduceUtil.makeString(farmingMethods.map {
            when (it.toLowerCase()) {
                "fr" -> context.getString(R.string.FR)
                "ug" -> context.getString(R.string.UG)
                "gg" -> context.getString(R.string.GG)
                "la" -> context.getString(R.string.LA)
                "ga" -> context.getString(R.string.GA)
                else -> "ALARM! + $it"
            }
        }, "", " oder ")
        Log.d("Laenderliste", blurb)
        map.addMarker(
            MarkerOptions()
                .position(germany)
                .title("Deutschland")
                .snippet(blurb)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )
        val layerGermany = GeoJsonLayer(
            map,
            germanyArea,
            context,
            markerManager,
            PolygonManager(map),
            polylineManager,
            GroundOverlayManager(map)
        )
        layerGermany.defaultPolygonStyle.strokeWidth = 0f
        when {
            farmingMethods.contains("gg") -> layerGermany.defaultPolygonStyle.fillColor =
                context.getColor(R.color.red)
            farmingMethods.contains("ug") || farmingMethods.contains("la") || farmingMethods.contains(
                "ga"
            ) -> layerGermany.defaultPolygonStyle.fillColor = context.getColor(R.color.orange)
            farmingMethods.contains("fr") -> layerGermany.defaultPolygonStyle.fillColor =
                context.getColor(R.color.green)
        }
        layerGermany.addLayerToMap()

    }

    //Markiert Deutschland als Heimatland, kann durch Geolocating geändert werden
    fun addOriginClustered(
        map: GoogleMap,
        name: String, position: LatLng, flaeche: Int,
        context: Context,
        markerManager: MarkerManager,
        polylineManager: PolylineManager
    ) {
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(name)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )

        val layerGermany = GeoJsonLayer(
            map, flaeche, context, markerManager, PolygonManager(map),
            polylineManager, GroundOverlayManager(map)
        )
        layerGermany.defaultPolygonStyle.strokeWidth = 0.0f
        layerGermany.defaultPolygonStyle.fillColor = Color.LTGRAY
        layerGermany.addLayerToMap()
    }


//fügt Polylines zwischen Deutschland und den Herkunftsländern
//geodesic bedeutet dass die Linie nicht einfach grade ist, sondern sich an der Krümmung der Erdkugel orientiert
//fügt routen hinzu und fügt auch nen clickable marker hinzu der es ermöglicht, das die distanz angezeigt wird

    fun addRoutesClustered(
        herkunft: List<Country>?,
        context: Context,
        origin: LatLng,
        markerCollection: MarkerManager.Collection,
        polylineCollection: PolylineManager.Collection,
        onMarkerClick: GoogleMap.OnMarkerClickListener,
        onPolyClick: GoogleMap.OnPolylineClickListener

    ) {
        herkunft?.forEach { country ->
            val coords = LatLng(country.latitude, country.longitude)
            runCatching {
                markerCollection.addMarker(
                    MarkerOptions()
                        .position(coords)
                        .title(country.land)
                        .snippet(
                            "Distanz: ${((SphericalUtil.computeDistanceBetween(
                                origin,
                                coords
                            ) / 10).roundToInt().toDouble() / 100)} km"
                        )
                        .flat(true)
                        .icon(vectorToBitmap(R.drawable.ic_pin, context))


                ).tag = country.laendercode
                markerCollection.setOnMarkerClickListener(onMarkerClick)
                Log.d("Marker", country.latitude.toString()+country.longitude)
            }.onFailure {
                Toast.makeText(
                    context,
                    "Markierung konnte nicht hinzugefügt werden",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Marker", "konnte nicht hinzugefügt werden", it)
            }
            runCatching {
                polylineCollection.addPolyline(
                    PolylineOptions().add(origin).add(coords).width(8f).color(
                        Color.GRAY
                    ).geodesic(true).clickable(true).zIndex(1.0f)
                ).tag = country.laendercode
                polylineCollection.setOnPolylineClickListener(onPolyClick)
            }.onFailure {
                Toast.makeText(context, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG)
                    .show()
                Log.e("Route", "konnte nicht hinzugefügt werden", it)
            }

            //bewegt die kamera zwischen die einzelnen marker

        }


    }


    fun addOrigin(map: GoogleMap, name: String, position: LatLng, flaeche: Int, context: Context) {
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(name)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )

        val layerGermany = GeoJsonLayer(map, flaeche, context)
        layerGermany.defaultPolygonStyle.strokeWidth = 0.0f
        layerGermany.defaultPolygonStyle.fillColor = Color.LTGRAY
        layerGermany.addLayerToMap()
    }


    fun addRoutes(mMap: GoogleMap, herkunft: List<Country>?, context: Context, origin: LatLng) {
        herkunft?.forEach { country ->
            val coords = LatLng(country.latitude, country.longitude)
            runCatching {
                mMap.addMarker(
                    MarkerOptions()
                        .position(coords)
                        .title(country.land)
                        .snippet(
                            "Distanz: ${((SphericalUtil.computeDistanceBetween(
                                origin,
                                coords
                            ) / 10).roundToInt().toDouble() / 100)} km"
                        )
                        .flat(true)
                        .icon(vectorToBitmap(R.drawable.ic_pin, context))


                ).tag = country.laendercode
            }.onFailure {
                Toast.makeText(
                    context,
                    "Markierung konnte nicht hinzugefügt werden",
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e("Marker", "konnte nicht hinzugefügt werden", it)
            }
            runCatching {
                mMap.addPolyline(
                    PolylineOptions().add(origin).add(coords).width(8f).color(
                        Color.GRAY
                    ).geodesic(true).clickable(true).zIndex(1.0f)
                )
                /*mMap.addMarker(MarkerOptions()
                .position(SphericalUtil.interpolate(origin,coords,0.5))
                .flat(true)
                .title("Distanz:")
                .snippet("${((SphericalUtil.computeDistanceBetween(origin, coords)) / 10).roundToInt().toDouble() / 100} km"))*/
            }.onFailure {
                Toast.makeText(context, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG)
                    .show()
                Log.e("Route", "konnte nicht hinzugefügt werden", it)
            }

            //bewegt die kamera zwischen die einzelnen marker

        }
    }


    fun addFarmingMethod(map: GoogleMap, context: Context, farmingMethods: List<String>) {
        //Log.d("Laenderliste", farmingMethods.fold(""){acc, cur -> acc+""+cur})
        val blurb = ProduceUtil.makeString(farmingMethods.map {
            when (it.toLowerCase()) {
                "fr" -> context.getString(R.string.FR)
                "ug" -> context.getString(R.string.UG)
                "gg" -> context.getString(R.string.GG)
                "la" -> context.getString(R.string.LA)
                "ga" -> context.getString(R.string.GA)
                else -> "ALARM!"
            }
        }, "", " oder ")
        Log.d("Laenderliste", blurb)
        map.addMarker(
            MarkerOptions()
                .position(germany)
                .title("Deutschland")
                .snippet(blurb)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )
        val layerGermany = GeoJsonLayer(map, germanyArea, context)
        layerGermany.defaultPolygonStyle.strokeWidth = 0f
        when {
            farmingMethods.contains("gg") -> layerGermany.defaultPolygonStyle.fillColor =
                context.getColor(R.color.red)
            farmingMethods.contains("ug") || farmingMethods.contains("la") || farmingMethods.contains(
                "ga"
            ) -> layerGermany.defaultPolygonStyle.fillColor = context.getColor(R.color.orange)
            farmingMethods.contains("fr") -> layerGermany.defaultPolygonStyle.fillColor =
                context.getColor(R.color.green)
        }
        layerGermany.addLayerToMap()

    }


    //markiert die in herkunft spezifizierten Länder
    fun addOutlineFromJSON(map: GoogleMap, herkunft: List<JSONObject>?, context: Context) {
        if (herkunft == null || herkunft.isEmpty()) {
            Toast.makeText(
                context,
                "Zu diesem Produkt existieren keine Herkunftsinformationen",
                Toast.LENGTH_LONG
            ).show()
            //Gibt Fehlermeldung aus, das keine Herkunftsinfos vorliegen
        } else {
            herkunft.forEach {
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
                    .onSuccess { layer ->
                        layer.defaultPolygonStyle.strokeWidth = 0.0f
                        layer.defaultPolygonStyle.fillColor = Color.DKGRAY
                        layer.addLayerToMap()

                        if (!layer.isLayerOnMap) {
                            Toast.makeText(
                                context,
                                "Land kann nicht markiert werden",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("GeoJson", "Layer konnte nicht zur Karte hinzugefügt werden")
                            //Gibt Fehlermeldung aus, das Länder nicht markiert werden können
                        }

                        //wenn Land angeklickt wird sollen routen angezeigt werden
                        //layer.setOnFeatureClickListener(onClick)
                    }


            }
        }
    }

    fun addLabel(map: GoogleMap, herkunft: List<Country>?, context: Context) {
        herkunft?.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.laendercode)
                    .snippet(
                        "Distanz: ${((SphericalUtil.computeDistanceBetween(
                            germany,
                            LatLng(it.latitude, it.longitude)
                        )) / 10).roundToInt().toDouble() / 100} km"
                    )
                    .icon(vectorToBitmap(R.drawable.ic_pin, context))

            )
        } ?: Log.e("Label", "Herkunft ist leer")
    }

}
/*
class MarkerClick(val mMap: GoogleMap, val context: Context, val origin: LatLng):GoogleMap.OnMarkerClickListener{
    override fun onMarkerClick(marker: Marker?): Boolean {
        runCatching {
            mMap.addPolyline(
                PolylineOptions().add(origin).add(marker?.position).width(10f).color(
                    Color.DKGRAY
                ).geodesic(true).clickable(true).zIndex(1.0f)
            )
            mMap.addMarker(
                MarkerOptions()
                    .position(SphericalUtil.interpolate(origin, marker?.position, 0.5))
                    .flat(true)
                    .title("Distanz:")
                    .snippet(
                        "${((SphericalUtil.computeDistanceBetween(
                            origin,
                            marker?.position
                        )) / 10).roundToInt().toDouble() / 100} km"
                    )
            )
            Log.d("Marker", "click ${marker?.tag}")
        }.onSuccess { return true }
        return true
    }
}*/
