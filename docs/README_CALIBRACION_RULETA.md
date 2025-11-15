# Calibración de la ruleta (assets PNG) – com.api.ruletaeuropea

Requisitos del asset
- Archivo: `app/src/main/res/drawable/ruleta.png` (nombre de recurso `R.drawable.ruleta`).
- PNG circular sin puntero y con fondo verdaderamente transparente.
- La corona de números debe estar centrada (lo más circular posible) para un acierto preciso.

Render y recorte
- La rueda se pinta con `Image(...).aspectRatio(1f).clip(CircleShape)` y rota bajo un puntero fijo a las 12.
- Si el PNG trae esquinas o un fondo ajedrez, el `clip(CircleShape)` las recorta.

Orden y mapeo
- Orden europeo single-zero (`EURO_ORDER`) fijo en el código.
- Tamaño de casilla: `360/37 = 9.7297°`. Media casilla: `4.8649°` (redondear a 4.86° en botones).

Ajustes de calibración
1) Offset del 0 (assetZeroOffsetDeg)
- Si el 0 del PNG no queda exactamente bajo el puntero tras caer en 0, ajusta el offset.
- Recomendación: usa incrementos de ±4.8649° (1/2 casilla) o ±9.7297° (1 casilla).
- Dónde: `GirandoSection` → parámetro `assetZeroOffsetDeg` en `RoulettePhysicsV2` (o `ControlledRouletteWheel`).

2) Sentido del asset (counterClockwiseAsset)
- Si los números del PNG están en sentido antihorario, activa `counterClockwiseAsset = true`.
- Se invierte la rotación visual manteniendo el orden europeo interno.

3) Depuración visual de casillas (opcional)
- Pasa `debugSlots = true` a `RoulettePhysicsV2(...)` para dibujar divisiones por ranura.

4) Verificación por logs
- Filtra Logcat por `RuletaMapping`. Al aterrizar verás:
  `Landed wheelDeg=X ballDeg=Y relativeDeg=Z idx=I numero=N offsetDeg=O ccw=W seed=S`
- `numero` debe coincidir con el número bajo el puntero. Si no, ajusta `assetZeroOffsetDeg` y/o `counterClockwiseAsset`.

Herramienta de calibración interactiva
- Usa `RouletteCalibrationPreview(wheelRes = R.drawable.ruleta, assetZeroOffsetDeg = 0f)` en una pantalla temporal.
- Botones incluidos para ir cambiando `probeNumber` (número bajo puntero) y offsets de ±4.86°/±9.73°.
- Objetivo: con `probeNumber=0` el símbolo 0 real del asset queda bajo el puntero.

## Modelo físico (RoulettePhysicsV2)
Sustituye el giro "scripted" por simulación donde la bola determina el resultado.
Fases:
- Fase 0: Rodadura en pista exterior inclinada (φ ≈ 18°) con pérdida de energía por fricción + arrastre y micro ruido.
- Fase 1: Transición radial: masa–muelle–amortiguador hacia el anillo de bolsillos (`R_POCKET_RING`).
- Fase 2: Captura angular: resorte + amortiguación centrando la bola en el bolsillo elegido geométricamente.
Diamantes (deflectores): 8 (cada 45°) con ventana de impacto ≈1.7°, choques inelásticos (restitución 0.78 ± 0.03) que invierten parcialmente la velocidad angular, generan tick.wav + haptic corto y añaden un pequeño kick radial.
Integración: bucle ~60 fps, clamp dt ≤ 25 ms, sub-stepping 8 ms, integrador semi-implícito Euler.
Determinismo: parámetro opcional `seed: Long?` en `RoulettePhysicsV2`. Si se fija, la secuencia de ruido y restituciones se reproduce.

### Cálculo geométrico del ganador
- slotRad = 2π / 37.
- thetaRel = normalizeAngle(ballTheta - wheelTheta - offsetRad).
- idx = floorMod(((thetaRel/slotRad) + 0.5).toInt(), 37).
- número = EURO_ORDER[idx].

### Parámetros físicos (por defecto)
- φ = 18°
- C_R = 1.1 (fricción lineal)
- C_A = 0.04 (arrastre cuadrático)
- Radial: k = 180, c = 12
- Angular captura: k_ang = 55, c_ang = 8
- Diamantes: 8, ventana 1.7°
- Restitución base: 0.78 ± 0.03
- Radios relativos: pista exterior 0.82, anillo bolsillos 0.67, transición 0.78 → 0.68

