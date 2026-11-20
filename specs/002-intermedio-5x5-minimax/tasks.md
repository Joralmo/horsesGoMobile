---
description: "Tareas de implementación para las aperturas aleatorias y el nivel intermedio 5 × 5 con MiniMax"
---

# Tasks: Aperturas aleatorias y nivel intermedio 5 × 5 con MiniMax

**Entrada** [Especificación](spec.md) · [Plan](plan.md) · [Investigación](research.md) · [Modelo de datos](data-model.md) · [Contrato](contracts/gameplay.md) · [Guía de validación](quickstart.md)

**Pruebas** Los casos automatizados cubren los escenarios de aceptación, el corte de MiniMax, la restauración y las regresiones de las aperturas fijas. Preparar los casos de cada historia antes de su implementación y confirmar la diferencia esperada frente al comportamiento actual cuando el punto de entrada ya exista.

## Formato

Cada tarea sigue `- [ ] TNNN [P?] [USn?] Descripción con ruta`. Las rutas parten de la raíz del repositorio. `[P]` indica tareas con archivos distintos que pueden ejecutarse juntas una vez satisfechas sus dependencias. `[USn]` identifica la historia de usuario.

## Phase 1 — Preparación

- [X] T001 Confirmar la preparación de Android e iOS según `specs/002-intermedio-5x5-minimax/quickstart.md`, revisar la versión de `gradle/wrapper/gradle-wrapper.properties` y establecer la referencia de regresión con `./gradlew :shared:allTests` para los destinos declarados en `shared/build.gradle.kts`.

## Phase 2 — Fundamentos compartidos

- [X] T002 Separar las posiciones de prueba del catálogo. Usar tableros explícitos en `shared/src/commonTest/kotlin/com/horsesgo/game/GameRulesTest.kt` y `shared/src/commonTest/kotlin/com/horsesgo/storage/MatchSnapshotTest.kt`. En `shared/src/commonTest/kotlin/com/horsesgo/ui/GameViewModelPersistenceTest.kt` y `shared/src/commonTest/kotlin/com/horsesgo/ui/MapReplacementTest.kt`, mantener la intención de los casos con estados iniciales explícitos o con un destino legal del tablero cargado. Conservar las aserciones de destrucción del origen, turno, guardado y descarte de la respuesta tardía.
- [X] T003 Permitir que el auxiliar `withGame` reciba las reglas que se inyectan en `GameViewModel`, conservando el comportamiento por defecto y el almacén en memoria de `shared/src/commonTest/kotlin/com/horsesgo/ui/GameViewModelPersistenceTest.kt`. Depende de T002.

**Punto de control** La suite base sigue pasando y las pruebas que requieren una posición concreta pueden prepararla sin depender de la posición que elija la apertura de producción.

## Phase 3 — US1: Empezar con posiciones aleatorias (P1, MVP)

**Objetivo** Sortear ambos caballos para cada partida nueva, recuperar una partida guardada y conservar la apertura original al reintentar una carga.

**Verificación independiente** Cargar dos o más partidas por cada nivel disponible. Con extracciones controladas, verificar cambios de posición y una repetición válida. Rechazar y reintentar una carga, y después reanudar una partida con jugadas guardadas.

### Casos de aceptación y regresión

- [X] T004 [P] [US1] Incorporar a `shared/src/commonTest/kotlin/com/horsesgo/game/GameRulesTest.kt` casos de apertura con extracciones controladas en los tamaños disponibles, incluidos bordes y el índice excluido. Comprobar posiciones distintas dentro del tablero, casillas restantes libres, cero jugadas, turno negro y al menos un salto legal por caballo.
- [X] T005 [P] [US1] Actualizar `shared/src/commonTest/kotlin/com/horsesgo/ui/MapLoadingTest.kt` para sustituir las coordenadas de apertura fijas por extracciones controladas. Comprobar que seleccionar un nivel por sí solo conserva la partida, que cada **Cargar mapa** nuevo consume su propio sorteo y que **Reintentar** guarda el mismo tablero que rechazó el almacén, preservando la partida previa hasta confirmar el reemplazo.

### Implementación

