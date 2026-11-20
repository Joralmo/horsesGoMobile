# Validación — Aperturas y árbol MiniMax 5 × 5

Ejecutar los comandos desde la raíz del repositorio después de implementar el [plan](plan.md). Consultar el [contrato de interacción](contracts/gameplay.md) para los estados visibles y el [modelo de datos](data-model.md) para las claves y las valoraciones.

## Preparación

- JDK 17 y Gradle Wrapper del repositorio. En Android, instalar SDK 36 y preparar un emulador o dispositivo de 64 bits con API 23 o superior.
- En una Mac con Apple Silicon, preparar Xcode y un simulador iOS ARM64. Usar el esquema `iosApp` del proyecto `iosApp/iosApp.xcodeproj`.
- Para comprobar una actualización, guardar una partida con la versión previa y conservar los datos de aplicación al instalar la nueva.

```bash
java -version
./gradlew --version
adb devices -l
xcodebuild -version
xcrun simctl list devices available
```

## Compilación y pruebas

```bash
./gradlew :shared:allTests :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :androidApp:installDebug
```

Abrir Horses Go en Android. En iOS, ejecutar el esquema del proyecto desde Xcode. La suite de `commonTest` debe cubrir las reglas, el árbol completo, la persistencia, los reintentos y las transiciones de la vista. Usar posiciones fijas y extracciones aleatorias controladas para que la suite sea reproducible.

## Escenarios de aceptación

| Escenario | Procedimiento | Resultado esperado |
| --- | --- | --- |
| Aperturas (SC-001) | En cada plataforma, cargar 20 mapas por nivel y registrar ambas coordenadas y el turno. | Las 80 aperturas por plataforma cumplen la forma del tablero y la legalidad inicial. Cada caballo muestra al menos dos coordenadas distintas por nivel. |
| Selección y partida (SC-002) | En cada plataforma, elegir 5 × 5, cargarlo y jugar hasta el resultado. Tocar también una casilla ocupada, una destruida y una libre fuera de alcance durante un turno negro. Repetir con una victoria de cada lado en casos controlados. | El selector distingue 5 × 5 de 6 × 6. Los intentos inválidos conservan el estado. Los dos caballos juegan saltos en L, cada jugada válida destruye su origen y gana el último en mover. |
| Reanudar (SC-002) | En 5 × 5, completar un par de saltos, registrar tablero y turno, cerrar el proceso y volver a abrir conservando los datos. Repetir con partidas guardadas en los niveles de la versión previa y cubrir un turno blanco pendiente en `commonTest`. | Se recuperan el nivel, las posiciones, el daño y el turno guardados. Si había un turno blanco pendiente se aplica una sola respuesta válida. |
| Reintentar una carga (FR-002) | En `commonTest`, registrar una carga que el almacén de prueba rechace. Aceptar el guardado y lanzar **Reintentar**. Realizar después una carga nueva con otras extracciones predefinidas. | El reintento escribe el mismo tablero que falló. La carga nueva consume extracciones propias. Si había una partida previa, permanece publicada hasta confirmarse la sustitución. |
| Profundidad real (FR-004) | Verificar los horizontes 4, 6, 6 y 8 en `commonTest` y comparar la búsqueda real con el oráculo exhaustivo. Usar también la posición blanca sensible a profundidad que figura abajo. | La búsqueda cuenta el primer salto candidato, evalúa el resultado terminal antes del corte y escoge un destino óptimo al horizonte del nivel. El estado de entrada se conserva. |
| Árbol completo (SC-003) | En `commonTest`, reconstruir las diez posiciones negras de referencia de abajo y comparar todas las ramas y hojas con las transiciones legales y los valores esperados. En ambos clientes, consultar un árbol desde una apertura real. | Cada réplica queda bajo el salto negro correspondiente. Todas las ramas terminan en el nivel 2 o en un estado terminal anterior. Como máximo hay dos saltos desde la raíz. |
| Consulta sin cambios (FR-005 a FR-008) | Abrir la vista en 5 × 5, desplazarse hasta la última rama, cerrar y comparar el tablero. Completar un turno negro y uno blanco, y volver a abrir. Comprobar el turno blanco, un estado terminado y los otros niveles. | El tablero, los destinos válidos y el turno se conservan al consultar. La siguiente vista parte del nuevo estado y la acción queda fuera de alcance en los estados excluidos. El almacén de prueba registra cero escrituras durante la consulta. |
| Tiempo y primer uso (SC-004) | En un dispositivo de referencia por plataforma, medir 20 consultas desde el toque en **Ver árbol MiniMax** hasta que todas las ramas están disponibles para recorrer. Observar a cinco personas que prueban el nivel por primera vez, desde un turno negro con réplica disponible. | Al menos 19 consultas por plataforma demoran como máximo 2 s. Al menos 4 de 5 personas abren la vista, señalan un salto y su réplica, y regresan al tablero en menos de 60 s sin ayuda. |

