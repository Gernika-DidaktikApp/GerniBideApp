package es.didaktikapp.gernikapp

import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.UserRepository
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pantalla de perfil del usuario con estilo gamificado.
 * Muestra estadísticas, logros y opciones de cuenta.
 * @author Wara Pacheco
 * @version 1.0
 */
class ProfileActivity : BaseMenuActivity() {

    /** Constantes de la clase. */
    companion object {
        /** Tag para logging de depuración. */
        private const val TAG = "ProfileActivity"
    }

    /** Gestor de tokens JWT y datos de sesión del usuario. */
    private lateinit var tokenManager: TokenManager

    /** Repositorio para operaciones de usuario (perfil, estadísticas). */
    private lateinit var userRepository: UserRepository

    // Views
    private lateinit var tvAvatarInitials: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvTopScore: TextView
    private lateinit var tvActivitiesCompleted: TextView
    private lateinit var tvStreak: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button

    // Logros
    private lateinit var achievementArbol: LinearLayout
    private lateinit var achievementBunkers: LinearLayout
    private lateinit var achievementPicasso: LinearLayout
    private lateinit var achievementPlaza: LinearLayout
    private lateinit var achievementFronton: LinearLayout

    /**
     * Devuelve el ID del layout para esta actividad.
     *
     * @return ID del recurso de layout.
     */
    override fun getContentLayoutId() = R.layout.activity_profile

    /**
     * Inicializa la actividad después de inflar el layout.
     * Configura los managers, inicializa las vistas, listeners y carga el perfil del usuario.
     */
    override fun onContentInflated() {
        tokenManager = TokenManager(this)
        userRepository = UserRepository(this)

        LogManager.write(this@ProfileActivity, "ProfileActivity iniciada")

        // Log del estado de la sesión
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 ProfileActivity iniciada")
            tokenManager.logSessionState(TAG)
        }

