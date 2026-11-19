# Feature Specification: Aperturas aleatorias y nivel intermedio 5 × 5 con MiniMax

**Feature Branch** `main` (rama actual)

**Created** 2026-11-19

**Status** Borrador

**Input** Colocar aleatoriamente los caballos de la persona y de la máquina al iniciar una partida. Agregar un nivel intermedio 5 × 5 en el que la persona pueda consultar un árbol de decisión MiniMax hasta el nivel 2.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Empezar con posiciones aleatorias (Priority: P1)

Como jugador, quiero que ambos caballos aparezcan en casillas elegidas al azar cada vez que cargo un mapa, para encontrar aperturas variadas aunque repita el mismo nivel.

**Why this priority** La variedad de aperturas se aplica a todas las partidas nuevas.

**Independent Test** Cargar varias partidas nuevas de cada nivel y reabrir una partida guardada tras realizar una jugada.

**Acceptance Scenarios**

1. **Dado** cualquier nivel seleccionado, **cuando** la persona pulsa **Cargar mapa** y empieza una partida nueva, **entonces** el caballo negro y el blanco ocupan dos casillas distintas elegidas al azar dentro del tablero, las demás casillas están libres, los dos caballos tienen al menos un salto legal en la posición inicial y el caballo negro tiene el primer turno.
2. **Dado** que la persona ya cargó un mapa del nivel, **cuando** vuelve a cargar ese mismo nivel, **entonces** se sortean de nuevo ambas posiciones. Un sorteo puede repetir posiciones antes vistas.
3. **Dada** una partida guardada, incluso de una versión anterior, **cuando** la persona vuelve a ella, **entonces** recupera la misma dificultad, las posiciones, las casillas destruidas y el turno.
4. **Dada** una carga que falla después de sortear la apertura, **cuando** la persona reintenta esa carga, **entonces** el mapa se abre con las posiciones que ya se habían sorteado.

---

### User Story 2 - Jugar en un tablero intermedio 5 × 5 (Priority: P2)

Como jugador, quiero elegir un nivel intermedio en un tablero de 25 casillas para jugar una partida de dificultad intermedia en un espacio más reducido.

**Why this priority** El nuevo tablero amplía las opciones de juego y ofrece el escenario para consultar el árbol.

**Independent Test** Elegir **Intermedio 5 × 5** en el selector, cargar el mapa y completar una partida.

**Acceptance Scenarios**

1. **Dado** el selector de niveles, **cuando** la persona elige **Intermedio 5 × 5** y pulsa **Cargar mapa**, **entonces** inicia una partida en cinco filas y cinco columnas. El selector ofrece también Principiante 4 × 4, Intermedio 6 × 6 y Avanzado 8 × 8.
2. **Dada** una partida 5 × 5 en curso, **cuando** se realizan jugadas válidas, **entonces** ambos caballos saltan en L a casillas libres, cada salto destruye su origen y cede el turno. Cuando quien tiene el turno se queda sin saltos, la partida termina y gana quien hizo la última jugada.
3. **Dado** un turno de la máquina con una jugada legal disponible, **cuando** le toca responder, **entonces** aplica una de las mejores jugadas legales según MiniMax con un límite de seis movimientos individuales y con evaluación terminal cuando corresponde.

---

### User Story 3 - Consultar el árbol MiniMax de las próximas dos jugadas (Priority: P3)

Como jugador del nivel 5 × 5, quiero ver mis saltos posibles y las respuestas de la máquina para comparar qué opciones me dejan con más movimientos.

**Why this priority** La vista permite entender las alternativas y sus consecuencias antes de mover el caballo.

**Independent Test** Abrir la vista en un turno de la persona con varias jugadas legales, comparar sus ramas con las del tablero y volver a la partida.

**Acceptance Scenarios**

1. **Dado** un turno activo de la persona en 5 × 5, incluido el turno de apertura, **cuando** abre **Ver árbol MiniMax**, **entonces** la raíz muestra la posición actual, el nivel 1 muestra todos sus saltos legales y el nivel 2 muestra todas las respuestas legales de la máquina debajo del salto correspondiente. Cada rama indica quién mueve y las casillas de origen y destino. La vista indica el límite de dos movimientos e identifica qué lado maximiza y cuál minimiza.
2. **Dada** una rama donde uno de los caballos queda sin saltos en su turno, **cuando** la persona la consulta, **entonces** ve quién gana y la rama termina ahí, incluso si la victoria ocurre justo en el nivel 2.
3. **Dados** varios saltos posibles, **cuando** la persona compara las ramas, **entonces** puede ver la cantidad de saltos legales del caballo negro en cada hoja no terminal, la valoración resultante de cada salto mostrado y el valor de la raíz. La valoración favorece una victoria del caballo negro sobre cualquier conteo de saltos y cualquiera de esos conteos sobre una derrota.
4. **Dada** la vista abierta, **cuando** la persona explora las ramas y vuelve al tablero, **entonces** las posiciones, las casillas destruidas y el turno siguen como estaban. Tras completar un turno de la persona y una respuesta de la máquina, la próxima consulta parte de la nueva posición.
5. **Dado** que la máquina está jugando, la partida terminó o se está jugando en otro nivel, **cuando** la persona consulta la pantalla, **entonces** no puede abrir **Ver árbol MiniMax**.

