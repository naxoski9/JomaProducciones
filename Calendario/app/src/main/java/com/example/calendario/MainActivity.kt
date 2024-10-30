package com.example.calendario

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var baseDatos: FirebaseFirestore
    private lateinit var etFecha: EditText
    private lateinit var etNombreEvento: EditText // EditText para el nombre del agendamiento
    private lateinit var btnSeleccionarFecha: Button
    private lateinit var btnGuardarFecha: Button
    private lateinit var btnConsultarStock: Button
    private lateinit var cuadrículaCalendario: GridLayout
    private lateinit var tvMes: TextView
    private lateinit var btnMesAnterior: Button
    private lateinit var btnMesSiguiente: Button
    private val eventosProgramados = mutableMapOf<String, String>() // Mapa para almacenar fecha y nombre del evento
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var mesActual: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var añoActual: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        baseDatos = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_main)

        etFecha = findViewById(R.id.etFecha)
        etNombreEvento = findViewById(R.id.etNombreEvento) // Inicializa el nuevo EditText
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        btnGuardarFecha = findViewById(R.id.btnGuardarFecha)
        btnConsultarStock = findViewById(R.id.btnConsultaStock)
        cuadrículaCalendario = findViewById(R.id.cuadriculaCalendario)
        tvMes = findViewById(R.id.tvMes)
        btnMesAnterior = findViewById(R.id.btnMesAnterior)
        btnMesSiguiente = findViewById(R.id.btnMesSiguiente)

        cargarEventosProgramados()

        btnConsultarStock.setOnClickListener {
            val intent = Intent(this, Main2::class.java)
            startActivity(intent)
        }

        btnSeleccionarFecha.setOnClickListener { mostrarSelectorFecha() }

        btnGuardarFecha.setOnClickListener {
            val fecha = etFecha.text.toString()
            val nombreEvento = etNombreEvento.text.toString() // Obtener el nombre del evento
            if (fecha.isNotEmpty() && nombreEvento.isNotEmpty()) {
                guardarFechaEnFirestore(fecha, nombreEvento) // Pasar el nombre del evento
            } else {
                if (fecha.isEmpty()) etFecha.error = "Por favor selecciona una fecha"
                if (nombreEvento.isEmpty()) etNombreEvento.error = "Por favor ingresa el nombre del agendamiento"
            }
        }

        btnMesAnterior.setOnClickListener {
            if (mesActual == 0) {
                mesActual = 11
                añoActual--
            } else {
                mesActual--
            }
            mostrarCalendario()
        }

        btnMesSiguiente.setOnClickListener {
            if (mesActual == 11) {
                mesActual = 0
                añoActual++
            } else {
                mesActual++
            }
            mostrarCalendario()
        }
    }

    private fun mostrarSelectorFecha() {
        val selectorFecha = DatePicker { día, mes, año -> fechaSeleccionada(día, mes, año) }
        selectorFecha.show(supportFragmentManager, "datePicker")
    }

    private fun fechaSeleccionada(día: Int, mes: Int, año: Int) {
        etFecha.setText("$día/${mes + 1}/$año")
    }

    private fun guardarFechaEnFirestore(fecha: String, nombreEvento: String) {
        val partesFecha = fecha.split("/")
        if (partesFecha.size == 3) {
            // Verificar si la fecha ya existe en el mapa de eventos programados
            if (eventosProgramados.containsKey(fecha)) {
                Toast.makeText(this, "Ya existe un evento para esta fecha.", Toast.LENGTH_SHORT).show()
                return // Salir del método si ya existe un evento
            }

            val datosEvento = hashMapOf(
                "nombre" to nombreEvento,
                "fecha" to fecha,
                "dia" to partesFecha[0],
                "mes" to partesFecha[1],
                "año" to partesFecha[2]
            )

            baseDatos.collection("Dias_Agendados")
                .add(datosEvento)
                .addOnSuccessListener {
                    eventosProgramados[fecha] = nombreEvento // Agregar la fecha y el nombre del evento al mapa
                    etFecha.setText("Fecha guardada correctamente")
                    etNombreEvento.setText("") // Limpiar el campo del nombre del evento
                    mostrarCalendario()
                }
                .addOnFailureListener { e -> etFecha.setText("Error al guardar: ${e.message}") }
        } else {
            etFecha.error = "Formato de fecha incorrecto"
        }
    }

    private fun cargarEventosProgramados() {
        baseDatos.collection("Dias_Agendados").get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val fechaStr = documento.getString("fecha")
                    val nombreEvento = documento.getString("nombre") // Obtener el nombre del evento
                    if (fechaStr != null && nombreEvento != null) {
                        eventosProgramados[fechaStr] = nombreEvento // Almacenar en el mapa
                    }
                }
                mostrarCalendario()
            }
            .addOnFailureListener { excepción ->
                Toast.makeText(this, "Error al cargar fechas: ${excepción.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarCalendario() {
        cuadrículaCalendario.removeAllViews()

        val calendario = Calendar.getInstance()
        calendario.set(añoActual, mesActual, 1)
        val maxDía = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)

        tvMes.text = "${obtenerNombreMes(mesActual + 1)} $añoActual"

        for (día in 1..maxDía) {
            val fecha = "$día/${mesActual + 1}/$añoActual"
            val vistaDía = TextView(this).apply {
                text = día.toString()
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(16, 16, 16, 16)
                textSize = 16f
                setTextColor(Color.BLACK)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    marginStart = 4
                    marginEnd = 4
                    topMargin = 4
                    bottomMargin = 4
                }

                // Cambiar color de fondo si hay un evento
                setBackgroundColor(if (eventosProgramados.containsKey(fecha)) Color.RED else Color.TRANSPARENT)

                // Mostrar el evento al hacer clic en el día
                setOnClickListener {
                    eventosProgramados[fecha]?.let { evento ->
                        Toast.makeText(this@MainActivity, "Evento: $evento", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(this@MainActivity, "No hay eventos para este día.", Toast.LENGTH_SHORT).show()
                }
            }
            cuadrículaCalendario.addView(vistaDía)
        }
    }

    private fun obtenerNombreMes(mes: Int): String {
        return when (mes) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
        }
    }
}
