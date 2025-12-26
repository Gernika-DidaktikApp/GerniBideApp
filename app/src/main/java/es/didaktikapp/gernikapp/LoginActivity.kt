package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.repository.AuthRepository
import es.didaktikapp.gernikapp.databinding.ActivityLoginBinding
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)

        if (authRepository.hasActiveSession()) {
            navigateToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val usuario = binding.editTextUsuario.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (usuario.isEmpty()) {
                binding.editTextUsuario.error = getString(R.string.error_username_required)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextPassword.error = getString(R.string.error_password_required)
                return@setOnClickListener
            }

            performLogin(usuario, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            setLoading(true)

            when (val result = authRepository.login(username, password)) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_welcome, username),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }

                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                    binding.editTextPassword.text.clear()
                }

                is Resource.Loading -> {
                    // Ya está en loading
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.editTextUsuario.isEnabled = !isLoading
        binding.editTextPassword.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}