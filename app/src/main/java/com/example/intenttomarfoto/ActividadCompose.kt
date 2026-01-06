package com.example.intenttomarfoto

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.intenttomarfoto.ui.theme.IntentTomarFotoTheme

class ActividadCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntentTomarFotoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   TomarFoto(Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun TomarFoto(modificador: Modifier) {
    //Obtenemos el contexto que lo usamos en un Toast
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher para solicitar permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    // Launcher que usa TakePicture, guarda la imagen en la URI input
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        //Devuelve true si se obtuvo la imagen correctamente y realmente
        //se almacena en la uri que se creo
        if (exito && imageUri != null) {
            // Convertir Uri → Bitmap
            bitmap = BitmapFactory.decodeStream(
                //Abre el  fichero con la uri que se genero, mediante el método
                //contenResolver y se convierte a un bitmap
                context.contentResolver.openInputStream(imageUri!!)
            )
        }
    }

    Column(
        modifier = modificador.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Cuando
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            //Preguntamos si concedemos el permiso de uso de la camara
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==PackageManager.PERMISSION_GRANTED) {
                // 1. Crear un Uri en el Proveedor de contenidos MediaStore.Images.Media
                //Un proveedor de contenidos es un mecanismo para compartir datos entre aplicaciones
                //Existen proveedores de contenidos definidos por el sistema operativo para acceder
                //a información del sistema (contactos, ficheros multimedia,registro de llamadas,  etc..)
                //Como indica la doc. oficial, https://developer.android.com/guide/topics/providers/content-provider-basics?hl=es-419#ClientProvider
                //para acceder a un proveedor de contenidos para añadir un registro, será necesario un ContentValues
                //es una especie de mapa (clave, valor), indicando los valores de los campos a añadir

                //Las columnas de este proveedor de contenidos MediaStore.Images.Media
                //se definen https://developer.android.com/reference/android/provider/MediaStore.MediaColumns

                val values = ContentValues().apply {
                    put(//El nombre del fichero imagen
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "foto_${System.currentTimeMillis()}.jpg"
                    )
                    //El tipo de fichero
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    //La ubicación relativa sobre la zona donde almacena los ficheros media el dispositivo
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraCompose")
                }

                //Se accede al proveedor de contenidos para añadir un registro
                // en la tabla correspondiente (dispositivo externo) y generar una uri
                // que se usará para guardar la foto
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )


                // 2. Lanzar TakePicture()
                uri?.let {
                    imageUri = uri
                    launcher.launch(uri)
                }
            }
            else
            {
                // Solicitar permiso si no lo tengo concedido
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }


        }) {
            Text("Tomar foto")
        }
    }
}

