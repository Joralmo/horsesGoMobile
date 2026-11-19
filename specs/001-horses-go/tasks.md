---
description: "Tareas de implementación de Horses Go"
---

# Tasks: Horses Go

**Entrada** [Plan](plan.md) · [Especificación](spec.md) · [Investigación](research.md) · [Modelo](data-model.md) · [Contrato](contracts/gameplay.md) · [Validación](quickstart.md)

Las tareas llevan un ID consecutivo. `[P]` identifica tareas que pueden
hacerse en paralelo una vez satisfechas sus dependencias. `[US1]`,
`[US2]` y `[US3]` vinculan el trabajo a las historias de la especificación.
Las rutas de código son relativas a la raíz y corresponden a archivos
que se crearán durante la implementación.
En cada historia, preparar primero las pruebas y comprobar que detectan
los comportamientos pendientes antes de completar la implementación.

## Phase 1 — Setup

**Objetivo** Compilar la interfaz compartida y verificar el arranque de MMKV en las dos plataformas.

- [X] T001 Crear el wrapper Gradle 9.3.1 y configurar JDK 17, repositorios, catálogo de versiones y módulos en `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` y `gradle/libs.versions.toml`.
- [X] T002 Configurar Kotlin 2.4.21, AGP 9.1.1 con el plugin de biblioteca Android KMP, Compose 1.10.3, Lifecycle 2.10.0, Coroutines 1.10.2, Serialization JSON 1.10.0 y MMKV KMP 2.4.3 en `shared/build.gradle.kts`. Habilitar pruebas comunes y Android host, configurar `iosArm64` e `iosSimulatorArm64` y exponer el punto de entrada en `shared/src/commonMain/kotlin/com/horsesgo/ui/App.kt`.
- [X] T003 [P] Crear el host Android de 64 bits con API mínima 23 y compilación y destino 36. Inicializar MMKV con el contexto de aplicación antes de abrir Compose en `androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`, `androidApp/src/main/kotlin/com/horsesgo/MainActivity.kt` y `shared/src/androidMain/kotlin/com/horsesgo/AndroidBootstrap.kt`.
- [X] T004 [P] Crear el host Swift para iOS 14+, inicializar MMKV al arrancar, montar Compose mediante el controlador compartido y configurar `embedAndSignAppleFrameworkForXcode` en `iosApp/iosApp.xcodeproj/project.pbxproj`, `iosApp/iosApp/HorsesGoApp.swift` y `shared/src/iosMain/kotlin/com/horsesgo/MainViewController.kt`.
- [X] T005 Comprobar ensamblado Android, enlace del framework iOS, arranque y escritura/lectura con una clave de prueba de MMKV en ambas plataformas. Registrar la versión de Xcode y el resultado en `specs/001-horses-go/quickstart.md`. Usar Xcode 26.4 si el entorno con 27.0 requiere la versión de referencia.

## Phase 2 — Foundational

**Objetivo** Definir el modelo común, el contrato de búsqueda y la instantánea que comparten las historias.

