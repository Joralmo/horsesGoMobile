# Modelo de datos — Aperturas y árbol MiniMax

## Nivel y partida

La entidad `Difficulty` identifica cada opción seleccionable. La tabla recoge la clave que se guarda en la partida, las dimensiones y el máximo de movimientos que examina la máquina, incluida su primera jugada candidata.

| Opción | Clave persistente | Tablero | Búsqueda de la máquina |
| --- | --- | --- | ---: |
| Principiante | `beginner` | 4 × 4 | 4 |
| Intermedio 5 × 5 | `intermediate-5x5` | 5 × 5 | 6 |
| Intermedio 6 × 6 | `intermediate` | 6 × 6 | 6 |
| Avanzado | `advanced` | 8 × 8 | 8 |

`MatchState` contiene el nivel, el tablero y el siguiente turno. Cada `Board` conserva su matriz de enteros, las posiciones de los dos caballos y el número de jugadas. Se conserva la codificación existente: libre 0, destruida 1, negro 2 y blanco 3. El origen de cada salto legal se convierte en una casilla destruida, el destino lo ocupa el caballo y el turno pasa al oponente. Si este se queda sin saltos legales gana el autor del último salto.

`MatchSnapshot` conserva `schemaVersion = 1`, `difficulty`, `cells` y `nextTurn`. La restauración exige dimensiones que coincidan con la clave, exactamente un caballo por color, valores de casilla reconocidos y un turno consistente con la paridad de casillas destruidas. Con cero o un número par de casillas destruidas juega el negro. Con un número impar juega el blanco.

## Apertura

La apertura es el par ordenado `(black, white)` con sendas posiciones `(row, column)`. Ambos índices están en el tablero y son distintos. Con el turno negro activo, todas las casillas restantes quedan libres y ambos caballos pueden saltar. La apertura es el tablero inicial de la partida y se guarda mediante la misma instantánea.

| Acción | Transición |
| --- | --- |
| Seleccionar un nivel | Cambia la opción seleccionada. |
| Cargar mapa nuevo | Se sortea un par, se construye el tablero con cero movimientos y se prepara su guardado como `PendingSave`. |
| Guardado aceptado | La partida pendiente pasa a ser la partida publicada. |
| Guardado rechazado | Queda disponible la misma partida pendiente para **Reintentar** y se conserva la última partida publicada. |
| Reintentar | Se vuelve a guardar la misma partida pendiente, con idéntico par de apertura. |
| Reanudar | Se recuperan las celdas y el turno publicados desde la instantánea, siguiendo el flujo de respuesta automática si toca a la máquina. |

## Árbol de decisión

El árbol pertenece temporalmente a la posición publicada desde la que se abrió el diálogo. Cada nodo representa el estado de una rama y tiene los siguientes datos conceptuales.

| Campo | Significado |
| --- | --- |
| Nivel | Cantidad de saltos desde la raíz. Valores admitidos: 0, 1 y 2. |
| Jugada entrante | Caballo, origen y destino. La raíz representa el tablero actual y carece de jugada entrante. |
| Turno de la posición | Negro (MAX) o blanco (MIN). La raíz empieza en negro. |
| Ganador | Caballo ganador si la posición es terminal. |
| Valor | 9 para victoria negra, −1 para victoria blanca y movilidad negra de 0 a 8 al corte en posiciones no terminales. Los nodos intermedios toman el máximo o mínimo de sus hijos según el turno de la posición. |
| Hijos | Un nodo por cada destino legal cuando la posición tiene movimientos y el nivel es menor que 2. Una posición terminal o de nivel 2 tiene cero hijos. |

Las posiciones de ambos caballos y las casillas destruidas de la raíz se leen del tablero publicado. Cada hijo se obtiene aplicando el salto a la posición simulada de su padre. Las ramas hermanas parten de la misma posición y la simulación se limita a dos saltos.

`GameUiState` mantiene el árbol mientras está visible. Puede abrirse en el turno negro activo del 5 × 5 con la partida publicada, fuera de una escritura pendiente. El diálogo bloquea las acciones del tablero mientras está visible. Cualquier forma de cierre descarta el árbol y conserva la partida. Publicar otra posición o abandonar el tablero también limpia el estado del árbol. Una nueva consulta deriva sus nodos de la posición que esté publicada en ese momento. El árbol queda fuera de la instantánea.
