package es.didaktikapp.gernikapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import com.google.android.gms.maps.model.MarkerOptions
import androidx.appcompat.app.AlertDialog

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var nMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap

        val gernika = LatLng(43.3170, -2.6789)
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gernika, 16f))

        nMap.uiSettings.isZoomControlsEnabled = true

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.313287, -2.679579))
                .title("Gernikako Arbola eta Juntetxea")
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.312137, -2.676608))
                .title("Astrako bunkerrak")
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.315513, -2.680047))
                .title("Picasso-ren “Guernica”")
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.316139, -2.676672))
                .title("Gernikako azoka plaza")
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.317399, -2.678783))
                .title("Jai-Alai frontoia")
        )

        nMap.setOnMarkerClickListener { marker ->
            when (marker.title) {
                "Astrako bunkerrak" -> {
                    mostrarDialogo(
                        titulo = "Astrako bunkerrak",
                        mensaje = "Astrako bunkerraren aurrean zaude.\nSartu nahi duzu?"
                    )
                }

                "Jai-Alai frontoia" -> {
                    mostrarDialogo(
                        titulo = "Jai-Alai frontoia",
                        mensaje = "Jai-Alai frontoira iritsi zara.\nSartu nahi duzu?"
                    )
                }

                "Gernikako Arbola eta Juntetxea" -> {
                    mostrarDialogo(
                        titulo = "Gernikako Arbola eta Juntetxea",
                        mensaje = "Gernikako arbolara iritsi zara.\nSartu nahi duzu?"
                    )
                }

                "Gernikako azoka plaza" -> {
                    mostrarDialogo(
                        titulo = "Gernikako azoka plaza",
                        mensaje = "Gernikako azoka plazara iritsi zara.\nSartu nahi duzu?"
                    )
                }

                "Picasso-ren “Guernica”" -> {
                    mostrarDialogo(
                        titulo = "Picasso-ren “Guernica”",
                        mensaje = "Picasso-ren 'Guernica-ra' iritsi zara.\nSartu nahi duzu?"
                    )
                }
            }

            true
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            nMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001)
        }
    }

    private fun mostrarDialogo(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Sartu") { _, _ ->
                Toast.makeText(this, "Sartu zara: $titulo", Toast.LENGTH_SHORT).show()

                // Aquí luego abriremos la Activity correspondiente
            }
            .setNegativeButton("Ez") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}