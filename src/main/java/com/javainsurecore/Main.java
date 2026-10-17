package com.javainsurecore;

import com.javainsurecore.exception.BusinessRuleException;
import com.javainsurecore.exception.InvalidPolicyDataException;
import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.Client;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.Insurance;
import com.javainsurecore.model.LifeInsurance;
import com.javainsurecore.service.InsurancePortfolio;
import com.javainsurecore.service.InsuranceQuoter;
import com.javainsurecore.service.PortfolioReport;
import com.javainsurecore.util.FileHandler;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Punto de entrada y coordinador de la interacción por consola de JavaInsure Core.
 */
public class Main {
    private static final String CURRENCY = "USD";
    private final Scanner scanner;
    private final InsurancePortfolio portfolio;
    private final InsuranceQuoter quoter;
    private final PortfolioReport report;

    /**
     * Crea la interfaz de consola con sus dependencias principales.
     *
     * @param scanner lector de las entradas del usuario
     * @param portfolio cartera que se administrará
     * @param quoter servicio para calcular las primas
     */
    private Main(Scanner scanner, InsurancePortfolio portfolio, InsuranceQuoter quoter) {
        this.scanner = scanner;
        this.portfolio = portfolio;
        this.quoter = quoter;
        this.report = new PortfolioReport(portfolio, quoter);
    }

    /**
     * Carga las pólizas iniciales e inicia el menú interactivo.
     *
     * @param args argumentos de línea de comandos, no utilizados
     */
    public static void main(String[] args) {
        InsurancePortfolio portfolio = new InsurancePortfolio();
        FileHandler fileHandler = new FileHandler();
        try {
            fileHandler.loadPolicies(Path.of("policies.csv")).forEach(portfolio::add);
        } catch (InvalidPolicyDataException exception) {
            System.out.println("No se pudo cargar policies.csv: " + exception.getMessage());
        }
        new Main(new Scanner(System.in), portfolio, new InsuranceQuoter()).run();
    }

