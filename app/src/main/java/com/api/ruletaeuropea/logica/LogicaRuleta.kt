package com.api.ruletaeuropea.logica

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.data.model.CategoriaApostada
import com.api.ruletaeuropea.data.entity.Apuesta as ApuestaEntity
import android.graphics.Bitmap
import com.api.ruletaeuropea.R
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.api.ruletaeuropea.data.entity.Ubicacion
import com.api.ruletaeuropea.data.db.RuletaDatabase
import android.os.Environment
import com.api.ruletaeuropea.data.entity.Jugador





// Números rojos en ruleta europea
val RedNumbers = setOf(
    1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
)

// Evalúa si una apuesta es ganadora según el resultado
fun evaluarApuesta(apuesta: Apuesta, resultado: Int): Boolean {
    return when (apuesta.numero) {
        resultado -> true // pleno
        -101 -> resultado in 1..12 // 1st 12
        -102 -> resultado in 13..24 // 2nd 12
        -103 -> resultado in 25..36 // 3rd 12
        -201 -> resultado in 1..18 // 1 to 18
        -202 -> resultado != 0 && resultado % 2 == 0 // par
        -203 -> resultado in RedNumbers // rojo
        -204 -> resultado != 0 && !RedNumbers.contains(resultado) // negro
        -205 -> resultado % 2 == 1 // impar
        -206 -> resultado in 19..36 // 19 to 36
        else -> false
    }
}

// Multiplicador de pago según tipo de apuesta
fun multiplicador(apuesta: Apuesta): Int {
    return when (apuesta.numero) {
        in 0..36 -> 36 // pleno
        -101, -102, -103 -> 3 // docenas
        -201, -202, -205, -206 -> 2 // mitades, par/impar
        -203, -204 -> 2 // color
        else -> 0
    }
}

// Calcula el pago total de todas las apuestas ganadoras
fun calcularPago(apuestas: List<Apuesta>, resultado: Int): Int {
    return apuestas
        .filter { evaluarApuesta(it, resultado) }
        .sumOf { it.valorMoneda * multiplicador(it) }
}
fun tipoApuesta(numero: Int): String {
    return when (numero) {
        in 0..36 -> numero.toString() // solo el número
        -101 -> "1st 12"
        -102 -> "2nd 12"
        -103 -> "3rd 12"
        -201 -> "1 to 18"
        -202 -> "EVEN"
        -203 -> "RED"
        -204 -> "BLACK"
        -205 -> "ODD"
        -206 -> "19 to 36"
        else -> "?"
    }
}

// Construye entidad Apuesta
fun construirApuestaCompleta(
    apuestaUI: Apuesta,
    jugador: Jugador,
    resultado: Int,
    idRuleta: Long
): ApuestaEntity {
    val numero = apuestaUI.numero
    val categoria = when (numero) {
        in 0..36 -> CategoriaApostada.PLENO
        -101, -102, -103 -> CategoriaApostada.DOCENA
        -201, -206 -> CategoriaApostada.MITAD
        -202, -205 -> CategoriaApostada.PARIDAD
        -203, -204 -> CategoriaApostada.COLOR
        else -> CategoriaApostada.OTRA
    }

    return ApuestaEntity(
        NombreJugador = jugador.NombreJugador,
        IDRuleta = idRuleta,
        MonedasApostadas = apuestaUI.valorMoneda,
        CategoriaApostada = categoria,
        RojoApostado = if (numero == -203) true else if (numero == -204) false else null,
        ParApostado = if (numero == -202) true else if (numero == -205) false else null,
        MitadInfApostada = if (numero == -201) true else if (numero == -206) false else null,
        NumerosApostados = if (numero >= 0) arrayListOf(numero) else null,
        Ganada = evaluarApuesta(apuestaUI, resultado),
        Pago = if (evaluarApuesta(apuestaUI, resultado)) apuestaUI.valorMoneda * multiplicador(apuestaUI) else 0
    )
}


// Guardar imagen en galería (Android 10+ seguro)
fun saveToGallery(context: Context, bitmap: Bitmap) {
    val filename = "screenshot_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)
    }
}




// Insertar evento en calendario seguro
fun addCalendarEvent(context: Context, title: String, description: String) {
    if (context.checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR)
        != PackageManager.PERMISSION_GRANTED
    ) return

    val calID: Long = 1
    val startMillis = System.currentTimeMillis()
    val endMillis = startMillis + 60 * 60 * 1000

    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.TITLE, title)
        put(CalendarContract.Events.DESCRIPTION, description)
        put(CalendarContract.Events.CALENDAR_ID, calID)
        put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Madrid")
    }

    try {
        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

// Mostrar notificación victoria seguro
fun mostrarNotificacionVictoria(context: Context, pagoTotal: Int) {
    val channelId = "victory_channel"
    val channelName = "Victory Notifications"
    val notificationId = System.currentTimeMillis().toInt()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Notifications when you win the game" }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.icvictory)
        .setContentTitle("¡Victory!")
        .setContentText("You have won $pagoTotal coins")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)

    // Ejecutar en el hilo principal
    Handler(Looper.getMainLooper()).post {
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

// Obtener y guardar Ubicación
fun obtenerUbicacion(context: Context) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Comprobar permiso
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
        return
    }

    // Intentar obtener la última ubicación conocida primero
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val ubicacion = Ubicacion(latitude = location.latitude, longitude = location.longitude)
            CoroutineScope(Dispatchers.IO).launch {
                RuletaDatabase.getDatabase(context).ubicacionDao().insert(ubicacion)
            }
        } else {
            // Si lastLocation es null, usar getCurrentLocation como fallback
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { currentLocation ->
                    if (currentLocation != null) {
                        val ubicacion = Ubicacion(
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            RuletaDatabase.getDatabase(context).ubicacionDao().insert(ubicacion)
                        }
                    }
                }
        }
    }
}

