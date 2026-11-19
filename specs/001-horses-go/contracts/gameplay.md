# Contrato de interacción de Horses Go

## Acciones y estados visibles

| Acción o fase | Entrada | Resultado |
| --- | --- | --- |
| Inicio | Abrir la aplicación | Recupera la última partida guardada o muestra el selector. Un resultado guardado se recupera con el tablero final. |
| Elegir dificultad | Principiante, intermedio o avanzado | Actualiza la selección. **Cargar mapa** se habilita con un nivel elegido. |
| Cargar mapa | Confirmar el nivel | Inicia un tablero de ese tamaño con las posiciones de apertura, las demás casillas libres y el turno negro. |
| Jugar | Tocar una casilla en el turno negro | Un salto legal destruye el origen y se guarda antes de presentarse. Tocar un destino inválido conserva la partida. |
| Esperar a la máquina | Turno blanco | Muestra el último estado confirmado y señala el turno blanco. Los toques en casillas quedan deshabilitados. |
| Cambiar mapa | Abrir el selector desde el tablero o el resultado | Permite elegir otro nivel. La partida persistida se reemplaza al confirmar el guardado del mapa nuevo. |
| Finalizar | El participante en turno se queda sin destinos | Anuncia al ganador y conserva el tablero final hasta cargar otro mapa. |

Si se restaura un turno blanco, se vuelve a calcular esa jugada. Al
confirmar otro mapa, cualquier resultado pendiente sobre la partida anterior
queda descartado. La partida terminada restaurada conserva su ganador
y muestra el tablero con los movimientos deshabilitados.

Durante la confirmación de una escritura se bloquean los envíos duplicados.
Si la escritura falla, se informa al jugador y puede reintentar la acción
desde el último estado confirmado. Si falla el guardado de un mapa nuevo,
puede volver a la partida anterior y continuar desde el turno guardado.
Una copia incompatible o ilegible muestra un aviso y permite seleccionar
otro mapa.

## Contrato interno de persistencia

`MatchStore` se inyecta en el ViewModel. La lectura devuelve una instantánea
válida, ausencia, error de formato o error de acceso. La escritura acepta
la [instantánea versionada](../data-model.md#instantánea-en-mmkv) y comunica
si fue aceptada. El adaptador MMKV administra la clave y la codificación.
El ViewModel controla el orden de las transiciones y la cancelación.
