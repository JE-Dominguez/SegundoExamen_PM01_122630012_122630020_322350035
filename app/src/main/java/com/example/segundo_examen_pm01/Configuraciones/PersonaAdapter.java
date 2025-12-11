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
import com.example.segundo_examen_pm01.R;
import com.example.segundo_examen_pm01.UpdateActivity;

import org.json.JSONObject;

import java.util.List;

public class PersonaAdapter extends BaseAdapter {

    private Context context;
    private List<Personas> listaPersonas;

    private TextView tvNoPersonas;

    public PersonaAdapter(Context context, List<Personas> listaPersonas, TextView tvNoPersonas) {
        this.context = context;
        this.listaPersonas = listaPersonas;
        this.tvNoPersonas = tvNoPersonas;
    }

    @Override
    public int getCount() {
        return listaPersonas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaPersonas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_persona, parent, false);
        }

        Personas persona = listaPersonas.get(position);

        ImageView imgFoto = convertView.findViewById(R.id.imgFotoItem);
        TextView txtNombres = convertView.findViewById(R.id.txtNombresItem);
        TextView txtDireccion = convertView.findViewById(R.id.txtDireccionItem);
        TextView txtTelefono = convertView.findViewById(R.id.txtTelefonoItem);
        ImageView btnEditar = convertView.findViewById(R.id.btnEditar);
        ImageView btnEliminar = convertView.findViewById(R.id.btnEliminar);

        txtNombres.setText(persona.getNombres() + " " + persona.getApellidos());
        txtDireccion.setText(persona.getDireccion());
        txtTelefono.setText(persona.getTelefono());

        if (persona.getFoto() != null && !persona.getFoto().isEmpty()) {
            byte[] decodedString = Base64.decode(persona.getFoto(), Base64.DEFAULT);
            imgFoto.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }

        btnEditar.setOnClickListener(v -> editarPersona(persona));
        btnEliminar.setOnClickListener(v -> eliminarPersona(persona));

        return convertView;
    }

    private void editarPersona(Personas persona) {
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.putExtra("id", persona.getId());
        intent.putExtra("nombres", persona.getNombres());
        intent.putExtra("apellidos", persona.getApellidos());
        intent.putExtra("direccion", persona.getDireccion());
        intent.putExtra("telefono", persona.getTelefono());
        intent.putExtra("foto", persona.getFoto());
        context.startActivity(intent);

    }
    private void eliminarPersona(Personas persona) {
        if (persona.getId() <= 0) {
            Toast.makeText(context, "Error: ID de persona no válido.", Toast.LENGTH_LONG).show();
            return;
        }

        // Confirmación antes de eliminar
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Eliminar Persona")
                .setMessage("¿Desea eliminar a " + persona.getNombres() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", persona.getId());

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                                RestApiMethods.ENDPOINT_DELETE, // POST temporal si tu API no acepta DELETE con body
                                jsonObject,
                                response -> {
                                    try {
                                        boolean exito = response.optBoolean("issuccess", false);
                                        String mensaje = response.optString("message", "Sin mensaje");
                                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();

                                        if (exito) {
                                            // Eliminar del ArrayList y refrescar el ListView
                                            listaPersonas.remove(persona);
                                            notifyDataSetChanged();

                                            // Mostrar u ocultar listView / TextView según corresponda
                                            if (listaPersonas.isEmpty()) {
                                                tvNoPersonas.setVisibility(View.VISIBLE);
                                            } else {
                                                tvNoPersonas.setVisibility(View.GONE);
                                            }
                                        }

                                    } catch (Exception ex) {
                                        Toast.makeText(context, "Error procesando respuesta.", Toast.LENGTH_LONG).show();
                                        ex.printStackTrace();
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

                })
                .setNegativeButton("No", null)
                .show();
    }

}