- [X] T006 [US1] Reemplazar las coordenadas fijas de `shared/src/commonMain/kotlin/com/horsesgo/game/Difficulty.kt` por un sorteo de dos índices sin reemplazo desde `shared/src/commonMain/kotlin/com/horsesgo/game/GameRules.kt`, con `Random` inyectable y valores por defecto para producción. Construir el tablero en `shared/src/commonMain/kotlin/com/horsesgo/game/Board.kt`. Aplicar las restricciones del modelo «Ambos índices están en el tablero y son distintos» y «Con el turno negro activo, todas las casillas restantes quedan libres y ambos caballos pueden saltar». Mantener la codificación «libre 0, destruida 1, negro 2 y blanco 3». Depende de T004 y T005.
- [X] T007 [US1] Verificar el punto de entrada `loadSelectedMap`, la publicación tras guardado, `retrySave` y la restauración en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameViewModel.kt`. Reutilizar `PendingSave` para el mismo reintento y ajustar el flujo solo si la prueba detecta una diferencia. Al reanudar, usar el tablero y el turno de la instantánea. Depende de T006.

**Punto de control** FR-001 y FR-002 funcionan en los niveles existentes y quedan cubiertos por los casos de carga y restauración.

## Phase 4 — US2: Jugar en un tablero intermedio 5 × 5 (P2)

**Objetivo** Incluir una variante intermedia de 25 casillas que pueda jugarse y reanudarse, con búsqueda real de seis movimientos.

**Verificación independiente** Seleccionar **Intermedio 5 × 5**, cargarlo, comprobar que sus saltos destruyen el origen y completar una partida. Reabrir una partida guardada en 5 × 5 y verificar el mejor salto de la máquina en la posición blanca de la guía.

### Casos de aceptación y regresión

- [X] T008 [P] [US2] Ampliar `shared/src/commonTest/kotlin/com/horsesgo/ui/MapLoadingTest.kt` para cubrir las cuatro opciones, las 25 casillas del 5 × 5 y su apertura aleatoria. Mantener las comprobaciones de selección, guardado y paso a la pantalla de juego.
- [X] T009 [P] [US2] Validar el guardado y la restauración de las cuatro claves, incluidos datos previos con `intermediate`, en `shared/src/commonTest/kotlin/com/horsesgo/storage/MatchSnapshotTest.kt`. Preservar `schemaVersion = 1`, `difficulty`, `cells` y `nextTurn` en `shared/src/commonMain/kotlin/com/horsesgo/storage/MatchSnapshot.kt`. Comprobar «dimensiones que coincidan con la clave, exactamente un caballo por color, valores de casilla reconocidos y un turno consistente con la paridad de casillas destruidas». Aplicar «Con cero o un número par de casillas destruidas juega el negro. Con un número impar juega el blanco».
- [X] T010 [P] [US2] Añadir a `shared/src/commonTest/kotlin/com/horsesgo/ai/MinimaxSearchTest.kt` la posición blanca de `specs/002-intermedio-5x5-minimax/quickstart.md`: a profundidad seis la máquina elige D5 y a dos o cinco el oráculo prefiere C4. Verificar legalidad, derrota y victoria terminal al corte, tablero de entrada intacto y horizontes 4, 6, 6 y 8, contando la primera candidata.

### Implementación

- [X] T011 [US2] Incorporar `Intermediate5x5` con clave `intermediate-5x5`, tamaño 5 y profundidad 6 en `shared/src/commonMain/kotlin/com/horsesgo/game/Difficulty.kt`. Aplicar la tabla del modelo con `beginner` (4 × 4, 4), `intermediate-5x5` (5 × 5, 6), `intermediate` (6 × 6, 6) y `advanced` (8 × 8, 8). Confirmar que las tablas de saltos de `shared/src/commonMain/kotlin/com/horsesgo/game/GameRules.kt` incluyen el tamaño nuevo a partir del catálogo y que `shared/src/commonMain/kotlin/com/horsesgo/storage/MatchSnapshot.kt` lo recupera por su clave. Depende de T008, T009 y T010.
- [X] T012 [US2] Hacer distinguibles por tamaño los dos niveles intermedios en `shared/src/commonMain/kotlin/com/horsesgo/ui/LevelSelector.kt`, con etiquetas utilizables por lector de pantalla y profundidad de búsqueda visible. Conectar la elección al flujo actual de **Cargar mapa** y comprobar la aparición de las cuatro opciones. Depende de T011.

**Punto de control** FR-003 y FR-004 funcionan en una partida completa. El nuevo nivel puede guardarse y restaurarse con su clave propia.

## Phase 5 — US3: Consultar el árbol MiniMax de las próximas dos jugadas (P3)

**Objetivo** Mostrar el árbol completo de dos movimientos desde la posición actual del jugador en 5 × 5, con valoraciones MiniMax y resultados terminales.

**Verificación independiente** Abrir en la apertura y en un turno posterior. Comparar los saltos y réplicas con las diez posiciones de referencia, revisar los cortes terminales y cerrar para confirmar que se conserva la posición y que la consulta siguiente refleja el turno nuevo.

### Casos de aceptación y regresión

- [X] T013 [P] [US3] Crear `shared/src/commonTest/kotlin/com/horsesgo/ai/MinimaxTreeTest.kt` con los diez casos de `specs/002-intermedio-5x5-minimax/quickstart.md`. Comparar todos los destinos, las réplicas, la puntuación de la raíz y de las ramas, los resultados terminales en niveles 1 y 2, y el tablero de entrada. Incluir casillas ocupadas y destruidas en las comprobaciones de legalidad y conservar las alternativas empatadas.
- [X] T014 [P] [US3] Crear `shared/src/commonTest/kotlin/com/horsesgo/ui/MinimaxTreeViewModelTest.kt` con `withGame`. Comprobar apertura durante el turno negro inicial y posterior, cierre sin jugadas ni escrituras, posiciones posteriores sin ramas obsoletas y ausencia de la acción en turno blanco, partida terminada, selector, otro nivel y guardado pendiente o fallido. Con el diálogo abierto, comprobar que se bloquean las jugadas y el cambio de mapa.

### Implementación

- [X] T015 [US3] Definir el modelo de nodo en `shared/src/commonMain/kotlin/com/horsesgo/ai/MinimaxTree.kt`. Aplicar «Cantidad de saltos desde la raíz. Valores admitidos: 0, 1 y 2», «Caballo, origen y destino. La raíz representa el tablero actual y carece de jugada entrante», «Negro (MAX) o blanco (MIN). La raíz empieza en negro» y «Caballo ganador si la posición es terminal». Depende de T013 y T014.
- [X] T016 [US3] Compartir la evaluación de hojas desde `shared/src/commonMain/kotlin/com/horsesgo/ai/MinimaxSearch.kt` para aplicarla al árbol y a la jugada real. Revisar primero el resultado terminal y usar «9 para victoria negra, −1 para victoria blanca y movilidad negra de 0 a 8 al corte en posiciones no terminales». Preservar selección de jugada, poda alfa-beta, cancelación y profundidad de la búsqueda real. Depende de T015.
- [X] T017 [US3] Construir el árbol desde la posición de `MatchState` con `GameRules.legalMoves` y `GameRules.play` en `shared/src/commonMain/kotlin/com/horsesgo/ai/MinimaxTree.kt`. Aplicar «Un nodo por cada destino legal cuando la posición tiene movimientos y el nivel es menor que 2. Una posición terminal o de nivel 2 tiene cero hijos» y «Los nodos intermedios toman el máximo o mínimo de sus hijos según el turno de la posición». Conservar cada rama, incluso en empate, y evitar mutaciones del tablero real. Depende de T016.
- [X] T018 [P] [US3] Gestionar la elegibilidad y el estado transitorio del árbol en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameUiState.kt` y `shared/src/commonMain/kotlin/com/horsesgo/ui/GameViewModel.kt`. Abrir solo desde el turno negro activo del 5 × 5 con tablero publicado dispuesto para jugar. Bloquear jugadas y cambio de mapa mientras se consulta. Limpiar el árbol al cerrar, publicar otra posición o abandonar el tablero y mantener la consulta fuera del guardado. Depende de T017.
- [X] T019 [P] [US3] Implementar `shared/src/commonMain/kotlin/com/horsesgo/ui/MinimaxTreeDialog.kt` con cierre accesible y contenido vertical desplazable. Mostrar la posición actual y valor de la raíz, el límite de dos movimientos, la identificación del jugador MAX y la máquina MIN, el origen y destino de cada salto, las réplicas agrupadas, la movilidad negra al corte y los resultados terminales. Depende de T017.
- [X] T020 [US3] Conectar **Ver árbol MiniMax** y **Volver al tablero** en `shared/src/commonMain/kotlin/com/horsesgo/ui/GameScreen.kt` y `shared/src/commonMain/kotlin/com/horsesgo/ui/App.kt`. Usar la misma acción de cierre con el gesto nativo y conservar el resaltado de destinos legales del tablero al volver. Depende de T018 y T019.

