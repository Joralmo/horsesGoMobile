# Feature Specification: Horses Go

**Feature Branch** `main` (rama actual)

**Created** 2026-11-19

**Status** Borrador

**Input** Juego móvil de estrategia por turnos entre una persona y la máquina,
con casillas que se destruyen, tres dificultades y un rival que decide con MiniMax.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Jugar hasta el último movimiento (Priority: P1)

Como jugador, quiero mover el caballo negro y ver la respuesta del caballo blanco
hasta que uno de los dos se quede sin saltos, para saber quién ganó.

**Why this priority** Una partida completa permite jugar y ganar o perder con las
reglas centrales de Horses Go.

**Independent Test** Cargar un mapa de principiante, realizar saltos legales e
intentos inválidos y completar partidas que cubran una victoria de cada participante.

**Acceptance Scenarios**

1. **Dado** un tablero con turno de la persona, **cuando** toca una casilla libre a
   un salto en L, **entonces** el caballo negro ocupa el destino, su origen se
   destruye y la máquina recibe el turno si aún tiene jugadas.
2. **Dado** el turno de la persona, **cuando** intenta ocupar una casilla destruida,
   ocupada o fuera del alcance de un salto en L, **entonces** se conservan el
   tablero y el turno.
3. **Dado** el turno de la máquina con al menos un movimiento legal, **cuando**
   realiza su jugada, **entonces** el caballo blanco ocupa un destino legal,
   su origen se destruye y la persona recibe el turno si aún tiene jugadas.
4. **Dado** que la última jugada de la persona deja a la máquina sin destinos
   legales, **cuando** se resuelve el siguiente turno, **entonces** se anuncia la
   victoria de la persona y el tablero queda cerrado a nuevas jugadas.
5. **Dado** que la última jugada de la máquina deja a la persona sin destinos
   legales, **cuando** se resuelve el siguiente turno, **entonces** se anuncia la
   victoria de la máquina y el tablero queda cerrado a nuevas jugadas.
6. **Dado** un tablero en juego que contiene casillas destruidas, **cuando** la
   persona consulta la partida, **entonces** puede identificar ambos caballos,
   el turno actual y qué casillas están libres o destruidas.

---

### User Story 2 - Elegir dificultad y cargar el mapa (Priority: P2)

Como jugador, quiero elegir el tamaño y el nivel de desafío antes de que comience
la partida.

**Why this priority** La selección permite empezar con un tablero pequeño y avanzar a
partidas con más espacio y mayor profundidad de búsqueda.

**Independent Test** Cargar, desde el selector, una partida de cada dificultad y
comprobar sus dimensiones, posiciones iniciales y turno de apertura.

**Acceptance Scenarios**

1. **Dado** que se muestra el selector, **cuando** la persona elige un nivel,
   **entonces** el nivel queda seleccionado y la partida aún espera la acción
   **Cargar mapa**.
2. **Dado** un nivel seleccionado, **cuando** pulsa **Cargar mapa**, **entonces**
   aparece el tablero de 4 × 4, 6 × 6 u 8 × 8 que corresponde al nivel, con ambos
   caballos en las posiciones de apertura, sin casillas destruidas y con el
   primer turno para la persona.
3. **Dado** que una partida terminó y la persona vuelve al selector, **cuando**
   carga un mapa, **entonces** comienza una nueva partida con las posiciones
   iniciales y todas las casillas desocupadas habilitadas.

---

### User Story 3 - Enfrentarse a una máquina que anticipa jugadas (Priority: P3)

Como jugador, quiero que la máquina evalúe mis respuestas posibles y busque dejar
a mi caballo sin movimientos, para enfrentar un rival estratégico.

**Why this priority** Las decisiones de la máquina dan sentido a la dificultad
elegida y al uso de las casillas destruidas como parte de la estrategia.

**Independent Test** Evaluar posiciones representativas con victorias forzadas,
derrotas evitables y estados no terminales en el límite de profundidad.

**Acceptance Scenarios**

1. **Dado** un turno de la máquina con una victoria garantizada dentro de la
   profundidad del nivel, **cuando** decide su jugada, **entonces** elige una
   opción que conserva esa victoria frente a cualquier réplica legal.
2. **Dados** estados no terminales que difieren en la movilidad del caballo negro,
   **cuando** se evalúan en el límite de búsqueda, **entonces** cada estado recibe
   el conteo de saltos legales del caballo negro y la máquina prefiere el conteo
   menor bajo la mejor respuesta de la persona.
3. **Dado** un estado que termina justo en el límite de búsqueda, **cuando** se
   evalúa, **entonces** se aplica el resultado de victoria o derrota y los
   movimientos hipotéticos dejan intacto el tablero visible.
4. **Dado** un turno de la máquina, **cuando** la persona toca el tablero antes
   de que se aplique la respuesta, **entonces** el toque no altera el tablero ni
   el turno.
5. **Dado** un caso de cada nivel con ramas que siguen abiertas al alcanzar el
   límite, **cuando** la máquina compara sus opciones, **entonces** evalúa esas
   ramas tras 4, 6 u 8 jugadas alternas según el nivel, incluida la primera
   jugada candidata.

---

### Edge Cases

- Los destinos fuera del tablero se excluyen al calcular saltos legales. Un
  caballo puede saltar sobre casillas ocupadas o destruidas si su destino es libre.
- Si ambos caballos carecen de saltos al terminar una jugada, pierde quien tiene
  el turno siguiente y gana quien hizo la última jugada.
