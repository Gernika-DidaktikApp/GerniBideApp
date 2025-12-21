package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import es.didaktikapp.gernikapp.dao.DaoAlumno

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val daoAlumno = DaoAlumno(this)

        // Asociar con el xml
        val editTextUsuario: EditText = findViewById(R.id.editTextUsuario)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val botonLogin: Button = findViewById(R.id.btnLogin)

        botonLogin.setOnClickListener {
            val usuario = editTextUsuario.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (usuario.isEmpty()) {
                editTextUsuario.error = "Ingrese su usuario"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Ingrese su contraseña"
                return@setOnClickListener
            }

            // Validar credenciales en segundo plano usando Thread
            Thread {
                val alumno = daoAlumno.validarCredenciales(usuario, password)

                // Volver al hilo principal para actualizar la UI
                runOnUiThread {
                    if (alumno != null) {
                        // Login exitoso
                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido ${alumno.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Guardar usuario en SharedPreferences
                        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit {
                            putString("username", alumno.usuario)
                            putBoolean("isFirstTime", false)
                        }

                        // Ir a MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Login fallido
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_SHORT
                        ).show()
                        editTextPassword.text.clear()
                    }
                }
            }.start()
        }
    }
}