package es.didaktikapp.gernikapp

import android.content.Intent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.android.gms.maps.model.MarkerOptions
import es.didaktikapp.gernikapp.databinding.ActivityMapaBinding
import es.didaktikapp.gernikapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.view.View
import es.didaktikapp.gernikapp.arbol.MainActivity as ArbolMainActivity
import es.didaktikapp.gernikapp.bunkers.MainActivity as BunkersMainActivity
import es.didaktikapp.gernikapp.fronton.MainActivity as FrontonMainActivity
import es.didaktikapp.gernikapp.picasso.MainActivity as PicassoMainActivity
import es.didaktikapp.gernikapp.plaza.MainActivity as PlazaMainActivity

/**
 *
 *
 * @author
 * @version
 */
class MapaActivity : BaseMenuActivity(), OnMapReadyCallback {

    /**  */
    private lateinit var binding: ActivityMapaBinding

    /**  */
    private lateinit var nMap: GoogleMap

    /**  */
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    /**  */
    private lateinit var ivBottomSheetIcon: ImageView

    /**  */
    private lateinit var tvBottomSheetTitle: TextView

    /**  */
    private lateinit var tvBottomSheetDescription: TextView

    /**  */
    private lateinit var btnGoToActivity: Button

    /**  */
    private var selectedLocation: String? = null

    /**
     *
     *
     */
    override fun onContentInflated() {
        binding = ActivityMapaBinding.inflate(layoutInflater, contentContainer, true)

        LogManager.write(this@MapaActivity, "MapaActivity iniciada")

        // Inicializar BottomSheet
        val bottomSheet = binding.root.findViewById<androidx.core.widget.NestedScrollView>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        ivBottomSheetIcon = binding.root.findViewById(R.id.ivBottomSheetIcon)
        tvBottomSheetTitle = binding.root.findViewById(R.id.tvBottomSheetTitle)
        tvBottomSheetDescription = binding.root.findViewById(R.id.tvBottomSheetDescription)
        btnGoToActivity = binding.root.findViewById(R.id.btnGoToActivity)

        btnGoToActivity.setOnClickListener {
            selectedTag?.let { tag ->
                LogManager.write(this@MapaActivity, "Navegando desde mapa a: $tag")
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
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     *
     *
     * @param googleMap
     */
    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap

        LogManager.write(this@MapaActivity, "GoogleMap listo")

        val gernika = LatLng(Constants.Map.GERNIKA_CENTER_LAT, Constants.Map.GERNIKA_CENTER_LNG)
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gernika, Constants.Map.DEFAULT_ZOOM_LEVEL))

        nMap.uiSettings.isZoomControlsEnabled = true

        // Configurar padding del mapa para que los controles no queden tapados por el bottom sheet
        val bottomPadding = (120 * resources.displayMetrics.density).toInt()
        nMap.setPadding(0, 0, 0, bottomPadding)

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
                actualizarBottomSheet(titulo, marker.tag as? String)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            true
        }

        nMap.setOnMapClickListener {
            resetBottomSheet()
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

    private var selectedTag: String? = null

    /**
     *
     *
     * @param titulo
     * @param tag
     */
    private fun actualizarBottomSheet(titulo: String, tag: String?) {
        tvBottomSheetTitle.text = titulo
        selectedLocation = titulo
        selectedTag = tag

        val descripcion: String
        val iconResId: Int

        when (titulo) {
            getString(R.string.map_marker_title_arbola) -> {
                descripcion = getString(R.string.map_description_arbola)
                iconResId = R.drawable.ic_tree
            }
            getString(R.string.map_marker_title_bunker) -> {
                descripcion = getString(R.string.map_description_bunker)
                iconResId = R.drawable.ic_bunker
            }
            getString(R.string.map_marker_title_guernica) -> {
                descripcion = getString(R.string.map_description_guernica)
                iconResId = R.drawable.ic_art
            }
            getString(R.string.map_marker_title_plaza) -> {
                descripcion = getString(R.string.map_description_plaza)
                iconResId = R.drawable.ic_market
            }
            getString(R.string.map_marker_title_frontoi) -> {
                descripcion = getString(R.string.map_description_frontoi)
                iconResId = R.drawable.ic_sports
            }
            else -> {
                descripcion = getString(R.string.map_bottom_sheet_default_description)
                iconResId = R.drawable.ic_map_pin
            }
        }

        tvBottomSheetDescription.text = descripcion
        ivBottomSheetIcon.setImageResource(iconResId)
        btnGoToActivity.visibility = View.VISIBLE
    }

    /**
     *
     *
     */
    private fun resetBottomSheet() {
        tvBottomSheetTitle.text = getString(R.string.map_bottom_sheet_default_title)
        tvBottomSheetDescription.text = getString(R.string.map_bottom_sheet_default_description)
        ivBottomSheetIcon.setImageResource(R.drawable.ic_map_pin)
        btnGoToActivity.visibility = View.GONE
        selectedLocation = null
        selectedTag = null
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     *
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.Permissions.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogManager.write(this@MapaActivity, "Permiso de ubicación concedido")
                // Permiso concedido, habilitar ubicación en el mapa
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    nMap.isMyLocationEnabled = true
                }
            }
            // Si el usuario rechaza el permiso, el mapa funciona sin mostrar la ubicación actual
        }
    }

}