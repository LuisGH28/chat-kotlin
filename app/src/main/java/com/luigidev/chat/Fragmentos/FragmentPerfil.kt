package com.luigidev.chat.Fragmentos

import android.app.Activity
import android.app.Dialog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.luigidev.chat.CambiarPassword
import com.luigidev.chat.Constantes
import com.luigidev.chat.EditarInformacion
import com.luigidev.chat.OpcionesLoginActivity
import com.luigidev.chat.R
import com.luigidev.chat.databinding.FragmentPerfilBinding
import de.hdodenhof.circleimageview.CircleImageView

class FragmentPerfil : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private var imagenUri: Uri? = null

    // Registros para manejar resultados de actividades
    private val galeriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data?.data
                subirImagenStorage(imagenUri)
            }else{
                Toast.makeText(
                    mContext,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val camaraARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            resultado.data?.extras?.get("data")?.let {
                // Guardar la imagen temporalmente y subirla
                guardarImagenCamara(it)
            }
        } else {
            Toast.makeText(mContext, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) abrirGaleria()
            else Toast.makeText(mContext, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }

    private val solicitarPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) abrirCamara()
            else Toast.makeText(mContext, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        cargarInformacion()

        binding.btnActualizarInfo.setOnClickListener {
            startActivity(Intent(mContext, EditarInformacion::class.java))
        }

        binding.btnCambiarPass.setOnClickListener{
            startActivity(Intent(mContext, CambiarPassword::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, OpcionesLoginActivity::class.java))
            activity?.finishAffinity()
        }

        binding.ivPerfil.setOnClickListener {
            mostrarImagenAmpliada()
        }
    }

    private fun mostrarImagenAmpliada() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_imagen_ampliada)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }

            val imagenAmpliada = findViewById<ImageView>(R.id.imagen_ampliada)
            val btnEditar = findViewById<ImageView>(R.id.IvEditarImg)

            Glide.with(mContext)
                .load(binding.ivPerfil.drawable)
                .into(imagenAmpliada)

            btnEditar.setOnClickListener {
                mostrarOpcionesEdicion()
                dismiss()
            }
        }
        dialog.show()
    }

    private fun mostrarOpcionesEdicion() {
        val options = arrayOf("Tomar foto", "Elegir de galería", "Cancelar")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar imagen de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                            abrirGaleria()
                        }else{
                            solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                }
            }
            .show()
    }

    private fun verificarPermisoAlmacenamiento() {
        when {
            ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirGaleria()
            }
            else -> {
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                solicitarPermisoCamara.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type ="image/*"
        galeriaARL.launch(intent)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraARL.launch(intent)
    }

    private fun guardarImagenCamara(bitmap: Any) {
        // Implementa la lógica para guardar la imagen temporalmente
        // y obtener su URI para subirla a Firebase
        Toast.makeText(mContext, "Implementa guardado de imagen", Toast.LENGTH_SHORT).show()
    }

    private fun subirImagenStorage(imagenUri: Uri?) {
        // Ejecutar la subida después de que el diálogo esté visible
        binding.root.post {
            val rutaImagen = "imagenesPerfil/${firebaseAuth.uid}"
            val ref = FirebaseStorage.getInstance().getReference(rutaImagen)

            ref.putFile(imagenUri ?: run {

                Toast.makeText(mContext, "Imagen no válida", Toast.LENGTH_SHORT).show()
                return@post
            })
                .addOnSuccessListener { taskSnapShot ->
                    taskSnapShot.storage.downloadUrl.addOnSuccessListener { uri ->
                        actualizarInfoBD(uri.toString())
                    }
                }
                .addOnFailureListener { e ->

                    Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun actualizarInfoBD(urlImagen: String) {

        val hashMap = hashMapOf<String, Any>("imagen" to urlImagen)
        FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                Toast.makeText(mContext, "Imagen actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->

                Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarInformacion() {
        FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(firebaseAuth.uid ?: "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = snapshot.child("nombres").value?.toString() ?: ""
                    val apellidos = snapshot.child("apellidos").value?.toString() ?: ""
                    val email = snapshot.child("email").value?.toString() ?: ""
                    val proveedor = snapshot.child("proveedor").value?.toString() ?: ""
                    val tiempoRegistro = snapshot.child("tiempoR").value?.toString()?.toLongOrNull() ?: 0
                    val imagenUrl = snapshot.child("imagen").value?.toString()

                    binding.tvNombres.text = nombres
                    binding.tvApellidos.text = apellidos
                    binding.tvEmail.text = email
                    binding.tvProveedor.text = proveedor
                    binding.tvTRegistro.text = Constantes.formatoFecha(tiempoRegistro)

                    Glide.with(mContext)
                        .load(imagenUrl)
                        .placeholder(R.drawable.ic_img_perfil)
                        .error(R.drawable.ic_img_perfil)
                        .into(binding.ivPerfil)

                    if(proveedor == "Email"){
                        binding.btnCambiarPass.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}