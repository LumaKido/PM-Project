package com.example.apptemperatura

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.apptemperatura.ui.theme.AppTemperaturaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {

    private lateinit var dataHoraInput: EditText
    private lateinit var verificarButton: Button
    private lateinit var verTodosButton: Button
    private lateinit var resultTextView: TextView
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")) // Fuso UTC-3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        dataHoraInput = findViewById(R.id.dataHoraInput)
        verificarButton = findViewById(R.id.verificarButton)
        verTodosButton = findViewById(R.id.verTodosButton)
        resultTextView = findViewById(R.id.resultTextView)

        dataHoraInput.setOnClickListener {
            showDatePicker()
        }

        verificarButton.setOnClickListener {
            val dataHora = dataHoraInput.text.toString().trim()
            if (!TextUtils.isEmpty(dataHora)) {
                verificarDataHora(dataHora)
            } else {
                resultTextView.text = "Por favor, insira uma data."
            }
        }

        verTodosButton.setOnClickListener {
            val intent = Intent(this, ListarTodosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            updateDateInput()
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun updateDateInput() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
        dataHoraInput.setText(dateFormat.format(selectedDate.time))
    }

    private fun verificarDataHora(dataHora: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")

        try {
            val date = dateFormat.parse(dataHora)
            if (date != null) {
                // Define o intervalo de busca no UTC-3
                val calendarStart = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calendarEnd = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                // Consulta ao Firestore com ajuste de fuso horário
                db.collection("Temperatura")
                    .whereGreaterThanOrEqualTo("dataHora", Timestamp(calendarStart.time))
                    .whereLessThanOrEqualTo("dataHora", Timestamp(calendarEnd.time))
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val result = StringBuilder()
                            val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                            dateTimeFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo") // UTC-3

                            for (document in querySnapshot.documents) {
                                val temperatura = document.getString("temperatura")
                                val umidade = document.getString("umidade")
                                val dataHora = document.getTimestamp("dataHora")?.toDate()

                                // Exibir data e hora no formato correto
                                val dataFormatada = if (dataHora != null) dateTimeFormat.format(dataHora) else "Data indisponível"

                                result.append(
                                    "Data: $dataFormatada\n" +
                                            "Temperatura: ${temperatura ?: "N/A"}°C\n" +
                                            "Umidade: ${umidade ?: "N/A"}%\n\n"
                                )
                            }
                            resultTextView.text = result.toString()
                        } else {
                            resultTextView.text = "Nenhum documento encontrado para a data fornecida."
                        }
                    }
                    .addOnFailureListener { e ->
                        resultTextView.text = "Erro ao consultar os dados: ${e.message}"
                    }
            }
        } catch (e: Exception) {
            resultTextView.text = "Formato de data inválido. Use DD-MM-AAAA."
        }
    }
}