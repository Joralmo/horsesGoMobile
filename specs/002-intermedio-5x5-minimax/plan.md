# Implementation Plan: Aperturas aleatorias y nivel intermedio 5 × 5 con MiniMax

**Rama de trabajo** `main` | **Fecha** 2026-11-19 | **Spec** [spec.md](spec.md)

**Feature en Spec Kit** `002-intermedio-5x5-minimax`

## Summary

Sortear las casillas iniciales de los dos caballos al cargar una nueva partida de cualquier nivel. Agregar **Intermedio 5 × 5** al selector actual y conservar los tres niveles existentes. Durante el turno negro del 5 × 5, mostrar un árbol MiniMax de todas las jugadas de la persona y las posibles réplicas de la máquina. El árbol evalúa dos movimientos a partir del tablero publicado. La jugada real de la máquina en ambos niveles intermedios se decide con una búsqueda de hasta seis movimientos.

## Technical Context

| Aspecto | Contexto |
| --- | --- |
| Lenguaje | Kotlin Multiplatform 2.4.21, JDK 17 y Gradle Wrapper 9.3.1. |
| Dependencias | Compose Multiplatform 1.10.3, Lifecycle 2.10.0, corrutinas 1.10.2 y Kotlinx Serialization 1.10.0. El sorteo usa la biblioteca estándar de Kotlin. |
| Almacenamiento | MMKV KMP 2.4.3, con una partida activa en `MatchSnapshot`, esquema 1. El árbol se deriva de la posición visible. |
| Pruebas | `kotlin.test` y `kotlinx-coroutines-test` en `shared/src/commonTest`, más validación manual de Android e iOS. |
| Plataformas | Android API 23 o superior con compile SDK 36. iOS ARM64 para dispositivo y simulador. El proyecto declara un mínimo de iOS 14 y lo eleva a iOS 15 con los SDK de Xcode 27. |
| Proyecto | Aplicación móvil local con motor y UI compartidos. |
| Rendimiento | En cada plataforma, mostrar el árbol completo en un máximo de 2 s en al menos 19 de 20 consultas (SC-004). Límite teórico de 73 nodos por consulta. |
| Restricciones | Compatibilidad con partidas guardadas de las tres dificultades existentes. Las simulaciones usan las reglas del motor y preservan el tablero y el turno publicados. |
| Alcance | Cuatro opciones en el selector. Aperturas aleatorias en las cuatro. Consulta del árbol disponible en el turno activo de la persona en 5 × 5. |

## Constitution Check

*Revisión frente a la constitución v1.0.0 antes de la investigación y tras el diseño.*

| Principio | Antes de fase 0 | Resultado tras fase 1 |
| --- | --- | --- |
| I. Motor compartido | Cumple. El cambio se concentra en `shared`. | Cumple. El sorteo, MiniMax y la interfaz se diseñan en `commonMain` para ambos clientes. |
| II. Saltos en L y destrucción | Cumple. Los saltos mantienen sus reglas. | Cumple. El árbol y la máquina usan `GameRules` y cada rama conserva las casillas destruidas por sus propios saltos. |
| III. Victoria por último movimiento | Cumple. Se mantiene la regla de victoria existente. | Cumple. El árbol y la búsqueda consultan al ganador antes del corte por profundidad. El tablero real mantiene el bloqueo de jugadas al terminar. |
| IV. Dificultad y MiniMax | Cumple. La opción adicional 5 × 5 aplica la profundidad intermedia de seis y conserva la entrada constitucional 6 × 6. | Cumple. La búsqueda real cuenta cada salto, alterna turnos y evalúa movilidad al corte. El árbol de dos saltos es una consulta del turno negro. |
| V. Selección y carga | Cumple. **Cargar mapa** inicia el tablero del nivel elegido. | Cumple. Se sortea al iniciar una carga nueva, se reutiliza la misma apertura al reintentar y se restaura el estado guardado al reanudar. |
| Modelo y verificación | Cumple. Se conservan la matriz de enteros y las pruebas compartidas. | Cumple por diseño. La guía cubre simulación sin mutación, legalidad, profundidad, arranque, ganador y reanudación en ambos clientes. La ejecución de la guía corresponde a la implementación. |

## Project Structure

### Documentation (this feature)

```text
specs/002-intermedio-5x5-minimax/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── gameplay.md
```

### Source Code (repository root)

```text
shared/src/
├── commonMain/kotlin/com/horsesgo/
│   ├── game/      # Catálogo de niveles, tablero, apertura y reglas
│   ├── ai/        # Búsqueda real, evaluación compartida y árbol de consulta
│   ├── storage/   # Validación y restauración del esquema existente
│   └── ui/        # Selector, tablero, estado del turno y diálogo del árbol
└── commonTest/kotlin/com/horsesgo/
    ├── game/
    ├── ai/
    ├── storage/
    └── ui/
androidApp/
iosApp/
```

**Decisión de estructura** Implementar en el módulo compartido y validar desde los dos clientes existentes. Reutilizar el catálogo `Difficulty`, el tablero inmutable, las transiciones de `GameRules` y el flujo de carga y reintento de `GameViewModel`. Conservar el resaltado actual de destinos legales en el tablero.

## Phase 0 — Research

El [registro de decisiones](research.md) documenta el muestreo uniforme, la clave del 5 × 5, la separación de horizontes MiniMax y el contenedor compartido de la vista. Las decisiones se basan en las dependencias y puntos de entrada existentes.

## Phase 1 — Design & Contracts

1. Cambiar la apertura en las reglas del motor y adaptar los casos de prueba que dependen de posiciones fijas. Validar que la selección por sí sola conserve la partida y que el reintento reutilice el tablero pendiente (FR-001 y FR-002).
2. Incorporar la entrada 5 × 5. Comprobar catálogo, guardado y restauración con la clave nueva y las claves existentes. Verificar profundidad seis con una posición de referencia sensible al horizonte real (FR-003 y FR-004).
3. Generar el árbol completo con la evaluación compartida y sin mutar la partida. Publicarlo como estado transitorio del turno negro activo y presentarlo con el diálogo desplazable. Validar ramas, corte terminal, puntuación, elegibilidad y vuelta al tablero (FR-005 a FR-008).

Las entidades y transiciones constan en el [modelo de datos](data-model.md). La interacción se especifica en el [contrato de juego](contracts/gameplay.md) y las pruebas en la [guía de validación](quickstart.md).

## Complexity Tracking

El diseño reutiliza el motor, la persistencia y el estado de interfaz existentes. La revisión posdiseño no identificó excepciones constitucionales bajo la interpretación aditiva del nivel 5 × 5.
