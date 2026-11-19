# Horses Go

*Gotta destroy them all!*

Horses Go es un juego de estrategia por turnos para Android e iOS. Cada participante
controla un caballo en un tablero de ajedrez. La persona juega con el caballo negro
y la máquina, con el blanco.

Android e iOS comparten las pantallas de Compose Multiplatform y el mismo
[tablero Chessground de Lichess](https://github.com/lichess-org/chessground).
El tablero y las piezas vienen incluidos en la app y funcionan sin conexión.
Cada nivel muestra únicamente las casillas de su mapa. Toca un destino marcado
para mover el caballo negro.

## Cómo se juega

1. Elige un nivel y pulsa **Cargar mapa** para iniciar una partida.
2. En tu turno, mueve el caballo en L hasta una casilla libre. Cada jugada válida
   destruye la casilla de origen y cede el turno. Ambos caballos pierden el acceso
   a esa casilla durante el resto de la partida.
3. Cuando un caballo se queda sin movimientos legales en su turno, gana quien
   hizo el último movimiento.

## Niveles de dificultad

| Nivel | Tablero | Profundidad máxima de MiniMax |
| --- | --- | --- |
| Principiante | 4 × 4 | 4 |
| Intermedio | 6 × 6 | 6 |
| Avanzado | 8 × 8 | 8 |

La máquina usa MiniMax para explorar jugadas alternas. Si alcanza el límite de
profundidad en una posición donde la partida sigue en curso, cuenta los saltos
legales del caballo negro. Cada movimiento individual cuenta como un nivel de
profundidad. MiniMax usa poda alfa-beta y calcula en segundo plano.

MMKV guarda el tablero y el turno después de cada jugada. Al reabrir la app se
recupera la última partida, incluido el ganador si ya terminó.

## Ejecutar la app

Instala JDK 17. La primera compilación necesita conexión para descargar las
dependencias. El repositorio incluye el Gradle Wrapper.

**Android.** Con Android SDK 36 y un emulador o dispositivo de 64 bits,
ejecuta desde la raíz y abre Horses Go.

```bash
./gradlew :androidApp:installDebug
```

**iOS.** En una Mac con Apple Silicon, abre `iosApp/iosApp.xcodeproj` con
Xcode y ejecuta el esquema `iosApp` en un iPhone o simulador con iOS 15 o
posterior. El proyecto ejecuta Gradle para integrar
el módulo compartido y empaquetar el tablero. Para instalar la app en un
iPhone físico, selecciona tu equipo en **Signing & Capabilities**.

**Tablero.** La app incluye el HTML ya generado. Si modificas los archivos
de `board/src`, regenera ese recurso con Node.js 24 desde la raíz antes de
compilar para cualquiera de las dos plataformas.

```bash
npm ci --prefix board
npm run build --prefix board
```

Chessground se distribuye bajo GPL-3.0-o-posterior. Las versiones, los
créditos de las piezas y las licencias están en los
[avisos del tablero](board/THIRD_PARTY.md).

Las pruebas compartidas se ejecutan con `./gradlew :shared:allTests`. La
[guía de validación](specs/001-horses-go/quickstart.md) contiene las pruebas de
aceptación y sus resultados.

## Constitución del proyecto

La [constitución de Horses Go v1.0.0](.specify/memory/constitution.md) rige la
especificación, la planificación y la implementación. Establece un motor compartido
en Kotlin Multiplatform, un tablero representado por una matriz de enteros y las
reglas que deben respetar tanto la partida como las simulaciones de MiniMax.
También define los criterios de verificación en Android e iOS y el proceso de
enmienda.
