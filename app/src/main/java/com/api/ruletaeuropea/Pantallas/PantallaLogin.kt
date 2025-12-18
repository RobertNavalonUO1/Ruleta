package com.api.ruletaeuropea.pantallas

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.api.ruletaeuropea.App
import com.api.ruletaeuropea.R
import com.api.ruletaeuropea.data.entity.Jugador
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


// Singleton para GoogleSignInClient
fun getGoogleSignInClient(activity: Activity): GoogleSignInClient =
    GoogleSignIn.getClient(
        activity,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

// ViewModel para Firebase Auth
class AuthViewModel {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUser get() = auth.currentUser

    fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onError(task.exception ?: Exception("Firebase sign-in failed"))
            }
    }

    fun signOut() {
        auth.signOut()
    }
}

@Composable
fun PantallaLogin(
    navController: NavController,
    jugador: MutableState<Jugador>
) {
    val nombre = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dorado = Color(0xFFFFD700)
    val fondo = painterResource(R.drawable.fondo)
    val logo = painterResource(R.drawable.logoinico)
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel() }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)

            account.idToken?.let { token ->
                authViewModel.firebaseAuthWithGoogle(
                    token,
                    onSuccess = {
                        val nombreGoogle = account.displayName ?: "Usuario Google"

                        scope.launch {
                            val dao = App.database.jugadorDao()

                            // Buscar si ya existe un jugador con ese nombre
                            val existente = withContext(Dispatchers.IO) {
                                dao.obtenerPorNombre(nombreGoogle)
                            }

                            val jugadorFinal = if (existente != null) {
                                existente
                            } else {
                                val nuevo = Jugador(
                                    NombreJugador = nombreGoogle,
                                    Contrasena = null,
                                    NumMonedas = 1000
                                )
                                withContext(Dispatchers.IO) {
                                    dao.insertar(nuevo)
                                }
                                nuevo
                            }

                            jugador.value = jugadorFinal

                            // Navegar SOLO una vez
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
  
                    ,
                    onError = { error.value = it.message }
                )
            }
        } catch (e: Exception) {
            error.value = e.message
        }
    }

    fun login() {
        if (nombre.value.isBlank() || password.value.isBlank()) {
            error.value = "Fill in all fields"
            return
        }

        scope.launch {
            loading.value = true
            val dao = App.database.jugadorDao()

            val existente = withContext(Dispatchers.IO) {
                dao.obtenerPorNombre(nombre.value)
            }

            when {
                existente == null ->
                    error.value = "User not registered"

                existente.Contrasena == null || existente.Contrasena != password.value ->
                    error.value = "Incorrect password"

                else -> {
                    jugador.value = existente
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            loading.value = false
        }
    }

    fun crearCuenta() {
        if (nombre.value.isBlank() || password.value.isBlank()) {
            error.value = "Fill in all fields"
            return
        }

        scope.launch {
            loading.value = true
            val dao = App.database.jugadorDao()

            val existente = withContext(Dispatchers.IO) {
                dao.obtenerPorNombre(nombre.value)
            }

            if (existente != null) {
                error.value = "User already exists"
            } else {
                val nuevo = Jugador(
                    NombreJugador = nombre.value,
                    Contrasena = password.value,
                    NumMonedas = 1000
                )

                withContext(Dispatchers.IO) {
                    dao.insertar(nuevo)
                }

                jugador.value = nuevo
                navController.navigate("menu") {
                    popUpTo("login") { inclusive = true }
                }
            }
            loading.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = fondo,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 COLUMNA IZQUIERDA – LOGO GRANDE
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = logo,
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
            }

            // 🔹 COLUMNA DERECHA – LOGIN
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it; error.value = null },
                    label = { Text("User", color = dorado) },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = dorado) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dorado,
                        unfocusedTextColor = dorado,
                        focusedBorderColor = dorado,
                        unfocusedBorderColor = dorado,
                        cursorColor = dorado
                    )
                )


                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it; error.value = null },
                    label = { Text("Password", color = dorado) },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = dorado) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(
                                if (passwordVisible.value)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = dorado
                            )
                        }
                    },
                    visualTransformation =
                        if (passwordVisible.value)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(onDone = { login() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dorado,
                        unfocusedTextColor = dorado,
                        focusedBorderColor = dorado,
                        unfocusedBorderColor = dorado,
                        cursorColor = dorado
                    )
                )



                error.value?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                // 🔹 FILA DE BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = ::login,
                        enabled = !loading.value,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dorado,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Login")
                    }

                    OutlinedButton(
                        onClick = ::crearCuenta,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, dorado)
                    ) {
                        Text("Create", color = dorado)
                    }

                    OutlinedButton(
                        onClick = {
                            jugador.value = Jugador(
                                NombreJugador = "Guest",
                                Contrasena = null,
                                NumMonedas = 1000
                            )
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, dorado)
                    ) {
                        Text("Guest", color = dorado)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        googleLauncher.launch(
                            getGoogleSignInClient(context as Activity).signInIntent
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dorado,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Login with Google")
                }
            }
        }
    }
}
