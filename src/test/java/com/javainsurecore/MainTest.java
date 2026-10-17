package com.javainsurecore;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba las validaciones de entrada del menú mediante una sesión de consola simulada. */
class MainTest {
    @Test
    void retriesInvalidMenuAndPolicyInputsUntilValidValuesAreProvided() throws Exception {
        String input = String.join("\n",
                "x",
                "2", "9", "1", "", "LIFE-TEST", "", "QA Client",
                "abc", "-1", "30", "not-money", "0", "10000",
                "3", "", "LIFE-TEST",
                "2", "2", "CAR-TEST", "Car QA", "40", "10000", "abc", "1800", "2020",
                "2", "3", "HOME-TEST", "Home QA", "50", "20000", "maybe", "s",
                "4", "0", "");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOut);

            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("Opción no válida."));
        assertTrue(console.contains("Tipo no válido. Seleccione 1, 2 o 3."));
        assertTrue(console.contains("El valor no puede estar vacío."));
        assertTrue(console.contains("Ingrese un número entero igual o mayor que 0."));
        assertTrue(console.contains("Ingrese un número entero igual o mayor que 1886."));
        assertTrue(console.contains("Ingrese un monto válido mayor que cero."));
        assertTrue(console.contains("Respuesta no válida. Ingrese s o n."));
        assertTrue(console.contains("Póliza agregada. Prima anual: 500.00 USD"));
        assertTrue(console.contains("Póliza agregada. Prima anual: 1000.00 USD"));
        assertTrue(console.contains("Póliza agregada. Prima anual: 800.00 USD"));
        assertTrue(console.contains("cliente=QA Client (LIFE-TEST)"));
        assertTrue(console.contains("Top 3 pólizas más costosas:"));
        assertTrue(console.contains("Hasta pronto."));
    }
}
