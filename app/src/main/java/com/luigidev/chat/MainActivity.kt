package com.luigidev.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            irOpcionesLogin()
        }

        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Sincronizar BottomNavigationView con ViewPager2
        binding.bottomNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_perfil -> {
                    binding.viewPager.currentItem = 0
                    binding.tvTitulo.text = "Perfil"
                    true
                }

                R.id.item_usuarios -> {
                    binding.viewPager.currentItem = 1
                    binding.tvTitulo.text = "Usuarios"
                    true
                }

                R.id.item_chats -> {
                    binding.viewPager.currentItem = 2
                    binding.tvTitulo.text = "Chats"
                    true
                }

                else -> false
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.bottomNV.selectedItemId = R.id.item_perfil
                    1 -> binding.bottomNV.selectedItemId = R.id.item_usuarios
                    2 -> binding.bottomNV.selectedItemId = R.id.item_chats
                }
            }
        })

    }

    private fun irOpcionesLogin() {
        startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
        finishAffinity()
    }
}