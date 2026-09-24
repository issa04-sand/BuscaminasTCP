package co.icesi.buscaminas.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Cell;

/**
 * Aplicacion de consola que actua como cliente del servidor de Buscaminas.
 * Gestiona el menu interactivo, delega la comunicacion de red a
 * BuscaminasTCPClient y dibuja el tablero recibido.
 */
public class MainClient {

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;

        BuscaminasTCPClient client = new BuscaminasTCPClient();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Conectando al servidor de Buscaminas en " + host + ":" + port + "...");

        boolean salir = false;
        while (!salir) {
            printMenu();
            System.out.print("Seleccione una opcion: ");
            String opcion = scanner.nextLine().trim();

            try {
                switch (opcion) {
                    case "1": {
                        System.out.print("Filas (n): ");
                        String n = scanner.nextLine().trim();
                        System.out.print("Columnas (m): ");
                        String m = scanner.nextLine().trim();
                        System.out.print("Minas: ");
                        String minas = scanner.nextLine().trim();

                        Map<String, String> data = new HashMap<>();
                        data.put("n", n);
                        data.put("m", m);
                        data.put("minas", minas);
                        Response response = client.sendRequest(host, port, buildRequest("INIT_GAME", data));
                        handleResponse(response);
                        break;
                    }
                    case "2": {
                        int[] ij = pedirCoordenadas(scanner);
                        Map<String, String> data = new HashMap<>();
                        data.put("i", String.valueOf(ij[0]));
                        data.put("j", String.valueOf(ij[1]));
                        Response response = client.sendRequest(host, port, buildRequest("SELECT_CELL", data));
                        handleResponse(response);
                        handleGameEnd(client, host, port, response);
                        break;
                    }
                    case "3": {
                        int[] ij = pedirCoordenadas(scanner);
                        Map<String, String> data = new HashMap<>();
                        data.put("i", String.valueOf(ij[0]));
                        data.put("j", String.valueOf(ij[1]));
                        Response response = client.sendRequest(host, port, buildRequest("MARK_CELL", data));
                        handleResponse(response);
                        break;
                    }
                    case "4": {
                        Response response = client.sendRequest(host, port, buildRequest("GET_BOARD", new HashMap<>()));
                        handleResponse(response);
                        break;
                    }
                    case "5": {
                        Response response = client.sendRequest(host, port, buildRequest("SOW_ALL", new HashMap<>()));
                        System.out.println("\nTe rendiste. Este era el tablero completo:");
                        handleResponse(response);
                        break;
                    }
                    case "6": {
                        salir = true;
                        System.out.println("Cerrando cliente. Hasta luego!");
                        break;
                    }
                    default:
                        System.out.println("Opcion no valida, intenta de nuevo.");
                }
            } catch (IOException e) {
                System.out.println("No se pudo contactar al servidor (" + host + ":" + port + "): " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida, se esperaba un numero.");
            }
        }
        scanner.close();
    }

    private static int[] pedirCoordenadas(Scanner scanner) {
        System.out.print("Fila (i): ");
        int i = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Columna (j): ");
        int j = Integer.parseInt(scanner.nextLine().trim());
        return new int[]{i, j};
    }

    private static void printMenu() {
        System.out.println("      BUSCAMINAS DISTRIBUIDO - CLIENTE TCP");
        System.out.println("=============================================");
        System.out.println("[1] Iniciar nueva partida (Filas, Columnas, Minas)");
        System.out.println("[2] Destapar celda (Fila, Columna)");
        System.out.println("[3] Marcar/Desmarcar bandera (Fila, Columna)");
        System.out.println("[4] Consultar estado actual del tablero");
        System.out.println("[5] Rendirse y revelar tablero completo");
        System.out.println("[6] Salir");
    }

    private static Request buildRequest(String action, Map<String, String> data) {
        Request request = new Request();
        request.action = action;
        request.data = data;
        return request;
    }

    private static void handleResponse(Response response) {
        if (response == null) {
            System.out.println("El servidor no respondio.");
            return;
        }
        if ("ERROR".equals(response.status)) {
            String message = response.data != null ? String.valueOf(response.data.get("message")) : "Error desconocido";
            System.out.println("Error del servidor: " + message);
        }
        if (response.data != null && response.data.get("board") != null) {
            Cell[][] board = GSON.fromJson(GSON.toJson(response.data.get("board")), Cell[][].class);
            BoardRenderer.print(board);
        }
    }


    private static void handleGameEnd(BuscaminasTCPClient client, String host, int port, Response response) throws IOException {
        if (response == null || response.data == null) return;

        Object gameEndObj = response.data.get("gameEnd");
        Object winObj = response.data.get("win");
        boolean gameEnd = Boolean.TRUE.equals(gameEndObj);
        boolean win = Boolean.TRUE.equals(winObj);

        if (!gameEnd) return;

        if (win) {
            System.out.println();
            System.out.println("   GANASTE! Felicitaciones, campeon!");
          
        } else {
            System.out.println();
            System.out.println("   BOOM! Pisaste una mina. Perdiste.");
          
            // Revelamos el tablero completo pidiendole al servidor SOW_ALL
            Response revealed = client.sendRequest(host, port, buildRequest("SOW_ALL", new HashMap<>()));
            handleResponse(revealed);
        }
    }
}
