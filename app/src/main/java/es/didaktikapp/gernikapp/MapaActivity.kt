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
import es.didaktikapp.gernikapp.databinding.ActivityMapaBinding

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var nMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                .title(getString(R.string.map_marker_title_arbola))
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.312137, -2.676608))
                .title(getString(R.string.map_marker_title_bunker))
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.315513, -2.680047))
                .title(getString(R.string.map_marker_title_guernica))
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.316139, -2.676672))
                .title(getString(R.string.map_marker_title_plaza))
        )

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(43.317399, -2.678783))
                .title(getString(R.string.map_marker_title_frontoi))
        )

        nMap.setOnMarkerClickListener { marker ->
            marker.title?.let { titulo ->
                mostrarDialogo(
                    titulo = titulo,
                    mensaje = getString(R.string.map_dialog_message, titulo)
                )
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
            .setPositiveButton(R.string.map_dialog_button_enter) { _, _ ->
                Toast.makeText(
                    this,
                    getString(R.string.map_entered_location, titulo),
                    Toast.LENGTH_SHORT
                ).show()

                // Aquí luego abriremos la Activity correspondiente
            }
            .setNegativeButton(R.string.map_dialog_button_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}