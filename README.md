# Horses Go

*Gotta destroy them all!*

Horses Go es un juego de estrategia por turnos para Android e iOS. Cada participante
controla un caballo en un tablero de ajedrez. La persona juega con el caballo negro
y la máquina, con el blanco.

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
profundidad en una posición donde la partida sigue en curso, evalúa esa posición
con una heurística. Cada movimiento individual cuenta como un nivel de profundidad.

## Constitución del proyecto

La [constitución de Horses Go v1.0.0](.specify/memory/constitution.md) rige la
especificación, la planificación y la implementación. Establece un motor compartido
en Kotlin Multiplatform, un tablero representado por una matriz de enteros y las
reglas que deben respetar tanto la partida como las simulaciones de MiniMax.
También define los criterios de verificación en Android e iOS y el proceso de
enmienda.

## Estado del proyecto

El proyecto está en la etapa de constitución. La implementación de la aplicación
está pendiente.
