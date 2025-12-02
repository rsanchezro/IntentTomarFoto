package com.example.intenttomarfoto

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var boton: Button
    lateinit var imageUri: Uri
    // Registrar contrato
    val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            // Si la foto se tomó correctamente, la mostramos
            imageView.setImageURI(imageUri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        imageView=findViewById<ImageView>(R.id.imageView)
        boton=findViewById<Button>(R.id.button)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        boton.setOnClickListener {
            //Vamos a crear un URI en MediaStore, que es el sistema de archivos multimedia de Android

            //ContentValues es un objeto tipo diccionario que almacena los metadatos de la foto
            //DISPLAY_NAME es el nombre del archivo
            //MIME_TYPE el tipo de archivo
            //RELATIVE_PATH la ruta relativa donde se almacenara la foto, dentro de Pictures
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "foto_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraCompose")
            }
            //Creo la URI, realmente crea un registro vacio en la base de datos de MediaStore,
            //Reserva un contenedor para almacenar la imagen, devuelve la uri
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

        //Take a picture necesita una uri

            uri?.let {
                imageUri=it
                takePictureLauncher.launch(imageUri) }
        }
    }
}