        initViews()
        setupListeners()
        loadUserProfile()
    }

    /**
     * Inicializa las referencias a las vistas del layout.
     * Incluye textos de información del usuario, estadísticas y logros de módulos.
     */
    private fun initViews() {
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials)
        tvFullName = findViewById(R.id.tvFullName)
        tvUsername = findViewById(R.id.tvUsername)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvTopScore = findViewById(R.id.tvTopScore)
        tvActivitiesCompleted = findViewById(R.id.tvActivitiesCompleted)
        tvStreak = findViewById(R.id.tvStreak)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)

        // Logros
        achievementArbol = findViewById(R.id.achievementArbol)
        achievementBunkers = findViewById(R.id.achievementBunkers)
        achievementPicasso = findViewById(R.id.achievementPicasso)
        achievementPlaza = findViewById(R.id.achievementPlaza)
        achievementFronton = findViewById(R.id.achievementFronton)
    }

    /**
     * Configura los listeners de los botones.
     * Editar perfil y cerrar sesión.
     */
    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, UserEditActivity::class.java))
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    /**
     * Carga el perfil completo del usuario desde la API.
     * Primero muestra datos locales (username) y luego obtiene información completa de la API.
     * Tras cargar el perfil, invoca [loadUserStats] para obtener estadísticas y logros.
     */
    private fun loadUserProfile() {
        Log.d(TAG, "📱 Cargando perfil de usuario...")

        LogManager.write(this@ProfileActivity, "Cargando perfil de usuario")

        // Mostrar datos locales primero (username guardado)
        tokenManager.getUsername()?.let { username ->
            tvUsername.text = "@$username"
            updateAvatarInitials(username, "")
        }

        // Cargar datos completos desde la API
        lifecycleScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    result.data.let { user ->
                        LogManager.write(this@ProfileActivity, "Perfil cargado: ${user.username} (ID: ${user.id})")
                        Log.d(TAG, "✅ Perfil cargado: ${user.username} (ID: ${user.id})")
                        updateUI(user.nombre, user.apellido, user.username, user.topScore, user.creation)
                        // Cargar estadísticas del usuario
                        loadUserStats(user.id)
                    }
                }
                is Resource.Error -> {
                    LogManager.write(this@ProfileActivity, "Error cargando perfil: ${result.message}")
                    Log.e(TAG, "❌ Error cargando perfil: ${result.message}")
                    // Si falla la API, mostrar datos locales
                    tokenManager.getUsername()?.let { username ->
                        tvFullName.text = username
                    }
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    /**
     * Carga las estadísticas del usuario desde la API usando el endpoint de perfil-progreso.
     * Incluye actividades completadas, racha de días y módulos completados.
     *
     * @param userId ID del usuario para obtener estadísticas.
     */
    private fun loadUserStats(userId: String) {
        Log.d(TAG, "📊 Cargando estadísticas para usuario: $userId")

        LogManager.write(this@ProfileActivity, "Cargando estadísticas para usuario: $userId")

        lifecycleScope.launch {
            when (val result = userRepository.getPerfilProgreso()) {
                is Resource.Success -> {
                    result.data.let { perfil ->
                        Log.d(TAG, "✅ Estadísticas cargadas: ${perfil.estadisticas.actividadesCompletadas} actividades, ${perfil.estadisticas.rachaDias} días racha")
                        LogManager.write(this@ProfileActivity, "Estadísticas cargadas: ${perfil.estadisticas.actividadesCompletadas} actividades, ${perfil.estadisticas.rachaDias} días racha")
                        updateStats(perfil.estadisticas.actividadesCompletadas, perfil.estadisticas.rachaDias)

                        // Actualizar topScore desde el perfil
                        tvTopScore.text = perfil.usuario.topScore.toString()

                        // Desbloquear módulos completados (puntos al 100%)
                        val modulosCompletados = perfil.puntos
                            .filter { it.estado == "completado" }
                            .map { it.nombrePunto }
                        unlockCompletedModules(modulosCompletados)
                    }
                }
                is Resource.Error -> {
                    LogManager.write(this@ProfileActivity, "Error cargando estadísticas: ${result.message}")
                    Log.e(TAG, "❌ Error cargando estadísticas: ${result.message}")
                    // Si falla la API, mostrar valores por defecto
                    updateStats(0, 0)
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    /**
     * Actualiza las vistas con las estadísticas del usuario.
     *
     * @param activitiesCompleted Número total de actividades completadas.
     * @param streak Número de días de racha consecutiva.
     */
    private fun updateStats(activitiesCompleted: Int, streak: Int) {
        tvActivitiesCompleted.text = activitiesCompleted.toString()
        tvStreak.text = streak.toString()
    }

    /**
     * Desbloquea visualmente los logros de los módulos completados.
     * Mapea los nombres de módulos devueltos por la API a IDs de achievements locales.
     *
     * @param modulosCompletados Lista de nombres de módulos completados desde la API.
     */
    private fun unlockCompletedModules(modulosCompletados: List<String>) {
        // Mapeo de nombres de módulos de la API a IDs de achievements
        val moduleMap = mapOf(
            "Árbol del Gernika" to "arbol",
            "Árbol de Gernika" to "arbol",
            "Arbol" to "arbol",
            "arbol" to "arbol",
            "Museo de la Paz" to "bunkers",
            "Bunkers" to "bunkers",
            "bunkers" to "bunkers",
            "Refugios" to "bunkers",
            "Picasso" to "picasso",
            "picasso" to "picasso",
            "Guernica" to "picasso",
            "Plaza" to "plaza",
            "Plaza Gernika" to "plaza",
            "mercado" to "plaza",
            "Frontón" to "fronton",
            "Fronton" to "fronton",
            "fronton" to "fronton",
            "Pelota Vasca" to "fronton"
        )

        modulosCompletados.forEach { moduloNombre ->
            val achievementId = moduleMap[moduloNombre] ?: moduloNombre.lowercase()
            unlockAchievement(achievementId)
        }
    }

    /**
     * Actualiza todas las vistas con los datos del perfil del usuario.
     *
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param username Nombre de usuario único.
     * @param topScore Puntuación máxima alcanzada.
     * @param creationDate Fecha de creación de la cuenta en formato ISO.
     */
    private fun updateUI(
        nombre: String,
        apellido: String,
        username: String,
        topScore: Int,
        creationDate: String?
    ) {
        tvFullName.text = "$nombre $apellido"
        tvUsername.text = "@$username"
        tvTopScore.text = topScore.toString()

        updateAvatarInitials(nombre, apellido)
        updateMemberSince(creationDate)
    }

    /**
     * Actualiza las iniciales del avatar del usuario.
     * Usa la primera letra del nombre y del apellido. Si no hay apellido, usa la segunda letra del nombre.
     *
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario (puede estar vacío).
     */
    private fun updateAvatarInitials(nombre: String, apellido: String) {
        val initial1 = nombre.firstOrNull()?.uppercaseChar() ?: '?'
        val initial2 = apellido.firstOrNull()?.uppercaseChar() ?: nombre.getOrNull(1)?.uppercaseChar() ?: ""
        tvAvatarInitials.text = "$initial1$initial2"
    }

    /**
     * Actualiza la fecha de registro del usuario ("Miembro desde XXXX").
     * Parsea el formato ISO de la fecha y extrae el año.
     *
     * @param creationDate Fecha de creación de cuenta en formato ISO (yyyy-MM-dd'T'HH:mm:ss).
     */
    private fun updateMemberSince(creationDate: String?) {
        if (creationDate.isNullOrEmpty()) {
            tvMemberSince.text = getString(R.string.profile_member_since, "2024")
            return
        }

        try {
            // Parse ISO date format (2024-01-15T10:30:00)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(creationDate)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date!!)
            tvMemberSince.text = getString(R.string.profile_member_since, year)
        } catch (e: Exception) {
            // Fallback: extraer año directamente del string
            val year = creationDate.take(4)
            tvMemberSince.text = getString(R.string.profile_member_since, year)
        }
    }

    /**
     * Activa visualmente un logro mostrándolo con animación de fade-in.
     * Se llama cuando el usuario completa un módulo temático.
     *
     * @param achievementId ID del logro a desbloquear ("arbol", "bunkers", "picasso", "plaza", "fronton").
     */
    fun unlockAchievement(achievementId: String) {
        val view = when (achievementId) {
            "arbol" -> achievementArbol
            "bunkers" -> achievementBunkers
            "picasso" -> achievementPicasso
            "plaza" -> achievementPlaza
            "fronton" -> achievementFronton
            else -> null
        }

        view?.animate()
            ?.alpha(1f)
            ?.setDuration(500)
            ?.start()
    }

    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     * Si el usuario confirma, llama a [performLogout].
     */
    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle(R.string.profile_logout_title)
            .setMessage(R.string.profile_logout_message)
            .setPositiveButton(R.string.profile_logout_confirm) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.profile_logout_cancel, null)
            .show()
    }

    /**
     * Cierra la sesión del usuario.
     * Limpia el token y datos de sesión, muestra un Toast y redirige al LoginActivity.
     */
    private fun performLogout() {
        tokenManager.clearSession()
        Toast.makeText(this, getString(R.string.profile_logout_success), Toast.LENGTH_SHORT).show()

        LogManager.write(this@ProfileActivity, "Usuario cerró sesión")

        // Navegar al login y limpiar el back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Recarga el perfil del usuario al volver a la actividad.
     * Útil para reflejar cambios realizados en [UserEditActivity].
     */
    override fun onResume() {
        super.onResume()
        // Recargar datos al volver (por si se editó el perfil)
        loadUserProfile()
    }
}