- [X] T006 [P] Modelar los identificadores estables de principiante, intermedio y avanzado en `shared/src/commonMain/kotlin/com/horsesgo/game/Difficulty.kt`. «Las parejas tamaño/profundidad son 4/4, 6/6 y 8/8. Las posiciones se definen en FR-002.» Usar B2/C3, C3/D4 y D4/E5 para negro/blanco.
- [X] T007 [P] Encapsular la matriz cuadrada de enteros en `shared/src/commonMain/kotlin/com/horsesgo/game/Board.kt` y definir las coordenadas en `shared/src/commonMain/kotlin/com/horsesgo/game/Position.kt`. «Contiene exactamente un caballo negro y uno blanco.» «Fila y columna desde cero. A1 es fila 0, columna 0.» «Los índices deben estar dentro del tablero.» Codificar 0 libre, 1 destruida, 2 negro y 3 blanco.
- [X] T008 Definir el próximo turno, los datos de una jugada y el estado inmutable en `shared/src/commonMain/kotlin/com/horsesgo/game/MatchState.kt` y `shared/src/commonMain/kotlin/com/horsesgo/game/Move.kt`. «El origen contiene ese caballo. El destino está libre y a un salto en L.» «El motor deriva las posiciones, el número de jugada y el ganador del tablero y el turno.» La validación de jugadas se aplica en el motor.
- [X] T009 [P] Definir, codificar y validar la instantánea en `shared/src/commonMain/kotlin/com/horsesgo/storage/MatchSnapshot.kt`. Para `schemaVersion`, «Versión inicial 1. La lectura acepta versiones soportadas.» Para `difficulty`, «`beginner`, `intermediate` o `advanced`. Fija dimensiones y profundidad.» Para `cells`, «Valores y dimensiones del tablero común. Orden de filas desde A1.» Para `nextTurn`, «`black` con cantidad par de casillas destruidas, `white` con cantidad impar.» Verificar ambos caballos en la restauración.
- [X] T010 [P] Definir el puerto de búsqueda inyectable y el estado que Compose observa en `shared/src/commonMain/kotlin/com/horsesgo/ai/MoveSearch.kt` y `shared/src/commonMain/kotlin/com/horsesgo/ui/GameUiState.kt`. Incluir pantalla, dificultad seleccionada, partida y operación en curso. «Las acciones disponibles se derivan de la pantalla, el turno y el resultado.»
- [X] T011 Implementar `shared/src/commonMain/kotlin/com/horsesgo/storage/MatchStore.kt` y `shared/src/commonMain/kotlin/com/horsesgo/storage/MmkvMatchStore.kt`. Distinguir lectura válida, ausencia, formato inválido y error de acceso. Guardar el JSON completo en la instancia `horses-go`, clave `active-match`, comprobar que MMKV acepte la escritura y solicitar sincronización antes de devolver éxito.

## Phase 3 — User Story 1 · Jugar hasta el último movimiento (P1)

**Objetivo** Entregar una partida de principiante con MiniMax, resultado y recuperación desde MMKV.

**Prueba independiente** Cargar un mapa 4 × 4, comprobar un salto legal y los rechazos por destino ocupado o destruido, reproducir una victoria por participante y reabrir una partida guardada.

### Pruebas de la historia

- [X] T012 [P] [US1] Preparar casos de saltos en L, límites, origen destruido, destino ocupado o destruido, alternancia y victoria de ambos bandos en `shared/src/commonTest/kotlin/com/horsesgo/game/GameRulesTest.kt`. Incluir el caso en que ambos quedan sin saltos y gana quien jugó último.
- [X] T013 [P] [US1] Preparar casos de publicación tras guardar, rechazo y reintento de escritura, recuperación de turno blanco, recuperación de resultado y copia inválida en `shared/src/commonTest/kotlin/com/horsesgo/ui/GameViewModelPersistenceTest.kt`. Usar un almacén y una búsqueda controlados para verificar una sola respuesta por turno.

### Implementación

- [X] T014 [P] [US1] Implementar la apertura, los destinos legales, la transición inmutable y el resultado terminal en `shared/src/commonMain/kotlin/com/horsesgo/game/GameRules.kt`. Destruir solo el origen de una jugada legal y mantener intactos tablero y turno ante un intento inválido.
- [X] T015 [P] [US1] Dibujar tablero, caballos, casillas destruidas, turno y ganador en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameScreen.kt`. Comunicar al ViewModel la coordenada tocada mediante un callback y deshabilitar jugadas en el turno blanco y al terminar.
- [X] T016 [US1] Implementar MiniMax en `shared/src/commonMain/kotlin/com/horsesgo/ai/MinimaxSearch.kt`. Reutilizar las jugadas legales del motor e incluir la primera candidata en la profundidad. Ordenar victoria blanca, estados no terminales y derrota blanca de menor a mayor puntuación. Al alcanzar el límite, contar saltos legales del negro. El blanco minimiza y el negro maximiza. Comprobar cancelación cooperativa durante el recorrido.
- [X] T017 [US1] Implementar el ViewModel de sesión en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameViewModel.kt`. Inyectar motor, búsqueda y almacén por constructor, exponer `StateFlow` de solo lectura, restaurar al iniciar, serializar transiciones, hacer la escritura en E/S, publicar tras guardar y bloquear envíos duplicados mientras se confirma. Permitir reintentos y ejecutar una sola búsqueda por turno en `Dispatchers.Default`.
- [X] T018 [US1] Conectar inicialización, dependencias, **Cargar mapa** de principiante, tablero, resultado y avisos de guardado en `shared/src/commonMain/kotlin/com/horsesgo/ui/App.kt`, `androidApp/src/main/kotlin/com/horsesgo/MainActivity.kt` y `shared/src/iosMain/kotlin/com/horsesgo/MainViewController.kt`.
- [X] T019 [US1] Ejecutar las pruebas comunes y demostrar el flujo de principiante, incluidos ganadores de ambos bandos y reapertura, con `specs/001-horses-go/quickstart.md`.

