<!--
Informe de impacto de sincronización
- Versión sin ratificar → 1.0.0 (adopción inicial)
- Principios incorporados: motor compartido en Kotlin Multiplatform, saltos en L y
  destrucción de casillas, victoria por último movimiento, dificultad y MiniMax,
  y selección de nivel y carga del mapa.
- Secciones incorporadas: modelo del tablero y verificación de cambios.
- Secciones eliminadas: ninguna.
- Marcadores pendientes: ninguno.
-->
# Horses Go Constitution

## Core Principles

### I. Motor compartido en Kotlin Multiplatform
Horses Go (Gotta destroy them all!) DEBE ser una aplicación móvil para Android e iOS.
El motor compartido de Kotlin Multiplatform DEBE gobernar el tablero, los turnos y las
decisiones de la máquina en ambas plataformas. La persona juega con el caballo negro
y la máquina, con el blanco.

### II. Saltos en L y destrucción de casillas
Un caballo DEBE desplazarse dos casillas en un eje y una en el otro y aterrizar
en una casilla libre del tablero. Un movimiento válido DEBE destruir la casilla de
origen y ceder el turno. Una casilla destruida DEBE seguir inhabilitada para ambos
caballos durante toda la partida. Un intento inválido DEBE conservar el tablero
y el turno.

### III. Victoria por último movimiento
Si el caballo al que le toca jugar carece de movimientos legales, la partida DEBE
terminar y el oponente, que realizó el último movimiento, DEBE ganar. La interfaz
DEBE mostrar el ganador y cerrar el tablero a nuevos movimientos.

### IV. Dificultad y MiniMax
La máquina DEBE elegir un movimiento legal con MiniMax. La búsqueda DEBE alternar
turnos, aplicar las mismas reglas que la partida y usar el resultado de los estados
terminales. Si alcanza el límite de profundidad en un estado no terminal, DEBE usar
una evaluación heurística. La profundidad se mide en movimientos individuales.

| Dificultad | Tablero | Profundidad máxima |
| --- | --- | --- |
| Principiante | 4 × 4 | 4 |
| Intermedio | 6 × 6 | 6 |
| Avanzado | 8 × 8 | 8 |

### V. Selección de nivel y carga del mapa
La interfaz DEBE permitir elegir la dificultad y ofrecer la acción «Cargar mapa».
Esa acción DEBE iniciar una partida con un tablero nuevo del tamaño seleccionado
y los dos caballos en sus posiciones iniciales.

## Modelo del tablero

El motor DEBE representar el tablero como una matriz de enteros que distinga las
casillas libres, las destruidas y las ocupadas por cada caballo. Las simulaciones de
MiniMax DEBEN preservar el tablero y el turno de la partida hasta aplicar la jugada
elegida.

## Verificación de cambios

La revisión DEBE verificar los saltos en L, los límites del tablero, el rechazo de
destinos ocupados o destruidos, la destrucción del origen, la alternancia de turnos
y la victoria de cualquiera de los participantes. Los cambios a MiniMax DEBEN
verificarse con casos que cubran la profundidad de cada nivel, la elección de destinos
legales y la preservación del estado durante la búsqueda. Los cambios al flujo de
partida DEBEN verificarse en Android e iOS desde la selección de nivel y la carga del
mapa hasta el anuncio del ganador.

## Governance

La constitución rige la especificación, la planificación y la implementación.
Cada enmienda DEBE documentar el motivo y el impacto, contar con la aprobación
de la persona responsable del proyecto y actualizar la fecha de última enmienda.
La revisión de cada cambio DEBE comprobar los principios aplicables y registrar
las verificaciones realizadas y las pendientes.

La versión DEBE seguir SemVer. Los cambios incompatibles en los principios
incrementan la versión mayor. Los principios nuevos o ampliados de forma sustancial
incrementan la versión menor. Las aclaraciones editoriales incrementan el parche.

**Version**: 1.0.0 | **Ratified**: 2026-11-19 | **Last Amended**: 2026-11-19
