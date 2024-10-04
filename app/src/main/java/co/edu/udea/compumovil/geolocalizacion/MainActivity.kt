package co.edu.udea.compumovil.geolocalizacion

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import co.edu.udea.compumovil.geolocalizacion.ui.theme.GeolocalizacionTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Registrar el contrato para la solicitud de permisos
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si los permisos fueron otorgados, obtener la ubicación actual
            setContent {
                GeolocalizacionTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LocationMap(fusedLocationClient)
                    }
                }
            }
        } else {
            // Si los permisos fueron denegados, mostrar un mensaje
            Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_LONG).show()
            finish() // Finaliza la aplicación si no se conceden los permisos
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificar si los permisos están otorgados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos no están otorgados, solicitarlos
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Si los permisos ya están otorgados, mostrar el mapa
            setContent {
                GeolocalizacionTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LocationMap(fusedLocationClient)
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationMap(fusedLocationClient: FusedLocationProviderClient) {
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Obtener la ubicación actual
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = LatLng(location.latitude, location.longitude)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        // Si la ubicación del usuario no está disponible, centra el mapa en una posición por defecto
        position = CameraPosition.fromLatLngZoom(userLocation ?: LatLng(0.0, 0.0), 11f)
    }

    // Actualizar la posición de la cámara cuando se obtenga la ubicación del usuario
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 11f)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        userLocation?.let {
            // Coloca un marcador en la ubicación del usuario
            Marker(
                state = MarkerState(position = it),
                title = "Tu ubicación",
                snippet = "Estás aquí"
            )
        }
    }
}