    /**
     * Mantiene el menú activo hasta que el usuario selecciona la opción de salida.
     */
    private void run() {
        boolean running = true;
        while (running) {
            showMenu();
            try {
                String option = scanner.nextLine().strip();
                switch (option) {
                    case "1" -> listPolicies();
                    case "2" -> addPolicy();
                    case "3" -> searchPolicies();
                    case "4" -> showReports();
                    case "0" -> running = false;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (NoSuchElementException exception) {
                running = false;
            } catch (BusinessRuleException | IllegalArgumentException exception) {
                System.out.println("No se pudo completar la operación: " + exception.getMessage());
            }
        }
        System.out.println("Hasta pronto.");
    }

    /** Muestra las operaciones disponibles en la aplicación. */
    private void showMenu() {
        System.out.println("\n=== JavaInsure Core ===");
        System.out.println("1. Listar pólizas");
        System.out.println("2. Agregar póliza");
        System.out.println("3. Buscar por ID de cliente");
        System.out.println("4. Reportes gerenciales");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    /** Lista todas las pólizas registradas o informa si la cartera está vacía. */
    private void listPolicies() {
        List<Insurance> policies = portfolio.listAll();
        if (policies.isEmpty()) {
            System.out.println("No hay pólizas registradas.");
            return;
        }
        policies.stream().map(this::formatPolicy).forEach(System.out::println);
    }

    /**
     * Solicita los datos de una póliza, la valida y la incorpora a la cartera.
     */
    private void addPolicy() {
        System.out.println("Tipo de póliza: 1. Vida  2. Automóvil  3. Hogar");
        String type = readPolicyType();
        String clientId = readRequiredText("ID del cliente: ");
        String clientName = readRequiredText("Nombre del cliente: ");
        int age = readInt("Edad del cliente: ", 0);
        BigDecimal insuredAmount = readAmount("Monto asegurado (USD): ");
        Client client = new Client(clientId, clientName, age);

        Insurance insurance = switch (type) {
            case "1" -> new LifeInsurance(client, insuredAmount);
            case "2" -> new CarInsurance(client, insuredAmount, readInt("Año del vehículo: ", 1886));
            case "3" -> new HomeInsurance(client, insuredAmount,
                    readBoolean("¿Está en zona de riesgo? (s/n): "));
            default -> throw new IllegalArgumentException("Tipo de póliza no válido.");
        };
        portfolio.add(insurance);
        System.out.println("Póliza agregada. Prima anual: "
                + quoter.calculateAnnualPremium(insurance) + " " + CURRENCY);
    }

    /** Busca y muestra las pólizas asociadas al ID de cliente indicado. */
    private void searchPolicies() {
        String clientId = readRequiredText("ID del cliente a buscar: ");
        List<Insurance> matchingPolicies = portfolio.findByClientId(clientId);
        if (matchingPolicies.isEmpty()) {
            System.out.println("No se encontraron pólizas para ese cliente.");
            return;
        }
        matchingPolicies.stream().map(this::formatPolicy).forEach(System.out::println);
    }

    /** Presenta los reportes gerenciales disponibles para la cartera actual. */
    private void showReports() {
        System.out.println("Prima promedio: " + report.averagePremium() + " " + CURRENCY);
        Map<String, Long> counts = report.countByType();
        System.out.println("Pólizas por tipo: " + counts);
        System.out.println("Top 3 pólizas más costosas:");
        report.topThreeMostExpensive().stream()
                .map(this::formatPolicy)
                .forEach(System.out::println);
    }

    /**
     * Construye la representación de una póliza para mostrarla en consola.
     *
     * @param insurance póliza que se desea presentar
     * @return texto con cliente, suma asegurada y prima anual
     */
    private String formatPolicy(Insurance insurance) {
        return "%s | cliente=%s (%s) | suma asegurada=%s USD | prima anual=%s USD"
                .formatted(policyType(insurance), insurance.client().name(), insurance.client().id(),
                        insurance.insuredAmount(), quoter.calculateAnnualPremium(insurance));
    }

    /**
     * Obtiene el nombre en español del tipo concreto de póliza.
     *
     * @param insurance póliza cuyo tipo se desea describir
     * @return nombre visible del tipo de seguro
     */
    private String policyType(Insurance insurance) {
        return switch (insurance) {
            case LifeInsurance ignored -> "Vida";
            case CarInsurance ignored -> "Automóvil";
            case HomeInsurance ignored -> "Hogar";
        };
    }

    /**
     * Muestra una solicitud y devuelve la respuesta sin espacios exteriores.
     *
     * @param message texto que se presenta antes de leer la respuesta
     * @return entrada del usuario normalizada
     */
    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().strip();
    }

    /**
     * Repite la selección hasta recibir uno de los tipos de póliza admitidos.
     *
     * @return opción válida entre 1 y 3
     */
    private String readPolicyType() {
        while (true) {
            String type = prompt("Seleccione el tipo: ");
            if (type.equals("1") || type.equals("2") || type.equals("3")) {
                return type;
            }
            System.out.println("Tipo no válido. Seleccione 1, 2 o 3.");
        }
    }

    /**
     * Solicita un texto obligatorio hasta que no esté vacío.
     *
     * @param message solicitud que se presenta al usuario
     * @return texto normalizado introducido
     */
    private String readRequiredText(String message) {
        while (true) {
            String value = prompt(message);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("El valor no puede estar vacío.");
        }
    }

    /**
     * Solicita un entero válido que no sea menor que el mínimo indicado.
     *
     * @param message solicitud que se presenta al usuario
     * @param minimum valor mínimo permitido
     * @return entero válido introducido
     */
    private int readInt(String message, int minimum) {
        while (true) {
            String value = prompt(message);
            try {
                int number = Integer.parseInt(value);
                if (number >= minimum) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // El mensaje unificado indica tanto formato incorrecto como valores fuera de rango.
            }
            System.out.println("Ingrese un número entero igual o mayor que " + minimum + ".");
        }
    }

    /**
     * Solicita un importe decimal válido y estrictamente positivo.
     *
     * @param message solicitud que se presenta al usuario
     * @return importe válido introducido
     */
    private BigDecimal readAmount(String message) {
        while (true) {
            String value = prompt(message);
            try {
                BigDecimal amount = new BigDecimal(value);
                if (amount.signum() > 0) {
                    return amount;
                }
            } catch (NumberFormatException ignored) {
                // Se informa el mismo mensaje para importes mal formados, nulos o negativos.
            }
            System.out.println("Ingrese un monto válido mayor que cero.");
        }
    }

    /**
     * Convierte las respuestas habituales en español o inglés a un valor lógico.
     *
     * @param message solicitud que se presenta al usuario
     * @return {@code true} para una respuesta afirmativa y {@code false} para una negativa
     */
    private boolean readBoolean(String message) {
        while (true) {
            switch (prompt(message).toLowerCase(Locale.ROOT)) {
                case "s", "si", "sí", "true" -> {
                    return true;
                }
                case "n", "no", "false" -> {
                    return false;
                }
                default -> System.out.println("Respuesta no válida. Ingrese s o n.");
            }
        }
    }
}
