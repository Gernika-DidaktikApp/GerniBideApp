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
 * Pantalla principal del mapa interactivo de Gernika.
 * Muestra los puntos de interés del recorrido mediante marcadores de Google Maps
 * y permite al usuario seleccionar una zona para acceder a sus actividades.
 *
 * @author Wara Pacheco, Arantxa Main, Erlantz García
 * @version 1.0
 * @see OnMapReadyCallback
 */
class MapaActivity : BaseMenuActivity(), OnMapReadyCallback {

    /** Binding de la vista del mapa. */
    private lateinit var binding: ActivityMapaBinding

    /** Instancia del mapa de Google. */
    private lateinit var nMap: GoogleMap

    /** Comportamiento del BottomSheet con información de la zona seleccionada. */
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    /** Icono de la zona mostrado en el BottomSheet. */
    private lateinit var ivBottomSheetIcon: ImageView

    /** Título de la zona mostrado en el BottomSheet. */
    private lateinit var tvBottomSheetTitle: TextView

    /** Descripción de la zona mostrada en el BottomSheet. */
    private lateinit var tvBottomSheetDescription: TextView

    /** Botón para navegar a la actividad de la zona seleccionada. */
    private lateinit var btnGoToActivity: Button

    /** Nombre de la ubicación seleccionada actualmente en el mapa. */
    private var selectedLocation: String? = null

    /** Inicializa las vistas, el mapa y el BottomSheet. */
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
     * Callback cuando el mapa de Google está listo.
     * Configura la cámara, los marcadores de cada zona y los listeners de interacción.
     *
     * @param googleMap Instancia del mapa de Google ya inicializada.
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
     * Actualiza el contenido del BottomSheet con la información de la zona seleccionada.
     *
     * @param titulo Nombre de la zona seleccionada.
     * @param tag Identificador interno del marcador.
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
     * Restablece el BottomSheet a su estado inicial sin ninguna zona seleccionada.
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
     * Gestiona la respuesta del usuario a la solicitud de permisos de ubicación.
     *
     * @param requestCode Código de la solicitud de permiso.
     * @param permissions Permisos solicitados.
     * @param grantResults Resultados de la solicitud.
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