package com.example.calendario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Main2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main2) // Enlazar con el archivo XML main2.xml

        // Encontrar el botón "Volver"
        val btnVolver = findViewById<Button>(R.id.btnVolverAtras)

        // Configurar el botón para volver a MainActivity
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza la actividad actual para que no se quede en la pila
        }
    }
}
