# Plan de implementación de Horses Go

**Rama Git** `main` · **Feature** `001-horses-go` · **Fecha** 2026-11-19

**Entrada** [Especificación de Horses Go](spec.md)

## Summary

Entregar un juego móvil local para Android e iOS desde la selección de nivel
hasta el anuncio del ganador. Un módulo Kotlin Multiplatform comparte Compose
Multiplatform, el motor y MiniMax. El ViewModel coordina las acciones con flujo
unidireccional. Mantiene la partida activa en memoria y guarda una instantánea
en MMKV para restaurarla al reabrir la aplicación.

## Technical Context

| Área | Decisión |
| --- | --- |
| Lenguajes y compilación | Kotlin 2.4.21, AGP 9.1.1, Gradle 9.3.1, JDK 17 y Swift para el arranque iOS |
| Dependencias | Compose Multiplatform 1.10.3, Lifecycle Multiplatform 2.10.0, Coroutines 1.10.2, Kotlin Serialization JSON 1.10.0 y MMKV KMP 2.4.3 |
| Almacenamiento | Una partida local en MMKV con una instantánea JSON versionada |
| Pruebas | `kotlin.test` y `kotlinx-coroutines-test` en código común, más verificación de la app y MMKV en ambas plataformas |
| Plataformas | Android API 23+ de 64 bits, con `compileSdk` y `targetSdk` 36. iOS 14+ en ARM64 para dispositivo y simulador |
| Herramientas iOS | Xcode 26.4 como referencia de Kotlin 2.4.21. En Xcode 27.0 local queda por verificar el enlace |
| Alcance | Una partida local a la vez, selección de dificultad, tablero y resultado |
| Rendimiento | SC-003 exige mostrar al menos el 95 % de las respuestas de la máquina en dos segundos o menos por nivel y plataforma |

### Arquitectura y flujo

1. Compose observa `StateFlow<GameUiState>` y envía acciones al
   `GameViewModel`. El ViewModel recibe el motor, MiniMax y `MatchStore`
   por constructor. Cada punto de arranque inicializa MMKV y crea las
   dependencias con alcance de aplicación o sesión según corresponda.
2. El motor común crea el mapa, enumera saltos, aplica jugadas y determina
   el ganador. Contiene la matriz de enteros. Una jugada aceptada produce
   un nuevo estado con el origen destruido.
3. El ViewModel serializa la restauración, las transiciones y sus escrituras.
   Lee y escribe MMKV en un contexto de E/S. Publica el mapa nuevo o la
   jugada al confirmarse el guardado. El resultado usa el mismo flujo.
4. En el turno de la máquina, el ViewModel ejecuta MiniMax con poda alfa-beta
   en `Dispatchers.Default` sobre un estado inmutable. El cálculo comprueba
   cancelación cooperativa, evalúa primero los estados terminales y cuenta
   los destinos legales del caballo negro al alcanzar el límite.
5. Al confirmar **Cargar mapa**, el ViewModel cancela la búsqueda anterior
   e invalida su generación. Aplica la respuesta de MiniMax solo si
   coinciden la generación, el número de jugada y el turno activos.

### Persistencia y cambio de mapa

**PERSIST-01.** Guardar el mapa inicial y cada jugada aceptada en una sola
clave. Al arrancar, restaurar automáticamente la última partida válida,
incluido el resultado si terminó. Si estaba en turno de la máquina,
reanudar el cálculo sobre el tablero restaurado. Si falta la copia o es
incompatible, abrir el selector. Si se rechaza una escritura, conservar
el último estado confirmado y permitir reintentar la acción.

**MAP-01.** Ofrecer **Cambiar mapa** desde la partida y el resultado.
Confirmar otro nivel reemplaza la partida persistida al guardarla.
Una respuesta anterior de MiniMax queda invalidada aunque llegue después
de la cancelación.

## Constitution Check

| Principio | Antes de investigar | Tras el diseño |
| --- | --- | --- |
| I. Motor compartido | Alineado | `shared/src/commonMain` contiene reglas, turnos y decisiones para Android e iOS. |
| II. Saltos y destrucción | Alineado | El motor valida el destino y destruye el origen en una sola transición. Un intento inválido conserva el estado. |
| III. Último movimiento | Alineado | El ganador se deriva de la falta de jugadas del participante en turno. La interfaz muestra el resultado y bloquea jugadas. |
| IV. MiniMax | Alineado | La búsqueda alterna turnos, usa profundidades 4, 6 y 8 y prioriza estados terminales sobre la heurística. |
| V. Carga del mapa | Alineado | **Cargar mapa** inicia una partida nueva con el nivel elegido. |
| Modelo del tablero | Alineado | Matriz común de enteros y estados inmutables para la búsqueda. La instantánea recupera el mismo tablero y turno. |
| Verificación | Alineado | La guía cubre reglas, resultados, profundidades, restauración y cambio de mapa en ambas plataformas. |

La [constitución](../../.specify/memory/constitution.md) rige la revisión.
La implementación deberá registrar los resultados de las comprobaciones
comunes y por plataforma.

## Project Structure

### Documentation (this feature)

- [Investigación](research.md)
- [Modelo de datos](data-model.md)
- [Contrato de interacción](contracts/gameplay.md)
- [Guía de validación](quickstart.md)

### Source Code (repository root)

```text
androidApp/                                      # Aplicación Android
iosApp/                                          # Host Swift y proyecto Xcode
shared/
└── src/
    ├── commonMain/kotlin/com/horsesgo/
    │   ├── game/                                # Modelo y reglas
    │   ├── ai/                                  # MiniMax y heurística
    │   ├── storage/                             # Instantánea y adaptador MMKV
    │   └── ui/                                  # ViewModel y Compose
    ├── androidMain/kotlin/com/horsesgo/           # Arranque MMKV con Context
    ├── iosMain/kotlin/com/horsesgo/               # Arranque MMKV y controlador
    └── commonTest/kotlin/com/horsesgo/            # Reglas, IA y coordinación
```

Los paquetes de `shared` separan las responsabilidades. Los proyectos
`androidApp` e `iosApp` empaquetan la aplicación para su plataforma.

### Orden de implementación

1. Preparar los dos arranques y comprobar que el módulo compartido y MMKV
   compilan, enlazan y permiten escribir y leer en ambas plataformas.
2. Implementar y verificar el motor común y MiniMax con posiciones
   que cubran los tres niveles.
3. Conectar Compose al ViewModel y al guardado. Validar restauración,
   reemplazo de mapa y tiempos de respuesta con la guía.
