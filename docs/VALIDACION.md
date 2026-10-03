# Validación del Reto 6

Fecha: 3 de octubre de 2026.

Compilación final: `assembleDebug testDebugUnitTest lintDebug`, BUILD SUCCESSFUL.
Las 8 pruebas unitarias pasan (0 errores, 0 fallos), incluyendo copias defensivas del tablero y exploración de todas las continuaciones humanas contra Experto.
Lint: 0 errores; avisos sobre textos en recursos/compatibilidad de APIs, sin bloqueo de compilación.

## Emulador Small Phone, API 37, 720 × 1280, 320 dpi
- Girar durante la espera: exactamente una X y una O, vuelve el turno humano — PASS
- Volver a vertical conserva todas las fichas y el turno — PASS
- Partida llega a un resultado — PASS
- Girar después del resultado conserva tablero y no duplica el marcador — PASS
- Seleccionar dificultad actualiza la pantalla — PASS
- Cerrar con Atrás y reabrir conserva marcadores y dificultad — PASS
- Cerrar con Atrás y reabrir inicia un tablero nuevo — PASS
- Reiniciar marcadores muestra cero — PASS
- Marcadores reiniciados y dificultad sobreviven a reinicio del proceso — PASS
- Archivo persistente confirma marcadores cero y dificultad Fácil — PASS

No se bloquea la orientación y la Activity se recrea realmente. Se desactiva el guardado automático del contenedor raíz porque el mismo ID corresponde a ScrollView en vertical y LinearLayout en horizontal; la partida se restaura explícitamente desde Bundle. El retorno predictivo usa OnBackInvokedDispatcher en API 33+ y onBackPressed es solo el respaldo para versiones anteriores.

La ejecución en API 24–36 no se probó en dispositivos separados.

![Vertical](Reto6-vertical.png)
![Horizontal](Reto6-horizontal.png)