- Si varias jugadas de la máquina tienen la misma valoración, cualquiera de las
  mejores es válida.
- Si la máquina solo tiene un salto legal, realiza ese salto y destruye su origen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001** La interfaz DEBE mostrar los tres niveles de la tabla y esperar a que
  la persona seleccione uno y pulse **Cargar mapa** para iniciar una partida.
- **FR-002** Cada mapa cargado DEBE tener las dimensiones del nivel elegido. Las
  posiciones iniciales del caballo negro y blanco DEBEN ser B2 y C3 en 4 × 4,
  C3 y D4 en 6 × 6 y D4 y E5 en 8 × 8. El caballo negro DEBE empezar y las
  demás casillas DEBEN estar libres.
- **FR-003** La interfaz DEBE distinguir las casillas libres, las destruidas y las
  ocupadas por cada caballo, e identificar el turno actual durante la partida.
- **FR-004** Solo DEBE aceptarse una jugada del participante en turno hasta una
  casilla libre dentro del tablero, a dos casillas de distancia en un eje y una en
  el otro. Un intento inválido DEBE conservar el estado de la partida.
- **FR-005** Cada jugada válida DEBE destruir exactamente su casilla de origen,
  ocupar el destino y ceder el turno. La casilla destruida DEBE quedar
  inhabilitada para ambos caballos hasta el final de la partida.
- **FR-006** Si el participante en turno carece de jugadas legales, la aplicación
  DEBE anunciar al otro participante como ganador y bloquear nuevos movimientos.
- **FR-007** La máquina DEBE elegir un salto legal con MiniMax. La búsqueda DEBE
  aplicar las reglas de la partida y alternar jugadas de ambos caballos hasta
  alcanzar un estado terminal o el límite del nivel. Cada salto, incluido el de
  la primera jugada candidata, DEBE contar como un movimiento de profundidad.
- **FR-008** En estados no terminales al límite de búsqueda, la heurística DEBE
  contar los destinos legales del caballo negro. La máquina DEBE minimizar ese
  conteo y la búsqueda DEBE considerar que la persona intenta maximizarlo. La
  máquina DEBE preferir una victoria forzada y evitar una derrota forzada cuando
  el árbol evaluado ofrece una opción mejor.
- **FR-009** El tablero y el turno visibles DEBEN conservarse mientras la máquina
  evalúa jugadas hipotéticas. La máquina DEBE aplicar una sola jugada por turno
  cuando tiene destinos legales.
- **FR-010** Desde el resultado de una partida la persona DEBE poder volver al
  selector y cargar un tablero nuevo.

| Dificultad | Tablero | Profundidad máxima |
| --- | --- | --- |
| Principiante | 4 × 4 | 4 jugadas |
| Intermedio | 6 × 6 | 6 jugadas |
| Avanzado | 8 × 8 | 8 jugadas |

**Regla de evaluación.** La movilidad inicial del caballo negro alcanza el
máximo del tablero para cada tamaño, 4, 8 y 8, respectivamente. Su movilidad es
cero cuando pierde por falta de saltos. La heurística estima las opciones del
caballo negro. Una garantía de admisibilidad como estimación de costo requeriría
definir un costo acumulado hasta la meta y demostrar que la heurística es una
cota inferior de ese costo.

**Dependencia.** El modelado y las plataformas de destino se rigen por la
[constitución de Horses Go](../../.specify/memory/constitution.md).

### Key Entities *(include if feature involves data)*

- **Partida.** Reúne el tablero, el nivel, el participante en turno y el ganador
  cuando la partida termina.
- **Tablero y casilla.** Cuadrícula con columnas identificadas por letras y filas
  por números a partir de la esquina inferior izquierda. Cada casilla tiene una
  coordenada y uno de cuatro estados, libre, destruida, ocupada por el caballo
  negro u ocupada por el caballo blanco.
- **Caballo.** Pieza con color, controlador y posición. La persona controla el
  negro y la máquina, el blanco.
- **Jugada.** Salto con un origen, un destino y un caballo. Cada jugada válida
  cambia el estado de ambas casillas.
- **Nivel.** Selección que fija las dimensiones, las posiciones iniciales y la
  profundidad de decisión de la máquina.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001** En cada plataforma, los tres niveles abren un tablero del tamaño
  esperado al pulsar **Cargar mapa** y permiten completar una partida hasta
  un ganador anunciado.
- **SC-002** En el 100 % de los casos de aceptación, una jugada inválida conserva
  el tablero y el turno, una jugada válida invalida su origen y el ganador
  anunciado coincide con quien realizó el último salto.
- **SC-003** En un dispositivo de referencia por plataforma, al menos el 95 %
  de 30 turnos de la máquina por nivel con un salto disponible muestran su
  jugada en un máximo de dos segundos desde que recibe el turno.
- **SC-004** Al menos cuatro de cinco personas que juegan por primera vez cargan
  un mapa y hacen su primer salto legal en menos de 60 segundos, sin ayuda.

## Assumptions

- El alcance inicial es una partida local para una persona contra la máquina,
  desde la selección del nivel hasta el resultado y el inicio de otra partida.
- El caballo negro abre cada partida. Los caballos comienzan en las dos casillas
  centrales de la diagonal B2–C3, C3–D4 o D4–E5 según el nivel.
- La heurística usa la movilidad del caballo negro porque el objetivo de la
  máquina es dejarlo sin movimientos. Los resultados terminales se resuelven
  por separado con la regla del último movimiento.
- Los umbrales de respuesta y de inicio de partida son objetivos iniciales para
  validar la experiencia móvil.
