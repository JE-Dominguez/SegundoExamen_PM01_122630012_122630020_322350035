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

        FloatingActionButton fab = findViewById(R.id.fabCancelar);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        inicializarVistas();
        inicializarCamara();
        cargarDatos();
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

    private void cargarDatos() {
        Intent i = getIntent();
        idPersona = i.getIntExtra("id", 0);

        Toast.makeText(this, "ID recibido: " + idPersona, Toast.LENGTH_SHORT).show();

        txtNombres.setText(i.getStringExtra("nombres"));
        txtApellidos.setText(i.getStringExtra("apellidos"));
        txtDireccion.setText(i.getStringExtra("direccion"));
        txtTelefono.setText(i.getStringExtra("telefono"));

        fotoActual = i.getStringExtra("foto");

        if (fotoActual != null && !fotoActual.isEmpty()) {
            byte[] decoded = Base64.decode(fotoActual, Base64.DEFAULT);
            imgFoto.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
        }
    }


    private void asignarEventos() {
        btnTomarFoto.setOnClickListener(v -> validarPermisoCamara());
        btnGuardar.setOnClickListener(v -> actualizarPersona());
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

    private void actualizarPersona() {
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

        // Mantener foto actual si no hay nueva
        String nuevaFoto = camara.obtenerImagenBase64();
        persona.setFoto((nuevaFoto == null || nuevaFoto.isEmpty()) ? fotoActual : nuevaFoto);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", idPersona);
            jsonObject.put("nombres", persona.getNombres());
            jsonObject.put("apellidos", persona.getApellidos());
            jsonObject.put("direccion", persona.getDireccion());
            jsonObject.put("telefono", persona.getTelefono());
            jsonObject.put("foto", persona.getFoto());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, RestApiMethods.ENDPOINT_UPDATE,
                    jsonObject, response -> {
                try {
                    String mensaje = response.getString("message");
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();

                    boolean exito = response.optBoolean("issuccess", false);
                    if (exito) {
                        Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
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
