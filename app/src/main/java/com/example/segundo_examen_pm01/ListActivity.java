package com.example.segundo_examen_pm01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.segundo_examen_pm01.Configuraciones.PersonaAdapter;
import com.example.segundo_examen_pm01.Configuraciones.RestApiMethods;
import com.example.segundo_examen_pm01.Modelos.Personas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView listViewPersonas;
    private TextView tvNoPersonas;
    private final List<Personas> listaPersonas = new ArrayList<>();
    private PersonaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        inicializarVistas();
        configurarAdapter();
        configurarFabAgregar();
        cargarPersonas();
    }

    // Inicializa las vistas
    private void inicializarVistas() {
        listViewPersonas = findViewById(R.id.listViewPersonas);
        tvNoPersonas = findViewById(R.id.tvNoPersonas);
    }

    // Configura el adapter del ListView
    private void configurarAdapter() {
        adapter = new PersonaAdapter(this, listaPersonas, tvNoPersonas);
        listViewPersonas.setAdapter(adapter);
    }

    // Configura el FAB para agregar nueva persona
    private void configurarFabAgregar() {
        FloatingActionButton fab = findViewById(R.id.fabAgregar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateActivity.class);
            startActivity(intent);
        });
    }

    // Carga las personas desde la API
    private void cargarPersonas() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, RestApiMethods.ENDPOINT_GET, null,
                response -> {
                    listaPersonas.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Personas p = crearPersonaDesdeJson(obj);
                            if (p != null) listaPersonas.add(p);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    actualizarVistaSegunDatos();
                },
                error -> Toast.makeText(this, "Error cargando datos: " + (error.getMessage() != null ? error.getMessage() : ""), Toast.LENGTH_LONG).show()
        );

        queue.add(request);
    }

    // Crea un objeto Personas desde JSON
    private Personas crearPersonaDesdeJson(JSONObject obj) {
        try {
            Personas p = new Personas();
            p.setId(obj.optInt("id", 0));
            p.setNombres(obj.optString("nombres", ""));
            p.setApellidos(obj.optString("apellidos", ""));
            p.setDireccion(obj.optString("direccion", ""));
            p.setTelefono(obj.optString("telefono", ""));
            p.setFoto(obj.optString("foto", ""));
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Actualiza la visibilidad del ListView y mensaje
    private void actualizarVistaSegunDatos() {
        if (listaPersonas.isEmpty()) {
            listViewPersonas.setVisibility(View.GONE);
            tvNoPersonas.setVisibility(View.VISIBLE);
        } else {
            listViewPersonas.setVisibility(View.VISIBLE);
            tvNoPersonas.setVisibility(View.GONE);
        }
    }
}
