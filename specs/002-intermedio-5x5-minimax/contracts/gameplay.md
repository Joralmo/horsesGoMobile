# Contrato de interacción — Nivel 5 × 5 y consulta MiniMax

## Selección y aperturas

1. El selector presenta **Principiante**, **Intermedio** para 5 × 5, **Intermedio** para 6 × 6 y **Avanzado**, con el tamaño visible y distinguible también al usar un lector de pantalla. La etiqueta visible o accesible de cada variante permite identificar su tamaño.
2. Tras seleccionar una opción, **Cargar mapa** inicia la partida con el caballo negro en turno, un salto disponible para cada caballo y las demás casillas libres. Repetir la acción vuelve a sortear ambos caballos. Es válido que coincida con posiciones anteriores.
3. Si falla el guardado y la persona pulsa **Reintentar**, se abre el tablero sorteado en la carga pendiente cuando se confirma su guardado. Si vuelve a una partida previa, se muestra el estado de esa partida. La reanudación tras reabrir la aplicación recupera la partida guardada y sigue el flujo habitual si hay un turno blanco pendiente.

## Apertura y cierre de la vista

| Estado visible | Comportamiento |
| --- | --- |
| 5 × 5, turno negro activo con tablero listo para jugar | Se ofrece **Ver árbol MiniMax**, también en la apertura. |
| Turno blanco, partida terminada, otro nivel, selector o guardado en curso o fallido | La acción queda fuera de alcance. |
| Árbol visible | Se puede recorrer el contenido y pulsar **Volver al tablero**. Cerrar con el gesto nativo, donde aplique, produce el mismo resultado. |
| Regreso al tablero | Se muestran el mismo turno, los mismos caballos, las mismas casillas destruidas y los mismos destinos legales que antes de abrir. |
| Consulta tras jugar otro turno | La raíz y las ramas corresponden a la nueva posición. |

El diálogo **Árbol MiniMax** muestra el límite de dos movimientos y permite desplazarse verticalmente hasta la última réplica. Mantiene el cierre accesible. El tablero queda inactivo durante la consulta y abrir, recorrer o cerrar la vista no produce jugadas ni escrituras de la partida.

## Lectura de la vista

- **Nivel 0 · Posición actual** muestra las coordenadas de los dos caballos, las casillas destruidas y el valor de la raíz. En apertura se indica que las demás casillas están libres.
- **Nivel 1 · Jugador (MAX)** lista una rama por salto legal negro. Cada rama muestra el origen, el destino y su valor. La raíz toma el mayor de esos valores.
- **Nivel 2 · Máquina (MIN)** agrupa las réplicas legales blancas debajo del salto negro al que responden. Se identifica el origen y el destino de cada réplica. El valor del salto negro toma el menor de los valores de sus réplicas.
- Una hoja no terminal del nivel 2 muestra la cantidad de saltos legales del caballo negro. Una hoja terminal muestra **Gana el jugador** o **Gana la máquina** y queda sin réplicas. Los valores terminales se ordenan según el [modelo de datos](../data-model.md). Los empates conservan todas las ramas con su valoración.

Los saltos del árbol se presentan en texto y respetan las coordenadas del tablero. El árbol describe dos movimientos hipotéticos. El selector indica la profundidad de seis movimientos que usa la máquina al decidir una jugada en el nivel intermedio.
