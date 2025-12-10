package com.example.segundo_examen_pm01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.segundo_examen_pm01.Configuraciones.RestApiMethods;
import com.example.segundo_examen_pm01.Modelos.Personas;
import com.google.android.material.textfield.TextInputEditText;
import com.example.segundo_examen_pm01.Configuraciones.Camara;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    // Inicializa la clase Camara
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Personas persona = new Personas();

        persona.setNombres(txtNombres.getText().toString());
        persona.setApellidos(txtApellidos.getText().toString());
        persona.setDireccion(txtDireccion.getText().toString());
        persona.setTelefono(txtTelefono.getText().toString());
        persona.setFoto(camara.obtenerImagenBase64());

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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, error -> {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
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
