# JavaInsure Core

Aplicación de consola para administrar pólizas de vida, automóvil y hogar. Requiere **JDK 21**: el proyecto utiliza records, sealed interfaces, pattern matching en `switch` y `SequencedCollection`.

## Estructura del proyecto

El código fuente se organiza en paquetes según su responsabilidad:

```text
src/
├── main/
│   ├── java/com/javainsurecore/
│   │   ├── Main.java                 # Punto de entrada y menú de consola
│   │   ├── model/                    # Cliente y tipos de póliza
│   │   ├── service/                  # Cartera, cotización y reportes
│   │   ├── exception/                # Excepciones de negocio y de datos
│   │   └── util/                     # Lectura y análisis del CSV
│   └── resources/                    # Configuración de Log4j 2
└── test/java/com/javainsurecore/     # Pruebas automatizadas JUnit 5
```

## Requisitos

- JDK 21 o superior.
- Apache Maven 3.9 o superior.

## Ejecutar

Desde la carpeta `Javalnsurecore`:

```bash
mvn clean test
mvn exec:java
```

El programa busca `policies.csv` en el directorio de trabajo al iniciar. El archivo incluido contiene diez registros de muestra. Si no existe o tiene una fila inválida, el error se registra en el log y se informa en consola; el menú sigue disponible.

Las opciones del menú permiten listar pólizas, agregar pólizas, buscar por ID de cliente y consultar los reportes. Al ingresar datos, la aplicación vuelve a solicitar opciones inválidas, campos obligatorios vacíos, valores numéricos no válidos o fuera de rango, montos no positivos y respuestas no reconocidas para la zona de riesgo. Los importes y las primas se expresan en USD; el redondeo se realiza a dos decimales con `HALF_UP`.

## Formato de `policies.csv`

El separador es coma y cada tipo tiene estas columnas:

```text
LIFE,CLIENT_ID,AMOUNT,CLIENT_NAME,AGE
CAR,CLIENT_ID,AMOUNT,CLIENT_NAME,MODEL_YEAR,AGE
HOME,CLIENT_ID,AMOUNT,CLIENT_NAME,RISK_ZONE,AGE
```

`RISK_ZONE` acepta `true` o `false`. Las líneas vacías y las líneas que comienzan con `#` se ignoran.

## Reglas de prima anual

- Vida: 5% del monto asegurado; si la edad es mayor que 60, la prima base aumenta 20%.
- Automóvil: 10% del valor; para vehículos anteriores a 2015, se agregan USD 50.
- Hogar: 2% del valor; en zona de riesgo, la prima se duplica.

## Logging y pruebas

Log4j 2 escribe en consola de errores y en `logs/javainsure.log`. Las pruebas JUnit 5 cubren los cálculos de primas, límites de negocio, reportes, carga y parsing de CSV mediante archivos temporales, y las validaciones del menú con entradas simuladas. Para ejecutar toda la suite:

```bash
mvn clean test
```

Mockito está declarado como dependencia de prueba, pero las pruebas actuales no lo utilizan; la lectura del CSV se prueba con archivos temporales.