## Cómo medir y aplicar el offset en tu asset PNG
1. Abre `ruleta.png` en un editor que permita guías (Figma, Photoshop, GIMP).
2. Asegúrate de que el lienzo no tiene relleno extra: la rueda debe ocupar todo el cuadrado.
3. Coloca una guía vertical en el centro (puntero 12 en punto) y otra horizontal para referencia.
4. Identifica el centro exacto del número 0 (o la línea que separa 0 y el siguiente número si usas anillo con divisores).
5. Mide el ángulo entre el puntero (arriba) y el centro del 0:
   - Puedes aproximar contando píxeles a lo largo de la circunferencia: ángulo ≈ (distancia_px / perímetro_px) * 360.
   - Mejor: usa herramienta de ángulo polar si el editor la ofrece.
6. Calcula el desfase: offsetDeg = (ángulo_actual_del_0) * signo.
   - Si el 0 aparece a la derecha (sentido horario positivo CCW interno), offset suele ser positivo.
   - Si aparece a la izquierda, será negativo.
7. Redondea y prueba primero con múltiplos de 4.8649° (media casilla). Ajusta finamente si necesario.
8. Establece ese valor en `assetZeroOffsetDeg` y ejecuta 3–5 giros para confirmar que `numero` en logs coincide.

Conversión rápida por índice
- Si el 0 está desplazado exactamente X casillas completas desde el puntero: offsetDeg = X * 9.7297° (usa signo según dirección).
- Si media casilla: ±4.8649°.

## Uso de `seed`
- Para reproducir un giro exacto (útil en QA o demos), fija `seed = 123456L` (por ejemplo) cada vez que crees `RoulettePhysicsV2`.
- Cambiar la semilla modifica la secuencia de choques y ligero ruido, manteniendo el mismo modelo determinista.

## debugSlots
- Activar `debugSlots = true` dibuja 37 líneas radiales que facilitan verificar que el número bajo el puntero coincide con el índice calculado.
- Útil antes de exportar el asset definitivo.

## Fallback de recursos
- Si `R.drawable.ruleta` no existe aún, el código usa `R.drawable.ruleta_girando` como backup.
- Sonidos: `tick.wav` y `final_sound.wav` en `res/raw`. Si no están presentes se evita crash (IDs inválidos se ignoran).

## Pruebas manuales sugeridas
- 20 giros con números aleatorios: el número bajo el puntero coincide con el log (RuletaMapping) tras calibración.
- Con `counterClockwiseAsset=true` y un asset CCW, también acierta tras calibrar.
- La rueda se ve perfectamente circular a diferentes tamaños; sin bordes ni esquinas.
- Ticks audibles y haptic en impactos con diamantes (fase 0); "clack" final (final_sound.wav + haptic largo) en asentamiento.
- Rotación de pantalla: no duplica escrituras; el resultado se conserva.
- Rendimiento estable: sin jank visible (profiling recomendado en gama media).

## Checklist de aceptación (resumen)
- Alineación visual correcta (0 bajo puntero con offset calibrado).
- Mapeo geométrico coincide con número mostrado ≥ 20 giros.
- Parámetros físicos producen desaceleración natural (no stops bruscos).
- Diamantes generan variación y ticks consistentes.
- No se rompen rutas, navegación ni persistencia (Room escribe una sola vez).
- Calibración reproducible con `RouletteCalibrationPreview`.

## Troubleshooting rápido
| Problema | Causa probable | Acción |
|----------|----------------|-------|
| Número incorrecto bajo puntero | offset mal calibrado | Ajustar ±4.8649° / ±9.7297° y revisar log idx/numero |
| Bola nunca cae | omega nunca < velocidad crítica | Verificar constantes φ, C_R, C_A (valores por defecto) |
| Jank / saltos | dt > 25 ms recurrente | Revisar carga en composición; evitar trabajo pesado en recomposición |
| Asset invertido | PNG CCW sin flag | Activar `counterClockwiseAsset=true` |
| Sonido no se reproduce | Faltan raw/tick.wav o final_sound.wav | Añadir archivos en `res/raw` |

---
Última actualización: 2025-11-08