## Posición blanca para distinguir profundidades

Empezar en 5 × 5 con negro en **E2** y blanco en **B3**. Aplicar estos destinos alternando desde el negro: `C1, C5, A2, A4, B4, B2, D3, D1, E1, E3, C2`. La raíz blanca queda con dos destinos legales.

| Horizonte en movimientos | Valor de C4 | Valor de D5 | Mejor salto blanco |
| ---: | ---: | ---: | --- |
| 2 | 2 | 3 | C4 |
| 5 | 2 | 3 | C4 |
| 6 | 9 | 2 | D5 |

## Posiciones negras de referencia

Cada fila empieza en 5 × 5 con negro en **B2** y blanco en **D3**. Aplicar en orden los destinos indicados, alternando desde el negro. El turno vuelve al negro al final de cada secuencia. En la tabla, `D1:-1/2` significa que la rama hacia D1 vale −1 y tiene dos réplicas. La última columna incluye ejemplos de final inmediato para un destino solo y de victoria blanca para una pareja de destinos.

| Caso | Destinos del prefijo | Valor de la raíz | Ramas del negro (`destino:valor/réplicas`) | Ejemplo terminal |
| --- | --- | ---: | --- | --- |
| P1 | `D1 E1 E3 C2 D5 D4 B4 B5 A2 C3 C1 E2` | 9 | `B3:9/0` | B3 |
| P2 | `D1 E5 C3 C4 E2 A5 D4 B3 B5 A1 A3 C2 B1 E3 D2 D5 E4 B4 C5 A2` | -1 | `A4:-1/1` | A4 → C1 |
| P3 | `C4 C5 A3 A4 B5 C3 D4 D5 C2 E3` | 1 | `B4:1/1 E1:-1/1 A1:1/1` | E1 → D1 |
| P4 | `D1 E1 C3 C2 D5 D4 B4 B3 A2 A5 C1 C4` | -1 | `E2:-1/4` | E2 → D2 |
| P5 | `D1 E1 E3 C2 D5 B4 C3 A2 A4 C1 C5 E2 B3 D4` | 3 | `A5:1/1 A1:-1/1 D2:3/1` | A1 → B5 |
| P6 | `C4 B4` | 3 | `D2:3/3 E5:-1/3 A5:1/3 E3:2/3 A3:2/3` | E5 → C2 |
| P7 | `C4 E1 D2 C2 B1 E3 A3 D5 B5 C3 D4 D1` | 9 | `E2:9/0 B3:9/0` | E2, B3 |
| P8 | `D1 E1 C3 C2 B1 D4 D2 B3 E4 C1 C5 E2` | 9 | `A4:9/0` | A4 |
| P9 | `C4 E1 A3 C2 B5 B4 C3 D5` | 2 | `D1:-1/1 B1:1/1 E4:2/1 A4:1/1 E2:2/1 A2:1/1` | D1 → E3 |
| P10 | `C4 C1 E3 A2` | 4 | `D5:1/2 D1:-1/2 C2:4/2` | D1 → C3 |

Registrar los casos ejecutados, versión y dispositivo, resultados de cada plataforma y mediciones de SC-004 en la revisión de implementación.

## Resultados de implementación

Validación local del 2026-11-19 con JDK 17.0.18, Gradle 9.3.1 y Xcode 27.0
(27A266a). Las 26 pruebas compartidas pasaron en Android host y en iOS
Simulator ARM64. Incluyen las diez posiciones negras y la posición blanca
que comprueba el horizonte de seis movimientos. Se generaron el APK debug,
el framework ARM64 de simulador y la aplicación iOS de simulador.

### Primer uso (SC-004)

Pendiente la observación con cinco personas que prueben el nivel por primera
vez. La validación automatizada mide la apertura del diálogo.
