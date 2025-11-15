# Calibración de la ruleta (asset y física)

Este documento resume cómo calibrar el PNG de la rueda para que el número ganador caiga exactamente bajo el puntero, y deja constancia de los valores elegidos.

## Parámetros relevantes (valores actuales en código)
- Orden: EURO_ORDER europeo (single zero) fijo.
- Radios (fase física):
  - R_TRACK_OUTER = 0.80
  - R_TRANSITION_START = 0.76 (reservado; no usado directamente)
  - R_TRANSITION_END   = 0.70 (reservado; no usado directamente)
  - R_POCKET_RING      = 0.69
- Ventana de aterrizaje (fase 2):
  - |delta| < 0.02 rad (~1.15°) y |ω_bola| < 3.2, o guardia |ω_bola| < 1.2
- Cooldown de diamantes (fase 0): 180 ms
- Precisión: física en Double; Float solo en render.

## Calibración vía BuildConfig (recomendada)
Ahora puedes ajustar el offset y el sentido del asset sin tocar el código Kotlin:
- Edita `app/build.gradle.kts` → `defaultConfig` y ajusta:
  - `buildConfigField("Float", "ROULETTE_ASSET_OFFSET_DEG", "4.8649f")`
  - `buildConfigField("Boolean", "ROULETTE_ASSET_CCW", "false")`
- Estos valores se leen en tiempo de ejecución desde `BuildConfig`:
  - `assetZeroOffsetDeg = BuildConfig.ROULETTE_ASSET_OFFSET_DEG`
  - `counterClockwiseAsset = BuildConfig.ROULETTE_ASSET_CCW`
- Para probar diferentes offsets, cambia el valor y recompila. Puedes crear variantes de build con diferentes offsets si lo prefieres.

## Dónde tocar (alternativa directa en código)
En `pantallas/PantallaRuletaGirando.kt`:
- Llamada a física (GirandoSection → RoulettePhysicsV2):
  - `assetZeroOffsetDeg = BuildConfig.ROULETTE_ASSET_OFFSET_DEG` (o literal en calibración rápida)
  - `counterClockwiseAsset = BuildConfig.ROULETTE_ASSET_CCW`
  - `debugSlots = <true|false>` (solo durante calibración)

## Proceso de calibración (5–10 minutos)
1) Habilitar líneas radiales de depuración
   - Poner `debugSlots = true` en la llamada a `RoulettePhysicsV2`.
   - Compilar y ejecutar.

2) Alinear el anillo de bolsillos con la órbita de la bola
   - Verifica visualmente que la bola (en fase 2) recorre el anillo de bolsillos.
   - Si la órbita queda por fuera/dentro, ajusta finamente `R_POCKET_RING` ±0.005 hasta que coincida.
   - Mantén `R_TRACK_OUTER = 0.80` como carril exterior visual de la bola.

3) Centrar el “0” bajo el puntero (offset)
   - Comienza con `assetZeroOffsetDeg = +4.8649f` (media ranura) o `-4.8649f`.
   - Si sigue corrido, prueba múltiplos: ±9.7297f (1 ranura), ±14.5946f, ...
   - Objetivo: con `debugSlots=true`, las líneas radiales deben pasar por el centro de cada bolsillo del PNG; con `debugSlots=false`, el “0” queda exactamente bajo el puntero.

4) Verificar el sentido del asset (horario vs antihorario)
   - Si el orden visual del PNG está invertido respecto a EURO_ORDER, usa `counterClockwiseAsset = true`.

5) Validar robustez de aterrizaje
   - Ejecuta ≥20 giros (seeds variadas) y confirma que el número reportado coincide con el bolsillo donde se asienta la bola.
   - Escucha los ticks en diamantes durante la fase 0 y el sonido final en el aterrizaje.

6) Desactivar depuración y fijar parámetros
   - Deja `debugSlots = false`.
   - Conserva los valores calibrados de `assetZeroOffsetDeg`, `counterClockwiseAsset` y (si cambió) `R_POCKET_RING`.

## Valores finales (rellenar tras calibrar)
- assetZeroOffsetDeg: <escribir aquí>
- counterClockwiseAsset (true/false): <escribir aquí>
- Radios definitivos:
  - R_TRACK_OUTER = 0.80
  - R_POCKET_RING = <0.69 si se mantiene; otro si se ajustó>

## Checklist de pruebas manuales
- [ ] Con `debugSlots=true`, las líneas radiales pasan por el centro de cada bolsillo.
- [ ] Con `debugSlots=false`, el 0 queda exactamente bajo el puntero con el offset calibrado.
- [ ] ≥100 giros con seeds variadas: el número reportado coincide visualmente.
- [ ] Sin jank ni desbordes en dispositivo medio; al salir del Composable, la animación se cancela.
- [ ] Sonoridad: ticks (diamantes) en fase 0; sonido final al aterrizar.
- [ ] UI coherente (i18n pendiente) y sin solaparse con el panel en pantallas pequeñas.

## Cómo compilar (Windows CMD)
```bat
cd C:\Users\mn_ro\AndroidStudioProjects\RuletaEuropea
gradlew.bat assembleDebug
```

## Notas
- Si el PNG tiene perspectiva o no es perfectamente ortográfico/concéntrico, es más difícil clavar la coincidencia del anillo. Se recomienda usar un asset ortográfico para el anillo de números.
- Extra opcional: exponer `assetZeroOffsetDeg` y `counterClockwiseAsset` vía BuildConfig/Remote Config para recalibraciones sin publicar la app.
