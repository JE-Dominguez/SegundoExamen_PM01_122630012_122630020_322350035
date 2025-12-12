package com.example.segundo_examen_pm01.Configuraciones;

public class RestApiMethods {

    // URL base de la API
    public static final String BASE_URL = "https://api-m41o.onrender.com/";

    // Endpoints de la API
    public static final String ENDPOINT_POST = BASE_URL + "PostPersons.php";     // Crear persona
    public static final String ENDPOINT_GET = BASE_URL + "GetPersons.php";       // Obtener personas
    public static final String ENDPOINT_UPDATE = BASE_URL + "UpdatePersons.php"; // Actualizar persona
    public static final String ENDPOINT_DELETE = BASE_URL + "DeletePersons.php"; // Eliminar persona
    public static final String ENDPOINT_UPDATE_FOTO = BASE_URL + "UpdateFoto.php"; // Eliminar persona
}
