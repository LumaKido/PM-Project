package com.example.apptemperatura

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ListarTodosActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var voltarButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_todos)

        recyclerView = findViewById(R.id.recyclerView)
        voltarButton = findViewById(R.id.voltarButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        voltarButton.setOnClickListener {
            finish()
        }

        buscarTodos()
    }

    private fun buscarTodos() {
        db.collection("Temperatura")
            .orderBy("dataHora", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val sensorDataList = mutableListOf<SensorData>()
                val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                dateTimeFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")

                for (document in querySnapshot.documents) {
                    val dataHora = document.getTimestamp("dataHora")?.toDate()
                    val temperatura = document.getString("temperatura") ?: "N/A"
                    val umidade = document.getString("umidade") ?: "N/A"
                    val dataFormatada = if (dataHora != null) dateTimeFormat.format(dataHora) else "Data indisponível"

                    sensorDataList.add(SensorData(dataFormatada, temperatura, umidade))
                }

                recyclerView.adapter = SensorDataAdapter(sensorDataList)
            }
    }
}

data class SensorData(val dataHora: String, val temperatura: String, val umidade: String)