### Edge Cases

- Una apertura puede colocar una pieza en una esquina o repetir las posiciones de una partida anterior. Se aplican las mismas reglas de salto desde cualquier casilla.
- Reintentar la carga fallida del mismo mapa conserva las posiciones ya sorteadas para esa carga. Iniciar otra partida realiza un sorteo nuevo.
- Si un salto de nivel 1 gana la partida, esa rama contiene el resultado y tiene cero réplicas. Si dos saltos tienen la misma valoración, ambos se muestran con ese valor.
- Una simulación hereda las casillas destruidas de su rama. La respuesta de la máquina tampoco puede usar la casilla destruida por el salto inmediatamente anterior.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** Al iniciar cada partida nueva, en cualquiera de los niveles, el juego DEBE sortear dos casillas distintas dentro del tablero, una para cada caballo. La apertura DEBE dejar al menos un salto legal a cada caballo, todas las demás casillas libres y el primer turno al caballo negro.
- **FR-002** El juego DEBE realizar un nuevo sorteo por cada partida nueva, incluso en el mismo nivel. Reintentar la misma carga DEBE conservar el sorteo de esa carga. Reanudar una partida guardada, incluso de una versión anterior, DEBE recuperar su tablero, dificultad y turno guardados.
- **FR-003** El selector DEBE ofrecer **Intermedio 5 × 5** como opción adicional, distinguible por tamaño del intermedio 6 × 6. Al cargarlo, DEBE abrir un mapa de 25 casillas y aplicar las reglas de salto, destrucción, alternancia y victoria de Horses Go.
- **FR-004** La máquina DEBE decidir el salto que realiza en el nivel 5 × 5 con las reglas de evaluación del nivel intermedio y un límite de seis movimientos individuales, incluida la primera jugada candidata.
- **FR-005** El juego DEBE permitir abrir **Ver árbol MiniMax** en los turnos activos de la persona en el nivel 5 × 5, desde el primer turno. La acción queda reservada para esos turnos.
- **FR-006** El árbol DEBE partir del estado actual (nivel 0), mostrar todos los saltos legales de la persona en el nivel 1 y mostrar todas las respuestas legales de la máquina por cada rama en el nivel 2. Cada salto mostrado DEBE identificar el caballo que mueve, su origen y su destino. Cada rama DEBE respetar la ocupación y la destrucción de casillas de sus propios saltos.
- **FR-007** El árbol DEBE mostrar la victoria o derrota en los estados terminales y detener esas ramas. En las hojas no terminales al nivel 2 DEBE mostrar el número de saltos legales del caballo negro. DEBE mostrar los valores resultantes en las ramas y en la raíz, con el caballo negro maximizando y el blanco minimizando. Una victoria del caballo negro DEBE valorarse por encima de cualquier conteo y una derrota, por debajo.
- **FR-008** La vista DEBE indicar su límite de dos movimientos. Abrirla, explorarla y cerrarla DEBE conservar el tablero y el turno. Cada nueva consulta DEBE mostrar la posición del turno desde el que se abre.

### Key Entities *(include if feature involves data)*

- **Apertura.** Par ordenado de casillas iniciales, una para el caballo negro y otra para el blanco, en el tablero de una partida nueva.
- **Nivel.** Opción del selector que determina la dificultad y el tamaño del tablero. El intermedio 5 × 5 habilita la vista del árbol.
- **Árbol de decisión MiniMax.** Representación de la posición actual y de sus continuaciones legales hasta dos saltos. Cada nodo muestra una valoración o un resultado terminal.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** En 20 aperturas nuevas de cada nivel por plataforma, el 100 % muestra dos caballos en casillas válidas y distintas, con al menos un salto legal para cada uno y el primer turno para el caballo negro. En cada serie se observan al menos dos casillas iniciales distintas para cada caballo.
- **SC-002** En Android y en iOS, una persona puede seleccionar **Intermedio 5 × 5**, jugar hasta un ganador anunciado y reanudar una partida guardada de ese nivel con las mismas posiciones, casillas destruidas y turno.
- **SC-003** En 10 posiciones de referencia en 5 × 5, incluidas posiciones con victorias en los niveles 1 y 2, el árbol muestra el 100 % de las ramas legales hasta el límite indicado, con cero ramas ilegales y con valoraciones coherentes con sus hojas.
- **SC-004** Al menos 4 de 5 personas que prueban el nivel por primera vez pueden abrir el árbol, señalar un salto propio y una respuesta asociada de la máquina, y volver a la misma posición en menos de 60 segundos y sin ayuda. En un dispositivo de referencia por plataforma, al menos 19 de 20 consultas muestran el árbol completo en un máximo de dos segundos.

## Assumptions

- El 5 × 5 se incorpora como variante adicional de dificultad intermedia. La decisión de la máquina conserva el límite intermedio de seis movimientos definido en la [constitución](../../.specify/memory/constitution.md). El límite de dos corresponde a la vista que consulta la persona.
- «Nivel 2» se cuenta desde la posición real como nivel 0. Cada salto de un solo caballo suma un nivel. La vista es un análisis del turno actual de la persona y usa las reglas de evaluación existentes a ese horizonte.
- La funcionalidad se apoya en el selector, las reglas de partida y la reanudación existentes de Horses Go en Android e iOS.