## Phase 4 — User Story 2 · Elegir dificultad y cargar el mapa (P2)

**Objetivo** Agregar la selección y el reemplazo de partidas de cualquier nivel, incluso durante una búsqueda.

**Prueba independiente** Elegir los tres niveles y verificar dimensiones, posiciones iniciales y turno negro. Cambiar de mapa con una respuesta anterior pendiente y confirmar que el tablero nuevo y su copia guardada conservan la apertura.

### Pruebas de la historia

- [X] T020 [P] [US2] Preparar casos para las tres aperturas, selección previa sin partida nueva, reinicio tras el resultado y carga rechazada que permite volver a la partida anterior en `shared/src/commonTest/kotlin/com/horsesgo/ui/MapLoadingTest.kt`.
- [X] T021 [P] [US2] Preparar una búsqueda controlada que devuelva una jugada después de confirmar otro mapa en `shared/src/commonTest/kotlin/com/horsesgo/ui/MapReplacementTest.kt`. Verificar que la respuesta tardía conserve tablero, turno y copia persistida del mapa nuevo.

### Implementación

- [X] T022 [P] [US2] Implementar el selector de principiante, intermedio y avanzado, la confirmación **Cargar mapa** y el regreso a una partida vigente en `shared/src/commonMain/kotlin/com/horsesgo/ui/LevelSelector.kt`. Habilitar la confirmación con un nivel seleccionado.
- [X] T023 [P] [US2] Implementar la selección sin efectos persistentes, el reemplazo serializado, la cancelación y la recuperación tras un guardado rechazado en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameViewModel.kt`. Asociar cada búsqueda con generación y número de jugada. «El token debe coincidir con el estado vigente y el turno blanco para aplicar el resultado.»
- [X] T024 [US2] Integrar el selector compartido y la acción **Cambiar mapa** desde tablero y resultado en `shared/src/commonMain/kotlin/com/horsesgo/ui/App.kt` y `shared/src/commonMain/kotlin/com/horsesgo/ui/GameScreen.kt`.
- [X] T025 [US2] Ejecutar las comprobaciones de mapas y respuesta tardía. Verificar en `specs/001-horses-go/quickstart.md` que reabrir la app después de reemplazar una partida recupera el mapa nuevo.

## Phase 5 — User Story 3 · Enfrentarse a una máquina que anticipa jugadas (P3)

**Objetivo** Optimizar MiniMax con poda alfa-beta y verificar las decisiones del rival en los tres niveles.

**Prueba independiente** Comparar con MiniMax exhaustivo en posiciones pequeñas y comprobar victorias forzadas, derrotas evitables, empates de valoración, profundidad 4/6/8 y conservación del estado durante el cálculo.

### Pruebas de la historia

- [X] T026 [P] [US3] Preparar casos de victoria forzada, derrota evitable, única jugada, empate, movilidad del caballo negro y equivalencia con MiniMax exhaustivo en `shared/src/commonTest/kotlin/com/horsesgo/ai/MinimaxDecisionTest.kt`.
- [X] T027 [P] [US3] Preparar casos de límite 4/6/8 incluida la primera jugada candidata, estado terminal justo en el límite, tablero sin mutaciones y cancelación cooperativa en `shared/src/commonTest/kotlin/com/horsesgo/ai/MinimaxSearchTest.kt`.

### Implementación

- [X] T028 [US3] Incorporar poda alfa-beta en `shared/src/commonMain/kotlin/com/horsesgo/ai/MinimaxSearch.kt`. Mantener la evaluación terminal por encima de la heurística, la minimización del caballo blanco, la maximización del negro, los límites por nivel y la cancelación en los nodos.
- [X] T029 [US3] Ejecutar las pruebas comunes de equivalencia, profundidad y cancelación con posiciones de los tres niveles. Comprobar en una partida de principiante que los toques durante la búsqueda conservan el estado según `specs/001-horses-go/quickstart.md`.

## Phase 6 — Polish & Cross-Cutting Concerns

**Objetivo** Verificar el resultado en Android e iOS y dejar constancia de las comprobaciones.

- [X] T030 [P] Compilar e instalar la app configurada en `androidApp/build.gradle.kts`. Ejecutar en Android los casos de niveles, ganadores, reapertura y cambio de mapa de `specs/001-horses-go/quickstart.md`. Medir 30 respuestas por nivel y verificar el umbral SC-003 de al menos 29 dentro de dos segundos.
- [X] T031 [P] Compilar y ejecutar `iosApp/iosApp.xcodeproj/project.pbxproj`. Ejecutar en iOS los casos de niveles, ganadores, reapertura y cambio de mapa de `specs/001-horses-go/quickstart.md`. Medir 30 respuestas por nivel, verificar el umbral SC-003 de al menos 29 dentro de dos segundos y registrar la versión de Xcode.
- [ ] T032 Comprobar SC-004 con la sesión de primer uso de `specs/001-horses-go/quickstart.md`. Verificar que al menos cuatro de cinco personas cargan el mapa y hacen su primer salto legal en menos de 60 segundos sin ayuda.
- [X] T033 Consolidar los resultados, dispositivos y verificaciones pendientes en `specs/001-horses-go/quickstart.md`. Actualizar en `README.md` el estado de implementación y las instrucciones de ejecución que se hayan verificado.

## Dependencias y orden de ejecución

```text
Setup → Base compartida → US1 → US2 ──┐
                           └→ US3 ──┴→ Validación final
