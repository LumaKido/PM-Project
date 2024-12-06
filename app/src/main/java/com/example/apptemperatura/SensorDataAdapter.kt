package com.example.apptemperatura

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SensorDataAdapter(private val sensorDataList: List<SensorData>) :
    RecyclerView.Adapter<SensorDataAdapter.SensorViewHolder>() {

    class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataHoraTextView: TextView = itemView.findViewById(R.id.dataHoraTextView)
        val temperaturaTextView: TextView = itemView.findViewById(R.id.temperaturaTextView)
        val umidadeTextView: TextView = itemView.findViewById(R.id.umidadeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_data, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensorData = sensorDataList[position]
        holder.dataHoraTextView.text = "Data: ${sensorData.dataHora}"
        holder.temperaturaTextView.text = "Temperatura: ${sensorData.temperatura}°C"
        holder.umidadeTextView.text = "Umidade: ${sensorData.umidade}%"
    }

    override fun getItemCount(): Int {
        return sensorDataList.size
    }
}
