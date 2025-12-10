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

        FloatingActionButton fab = findViewById(R.id.fabCancelar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(CreateActivity.this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        inicializarVistas();
        inicializarCamara();
        asignarEventos();
    }

    private void inicializarVistas() {
        imgFoto = findViewById(R.id.imgFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);

        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtTelefono = findViewById(R.id.txtTelefono);
    }

    private void inicializarCamara() {
        camara = new Camara(this, imgFoto);
    }

    private void asignarEventos() {
        btnTomarFoto.setOnClickListener(v -> validarPermisoCamara());
        btnGuardar.setOnClickListener(v -> guardarPersona());
    }
    private void validarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 200);
        } else {
            camara.abrirCamara();
        }
    }


    private void guardarPersona() {
        // Validar campos obligatorios
        if (txtNombres.getText().toString().trim().isEmpty()) {
            txtNombres.setError("Este campo es obligatorio");
            txtNombres.requestFocus();
            return;
        }
        if (txtApellidos.getText().toString().trim().isEmpty()) {
            txtApellidos.setError("Este campo es obligatorio");
            txtApellidos.requestFocus();
            return;
        }
        if (txtDireccion.getText().toString().trim().isEmpty()) {
            txtDireccion.setError("Este campo es obligatorio");
            txtDireccion.requestFocus();
            return;
        }
        if (txtTelefono.getText().toString().trim().isEmpty()) {
            txtTelefono.setError("Este campo es obligatorio");
            txtTelefono.requestFocus();
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Personas persona = new Personas();

        persona.setNombres(txtNombres.getText().toString());
        persona.setApellidos(txtApellidos.getText().toString());
        persona.setDireccion(txtDireccion.getText().toString());
        persona.setTelefono(txtTelefono.getText().toString());

        // Foto opcional
        String fotoBase64 = camara.obtenerImagenBase64();
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            persona.setFoto(fotoBase64);
        } else {
            persona.setFoto(""); // O null según tu API
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nombres", persona.getNombres());
            jsonObject.put("apellidos", persona.getApellidos());
            jsonObject.put("direccion", persona.getDireccion());
            jsonObject.put("telefono", persona.getTelefono());
            jsonObject.put("foto", persona.getFoto());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RestApiMethods.ENDPOINT_POST,
                    jsonObject, response -> {
                try {
                    String mensaje = response.getString("message");
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();

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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            });

            requestQueue.add(request);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        camara.procesarResultado(requestCode, resultCode, data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camara.abrirCamara();
            }
        }
    }

}
