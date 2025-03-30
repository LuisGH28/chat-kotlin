package com.luigidev.chat.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luigidev.chat.Constantes
import com.luigidev.chat.OpcionesLoginActivity
import com.luigidev.chat.R
import com.luigidev.chat.databinding.FragmentPerfilBinding

class FragmentPerfil : Fragment() {

    private lateinit var binding : FragmentPerfilBinding
    private lateinit var mContex : Context
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContex = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPerfilBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarInformacion()

        binding.btnCerrarSesion.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContex, OpcionesLoginActivity::class.java))
            activity?.finishAffinity()
        }
    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val preoveedor = "${snapshot.child("preoveedor").value}"
                    var tiempoR = "${snapshot.child("tiempoR").value}"
                    val imagen = "${snapshot.child("imagen").value}"

                    if(tiempoR == "null") tiempoR = "0"

                    val fecha = Constantes.formatoFecha(tiempoR.toLong())

                    /*
                    Setter la info en las vistas
                     */

                    binding.tvNombres.text = nombres
                    binding.tvEmail.text = email
                    binding.tvProveedor.text = preoveedor
                    binding.tvTRegistro.text = fecha

                    try {
                        Glide.with(mContex)
                            .load(imagen)
                            .placeholder(R.drawable.ic_img_perfil)
                            .into(binding.ivPerfil)
                    }catch (e: Exception){
                        Toast.makeText(
                            mContex,
                            "Error al cargar la imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


}