package com.example.segundo_examen_pm01.Configuraciones;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.segundo_examen_pm01.Modelos.Personas;
import com.example.segundo_examen_pm01.PhotoActivity;
import com.example.segundo_examen_pm01.R;
import com.example.segundo_examen_pm01.UpdateActivity;

import org.json.JSONObject;

import java.util.List;

public class PersonaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Personas> listaPersonas;
    private final TextView tvNoPersonas;

    // Constructor del Adapter
    public PersonaAdapter(Context context, List<Personas> listaPersonas, TextView tvNoPersonas) {
        this.context = context;
        this.listaPersonas = listaPersonas;
        this.tvNoPersonas = tvNoPersonas;
    }

    @Override
    public int getCount() {
        return listaPersonas != null ? listaPersonas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return listaPersonas != null ? listaPersonas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Crea cada item de la lista
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_persona, parent, false);
        }

        Personas persona = listaPersonas.get(position);
        if (persona == null) return convertView;

        ImageView imgFoto = convertView.findViewById(R.id.imgFotoItem);
        TextView txtNombres = convertView.findViewById(R.id.txtNombresItem);
        TextView txtDireccion = convertView.findViewById(R.id.txtDireccionItem);
        TextView txtTelefono = convertView.findViewById(R.id.txtTelefonoItem);
        ImageView btnEditar = convertView.findViewById(R.id.btnEditar);
        ImageView btnEliminar = convertView.findViewById(R.id.btnEliminar);

        // Validaciones y asignación de valores
        txtNombres.setText((persona.getNombres() != null ? persona.getNombres() : "")
                + " " + (persona.getApellidos() != null ? persona.getApellidos() : ""));
        txtDireccion.setText(persona.getDireccion() != null ? persona.getDireccion() : "");
        txtTelefono.setText(persona.getTelefono() != null ? persona.getTelefono() : "");

        // Mostrar foto si existe
        if (persona.getFoto() != null && !persona.getFoto().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(persona.getFoto(), Base64.DEFAULT);
                imgFoto.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            } catch (Exception e) {
                imgFoto.setImageResource(R.drawable.ic_user);
                e.printStackTrace();
            }
        } else {
            imgFoto.setImageResource(R.drawable.ic_user);
        }

        // Botones de acción
        btnEditar.setOnClickListener(v -> editarPersona(persona));
        btnEliminar.setOnClickListener(v -> eliminarPersona(persona));
        imgFoto.setOnClickListener(v -> mostrarFoto(persona));

        return convertView;
    }

    // Abre la actividad para actualizar persona
    private void editarPersona(Personas persona) {
        if (persona == null) return;
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra("id", persona.getId());
        intent.putExtra("nombres", persona.getNombres());
        intent.putExtra("apellidos", persona.getApellidos());
        intent.putExtra("direccion", persona.getDireccion());
        intent.putExtra("telefono", persona.getTelefono());
        intent.putExtra("foto", persona.getFoto());
        context.startActivity(intent);
    }

    // Elimina persona con confirmación
    private void eliminarPersona(Personas persona) {
        if (persona == null || persona.getId() <= 0) {
            Toast.makeText(context, "Error: ID de persona no válido.", Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Eliminar Persona")
                .setMessage("¿Desea eliminar a " + persona.getNombres() + "?")
                .setPositiveButton("Sí", (dialog, which) -> enviarSolicitudEliminar(persona))
                .setNegativeButton("No", null)
                .show();
    }

    // Envía solicitud HTTP para eliminar persona
    private void enviarSolicitudEliminar(Personas persona) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", persona.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    RestApiMethods.ENDPOINT_DELETE,
                    jsonObject,
                    response -> {
                        boolean exito = response.optBoolean("issuccess", false);
                        String mensaje = response.optString("message", "Sin mensaje");
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();

                        if (exito) {
                            listaPersonas.remove(persona);
                            notifyDataSetChanged();
                            tvNoPersonas.setVisibility(listaPersonas.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    },
                    error -> {
                        String errorMsg = "Error al eliminar: fallo de conexión.";
                        if (error != null && error.getMessage() != null) {
                            errorMsg += " (" + error.getMessage() + ")";
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(request);

        } catch (Exception ex) {
            Toast.makeText(context, "Error preparando solicitud.", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
    private void mostrarFoto(Personas persona) {
        if (persona.getFoto() != null && !persona.getFoto().isEmpty()) {
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("id", persona.getId());
            intent.putExtra("foto", persona.getFoto());
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No hay foto disponible", Toast.LENGTH_SHORT).show();
        }
    }
}
