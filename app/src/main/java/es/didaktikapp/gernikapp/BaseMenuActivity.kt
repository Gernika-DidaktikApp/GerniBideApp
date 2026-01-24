package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Activity base que incluye el menú FAB flotante.
 * Las activities que necesiten el menú deben heredar de esta clase
 * e implementar getContentLayoutId() para especificar su layout.
 */
abstract class BaseMenuActivity : AppCompatActivity() {

    private var isMenuOpen = false

    // Views del menú
    private lateinit var fabOverlay: View
    private lateinit var fabMain: ImageButton
    private lateinit var fabHome: ImageButton
    private lateinit var fabSettings: ImageButton
    private lateinit var fabProfile: ImageButton

    // Configuración del abanico
    private val fanRadius = 80f // dp
    private val angleHome = 180f
    private val angleSettings = 225f
    private val angleProfile = 270f

    /**
     * Las activities hijas pueden devolver el ID del layout que quieren usar.
     * Si devuelve 0, la activity hija debe inflar su propio contenido en onContentInflated().
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
        onContentInflated()
    }

    /**
     * Llamado después de que el contenido de la activity hija ha sido inflado.
     * Las activities hijas pueden sobreescribir este método para inicializar sus views.
     */
    protected open fun onContentInflated() {}

    private fun setupMenuListeners() {
        fabMain.setOnClickListener { toggleMenu() }

        fabOverlay.setOnClickListener { closeMenu() }

        fabHome.setOnClickListener {
            closeMenu()
            startActivity(Intent(this, MapaActivity::class.java))
            finish()
        }

        fabSettings.setOnClickListener {
            closeMenu()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        fabProfile.setOnClickListener {
            closeMenu()
            Toast.makeText(this, getString(R.string.fab_profile), Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleMenu() {
        if (isMenuOpen) closeMenu() else openMenu()
    }

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isMenuOpen) {
            closeMenu()
        } else {
            super.onBackPressed()
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun getTranslationForAngle(angleDegrees: Float, radius: Float): Pair<Float, Float> {
        val angleRadians = Math.toRadians(angleDegrees.toDouble())
        val x = (radius * kotlin.math.cos(angleRadians)).toFloat()
        val y = -(radius * kotlin.math.sin(angleRadians)).toFloat()
        return Pair(x, y)
    }
}