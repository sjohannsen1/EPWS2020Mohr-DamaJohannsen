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

/**
 * Objekt zum Handling aller Interaktionen mit der Karte
 *
 */

object MapsUtil {

    //die Fläche und der Mittelpunkt von Deutschland, um zur Einzeichnung der Herkunft keine Interaktion mit der Dateenbank zu benötigen
    val germany = LatLng(51.5167, 9.9167)
    const val germanyArea = R.raw.germany

    /***
     * Erzeugt aus einer Vektorgrafik eine Bitmap
     * @param id: Id der Verktografik
     * @param context: Kontext um auf die Resourcen zuzugreifen
     * @param scaling: Skalierung der Bitmap
     * @return Bitmapdescriptor mit der erzeugten Bitmap
     * @return Bitmapdescriptor mit dem defaultMarker bei Fehlern
     */
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

    /***
     * Markiert das in herkunft spezifizierte Land als GEOJsonLayer
     * @param map: Karte auf der das Land eingezeichnet werden soll
     * @param herkunft: JSONObjekt mit de GEOJson des zu markierenden Landes
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param markerManager: Manager der alle Marker auf der Karte enthält, um onMarkerClick Ereignisse zu erfassen
     * @param polyloneManager: Manager der alle Polylines auf der Karte enthält, um onPolylineClick Ereignisse zu erfassen
     * @return das erzeugte GEOJsonLayer
     */
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

    /***
     * Markiert die in farmingMethods spezifizierten Methoden farblich auf der FLäche von Deutschland
     * und fügt Marker auf Deutschland hinzu der diese textuell enthält
     * @param map: Karte auf der die Methoden deingezeichnet werden sollen
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param farmingMethods: Liste mit String die die Farmingmethoden enthalten
     * @param markerManager: Manager der alle Marker auf der Karte enthält, um onMarkerClick Ereignisse zu erfassen
     * @param polyloneManager: Manager der alle Polylines auf der Karte enthält, um onPolylineClick Ereignisse zu erfassen
     * @return das erzeugte GEOJsonLayer
     */
    fun addFarmingMethodClustered(
        map: GoogleMap,
        context: Context,
        farmingMethods: List<String>,
        markerManager: MarkerManager,
        polylineManager: PolylineManager
    ) {
        //erzeugt String mit Farmingmethoden
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
        //fügt Marker auf Deutschland zur Karte hinzu
        map.addMarker(
            MarkerOptions()
                .position(germany)
                .title("Deutschland")
                .snippet(blurb)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )
        //Fügt Fläche zu Deutschland hinzu
        val layerGermany = GeoJsonLayer(
            map,
            germanyArea,
            context,
            markerManager,
            PolygonManager(map),
            polylineManager,
            GroundOverlayManager(map)
        )
        //Färbt diese Fläche in Korrelation zu den Anbaumethoden ein
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
    /***
     * Markiert Heimat/Aufenthaltsland
     * @param map: Karte auf der das Land eingezeichnet werden soll
     * @param name, position, fläche: Name, Mittelpunkt und Fläche des Heimat/Aufenthaltslandes
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param markerManager: Manager der alle Marker auf der Karte enthält, um onMarkerClick Ereignisse zu erfassen
     * @param polyloneManager: Manager der alle Polylines auf der Karte enthält, um onPolylineClick Ereignisse zu erfassen
     */

    fun addOriginClustered(
        map: GoogleMap,
        name: String="Deutschland", position: LatLng = germany, flaeche: Int = germanyArea,
        context: Context,
        markerManager: MarkerManager,
        polylineManager: PolylineManager
    ) {
        //Fügt Marker im Heimat/Aufenthaltsland hinzu
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(name)
                .icon(vectorToBitmap(R.drawable.ic_pinhomedark, context))
        )
        //Fügt Fläche des Heimat/Aufenthaltsland hinzu
        val layerGermany = GeoJsonLayer(
            map, flaeche, context, markerManager, PolygonManager(map),
            polylineManager, GroundOverlayManager(map)
        )
        layerGermany.defaultPolygonStyle.strokeWidth = 0.0f
        layerGermany.defaultPolygonStyle.fillColor = Color.LTGRAY
        layerGermany.addLayerToMap()
    }

