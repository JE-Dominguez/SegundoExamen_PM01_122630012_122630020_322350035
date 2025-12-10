package com.example.segundo_examen_pm01.Configuraciones;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.segundo_examen_pm01.Modelos.Personas;
import com.example.segundo_examen_pm01.R;

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
    }

    private void eliminarPersona(Personas persona) {
    }

}
