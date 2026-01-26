package es.didaktikapp.gernikapp

import es.didaktikapp.gernikapp.databinding.ActivityAboutBinding

/**
 * Activity que muestra información "Acerca de" de la aplicación GernikApp.
 *
 * @author Erlantz
 * @version 1.0
 */
class AboutActivity : BaseMenuActivity() {

    /** Binding generado para el layout activity_about.xml */
    private lateinit var binding: ActivityAboutBinding

    /**
     * Retorna el ID del layout a cargar en BaseMenuActivity.
     * @return R.layout.activity_about
     */
    override fun getContentLayoutId() = R.layout.activity_about

    /**
     * Se ejecuta después de inflar el layout del "Acerca de".
     * Inicializa binding y configura listeners del botón cerrar.
     */
    override fun onContentInflated() {
        binding = ActivityAboutBinding.bind(contentContainer.getChildAt(0))
        setupListeners()
    }

    /**
     * Configura el listener del botón cerrar ventana "Acerca de".
     * Cierra la actividad actual y regresa al menú anterior.
     */
    private fun setupListeners() {
        binding.btnCloseAbout.setOnClickListener {
            finish()
        }
    }
}