    /***
     * fügt Polylines zwischen dem Heimat/Aufenthaltsland und den Herkunftsländern zur Collection hinzu
     * fügt Marker im Mittelpunkt der Herkunftsländer zur Collection hinzu
     * @param herkunft: Liste der einzufügenden Länder
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param origin: Mittelpunkt des Heimat/Aufenthaltslandes
     * @param markerCollection: Collection die alle Marker auf der Karte enthält, um onMarkerClick Ereignisse zu erfassen
     * @param polyloneCollection: Collection die alle Polylines auf der Karte enthält, um onPolylineClick Ereignisse zu erfassen
     * @param onMarkerClick: onClickListener in dem steht was bei onClick Ereignissen auf die Marker passieren soll
     * @param onPolyClick: onClickListener in dem steht was bei onClick Ereignissen auf die Polylines passieren soll
     */
    fun addRoutesClustered(
        herkunft: List<Country>?,
        context: Context,
        origin: LatLng= germany,
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
                //Bei Fehlern
                Toast.makeText(
                    context,
                    "Markierung konnte nicht hinzugefügt werden",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Marker", "konnte nicht hinzugefügt werden", it)
            }
            //Fügt Routen hinzu
            runCatching {
                polylineCollection.addPolyline(
                    PolylineOptions().add(origin).add(coords).width(8f).color(
                        Color.GRAY
                    ).geodesic(true) //geodesic bedeutet dass die Linie nicht einfach grade ist, sondern sich an der Krümmung der Erdkugel orientiert
                        .clickable(true)
                        .zIndex(1.0f)
                ).tag = country.laendercode
                polylineCollection.setOnPolylineClickListener(onPolyClick)
            }.onFailure {
                //Bei Fehlern
                Toast.makeText(context, "Route konnte nicht hinzugefügt werden", Toast.LENGTH_LONG)
                    .show()
                Log.e("Route", "konnte nicht hinzugefügt werden", it)
            }
        }
    }

    /***
     * Markiert Heimat/Aufenthaltsland, gewährleisten nicht die Klickbarkeit mehrer Objekte
     * @param map: Karte auf der das Land eingezeichnet werden soll
     * @param name, position, fläche: Name, Mittelpunkt und Fläche des Heimat/Aufenthaltslandes
     * @param context: Kontext um Fehlertoast anzuzeigen
     */
    fun addOrigin(map: GoogleMap, name: String="Deutschland", position: LatLng=germany, flaeche: Int= germanyArea, context: Context) {
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

    /***
     * fügt Polylines zwischen dem Heimat/Aufenthaltsland und den Herkunftsländern zur Karte hinzu
     * fügt Marker im Mittelpunkt der Herkunftsländer zur Karte hinzu
     * Gewährleistet nicht die Klickbarkeit der einzelnen Elemente
     * @param herkunft: Liste der einzufügenden Länder
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param origin: Mittelpunkt des Heimat/Aufenthaltslandes
      */
    fun addRoutes(mMap: GoogleMap, herkunft: List<Country>?, context: Context, origin: LatLng=germany) {
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

    /***
     * Markiert die in farmingMethods spezifizierten Methoden farblich auf der Fläche von Deutschland
     * und fügt Marker auf Deutschland hinzu der diese textuell enthält
     * Gewährleistet nicht die Klickbarkeit der einzelnen Elemente
     * @param map: Karte auf der die Methoden deingezeichnet werden sollen
     * @param context: Kontext um Fehlertoast anzuzeigen
     * @param farmingMethods: Liste mit String die die Farmingmethoden enthalten
     */
    fun addFarmingMethod(map: GoogleMap, context: Context, farmingMethods: List<String>) {

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

    /***
     * Markiert das in herkunft spezifizierte Land als GEOJsonLayer
     * Gewährleistet nicht die Klickbarkeit der einzelnen Elemente
     * @param map: Karte auf der das Land eingezeichnet werden soll
     * @param herkunft: Liste mit JSONObjekten mit der GEOJson der zu markierenden Ländern
     * @param context: Kontext um Fehlertoast anzuzeigen
     */
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
    /***
     * Markiert das in herkunft spezifizierte Land mit einem Marker im Mittelpunkt
     * Gewährleistet nicht die Klickbarkeit der einzelnen Elemente
     * @param map: Karte auf der das Land eingezeichnet werden soll
     * @param herkunft: Liste mit den hinzuzufügenden Ländern
     * @param context: Kontext um Fehlertoast anzuzeigen
     */
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