**Punto de control** FR-005 a FR-008 funcionan en el 5 × 5 y la consulta no realiza ni guarda jugadas.

## Phase 6 — Cierre y validación transversal

- [X] T021 [P] Ejecutar `./gradlew :shared:allTests :androidApp:assembleDebug` y `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` siguiendo `specs/002-intermedio-5x5-minimax/quickstart.md`. Revisar los resultados de Android host e iOS Simulator ARM64 antes de la aceptación en dispositivos.
- [X] T022 [P] Actualizar en `README.md` la tabla de niveles, las aperturas aleatorias y el alcance del árbol. Indicar dos movimientos en la consulta del 5 × 5 y seis en la búsqueda real de los dos niveles intermedios.
- [ ] T023 Ejecutar los escenarios en Android e iOS de `specs/002-intermedio-5x5-minimax/quickstart.md`, registrando 20 aperturas por nivel y plataforma (SC-001), una partida completa y restauración (SC-002), correspondencia de ramas (SC-003) y 20 mediciones por plataforma con umbral de 19 en dos segundos o menos (SC-004). Registrar dispositivo, versión, resultado y verificaciones pendientes. Depende de T021.
- [ ] T024 Ejecutar y registrar en `specs/002-intermedio-5x5-minimax/quickstart.md` la prueba de primer uso de SC-004 con cinco personas. Verificar que al menos cuatro pueden abrir el árbol, identificar un salto propio y una réplica, y volver a la misma posición sin ayuda en menos de 60 segundos. Registrar disponibilidad y dejar el criterio pendiente si falta la muestra. Depende de T023.

