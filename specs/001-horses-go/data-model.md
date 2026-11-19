# Modelo de datos de Horses Go

## Entidades de la partida

| Entidad | Campos y relaciones | Validación |
| --- | --- | --- |
| `Difficulty` | Identificador estable, tamaño, profundidad y posiciones de apertura | Las parejas tamaño/profundidad son 4/4, 6/6 y 8/8. Las posiciones se definen en FR-002. |
| `Board` | Matriz cuadrada de enteros encapsulada | Contiene exactamente un caballo negro y uno blanco. |
| `Position` | Fila y columna desde cero. A1 es fila 0, columna 0. | Los índices deben estar dentro del tablero. |
| `Move` | Origen y destino del caballo en turno | El origen contiene ese caballo. El destino está libre y a un salto en L. |
| `MatchState` | Dificultad, tablero y próximo turno | El motor deriva las posiciones, el número de jugada y el ganador del tablero y el turno. |
| `GameUiState` | Pantalla actual, dificultad seleccionada, partida visible y operación en curso | Las acciones disponibles se derivan de la pantalla, el turno y el resultado. |
| `SearchToken` | Generación de sesión y número de jugada al iniciar MiniMax | El token debe coincidir con el estado vigente y el turno blanco para aplicar el resultado. |

### Codificación de casillas

| Valor | Estado |
| --- | --- |
| 0 | Libre |
| 1 | Destruida |
| 2 | Caballo negro |
| 3 | Caballo blanco |

La primera fila de la matriz corresponde a la fila 1 de la notación del
tablero. Cada jugada válida suma una casilla destruida y cambia el turno.
El estado es terminal si el siguiente turno carece de destinos legales.
El motor asigna la victoria al otro participante.

## Transiciones

| Evento | Estado resultante |
| --- | --- |
| Arranque con copia válida | Recuperar tablero y turno. Derivar el ganador o buscar una jugada si le toca al caballo blanco. |
| Arranque con copia ausente, inválida o inaccesible | Abrir el selector. Mostrar un aviso ante un problema de lectura o formato. |
| Confirmar **Cargar mapa** | Invalidar y cancelar la búsqueda previa. Crear y guardar un mapa nuevo con turno negro antes de publicarlo. |
| Salto de la persona | Si es legal y es su turno, derivar y guardar el nuevo estado. Publicarlo y evaluar si debe iniciarse MiniMax. |
| Respuesta de MiniMax | Validar generación, número de jugada y turno. Aplicar y guardar una sola jugada legal del caballo blanco. |
| Estado terminal confirmado | Mostrar el ganador y bloquear movimientos. El selector sigue disponible. |
| Escritura rechazada | Conservar el estado publicado, indicar el fallo y permitir reintentar la transición. |

El motor devuelve un tablero nuevo en cada jugada y encapsula la matriz
para impedir que la interfaz o una búsqueda alteren un tablero existente.
MiniMax reutiliza el motor en sus estados hipotéticos. La generación de
sesión vive en memoria y cambia al confirmar otro mapa. Un reintento de
jugada se descarta cuando cambia la partida o el turno que lo originó.
Si la escritura del mapa nuevo falla y la persona vuelve a la partida
vigente, el ViewModel inicia una búsqueda con la generación actual
cuando todavía corresponde mover al caballo blanco.

## Instantánea en MMKV

**Instancia** `horses-go` · **Clave** `active-match` · **Formato** JSON

| Campo | Tipo | Regla |
| --- | --- | --- |
| `schemaVersion` | Entero | Versión inicial 1. La lectura acepta versiones soportadas. |
| `difficulty` | Texto | `beginner`, `intermediate` o `advanced`. Fija dimensiones y profundidad. |
| `cells` | Matriz de enteros | Valores y dimensiones del tablero común. Orden de filas desde A1. |
| `nextTurn` | Texto | `black` con cantidad par de casillas destruidas, `white` con cantidad impar. |

Al restaurar, validar la versión, dimensiones, códigos de casilla,
presencia de ambos caballos y coherencia del turno. El motor recalcula el
ganador. La selección de nivel previa a **Cargar mapa**, la generación y
las tareas de búsqueda existen en memoria.

El adaptador escribe el valor completo y solicita sincronización de MMKV
antes de informar el guardado. El ViewModel serializa las escrituras para
que una partida anterior jamás sobrescriba el nuevo mapa. El guardado de
un mapa nuevo reemplaza la copia previa, incluida una copia ilegible o de
versión incompatible.