```

| Tramo | Dependencias |
| --- | --- |
| Setup | T001 → T002 → T003 y T004 → T005. |
| Base | Tras T005, T006 y T007 en paralelo. T008 requiere ambas. T009 y T010 pueden empezar tras T008. T011 requiere T009. |
| US1 | Tras la base, redactar T012 y T013. Luego T014 y T015 en paralelo. T016 requiere T014. T017 requiere T016 y T011. T018 requiere T015 y T017. T019 verifica la integración. |
| US2 | Tras US1, T020 y T021 en paralelo. Luego T022 y T023 en paralelo. T024 integra ambas y T025 verifica. |
| US3 | T026 y T027 pueden redactarse tras la base. T028 requiere ambas pruebas y la búsqueda de T016. T029 usa la integración de US1 y posiciones preparadas para los tres niveles. |
| Validación final | Tras US2 y US3, T030 y T031 pueden ejecutarse en paralelo. T033 consolida T030 a T032. |

### Cobertura de requisitos

| Requisitos | Tareas principales |
| --- | --- |
| FR-001, FR-002 y FR-010 | T006, T014 y T020–T025. |
| FR-003 a FR-006 | T012, T014, T015 y T017–T019. |
| FR-007 a FR-009 | T016, T017 y T026–T029. |
| PERSIST-01 | T009, T011, T013, T017, T019 y T030–T031. |
| MAP-01 | T021, T023–T025 y T030–T031. |
| SC-001 a SC-004 | T030–T033. |

### Ejemplos paralelos por historia

- **US1.** T012 verifica el motor y T013 prepara los casos de persistencia del ViewModel. Después, T014 implementa reglas mientras T015 construye el tablero.
- **US2.** T022 implementa el selector y T023 prepara el reemplazo de la partida. T024 conecta ambas piezas.
- **US3.** T026 prepara posiciones tácticas y T027 cubre profundidad, inmutabilidad y cancelación en otro archivo. T028 ejecuta ambas baterías sobre la búsqueda optimizada.

## Estrategia de implementación

1. **MVP.** Completar Setup, Base y US1. Demostrar una partida de principiante contra MiniMax, ver el resultado y recuperar la partida al reabrir.
2. **Entrega incremental.** Agregar US2 para jugar y cambiar de mapa en tres niveles. Integrar US3 para comprobar las decisiones y la poda alfa-beta.
3. **Validación.** Ejecutar los casos de reglas, persistencia y rendimiento en las dos plataformas. Registrar la evidencia de aceptación con la revisión.
