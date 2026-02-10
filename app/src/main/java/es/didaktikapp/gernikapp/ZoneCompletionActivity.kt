package es.didaktikapp.gernikapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.models.UpdateUserRequest
import es.didaktikapp.gernikapp.data.repository.UserRepository
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.SpriteAnimationView
import es.didaktikapp.gernikapp.utils.ZoneConfig
import es.didaktikapp.gernikapp.utils.ZoneInfo
import kotlinx.coroutines.launch

/**
 * Pantalla de resumen al completar una zona del recorrido.
 * Muestra las puntuaciones obtenidas en cada actividad de la zona,
 * la puntuación total y un mensaje según el rendimiento del usuario.
 *
 * @author Arantxa Main
 * @version 1.0
 * @see ZoneConfig
 * @see ZoneInfo
 */
class ZoneCompletionActivity : BaseMenuActivity() {

    /** Layout asociado a esta pantalla. */
    override fun getContentLayoutId() = R.layout.activity_zone_completion

    /**
     * Inicializa la pantalla una vez inflado el layout.
     */
    override fun onContentInflated() {
        val prefsName = intent.getStringExtra(EXTRA_ZONE_PREFS_NAME) ?: run {
            finish()
            return
        }

        val zone = ZoneConfig.findByPrefsName(prefsName) ?: run {
            finish()
            return
        }

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvZoneName = findViewById<TextView>(R.id.tvZoneName)
        val scoresContainer = findViewById<LinearLayout>(R.id.scoresContainer)
        val tvTotalScore = findViewById<TextView>(R.id.tvTotalScore)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val spriteView = findViewById<SpriteAnimationView>(R.id.spriteAnimationView)

        tvZoneName.text = zone.zoneName

        var totalScore = 0f
        val maxScore = zone.activities.size * 100f

        for (activity in zone.activities) {
            val score = prefs.getFloat(activity.scoreKey, 0f)
            totalScore += score

            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_score_row, scoresContainer, false)
            row.findViewById<TextView>(R.id.tvActivityName).text = activity.displayName
            row.findViewById<TextView>(R.id.tvActivityScore).text = "${score.toInt()}"
            scoresContainer.addView(row)
        }

        tvTotalScore.text = "${totalScore.toInt()}"

        val percentage = if (maxScore > 0) totalScore / maxScore * 100 else 0f
        tvMessage.text = when {
            percentage >= 80 -> getString(R.string.zone_msg_excellent)
            percentage >= 50 -> getString(R.string.zone_msg_good)
            else -> getString(R.string.zone_msg_try_again)
        }

        spriteView.startAnimation()

        // Actualizar puntuación máxima en el servidor
        updateTopScore()

        btnContinue.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Calcula la puntuación total de todas las zonas y actualiza el top_score
     * en el servidor si la puntuación actual es mayor que la registrada.
     */
    private fun updateTopScore() {
        var globalTotal = 0f
        for (zone in ZoneConfig.ALL_ZONES) {
            val zonePrefs = getSharedPreferences(zone.prefsName, MODE_PRIVATE)
            for (activity in zone.activities) {
                globalTotal += zonePrefs.getFloat(activity.scoreKey, 0f)
            }
        }

        val currentTotal = globalTotal.toInt()
        if (currentTotal <= 0) return

        val userRepository = UserRepository(this)

        lifecycleScope.launch {
            // Obtener el top_score actual del usuario
            when (val profileResult = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    val currentTopScore = profileResult.data.topScore
                    if (currentTotal > currentTopScore) {
                        // Actualizar solo si la puntuación actual supera la máxima
                        val updateRequest = UpdateUserRequest(topScore = currentTotal)
                        when (val updateResult = userRepository.updateUserProfile(updateRequest)) {
                            is Resource.Success -> {
                                Log.d(TAG, "Top score actualizado: $currentTopScore -> $currentTotal")
                            }
                            is Resource.Error -> {
                                Log.e(TAG, "Error actualizando top score: ${updateResult.message}")
                            }
                            is Resource.Loading -> { /* No-op */ }
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error obteniendo perfil para top score: ${profileResult.message}")
                }
                is Resource.Loading -> { /* No-op */ }
            }
        }
    }

    companion object {
        /** Tag para logs de depuración. */
        private const val TAG = "ZoneCompletionActivity"

        /** Clave del Intent para identificar las preferencias de la zona. */
        private const val EXTRA_ZONE_PREFS_NAME = "zone_prefs_name"

        /**
         * Comprueba si una zona está completamente terminada.
         * Revisa si todas las actividades tienen su flag "completed" en true.
         */
        fun isZoneComplete(context: Context, zone: ZoneInfo): Boolean {
            val prefs = context.getSharedPreferences(zone.prefsName, MODE_PRIVATE)
            return zone.activities.all { prefs.getBoolean(it.completedKey, false) }
        }

        /**
         * Lanza esta Activity solo si la zona está completa y aún no se ha mostrado el resumen.
         * Evita mostrar la pantalla más de una vez por zona.
         */
        fun launchIfComplete(context: Context, zone: ZoneInfo) {
            val prefs = context.getSharedPreferences(zone.prefsName, MODE_PRIVATE)
            val shownKey = "zone_completion_shown"
            if (prefs.getBoolean(shownKey, false)) return
            if (!isZoneComplete(context, zone)) return

            prefs.edit().putBoolean(shownKey, true).apply()
            val intent = Intent(context, ZoneCompletionActivity::class.java).apply {
                putExtra(EXTRA_ZONE_PREFS_NAME, zone.prefsName)
            }
            context.startActivity(intent)
        }
    }
}
