# Investigación — Aperturas aleatorias y nivel intermedio 5 × 5

## 1. Muestreo de aperturas

- **Decisión** Elegir dos índices de casilla sin reemplazo con `kotlin.random.Random`. Sortear el índice negro en el rango de todas las casillas y el blanco en un rango de una casilla menos. Convertir el segundo índice saltando el del caballo negro. Inyectar la fuente aleatoria en las reglas para poder fijar las extracciones en pruebas.
- **Motivo** `nextInt(until)` da una distribución uniforme en su rango y este mapeo produce todos los pares ordenados de casillas distintas con la misma probabilidad. La enumeración de los cuatro tamaños confirma que cada casilla tiene al menos dos destinos en L sobre el tablero vacío. El otro caballo ocupa, como máximo, uno. Cada pareja empieza con un salto legal por caballo. [API oficial de Kotlin](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.random/-random/next-int.html)
- **Alternativas evaluadas** Barajar todo el tablero requiere recorrer y almacenar la cuadrícula para tomar dos índices. Volver a sortear por colisión también mantendría la uniformidad, con un número variable de extracciones. Exigir que cada pareja difiera de la anterior rompería la independencia entre partidas y añadiría estado.

## 2. Nivel y compatibilidad con partidas guardadas

- **Decisión** Agregar `Intermediate5x5` a `Difficulty`, con clave persistente `intermediate-5x5`, tamaño 5 y horizonte de búsqueda 6. Conservar las claves, tamaños y horizontes de las tres entradas actuales.
- **Motivo** El selector y `GameRules.jumpTables` recorren `Difficulty.entries`. Por su parte, `MatchSnapshot` guarda la clave y las celdas, y restaura con el tamaño de la entrada elegida. El esquema 1 ya expresa el 5 × 5 y conserva las partidas previas. El tablero sorteado se crea en `loadSelectedMap` y se guarda como parte del `PendingSave` que reutiliza `retrySave`.
- **Alternativas evaluadas** Cambiar el tamaño de la entrada `intermediate` haría fallar la validación de los tableros 6 × 6 guardados. Guardar una semilla sería redundante porque la instantánea contiene ambas posiciones y todos los cambios realizados.

## 3. MiniMax exploratorio y decisión de juego

- **Decisión** Generar el árbol completo de dos movimientos con las transiciones de `GameRules` y la misma evaluación de hojas que `MinimaxSearch`. Usar máximo en turno negro y mínimo en turno blanco y comprobar el resultado terminal antes de aplicar el corte por profundidad. La búsqueda de la máquina conserva su poda alfa-beta y la profundidad del nivel.
- **Motivo** La vista debe mostrar todas las alternativas y sus valores a dos saltos. En el peor caso hay 1 raíz, 8 saltos negros y 64 réplicas blancas, un máximo de 73 nodos. El cálculo cabe en una acción acotada sin iniciar otro trabajo asíncrono. El plazo de dos segundos incluye la presentación y se medirá en ambos clientes. `Board.afterLegalMove` construye tableros independientes para las ramas al copiar las filas afectadas.
- **Alternativas evaluadas** Usar la traza de alfa-beta ocultaría ramas y podría devolver cotas donde la vista requiere valores exactos. Mostrar la decisión de seis movimientos con las ramas cortadas visualmente asociaría valores de un horizonte distinto al que describe la vista.

## 4. Presentación y verificación

- **Decisión** Presentar el árbol en un `Dialog` compartido con contenido desplazable y cierre explícito. Mostrar la posición de la raíz, agrupar las réplicas por el salto que las genera y rotular cada turno y evaluación con texto. Exponer apertura y cierre desde el mismo `GameViewModel` que controla la partida.
- **Motivo** Compose Multiplatform admite `Dialog` en código común. El diálogo conserva el contexto de navegación y concentra el gesto de volver en el cierre. El estado del árbol puede vincularse al turno publicado y eliminarse al cerrar, publicar otro tablero o dejar el tablero. Se reutilizarán el tema y los controles actuales. [Compose Multiplatform](https://blog.jetbrains.com/kotlin/2023/08/compose-multiplatform-1-5-0-release/) · [Documentación de Dialog](https://developer.android.com/develop/ui/compose/components/dialog)
- **Alternativas evaluadas** Un grafo horizontal con decenas de nodos requiere zoom y desplazamiento adicional en móvil. Una pantalla nueva requiere coordinar una ruta y su retorno para una consulta breve. Una proyección en el mismo tablero complicaría distinguir un salto hipotético de un movimiento real.
- **Validación** Usar `kotlin.test`, `runTest`, el oráculo exhaustivo y el almacén en memoria del proyecto. Sustituir las aperturas fijas de las pruebas de reglas por estados explícitos y usar secuencias de extracciones controladas al probar el sorteo y el reintento. Verificar el flujo y el tiempo de presentación en Android e iOS.
