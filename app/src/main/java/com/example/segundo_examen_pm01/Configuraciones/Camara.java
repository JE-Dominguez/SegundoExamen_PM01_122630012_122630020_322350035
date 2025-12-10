package com.example.segundo_examen_pm01.Configuraciones;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class Camara {

    public static final int CODIGO_CAMARA = 101;

    private Activity actividad;
    private ImageView imageView;
    private String imagenBase64 = "";

    public Camara(Activity actividad, ImageView imageView) {
        this.actividad = actividad;
        this.imageView = imageView;
    }

    // Abre la cámara
    public void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        actividad.startActivityForResult(intent, CODIGO_CAMARA);
    }

    // Procesa el resultado de la cámara
    public void procesarResultado(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODIGO_CAMARA && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            imagenBase64 = convertirABase64(bitmap);
        }
    }

    // Convierte Bitmap a Base64
    private String convertirABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Devuelve la imagen en Base64
    public String obtenerImagenBase64() {
        return imagenBase64;
    }
}
