package com.example.segundo_examen_pm01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class CreateActivity extends AppCompatActivity {

    private TextInputEditText txtNombres, txtApellidos, txtDireccion, txtTelefono;
    private ImageView imgFoto;
    private Button btnTomarFoto, btnGuardar;
    private Camara camara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        inicializarVistas();
        inicializarCamara();
        asignarEventos();
        configurarFabCancelar();
    }

    // Inicializa vistas de layout
    private void inicializarVistas() {
        imgFoto = findViewById(R.id.imgFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);

        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtTelefono = findViewById(R.id.txtTelefono);
    }

    // Inicializa la cámara
    private void inicializarCamara() {
        camara = new Camara(this, imgFoto);
    }

    // Asigna eventos a botones
    private void asignarEventos() {
        btnTomarFoto.setOnClickListener(v -> validarPermisoCamara());
        btnGuardar.setOnClickListener(v -> guardarPersona());
    }

    // Configura el FAB para cancelar y volver al listado
    private void configurarFabCancelar() {
        FloatingActionButton fab = findViewById(R.id.fabCancelar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(CreateActivity.this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Valida permiso de cámara antes de abrir
    private void validarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            camara.abrirCamara();
        }
    }

    // Guarda la persona en la API
    private void guardarPersona() {
        if (!validarCampos()) return;

        Personas persona = crearPersonaDesdeFormulario();

        // JSON para la solicitud
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nombres", persona.getNombres());
            jsonObject.put("apellidos", persona.getApellidos());
            jsonObject.put("direccion", persona.getDireccion());
            jsonObject.put("telefono", persona.getTelefono());
            jsonObject.put("foto", persona.getFoto());

            enviarSolicitudCrear(jsonObject);

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error preparando datos", Toast.LENGTH_LONG).show();
        }
    }

    // Valida que los campos obligatorios no estén vacíos
    private boolean validarCampos() {
        if (txtNombres.getText().toString().trim().isEmpty()) {
            txtNombres.setError("Este campo es obligatorio");
            txtNombres.requestFocus();
            return false;
        }
        if (txtApellidos.getText().toString().trim().isEmpty()) {
            txtApellidos.setError("Este campo es obligatorio");
            txtApellidos.requestFocus();
            return false;
        }
        if (txtDireccion.getText().toString().trim().isEmpty()) {
            txtDireccion.setError("Este campo es obligatorio");
            txtDireccion.requestFocus();
            return false;
        }
        if (txtTelefono.getText().toString().trim().isEmpty()) {
            txtTelefono.setError("Este campo es obligatorio");
            txtTelefono.requestFocus();
            return false;
        }
        return true;
    }

    // Crea objeto Personas desde los campos del formulario
    private Personas crearPersonaDesdeFormulario() {
        Personas persona = new Personas();
        persona.setNombres(txtNombres.getText().toString().trim());
        persona.setApellidos(txtApellidos.getText().toString().trim());
        persona.setDireccion(txtDireccion.getText().toString().trim());
        persona.setTelefono(txtTelefono.getText().toString().trim());

        String fotoBase64 = camara.obtenerImagenBase64();
        persona.setFoto(fotoBase64 != null ? fotoBase64 : "");

        return persona;
    }

    // Envía solicitud POST para crear persona
    private void enviarSolicitudCrear(JSONObject jsonObject) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RestApiMethods.ENDPOINT_POST,
                jsonObject, response -> {
            try {
                String mensaje = response.optString("message", "Sin mensaje");
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

                boolean exito = response.optBoolean("issuccess", false);
                if (exito) {
                    Intent intent = new Intent(CreateActivity.this, ListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, "Error al guardar: " + (error.getMessage() != null ? error.getMessage() : ""), Toast.LENGTH_LONG).show();
            error.printStackTrace();
        });

        requestQueue.add(request);
    }

    // Recibe resultado de la cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        camara.procesarResultado(requestCode, resultCode, data);
    }

    // Resultado de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            camara.abrirCamara();
        }
    }
}
