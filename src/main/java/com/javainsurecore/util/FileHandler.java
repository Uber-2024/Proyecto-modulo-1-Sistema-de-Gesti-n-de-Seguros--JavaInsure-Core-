package com.javainsurecore.util;

import com.javainsurecore.exception.BusinessRuleException;
import com.javainsurecore.exception.InvalidPolicyDataException;
import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.Client;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.Insurance;
import com.javainsurecore.model.LifeInsurance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Lee archivos CSV y convierte sus filas en pólizas validadas. */
public class FileHandler {
    private static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    /**
     * Carga todas las pólizas de un CSV, omitiendo líneas vacías y comentarios.
     *
     * @param csvPath ruta del archivo que se leerá
     * @return pólizas interpretadas en el orden del archivo
     * @throws InvalidPolicyDataException si el archivo no se puede leer o contiene datos inválidos
     */
    public List<Insurance> loadPolicies(Path csvPath) throws InvalidPolicyDataException {
        try (Stream<String> lines = Files.lines(csvPath)) {
            return lines.filter(line -> !line.isBlank())
                    .filter(line -> !line.stripLeading().startsWith("#"))
                    .map(line -> {
                        try {
                            return parseLine(line);
                        } catch (InvalidPolicyDataException exception) {
                            throw new InvalidPolicyDataRuntimeException(exception);
                        }
                    })
                    .toList();
        } catch (InvalidPolicyDataRuntimeException exception) {
            InvalidPolicyDataException cause = exception.getCause();
            LOGGER.error("Failed to read or parse policy CSV {}", csvPath, cause);
            throw cause;
        } catch (java.io.IOException exception) {
            LOGGER.error("Failed to read policy CSV {}", csvPath, exception);
            throw new InvalidPolicyDataException("No se pudo leer el archivo CSV: " + csvPath, exception);
        }
    }

    /**
     * Interpreta una fila según el tipo y el formato CSV documentados.
     *
     * @param line fila de datos que se analizará
     * @return póliza construida a partir de la fila
     * @throws InvalidPolicyDataException si el tipo, las columnas o sus valores son inválidos
     */
    public Insurance parseLine(String line) throws InvalidPolicyDataException {
        String[] fields = line.split(",", -1);
        try {
            if (fields.length < 5) {
                throw new IllegalArgumentException("La fila no contiene todas las columnas requeridas.");
            }
            String type = fields[0].strip().toUpperCase();
            String clientId = fields[1].strip();
            BigDecimal insuredAmount = new BigDecimal(fields[2].strip());
            String clientName = fields[3].strip();
            Client client;

            return switch (type) {
                case "LIFE" -> {
                    requireLength(fields, 5);
                    client = new Client(clientId, clientName, Integer.parseInt(fields[4].strip()));
                    yield new LifeInsurance(client, insuredAmount);
                }
                case "CAR" -> {
                    requireLength(fields, 6);
                    client = new Client(clientId, clientName, Integer.parseInt(fields[5].strip()));
                    yield new CarInsurance(client, insuredAmount, Integer.parseInt(fields[4].strip()));
                }
                case "HOME" -> {
                    requireLength(fields, 6);
                    client = new Client(clientId, clientName, Integer.parseInt(fields[5].strip()));
                    yield new HomeInsurance(client, insuredAmount, parseBoolean(fields[4].strip()));
                }
                default -> throw new IllegalArgumentException("Tipo de póliza desconocido: " + type);
            };
        } catch (IllegalArgumentException | BusinessRuleException exception) {
            throw new InvalidPolicyDataException("Datos inválidos en la fila: " + line, exception);
        }
    }

    /** Comprueba que la fila tenga exactamente la cantidad de columnas esperada. */
    private void requireLength(String[] fields, int expectedLength) {
        if (fields.length != expectedLength) {
            throw new IllegalArgumentException("Cantidad incorrecta de columnas.");
        }
    }

    /** Convierte el campo de riesgo; solo acepta los textos true y false. */
    private boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("El indicador de zona de riesgo debe ser true o false.");
    }

    /** Adapta la excepción comprobada al procesamiento interno de la secuencia de filas. */
    private static final class InvalidPolicyDataRuntimeException extends RuntimeException {
        /** Conserva la excepción comprobada original como causa. */
        private InvalidPolicyDataRuntimeException(InvalidPolicyDataException cause) {
            super(cause);
        }

        /** Devuelve la causa con su tipo concreto de error de datos. */
        @Override
        public synchronized InvalidPolicyDataException getCause() {
            return (InvalidPolicyDataException) super.getCause();
        }
    }
}
