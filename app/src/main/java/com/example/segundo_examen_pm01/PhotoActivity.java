package com.example.segundo_examen_pm01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.segundo_examen_pm01.Configuraciones.Camara;
import com.example.segundo_examen_pm01.Configuraciones.RestApiMethods;
import com.example.segundo_examen_pm01.Modelos.Personas;

import org.json.JSONObject;

public class PhotoActivity extends AppCompatActivity {

    private Button btnEliminar, btnCambiar, btnAtras, btnGuardar;
    private ImageView imgFoto;
    private Camara camara;

    private int idPersona = 0;
    private String fotoActual = "";
    private String fotoNueva = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        inicializarVistas();
        inicializarCamara();
        cargarDatos();
        asignarEventos();
    }

    // Inicializar vistas
    private void inicializarVistas() {
        imgFoto = findViewById(R.id.imgFoto);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCambiar = findViewById(R.id.btnCambiar);
        btnAtras = findViewById(R.id.btnAtras);
        btnGuardar = findViewById(R.id.Btnguardar);
        btnGuardar.setVisibility(Button.GONE); // oculto al inicio
    }

    // Inicializar objeto cámara
    private void inicializarCamara() {
        camara = new Camara(this, imgFoto);
    }

    // Cargar datos recibidos
    private void cargarDatos() {
        Intent i = getIntent();
        idPersona = i.getIntExtra("id", 0);
        fotoActual = i.getStringExtra("foto");
        mostrarFoto(fotoActual);
    }

    // Mostrar imagen en ImageView
    private void mostrarFoto(String fotoBase64) {
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(fotoBase64, Base64.DEFAULT);
                imgFoto.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
            } catch (Exception e) {
                imgFoto.setImageResource(R.drawable.ic_user);
                e.printStackTrace();
            }
        } else {
            imgFoto.setImageResource(R.drawable.ic_user);
        }
    }

    // Asignar eventos a botones
    private void asignarEventos() {
        btnCambiar.setOnClickListener(v -> validarPermisoCamara());
        btnGuardar.setOnClickListener(v -> guardarNuevaFoto());
        btnAtras.setOnClickListener(v -> volverListado());
        btnEliminar.setOnClickListener(v -> confirmarEliminarFoto());
    }

    // Validar permisos de cámara
    private void validarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            camara.abrirCamara();
        }
    }

    // Guardar foto nueva
    private void guardarNuevaFoto() {
        if (fotoNueva == null || fotoNueva.isEmpty()) {
            Toast.makeText(this, "No hay foto para guardar", Toast.LENGTH_SHORT).show();
            return;
        }
        actualizarFotoEnDB(fotoNueva);
        fotoActual = fotoNueva;
        fotoNueva = "";
        btnGuardar.setVisibility(Button.GONE);
    }

    // Confirmar eliminación de foto
    private void confirmarEliminarFoto() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar foto")
                .setMessage("¿Deseas eliminar la foto?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    fotoActual = "";
                    fotoNueva = "";
                    mostrarFoto("");
                    Personas persona = new Personas();
                    persona.setFoto(""); // foto vacía al eliminar
                    enviarSolicitudActualizar(persona);
                    Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Volver a ListActivity
    private void volverListado() {
        Intent intent = new Intent(PhotoActivity.this, ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Enviar solicitud actualizar foto
    private void enviarSolicitudActualizar(Personas persona) {
        if (persona.getFoto() == null) persona.setFoto(""); // Validar valor
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", idPersona);
            jsonObject.put("foto", persona.getFoto());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    RestApiMethods.ENDPOINT_UPDATE_FOTO,
                    jsonObject,
                    response -> Toast.makeText(this, response.optString("message", "Sin mensaje"), Toast.LENGTH_LONG).show(),
                    error -> {
                        Toast.makeText(this, "Error al actualizar foto", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
            );

            requestQueue.add(request);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error preparando solicitud", Toast.LENGTH_LONG).show();
        }
    }

    // Resultado cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        camara.procesarResultado(requestCode, resultCode, data);

        fotoNueva = camara.obtenerImagenBase64();
        if (fotoNueva != null && !fotoNueva.isEmpty()) {
            mostrarFoto(fotoNueva);
            btnGuardar.setVisibility(Button.VISIBLE);
        }
    }

    // Resultado permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            camara.abrirCamara();
        }
    }

    // Actualizar foto en DB
    private void actualizarFotoEnDB(String foto) {
        Personas persona = new Personas();
        persona.setFoto(foto != null ? foto : "");
        enviarSolicitudActualizar(persona);
    }
}
