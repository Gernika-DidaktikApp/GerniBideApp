package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Activity base que incluye el menú FAB flotante en abanico.
 * Las activities que necesiten el menú deben heredar de esta clase.
 *
 * Funcionalidades:
 * - Menú FAB flotante con animación de abanico
 * - Navegación a Home (MapaActivity), Settings y Profile
 * - Overlay oscuro cuando el menú está abierto
 * - Animaciones suaves con interpoladores
 *
 * Uso:
 * 1. Heredar de BaseMenuActivity
 * 2. Implementar getContentLayoutId() o inflar contenido en onContentInflated()
 * 3. Acceder al contentContainer para añadir vistas
 *
 * @property isMenuOpen Estado del menú (abierto/cerrado)
 * @property contentContainer Contenedor donde se infla el contenido de la activity hija
 * @property fanRadius Radio del abanico en dp (80dp)
 *
 * @author Wara Pacheco
 * @version 1.0
 */
abstract class BaseMenuActivity : AppCompatActivity() {

    /** Indica si el menú flotante está actualmente abierto. */
    private var isMenuOpen = false

    /** Evita múltiples navegaciones simultáneas mientras se cambia de pantalla. */
    private var isNavigating = false

    /** Capa oscura que aparece detrás del menú cuando está abierto. */
    private lateinit var fabOverlay: View

    /** Botón principal del menú (abre/cierra el abanico). */
    private lateinit var fabMain: ImageButton

    /** Botón del menú que navega a la pantalla Home (MapaActivity). */
    private lateinit var fabHome: ImageButton

    /** Botón del menú que navega a la pantalla de Ajustes. */
    private lateinit var fabSettings: ImageButton

    /** Botón del menú que navega al Perfil del usuario. */
    private lateinit var fabProfile: ImageButton

    /** Radio del abanico en dp: distancia a la que se desplazan los mini FABs. */
    private val fanRadius = 80f

    /**  Ángulo del botón Home dentro del abanico (180° = izquierda). */
    private val angleHome = 180f

    /** Ángulo del botón Settings dentro del abanico (225° = abajo-izquierda). */
    private val angleSettings = 225f

    /** Ángulo del botón Profile dentro del abanico (270° = abajo). */
    private val angleProfile = 270f

    /**
     * Las activities hijas pueden devolver el ID del layout que quieren usar.
     * Si devuelve 0, la activity hija debe inflar su propio contenido en onContentInflated().
     *
     * @return ID del layout o 0 para inflado manual
     */
    protected open fun getContentLayoutId(): Int = 0

