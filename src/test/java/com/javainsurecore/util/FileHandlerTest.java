package com.javainsurecore.util;

import com.javainsurecore.exception.InvalidPolicyDataException;
import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.LifeInsurance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba la lectura, filtrado y validación de filas CSV con archivos temporales. */
class FileHandlerTest {
    /** Directorio aislado que JUnit crea y elimina para cada prueba. */
    @TempDir
    Path temporaryDirectory;

    /** Lector CSV que se ejercita en los casos de prueba. */
    private final FileHandler fileHandler = new FileHandler();

    /** Comprueba que cada formato CSV válido produzca el tipo de póliza esperado. */
    @Test
    void loadsPoliciesFromTemporaryCsvFile() throws IOException, InvalidPolicyDataException {
        Path csv = temporaryDirectory.resolve("policies.csv");
        Files.writeString(csv, """
                LIFE,C-1,100000,Ana,61
                CAR,C-2,20000,Luis,2014,40
                HOME,C-3,300000,Elena,true,35
                """);

        List<?> policies = fileHandler.loadPolicies(csv);

        assertEquals(3, policies.size());
        assertInstanceOf(LifeInsurance.class, policies.get(0));
        assertInstanceOf(CarInsurance.class, policies.get(1));
        assertInstanceOf(HomeInsurance.class, policies.get(2));
    }

    /** Comprueba que se ignoren las filas vacías y las líneas de comentario. */
    @Test
    void skipsBlankLinesAndCommentLines() throws IOException, InvalidPolicyDataException {
        Path csv = temporaryDirectory.resolve("policies.csv");
        Files.writeString(csv, """
                # datos de prueba

                LIFE,C-1,100000,Ana,61
                """);

        assertEquals(1, fileHandler.loadPolicies(csv).size());
    }

    /** Comprueba que un importe no numérico produzca un error de datos explícito. */
    @Test
    void rejectsMalformedCsvWithCheckedException() {
        assertThrows(InvalidPolicyDataException.class,
                () -> fileHandler.parseLine("LIFE,C-1,no-es-monto,Ana,61"));
    }

    /** Comprueba que se rechacen tipos de póliza que el sistema no reconoce. */
    @Test
    void rejectsUnknownPolicyType() {
        InvalidPolicyDataException exception = assertThrows(InvalidPolicyDataException.class,
                () -> fileHandler.parseLine("BOAT,C-1,10000,Ana,61"));
        assertTrue(exception.getMessage().contains("Datos inválidos"));
    }
}
