package com.luigidev.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.luigidev.chat.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_login_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnIngresar.setOnClickListener{
            validarInformacion()
        }


        binding.tvRegistrarme.setOnClickListener{
            startActivity(Intent(applicationContext, RegistroEmailActivity::class.java))
        }
    }

    private var email = ""
    private var passwd = ""

    private fun validarInformacion() {
        email = binding.etEmail.text.toString().trim()
        passwd = binding.etPassword.text.toString().trim()

        if(email.isEmpty()){
            binding.etEmail.error = "Ingrese su correo por favor"
            binding.etEmail.requestFocus()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Email incorrecto"
            binding.etEmail.requestFocus()
        }else if(passwd.isEmpty()){
            binding.etPassword.error = "Ingrese password por favor"
            binding.etPassword.requestFocus()
        }else{
            logearUsuario()
        }
    }

    private fun logearUsuario() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, passwd)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }

            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se realizo el logeo debido a {${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
    }
}