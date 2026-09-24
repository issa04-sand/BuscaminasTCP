package co.icesi.buscaminas.client;

import co.icesi.buscaminas.model.Cell;

/**
 * Dibuja el tablero de Buscaminas en consola con cabeceras de fila/columna
 * y colores ANSI, segun las reglas visuales de la guia:
 *  - celda marcada con bandera -> [ M ] en amarillo
 *  - celda oculta               -> [ . ]
 *  - mina descubierta           -> [ * ] en rojo
 *  - celda destapada sin mina   -> [ v ] (su valor numerico, espacio si es 0)
 */
public class BoardRenderer {

    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    public static void print(Cell[][] board) {
        if (board == null || board.length == 0) {
            System.out.println("No hay tablero para mostrar.");
            return;
        }

        System.out.println();
        StringBuilder header = new StringBuilder("     ");
        for (int j = 0; j < board[0].length; j++) {
            header.append(String.format("%-4d", j));
        }
        System.out.println(header);

        for (int i = 0; i < board.length; i++) {
            StringBuilder row = new StringBuilder(String.format("%-4d ", i));
            for (int j = 0; j < board[i].length; j++) {
                row.append(renderCell(board[i][j]));
            }
            System.out.println(row);
        }
        System.out.println();
    }

    private static String renderCell(Cell cell) {
        if (cell == null) return "[ ? ]";

        if (cell.isMarked()) {
            return "[ " + YELLOW + "M" + RESET + " ]";
        }
        if (cell.isHide() && !cell.isShowAll()) {
            return "[ . ]";
        }
        if (cell.isLandMine()) {
            return "[ " + RED + "*" + RESET + " ]";
        }
        int value = cell.getValue();
        String v = value == 0 ? " " : String.valueOf(value);
        return "[ " + v + " ]";
    }
}
