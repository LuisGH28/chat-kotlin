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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.luigidev.chat.databinding.ActivityRegistroEmailBinding

class RegistroEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistroEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_registro_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere, por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnRegistrar.setOnClickListener{
            validarInformacion()
        }
    }

    private var nombres = ""
    private var email = ""
    private var passwd = ""
    private var rPasswd = ""
    private fun validarInformacion() {
        nombres = binding.etNombre.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        passwd = binding.etPassword.text.toString().trim()
        rPasswd = binding.etRPassword.text.toString().trim()

        if (nombres.isEmpty()){
            binding.etNombre.error = "Ingresa tu nombre"
            binding.etNombre.requestFocus()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Correo inválido"
            binding.etEmail.requestFocus()
        }else if (email.isEmpty()){
            binding.etEmail.error = "Ingrese su correo"
            binding.etEmail.requestFocus()
        }else if(passwd.isEmpty()) {
            binding.etPassword.error = "Ingrese su contraseña"
            binding.etPassword.requestFocus()
        }else if (rPasswd.isEmpty()) {
            binding.etRPassword.error = "Repita su contraseña"
            binding.etRPassword.requestFocus()
        }else if(passwd != rPasswd){
            binding.etRPassword.error = "Las contraseñas no coinciden"
            binding.etRPassword.requestFocus()
        }else{
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, passwd)
            .addOnSuccessListener {
                actaulizarInformacion()

            }

            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo la creacion de la cuenta debido a ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
    }

    private fun actaulizarInformacion() {
        progressDialog.setMessage("Guardando informacion")

        val uidU = firebaseAuth.uid
        val nombresU = nombres
        val emailU = firebaseAuth.currentUser!!.email
        val tiempoR = Constantes.obtenerTiempoD()

        val datosUsuarios = HashMap<String, Any>()
        datosUsuarios["uid"] = "$uidU"
        datosUsuarios["nombres"] = "$nombresU"
        datosUsuarios["email"] = "$emailU"
        datosUsuarios["tiempoR"] = "$tiempoR"
        datosUsuarios["preoveedor"] = "Email"
        datosUsuarios["estado"] = "Online"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidU!!)
            .setValue(datosUsuarios)
            .addOnSuccessListener {
                progressDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }

            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo al guardar la informacion debido a ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }
}