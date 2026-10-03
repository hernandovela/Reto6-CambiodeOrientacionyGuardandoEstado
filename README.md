# Reto 6: Cambio de orientación y guardando estado

Proyecto Android Studio independiente, Java y vistas XML. Paquete `com.hernandovela.reto6`. Android 7.0 (API 24) o superior; compile/target SDK 37; AGP 9.3.2; Gradle 9.5; JDK incluido en Android Studio.

## Funcionalidad
- Tres en raya contra Android, tablero Canvas con imágenes y sonidos.
- Sonido de victoria al ganar y sonido diferente de derrota cuando gana Android. Respetan la opción Sonido; al girar, el audio en curso continúa sin reiniciarse.
- Diseño visual: X azul, O frambuesa, tablero claro y fondo azul oscuro con degradado. Botón principal destacado y resultado con el color del ganador.
- Vertical: tablero de 250 dp y controles en columna. Horizontal: tablero de 270 dp a la izquierda y controles desplazables a la derecha, sin barra de título.
- `onSaveInstanceState` conserva tablero (copia de char[]), fin de partida, mensaje, primer jugador y turno actual. Android recrea la Activity normalmente: no se bloquea orientación ni se usa configChanges.
- `SharedPreferences` (`ttt_prefs`) conserva victorias, empates, dificultad (ordinal del enum) y sonido. Se escribe al cambiar y en onStop.
- Opciones con icono para reiniciar marcadores; sin opción Salir.
- Dificultades Fácil (aleatoria), Normal (gana/bloquea) y Experto (minimax).
- El Handler se cancela en onPause y se programa una sola respuesta en onResume si sigue siendo turno de Android. Girar durante la espera no duplica movimientos ni deja el turno bloqueado.
- Nueva partida alterna quién empieza. Salir con Atrás y abrir inicia una partida nueva; conserva marcadores y ajustes. La partida transitoria se restaura al girar o tras una recreación gestionada por Android.

## Abrir y ejecutar
1. Android Studio > File > Open > carpeta del proyecto.
2. Instalar SDK Platform 37 si falta y dejar sincronizar Gradle. `local.properties` contiene la ruta local del SDK y no se publica.
3. Seleccionar un emulador API 24+ y pulsar Run (app).
4. Usar los botones de rotación del emulador para probar ambas orientaciones.

En Windows, con JAVA_HOME apuntando al JBR de Android Studio: `gradlew.bat assembleDebug testDebugUnitTest lintDebug`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Verificación manual
1. Jugar, girar y comprobar las mismas fichas y el mismo turno.
2. Girar inmediatamente después de tocar una casilla: Android debe hacer exactamente una respuesta y devolver el turno.
3. Completar una partida, girar varias veces y comprobar que el marcador no se incrementa de nuevo.
4. Cambiar dificultad, cerrar con Atrás y reabrir: conserva dificultad y marcadores.
5. Reiniciar marcadores, cerrar y reabrir: siguen en cero y no cambia la partida actual al reiniciar el marcador.

## Referencias y atribución
Implementación basada en “Challenge: Changing the Orientation and Saving State”, Frank McCown, Harding University, CC BY 3.0: https://creativecommons.org/licenses/by/3.0/ . El tutorial usa Eclipse; este proyecto adapta sus requisitos a Android Studio.
Documentación: https://developer.android.com/topic/libraries/architecture/views/saving-states-views y https://developer.android.com/reference/android/app/Activity .

## Evidencias
Ver [validación y capturas](docs/VALIDACION.md).
