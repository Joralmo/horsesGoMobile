# Investigación técnica de Horses Go

## Herramientas de compilación

**Decisión.** Usar Kotlin y los plugins de Compose y serialización de Kotlin 2.4.21,
Compose Multiplatform 1.10.3, AGP 9.1.1, Gradle 9.3.1 y JDK 17. Fijar
`compileSdk` y `targetSdk` en 36 y `minSdk` en 23. Para iOS, usar
`iosArm64` e `iosSimulatorArm64`, iOS 14 como mínimo y Xcode 26.4 como
referencia de compatibilidad.

**Motivo.** Las matrices de JetBrains y Android respaldan la combinación de
Kotlin, AGP, Gradle y JDK. Se consultaron los metadatos publicados del AAR de
Compose UI para Android (Jetpack Compose 1.10.5), que pide al menos API 35 de
compilación. El entorno tiene JDK 17, Android SDK 36 y Xcode 27.0. JetBrains
lista Xcode 26.4 para Kotlin 2.4.21. La implementación tendrá que verificar el
enlace y la ejecución en Xcode 27.0 local o seleccionar Xcode 26.4 para
reproducir la combinación documentada.

**Alternativa evaluada.** Compose Multiplatform 1.12.2 requiere Android SDK
37.1 para compilar. El entorno dispone de 37.0 y 36.

[Matriz de Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html) ·
[Compatibilidad de Compose](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) ·
[AGP 9.1.1](https://developer.android.com/build/releases/agp-9-1-0-release-notes) ·
[Compose 1.12.2](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.12.2)

## Módulo compartido y ciclo de vida

**Decisión.** Un módulo `shared` con Compose Multiplatform, motor, IA y
persistencia. El módulo usa `com.android.kotlin.multiplatform.library`.
`androidApp` empaqueta la app Android. `iosApp` integra el framework de
Kotlin desde Xcode mediante `embedAndSignAppleFrameworkForXcode`.
Usar Lifecycle Multiplatform 2.10.0 y un ViewModel compartido para toda la
sesión, con `StateFlow` de solo lectura e inyección por constructor.

**Motivo.** El alcance tiene un flujo de interfaz y un conjunto de reglas.
El ViewModel puede conservar el estado al cambiar de pantalla y orquestar
la persistencia y la cancelación. El motor queda accesible a pruebas
compartidas de Kotlin.

**Alternativa evaluada.** Módulos Gradle separados para UI y motor permiten
aplicar límites de dependencias en compilación. Los paquetes dentro de
`shared` cubren la organización necesaria para esta partida local.

[Plugin de biblioteca Android para KMP](https://developer.android.com/kotlin/multiplatform/plugin) ·
[ViewModel compartido](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html) ·
[Integración con Xcode](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)

## Persistencia local

**Decisión.** Usar `com.tencent:mmkv-kmp:2.4.3` en `commonMain`, con
inicialización en los arranques específicos de plataforma. Un adaptador detrás
de `MatchStore` guarda una instantánea JSON con versión de esquema mediante
`kotlinx-serialization-json:1.10.0`. MMKV contiene una sola partida local.
La sesión en memoria publica el tablero una vez aceptada la escritura. Al
arrancar, valida y recupera la instantánea.

**Motivo.** Los metadatos publicados de MMKV declaran variantes Android e iOS
y Kotlin stdlib 2.2.20. El AAR contiene `arm64-v8a` y `x86_64`; la guía
requiere Android API 23 y dispositivos de 64 bits. El artefacto iOS incorpora
MMKV Core. El API KMP es experimental, por lo que se fija la versión y se
encapsulan sus llamadas. `encodeString` informa si la escritura fue
aceptada. `sync()` solicita sincronización inmediata y devuelve `Unit`.
Las pruebas en dispositivos cubren el cierre y la reapertura después de la
confirmación. Una única clave mantiene juntos el tablero y el turno.

**Alternativa evaluada.** Los SDK nativos de MMKV también permiten implementar
el mismo contrato mediante adaptadores Android y Swift. Esa opción requiere
mantener dos integraciones de almacenamiento y su enlace por separado.

[MMKV KMP 2.4.3](https://github.com/Tencent/MMKV/blob/v2.4.3/KMP/README.md) ·
[API común de MMKV](https://github.com/Tencent/MMKV/blob/v2.4.3/KMP/mmkv/src/commonMain/kotlin/com/tencent/mmkv/kmp/MMKV.kt) ·
[Requisitos de MMKV para Android](https://github.com/Tencent/MMKV/wiki/android_setup) ·
[FAQ de persistencia de MMKV](https://github.com/Tencent/MMKV/wiki/FAQ)

## Búsqueda y coordinación de turnos

**Decisión.** MiniMax de profundidad fija con poda alfa-beta, una instantánea
inmutable de la partida y Kotlin Coroutines 1.10.2. La máquina minimiza la
movilidad del caballo negro. Los estados terminales se evalúan antes del
límite de profundidad. La búsqueda se ejecuta en `Dispatchers.Default`,
consulta cancelación cooperativa en los nodos y devuelve una jugada. El ViewModel
valida la generación de la partida y el número de jugada antes de aplicarla.

**Motivo.** La poda conserva la evaluación del árbol a la profundidad pedida.
Reutilizar las reglas del motor permite simular los mismos saltos y casillas
destruidas. El cálculo de CPU queda fuera del hilo de interfaz. La validación
del turno protege el mapa actual si una búsqueda anterior llegó a devolver
un resultado mientras se procesaba el cambio de mapa.

**Alternativas evaluadas.** MiniMax exhaustivo sirve como referencia en
posiciones pequeñas. El cálculo con tableros mutables y deshacer jugadas
puede reducir asignaciones si el perfil de rendimiento lo requiere.

[Análisis de la poda alfa-beta](https://www.sciencedirect.com/science/article/pii/0004370275900193) ·
[Dispatchers en Kotlin](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html) ·
[Cancelación cooperativa](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/ensure-active.html)
