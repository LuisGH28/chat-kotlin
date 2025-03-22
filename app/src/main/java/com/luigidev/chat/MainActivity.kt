package com.luigidev.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.luigidev.chat.Fragmentos.FragmentChats
import com.luigidev.chat.Fragmentos.FragmentPerfil
import com.luigidev.chat.Fragmentos.FragmentUsuarios
import com.luigidev.chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Access to the views
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            irOpcionesLogin()
        }

        verFragmentoPerfil()

        binding.bottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.item_perfil->{
                    // Perfil visualization
                    verFragmentoPerfil()
                    true
                }
                R.id.item_usuarios->{
                    verFragmentoUsuarios()
                    true
                }
                R.id.item_chats->{
                    verFragmentoChats()
                    true
                }
                else->{
                    false
                }
            }
        }
    }

    private fun irOpcionesLogin() {
        startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
        finishAffinity()
    }

    private fun verFragmentoPerfil(){
        binding.tvTitulo.text= "Perfil"

        val fragment = FragmentPerfil()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFl.id, fragment, "Fragment Perfil")
        fragmentTransaction.commit()
    }

    private fun verFragmentoUsuarios(){
        binding.tvTitulo.text= "Usuarios"

        val fragment = FragmentUsuarios()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFl.id, fragment, "Fragment Usuarios")
        fragmentTransaction.commit()
    }

    private fun verFragmentoChats(){
        binding.tvTitulo.text= "Chats"

        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFl.id, fragment, "Fragment Chats")
        fragmentTransaction.commit()
    }
}