package com.luigidev.chat

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.luigidev.chat.databinding.ActivityCambiarPasswordBinding

class CambiarPassword : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCambiarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere un momento")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCambiarPass.setOnClickListener{
            validarInformacion()
        }

    }

    private var pass_actual = ""
    private var pass_nueva = ""
    private var r_pass_nueva = ""

    private fun validarInformacion() {
        pass_actual = binding.etPassActual.text.toString().trim()
        pass_nueva = binding.etPassNueva.text.toString().trim()
        r_pass_nueva = binding.etRPassNueva.text.toString().trim()

        if(pass_actual.isEmpty()){
            binding.etPassActual.error = "Ingrese contraseña actual"
            binding.etPassActual.requestFocus()
        }else if(pass_nueva.isEmpty()){
            binding.etPassNueva.error = "Ingrese nueva contraseña"
            binding.etPassNueva.requestFocus()
        }else if(r_pass_nueva.isEmpty()){
            binding.etRPassNueva.error = "Ingrese nueva contraseña"
            binding.etRPassNueva.requestFocus()
        }else if(pass_nueva != r_pass_nueva){
            binding.etRPassNueva.error = "Las contraseñas no coinciden"
            binding.etRPassNueva.requestFocus()
        }else{
            autenticarUsuario()
        }
    }

    private fun autenticarUsuario() {
        progressDialog.setMessage("Autenticando usuario...")
        progressDialog.show()

       val authCrendential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), pass_actual)
        firebaseUser.reauthenticate(authCrendential)
            .addOnSuccessListener {
                actualizarPass()
            }

            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo la autenticacion debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun actualizarPass() {
        progressDialog.setMessage("Cambiando contraseña...")
        progressDialog.show()

        firebaseUser.updatePassword(pass_nueva)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Contraseña cambiada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                cerrarSesion()
            }

            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Erroar al cambiar la contraseña debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun cerrarSesion() {
        firebaseAuth.signOut()
        startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
        finishAffinity()

    }
}