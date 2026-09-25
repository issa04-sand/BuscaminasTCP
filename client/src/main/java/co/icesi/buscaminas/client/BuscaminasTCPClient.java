package co.icesi.buscaminas.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

/**
 * Emisor de peticiones TCP hacia el servidor de Buscaminas.
 *
 * El protocolo es de conexion corta (short-lived): cada llamada a sendRequest
 * abre un socket nuevo, serializa el Request a JSON terminado en salto de
 * linea, espera una unica linea de respuesta JSON y cierra el socket.
 */
public class BuscaminasTCPClient {

    private final Gson gson = new GsonBuilder().create();

    /**
     * Envia una peticion al servidor y espera su respuesta.
     *
     * @param host servidor destino (IP o nombre de host)
     * @param port puerto TCP del servidor (12345 por defecto en el proyecto)
     * @param request accion y datos a enviar
     * @return la respuesta deserializada, o null si el servidor cerro sin responder
     */
    public Response sendRequest(String host, int port, Request request) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // Serializar Request a JSON y enviar con salto de linea
            String jsonOut = gson.toJson(request);
            writer.write(jsonOut);
            writer.newLine();
            writer.flush();

            // Leer la respuesta delimitada por fin de linea
            String jsonIn = reader.readLine();
            return jsonIn == null ? null : gson.fromJson(jsonIn, Response.class);
        }
    }
}
