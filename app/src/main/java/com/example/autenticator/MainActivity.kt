package com.example.autenticator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autenticator.ui.theme.AutenticatorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance() // Inicializa o FirebaseAuth

        setContent {
            AutenticatorTheme {
                NavigationSetup(auth, db)
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        validation(auth, email, senha) { success ->
                            if (success) {
                                info = "Login bem-sucedido!"
                                navController.navigate("home") // Navega para a tela principal
                            } else {
                                info = "Falha na validação!"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Validar")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(info, color = if (info.startsWith("Falha")) Color.Red else Color.Green)
            }
        }
    )
}

private fun validation(auth: FirebaseAuth, email: String, password: String, callback: (Boolean) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("Login", "Login bem-sucedido")
                callback(true)
            } else {
                Log.e("Login", "Falha no login: ${task.exception?.message}")
                callback(false)
            }
        }
}

@Composable
fun HomeScreen(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    val clientes = remember { mutableStateListOf<Client>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "App Firebase Firestore", modifier = Modifier.align(Alignment.CenterHorizontally))

        TextFieldWithLabel(label = "Nome:", value = nome, onValueChange = { nome = it })
        TextFieldWithLabel(label = "Telefone:", value = telefone, onValueChange = { telefone = it })

        Button(
            onClick = {
                val pessoas = hashMapOf("nome" to nome, "telefone" to telefone)
                db.collection("Clientes").add(pessoas)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written ID: ${documentReference.id}")
                        fetchClientes(db, clientes)
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                    }
            },
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(text = "Cadastrar")
        }

        // Fetch clients
        LaunchedEffect(Unit) {
            fetchClientes(db, clientes)
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(clientes) { cliente ->
                ClientRow(cliente) { clientId ->
                    db.collection("Clientes").document(clientId).delete()
                        .addOnSuccessListener {
                            Log.d("TAG", "DocumentSnapshot successfully deleted!")
                            clientes.remove(cliente)
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error deleting document", e)
                        }
                }
            }
        }
    }
}

@Composable
fun TextFieldWithLabel(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(text = label)
        TextField(value = value, onValueChange = onValueChange)
    }
}

@Composable
fun ClientRow(cliente: Client, onDelete: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(0.5f)) {
            Text(text = cliente.nome)
        }
        Column(modifier = Modifier.weight(0.5f)) {
            Text(text = cliente.telefone)
        }
        Column(modifier = Modifier.weight(0.5f)) {
            Button(onClick = { onDelete(cliente.id) }) {
                Text(text = "Deletar")
            }
        }
    }
}

data class Client(val id: String, val nome: String, val telefone: String)

fun fetchClientes(db: FirebaseFirestore, clientes: SnapshotStateList<Client>) {
    Log.d("Firestore", "Fetching clients")
    clientes.clear()
    db.collection("Clientes")
        .get()
        .addOnSuccessListener { documents ->
            Log.d("Firestore", "Documents fetched successfully")
            for (document in documents) {
                val client = Client(
                    id = document.id,
                    nome = document.getString("nome") ?: "--",
                    telefone = document.getString("telefone") ?: "--"
                )
                clientes.add(client)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error fetching documents: ", exception)
        }
}

@Composable
fun NavigationSetup(auth: FirebaseAuth, db: FirebaseFirestore) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(auth, navController) }
        composable("home") {
            HomeScreen(db)
        }
    }
}