    /**
     * Contenedor donde las activities hijas pueden acceder a su contenido.
     */
    protected lateinit var contentContainer: FrameLayout
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_menu)

        // Obtener referencias del layout base
        val rootLayout: CoordinatorLayout = findViewById(R.id.baseRootLayout)
        contentContainer = findViewById(R.id.contentContainer)
        fabOverlay = findViewById(R.id.fabOverlay)
        fabMain = findViewById(R.id.fabMain)
        fabHome = findViewById(R.id.fabHome)
        fabSettings = findViewById(R.id.fabSettings)
        fabProfile = findViewById(R.id.fabProfile)

        // Inflar el layout de la activity hija en el contenedor (si se especificó)
        val layoutId = getContentLayoutId()
        if (layoutId != 0) {
            LayoutInflater.from(this).inflate(layoutId, contentContainer, true)
        }

        // Inicializar mini FABs
        listOf(fabHome, fabSettings, fabProfile).forEach { fab ->
            fab.scaleX = 0f
            fab.scaleY = 0f
            fab.alpha = 0f
        }

        setupMenuListeners()
        setupBackHandler()
        onContentInflated()
    }

    /**
     * Llamado después de que el contenido de la activity hija ha sido inflado.
     * Las activities hijas pueden sobreescribir este metodo para inicializar sus views.
     */
    protected open fun onContentInflated() {}

    /**
     * Configura los listeners de los botones del menú FAB.
     * - Main FAB: Toggle menu
     * - Overlay: Cierra el menú
     * - Home: Navega a MapaActivity
     * - Settings: Navega a SettingsActivity
     * - Profile: Navega a ProfileActivity
     */
    private fun setupMenuListeners() {
        fabMain.setOnClickListener { toggleMenu() }

        fabOverlay.setOnClickListener { closeMenu() }

        fabHome.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true
            closeMenu()
            startActivity(Intent(this, MapaActivity::class.java))
            finish()
        }

        fabSettings.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            if (this is SettingsActivity) { closeMenu(); return@setOnClickListener }
            isNavigating = true
            closeMenu()
            startActivity(Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        fabProfile.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            if (this is ProfileActivity) { closeMenu(); return@setOnClickListener }
            isNavigating = true
            closeMenu()
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    /**
     * Alterna entre abrir y cerrar el menú.
     */
    private fun toggleMenu() {
        if (isMenuOpen) closeMenu() else openMenu()
    }

    /**
     * Abre el menú con animación de abanico.
     * Muestra overlay, rota el FAB principal y anima los mini FABs.
     */
    private fun openMenu() {
        isMenuOpen = true

        fabOverlay.visibility = View.VISIBLE
        fabOverlay.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        fabMain.animate()
            .rotation(45f)
            .setDuration(250)
            .start()
        fabMain.setImageResource(R.drawable.ic_close)

        val radiusPx = dpToPx(fanRadius)
        val interpolator = OvershootInterpolator(1.5f)

        animateFabOpen(fabHome, angleHome, radiusPx, interpolator, 0)
        animateFabOpen(fabSettings, angleSettings, radiusPx, interpolator, 50)
        animateFabOpen(fabProfile, angleProfile, radiusPx, interpolator, 100)
    }

    /**
     * Anima la apertura de un mini FAB individual.
     *
     * @param fab Botón a animar
     * @param angle Ángulo en grados para posicionar el FAB
     * @param radius Radio del abanico en píxeles
     * @param interpolator Interpolador para la animación
     * @param delay Delay en milisegundos antes de iniciar
     */
    private fun animateFabOpen(
        fab: ImageButton,
        angle: Float,
        radius: Float,
        interpolator: OvershootInterpolator,
        delay: Long
    ) {
        val (x, y) = getTranslationForAngle(angle, radius)
        fab.visibility = View.VISIBLE
        fab.animate()
            .translationX(x)
            .translationY(y)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setInterpolator(interpolator)
            .setDuration(300)
            .setStartDelay(delay)
            .start()
    }

    /**
     * Cierra el menú con animación.
     * Oculta overlay, restaura rotación del FAB principal y anima los mini FABs.
     */
    private fun closeMenu() {
        isMenuOpen = false

        fabOverlay.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { fabOverlay.visibility = View.GONE }
            .start()

        fabMain.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
        fabMain.setImageResource(R.drawable.ic_menu)

        val interpolator = AccelerateInterpolator()
        animateFabClose(fabHome, interpolator, 0)
        animateFabClose(fabSettings, interpolator, 30)
        animateFabClose(fabProfile, interpolator, 60)
    }

    /**
     * Anima el cierre de un mini FAB individual.
     *
     * @param fab Botón a animar
     * @param interpolator Interpolador para la animación
     * @param delay Delay en milisegundos antes de iniciar
     */
    private fun animateFabClose(
        fab: ImageButton,
        interpolator: AccelerateInterpolator,
        delay: Long
    ) {
        fab.animate()
            .translationX(0f)
            .translationY(0f)
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setInterpolator(interpolator)
            .setDuration(200)
            .setStartDelay(delay)
            .withEndAction { fab.visibility = View.INVISIBLE }
            .start()
    }

    /**
     * Se ejecuta cada vez que la activity vuelve a primer plano.
     */
    override fun onResume() {
        super.onResume()
        isNavigating = false
    }

    /**
     * Maneja el botón atrás:
     * - Si el menú está abierto, lo cierra
     * - Si no, deja que Android gestione el back normal
     */
    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isMenuOpen) {
                    closeMenu()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    /**
     * Convierte density-independent pixels (dp) a píxeles de pantalla.
     *
     * @param dp Valor en dp
     * @return Valor equivalente en píxeles
     */
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    /**
     * Calcula las coordenadas X e Y para un ángulo y radio dados.
     *
     * @param angleDegrees Ángulo en grados (0° = derecha, 90° = arriba, 180° = izquierda, 270° = abajo)
     * @param radius Radio del círculo en píxeles
     * @return Par (x, y) con las coordenadas de traslación
     */
    private fun getTranslationForAngle(angleDegrees: Float, radius: Float): Pair<Float, Float> {
        val angleRadians = Math.toRadians(angleDegrees.toDouble())
        val x = (radius * kotlin.math.cos(angleRadians)).toFloat()
        val y = -(radius * kotlin.math.sin(angleRadians)).toFloat()
        return Pair(x, y)
    }
}