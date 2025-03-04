package com.tcontur.qrbus.core.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.tcontur.qrbus.R
import com.tcontur.qrbus.client.ServiceClient
import com.tcontur.qrbus.client.models.EmpresaModel
import com.tcontur.qrbus.client.models.LoginRequest
import com.tcontur.qrbus.client.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit = {},
    context: Context
) {
//    var context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    var empresas by remember { mutableStateOf<List<EmpresaModel>>(emptyList()) }
    var selectedEmpresa by remember { mutableStateOf<EmpresaModel?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingEmpresas by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (sessionManager.isRememberMe()) {
            rememberMe = true
            username = sessionManager.getUsername() ?: ""
        }
        val empresaService = ServiceClient.getEmpresaService()
        if (empresaService != null) {
            empresaService.getEmpresas().enqueue(object : Callback<List<EmpresaModel>> {
                override fun onResponse(
                    call: Call<List<EmpresaModel>>,
                    response: Response<List<EmpresaModel>>
                ) {
                    isLoadingEmpresas = false
                    if (response.isSuccessful) {
                        empresas = response.body().orEmpty()
                        if (sessionManager.isRememberMe()) {
                            val empresa = sessionManager.getEmpresa()
                            selectedEmpresa = empresas.find { it.codigo == empresa?.codigo }
                        }
                    } else {
                        errorMessage = "No se pudieron cargar las empresas"
                        showDialog = true
                    }
                }

                override fun onFailure(call: Call<List<EmpresaModel>>, t: Throwable) {
                    isLoadingEmpresas = false
                    errorMessage = "Error al conectar con el servidor: ${t.localizedMessage}"
                    showDialog = true
                }
            })
        } else {
            isLoadingEmpresas = false
            errorMessage = "Error interno en la configuración de red"
            showDialog = true
        }
    }

    fun performLogin() {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Usuario y contraseña son requeridos"
            showDialog = true
            return
        }
        val authService = ServiceClient.getAuthService(selectedEmpresa?.codigo ?: "")
        if (authService == null) {
            errorMessage = "Error interno en la configuración de red"
            showDialog = true
            return
        }

        isLoading = true
        val empresaModel = selectedEmpresa
        val request = LoginRequest(username, password)
        authService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val token = response.body()?.token
                val user = response.body()?.userId
                isLoading = false
                if (response.isSuccessful) {
                    if (token != null) {
                        sessionManager.saveAuthToken(token = token, user.toString())
                        sessionManager.saveEmpresa(empresaModel)
                        sessionManager.saveUsername(username)
                    }
                    sessionManager.saveRememberMe(rememberMe)
                    navigateToHome()
                } else {
                    errorMessage = "Error al iniciar sesión. Inténtalo de nuevo."
                    showDialog = true
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Error de conexión: ${t.localizedMessage}"
                showDialog = true
            }
        })
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8db8d9), Color(0xFF0066a8))
                )
            )
            .padding(16.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo TCONTUR",
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxSize()

            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Empresa", fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = true
                        }
                ) {

                    TextField(
                        value = selectedEmpresa?.nombre ?: "",
                        placeholder = { Text("Empresa", color = Color.Gray) },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = TextStyle(
                            color = Color.Black,
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            disabledIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (isLoadingEmpresas) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.material3.IconButton(onClick = {
                                    expanded = true
                                }) {
                                    androidx.compose.material3.Icon(
                                        tint = Color.Black,
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = "Expandir"
                                    )
                                }
                            }
                        }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    empresas.forEach { empresa ->
                        DropdownMenuItem(
                            text = { Text(empresa.nombre) },
                            onClick = {
                                selectedEmpresa = empresa
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "Usuario",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp, color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = {
                    username = it
                    isError = false
                },
                textStyle = TextStyle(
                    color = Color.Black,
                ),
                placeholder = { Text("Usuario", color = Color.Gray) },
                singleLine = true,
                isError = isError,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(15.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Contraseña", fontSize = 16.sp, color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    color = Color.Black,
                ),
                value = password,
                onValueChange = {
                    password = it
                    isError = false
                },
                placeholder = { Text("Contraseña", color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = isError,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        uncheckedColor = Color.White,
                        checkmarkColor = Color(0xFF2196F3),
                        disabledCheckedColor = Color.White,
                        disabledUncheckedColor = Color.White
                    )
                )
                Text(
                    text = "Recordarme",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isLoading) return@Button
                    performLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Iniciar Sesión", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Versión 0.9.0",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Error") },
            text = { Text(text = errorMessage, color = Color.Red) },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    }
}

@Preview(
    name = "Pixel 8",
//    showBackground = true,
//    widthDp = 720,
//    heightDp = 1600
)
@Composable
fun LoginScreenPreviewPixel8() {
    LoginScreen(
        navigateToHome = {},
        context = LocalContext.current
    )
}
