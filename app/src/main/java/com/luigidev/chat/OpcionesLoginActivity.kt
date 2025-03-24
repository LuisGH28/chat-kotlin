package com.luigidev.chat

import android.app.ProgressDialog
import android.content.Intent
import android.media.tv.TvContract.Programs
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Api.AnyClient
import com.google.android.gms.common.api.Api.Client
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.luigidev.chat.databinding.ActivityMainBinding
import com.luigidev.chat.databinding.ActivityOpcionesLoginBinding

class OpcionesLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpcionesLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progreessDialog: ProgressDialog
    private lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_opciones_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        progreessDialog = ProgressDialog(this)
        progreessDialog.setTitle("Espere por favor")
        progreessDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        comprobarSesion()

        binding.opcionEmail.setOnClickListener{
              startActivity(Intent(applicationContext, LoginEmailActivity::class.java))
        }

        binding.opcionGoogle.setOnClickListener{
            iniciarGoogle()
        }
    }

    private fun iniciarGoogle() {
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSingInARL.launch(googleSignInIntent)
    }

    private val googleSingInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ resultado ->
        if(resultado.resultCode == RESULT_OK ){
            val data = resultado.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                val cuenta = task.getResult(ApiException::class.java)
                autenticarCuentaGoogle(cuenta.idToken)
            }catch (e: Exception){
                Toast.makeText(
                        this,
                        "${e.message}",
                        Toast.LENGTH_SHORT
                        ).show()
            }
        }else{
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun autenticarCuentaGoogle(idToken: String?) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { authResultado ->
               if(authResultado.additionalUserInfo!!.isNewUser){
                   actualizarInfoUsuario()
               }else{
                   startActivity(Intent(this, MainActivity::class.java))
                   finishAffinity()
               }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun actualizarInfoUsuario() {
        progreessDialog.setMessage("Guardando informacion")

        val uidU = firebaseAuth.uid
        val nombresU = firebaseAuth.currentUser!!.displayName
        val emailU = firebaseAuth.currentUser!!.email
        val tiempoR = Constantes.obtenerTiempoD()

        val datosUsuarios = HashMap<String, Any>()
        datosUsuarios["uid"] = "$uidU"
        datosUsuarios["nombres"] = "$nombresU"
        datosUsuarios["email"] = "$emailU"
        datosUsuarios["tiempoR"] = "$tiempoR"
        datosUsuarios["preoveedor"] = "Google"
        datosUsuarios["estado"] = "Online"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidU!!)
            .setValue(datosUsuarios)
            .addOnSuccessListener {
                progreessDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }

            .addOnFailureListener { e->
                progreessDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo al guardar la informacion debido a ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun comprobarSesion() {
        if(firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}