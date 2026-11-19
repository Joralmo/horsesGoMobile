# Validación de Horses Go

Ejecutar esta guía cuando el repositorio tenga el proyecto Android, el host
iOS y el wrapper de Gradle definidos en el [plan](plan.md).

## Preparación

1. Desde la raíz del repositorio, verificar JDK 17 y Gradle 9.3.1.
2. Preparar Android SDK 36 y un dispositivo o emulador de 64 bits con
   Android API 23 o superior.
3. En una Mac con Apple Silicon, preparar Xcode y un simulador iOS ARM64.
   Xcode 26.4 es la referencia del compilador. Si se usa Xcode 27.0, registrar
   el resultado de enlace y ejecución.

```bash
java -version
./gradlew --version
xcodebuild -version
xcrun simctl list devices available
adb devices -l
```

## Compilación y pruebas

```bash
./gradlew :shared:allTests :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :androidApp:installDebug
```

Para iOS, abrir `iosApp/iosApp.xcodeproj` en Xcode y ejecutar el esquema
`iosApp` en un simulador ARM64. El paso de compilación del host invoca
`:shared:embedAndSignAppleFrameworkForXcode`. Para Android, abrir Horses Go
desde el emulador o dispositivo.

## Escenarios de aceptación

| Escenario | Acción | Resultado esperado |
| --- | --- | --- |
| Niveles | En cada plataforma, elegir cada nivel y pulsar **Cargar mapa**. | Se abre el tablero correcto con posiciones de FR-002 y turno negro. Se puede completar una partida por nivel. |
| Legalidad y victoria | Realizar saltos legales y tocar destinos ocupados, destruidos y fuera de alcance. Ejercitar una victoria de cada participante. | Los rechazos conservan el estado. Cada salto válido destruye el origen. Se anuncia al autor del último salto y se bloquean más jugadas. |
| Profundidad de MiniMax | Ejecutar casos controlados en `commonTest` con ramas abiertas y terminales justo en el límite. | La primera jugada candidata cuenta para la profundidad. Se aplican los límites 4, 6 y 8, la evaluación terminal y las reglas comunes. |
| Reapertura | Tras un salto confirmado, anotar posiciones, casillas destruidas y turno. Terminar el proceso conservando los datos y reabrir. Repetir con una partida terminada. | Se recupera la última copia, incluido el resultado. Si la copia indica turno blanco, se calcula y aplica una sola jugada. |
| Resultado tardío | En `commonTest`, controlar cuándo termina MiniMax, cargar otro mapa y liberar la respuesta anterior. | El tablero, turno y copia guardada corresponden al mapa nuevo. |
| Recuperación ante fallos | En pruebas con almacén controlado, devolver datos inválidos o rechazar una escritura. | La copia inválida abre el selector con aviso. La escritura rechazada conserva el estado y deja aplicar el cambio una sola vez al reintentar. |
| Tiempo de respuesta | Medir 30 turnos blancos con jugadas disponibles por nivel en un dispositivo de referencia por plataforma. Medir desde que se muestra el turno blanco hasta que aparece la jugada. | Al menos 29 de 30 respuestas por nivel y dispositivo aparecen en dos segundos o menos. |
| Primer uso | Observar a cinco personas que juegan por primera vez. Medir desde el selector hasta el primer salto legal. | Al menos cuatro cargan un mapa y hacen un salto legal en menos de 60 segundos sin ayuda. |

Registrar versiones, dispositivos y resultados con la revisión de
implementación. El [contrato de interacción](contracts/gameplay.md)
describe los estados que deben observarse.
