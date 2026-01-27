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
 */
class ProfileActivity : BaseMenuActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var tokenManager: TokenManager
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

    override fun getContentLayoutId() = R.layout.activity_profile

    override fun onContentInflated() {
        tokenManager = TokenManager(this)
        userRepository = UserRepository(this)

        // Log del estado de la sesión
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 ProfileActivity iniciada")
            tokenManager.logSessionState(TAG)
        }

        initViews()
        setupListeners()
        loadUserProfile()
    }

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

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, UserEditActivity::class.java))
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        Log.d(TAG, "📱 Cargando perfil de usuario...")

        // Mostrar datos locales primero (username guardado)
        tokenManager.getUsername()?.let { username ->
            tvUsername.text = "@$username"
            updateAvatarInitials(username, "")
        }

        // Cargar datos completos desde la API
        lifecycleScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        Log.d(TAG, "✅ Perfil cargado: ${user.username} (ID: ${user.id})")
                        updateUI(user.nombre, user.apellido, user.username, user.topScore, user.creation)
                        // Cargar estadísticas del usuario
                        loadUserStats(user.id)
                    }
                }
                is Resource.Error -> {
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

    private fun loadUserStats(userId: String) {
        Log.d(TAG, "📊 Cargando estadísticas para usuario: $userId")

        lifecycleScope.launch {
            when (val result = userRepository.getUserStats(userId)) {
                is Resource.Success -> {
                    result.data?.let { stats ->
                        Log.d(TAG, "✅ Estadísticas cargadas: ${stats.actividadesCompletadas} actividades, ${stats.rachaDias} días racha")
                        updateStats(stats.actividadesCompletadas, stats.rachaDias)
                        unlockCompletedModules(stats.modulosCompletados)
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Error cargando estadísticas: ${result.message}")
                    // Si falla la API, mostrar valores por defecto
                    updateStats(0, 0)
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    private fun updateStats(activitiesCompleted: Int, streak: Int) {
        tvActivitiesCompleted.text = activitiesCompleted.toString()
        tvStreak.text = streak.toString()
    }

    private fun unlockCompletedModules(modulosCompletados: List<String>) {
        // Mapeo de nombres de módulos de la API a IDs de achievements
        val moduleMap = mapOf(
            "Árbol del Gernika" to "arbol",
            "Árbol de Gernika" to "arbol",
            "Arbol" to "arbol",
            "Museo de la Paz" to "bunkers",
            "Bunkers" to "bunkers",
            "Refugios" to "bunkers",
            "Picasso" to "picasso",
            "Guernica" to "picasso",
            "Plaza" to "plaza",
            "Plaza Gernika" to "plaza",
            "Frontón" to "fronton",
            "Fronton" to "fronton",
            "Pelota Vasca" to "fronton"
        )

        modulosCompletados.forEach { moduloNombre ->
            val achievementId = moduleMap[moduloNombre] ?: moduloNombre.lowercase()
            unlockAchievement(achievementId)
        }
    }

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

    private fun updateAvatarInitials(nombre: String, apellido: String) {
        val initial1 = nombre.firstOrNull()?.uppercaseChar() ?: '?'
        val initial2 = apellido.firstOrNull()?.uppercaseChar() ?: nombre.getOrNull(1)?.uppercaseChar() ?: ""
        tvAvatarInitials.text = "$initial1$initial2"
    }

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
     * Activa visualmente un logro (cuando el usuario completa una zona).
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

    private fun performLogout() {
        tokenManager.clearSession()
        Toast.makeText(this, getString(R.string.profile_logout_success), Toast.LENGTH_SHORT).show()

        // Navegar al login y limpiar el back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos al volver (por si se editó el perfil)
        loadUserProfile()
    }
}