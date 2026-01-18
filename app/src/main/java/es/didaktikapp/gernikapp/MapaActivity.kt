package es.didaktikapp.gernikapp

import android.content.Intent
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
import es.didaktikapp.gernikapp.utils.Constants
import es.didaktikapp.gernikapp.arbol.MainActivity as ArbolMainActivity
import es.didaktikapp.gernikapp.bunkers.MainActivity as BunkersMainActivity
import es.didaktikapp.gernikapp.fronton.MainActivity as FrontonMainActivity
import es.didaktikapp.gernikapp.picasso.MainActivity as PicassoMainActivity
import es.didaktikapp.gernikapp.plazagernika.MainActivity as PlazaMainActivity

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

        val gernika = LatLng(Constants.Map.GERNIKA_CENTER_LAT, Constants.Map.GERNIKA_CENTER_LNG)
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gernika, Constants.Map.DEFAULT_ZOOM_LEVEL))

        nMap.uiSettings.isZoomControlsEnabled = true

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(Constants.Map.ARBOLA_LAT, Constants.Map.ARBOLA_LNG))
                .title(getString(R.string.map_marker_title_arbola))
        )?.tag = "arbola"

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(Constants.Map.BUNKER_LAT, Constants.Map.BUNKER_LNG))
                .title(getString(R.string.map_marker_title_bunker))
        )?.tag = "bunkers"

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(Constants.Map.GUERNICA_LAT, Constants.Map.GUERNICA_LNG))
                .title(getString(R.string.map_marker_title_guernica))
        )?.tag = "picasso"

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(Constants.Map.PLAZA_LAT, Constants.Map.PLAZA_LNG))
                .title(getString(R.string.map_marker_title_plaza))
        )?.tag = "plaza"

        nMap.addMarker(
            MarkerOptions()
                .position(LatLng(Constants.Map.FRONTOI_LAT, Constants.Map.FRONTOI_LNG))
                .title(getString(R.string.map_marker_title_frontoi))
        )?.tag = "fronton"

        nMap.setOnMarkerClickListener { marker ->
            marker.title?.let { titulo ->
                mostrarDialogo(
                    titulo = titulo,
                    mensaje = getString(R.string.map_dialog_message, titulo),
                    tag = marker.tag as? String
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
                Constants.Permissions.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun mostrarDialogo(titulo: String, mensaje: String, tag: String?) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton(R.string.map_dialog_button_enter) { _, _ ->
                val intent = when (tag) {
                    "arbola" -> Intent(this, ArbolMainActivity::class.java)
                    "bunkers" -> Intent(this, BunkersMainActivity::class.java)
                    "picasso" -> Intent(this, PicassoMainActivity::class.java)
                    "plaza" -> Intent(this, PlazaMainActivity::class.java)
                    "fronton" -> Intent(this, FrontonMainActivity::class.java)
                    else -> null
                }
                intent?.let { startActivity(it) }
            }
            .setNegativeButton(R.string.map_dialog_button_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}