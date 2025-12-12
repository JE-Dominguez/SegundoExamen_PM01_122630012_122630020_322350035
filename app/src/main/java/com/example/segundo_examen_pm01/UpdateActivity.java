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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class UpdateActivity extends AppCompatActivity {

    private TextInputEditText txtNombres, txtApellidos, txtDireccion, txtTelefono;
    private ImageView imgFoto;
    private Button btnTomarFoto, btnGuardar;

    private Camara camara;
    private int idPersona = 0;
    private String fotoActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        inicializarVistas();
        inicializarCamara();
        cargarDatos();
        asignarEventos();
        configurarFabCancelar();
    }

    // Inicializa vistas
    private void inicializarVistas() {
        imgFoto = findViewById(R.id.imgFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);

        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtTelefono = findViewById(R.id.txtTelefono);
    }

    // Inicializa cámara
    private void inicializarCamara() {
        camara = new Camara(this, imgFoto);
    }

    // Configura FAB para cancelar y volver al listado
    private void configurarFabCancelar() {
        FloatingActionButton fab = findViewById(R.id.fabCancelar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Carga los datos recibidos en la UI
    private void cargarDatos() {
        Intent i = getIntent();
        idPersona = i.getIntExtra("id", 0);

        txtNombres.setText(i.getStringExtra("nombres"));
        txtApellidos.setText(i.getStringExtra("apellidos"));
        txtDireccion.setText(i.getStringExtra("direccion"));
        txtTelefono.setText(i.getStringExtra("telefono"));

        fotoActual = i.getStringExtra("foto");
        mostrarFoto(fotoActual);
    }

    // Muestra la foto si existe
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

    // Asigna eventos a botones
    private void asignarEventos() {
        btnTomarFoto.setOnClickListener(v -> validarPermisoCamara());
        btnGuardar.setOnClickListener(v -> actualizarPersona());
    }

    // Valida permiso de cámara antes de abrir
    private void validarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            camara.abrirCamara();
        }
    }

    // Valida campos obligatorios
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

    // Actualiza la persona en la API
    private void actualizarPersona() {
        if (!validarCampos()) return;

        Personas persona = crearPersonaDesdeFormulario();
        enviarSolicitudActualizar(persona);
    }

    // Crea objeto Personas desde formulario
    private Personas crearPersonaDesdeFormulario() {
        Personas persona = new Personas();
        persona.setNombres(txtNombres.getText().toString().trim());
        persona.setApellidos(txtApellidos.getText().toString().trim());
        persona.setDireccion(txtDireccion.getText().toString().trim());
        persona.setTelefono(txtTelefono.getText().toString().trim());

        // Mantener foto actual si no hay nueva
        String nuevaFoto = camara.obtenerImagenBase64();
        persona.setFoto((nuevaFoto == null || nuevaFoto.isEmpty()) ? fotoActual : nuevaFoto);

        return persona;
    }

    // Envía solicitud PUT para actualizar persona
    private void enviarSolicitudActualizar(Personas persona) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", idPersona);
            jsonObject.put("nombres", persona.getNombres());
            jsonObject.put("apellidos", persona.getApellidos());
            jsonObject.put("direccion", persona.getDireccion());
            jsonObject.put("telefono", persona.getTelefono());
            jsonObject.put("foto", persona.getFoto());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, RestApiMethods.ENDPOINT_UPDATE,
                    jsonObject, response -> {
                String mensaje = response.optString("message", "Sin mensaje");
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

                boolean exito = response.optBoolean("issuccess", false);
                if (exito) {
                    Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }, error -> {
                Toast.makeText(this, "Error: " + (error.getMessage() != null ? error.getMessage() : ""), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            });

            requestQueue.add(request);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error preparando solicitud", Toast.LENGTH_LONG).show();
        }
    }

    // Resultado de cámara
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