## Dependencias y orden de ejecución

```text
Preparación (T001)
  → Fundamentos (T002 → T003)
  → US1 (T004 ∥ T005 → T006 → T007)
  → US2 (T008 ∥ T009 ∥ T010 → T011 → T012)
  → US3 (T013 ∥ T014 → T015 → T016 → T017 → T018 ∥ T019 → T020)
  → Cierre (T021 → T023 → T024, con T022 en paralelo a T021)
```

- **US1** requiere los fundamentos y puede demostrarse en los niveles existentes.
- **US2** se integra después de US1 porque ambas historias modifican el catálogo y las pruebas de carga. La partida 5 × 5 se puede verificar por separado de la consulta.
- **US3** requiere que US2 ofrezca partidas 5 × 5 publicadas. Sus casos de referencia crean tableros explícitos y se pueden reproducir independientemente del sorteo.
- **Cierre** empieza después de integrar las tres historias. T023 requiere las compilaciones de T021.

## Ejemplos de trabajo paralelo

- **US1** Después de T003, ejecutar T004 para las reglas de apertura y T005 para carga y reintento. Ambas tareas usan archivos de prueba distintos.
- **US2** Después de T007, ejecutar T008, T009 y T010 sobre carga, instantáneas y decisiones de búsqueda. T009 verifica el contrato de almacenamiento y puede hacerse junto con las otras pruebas.
- **US3** Después de T012, ejecutar T013 y T014 en los archivos de pruebas del motor y del estado de interfaz. Tras T017, T018 integra el estado y T019 construye el diálogo. T020 une ambos trabajos.

## Estrategia de implementación

1. **MVP** Completar T001 a T007. Demostrar aperturas aleatorias y el manejo de una carga fallida y una partida guardada.
2. **Segundo incremento** Completar T008 a T012. Demostrar el 5 × 5 desde el selector hasta el resultado y verificar su decisión real a seis movimientos.
3. **Tercer incremento** Completar T013 a T020. Demostrar toda la consulta de dos movimientos y el retorno a la misma partida.
4. **Validación final** Ejecutar T021 a T024 y registrar las comprobaciones y los resultados de aceptación.
