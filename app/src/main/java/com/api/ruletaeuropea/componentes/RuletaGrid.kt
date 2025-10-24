package com.api.ruletaeuropea.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.graphicsLayer

/* ====== Colores ====== */
private val TableGreen = Color(0xFF157A3E)
private val PocketGreen = Color(0xFF0FA54B)
private val RouletteRed = Color(0xFFB51414)
private val RouletteBlack = Color(0xFF101010)
private val IvoryText = Color(0xFFF3F1E8)
private val Gold = Color(0xFFD4AF37)

/* ====== Números rojos ====== */
private val RedNumbers = setOf(
    1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
)

/* ====== Filas ====== */
private val TopRow = listOf(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36)
private val MiddleRow = listOf(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35)
private val BottomRow = listOf(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34)

/* ====== Utilidades ====== */
private fun colorForNumber(n: Int): Color =
    when {
        n == 0 -> PocketGreen
        RedNumbers.contains(n) -> RouletteRed
        else -> RouletteBlack
    }

private fun coinRes(valor: Int): Int = when (valor) {
    1 -> R.drawable.coin1
    5 -> R.drawable.coin5
    10 -> R.drawable.coin10
    20 -> R.drawable.coin20
    50 -> R.drawable.coin50
    100 -> R.drawable.coin100
    else -> R.drawable.coin1
}

/* ====== TABLERO ====== */
@Composable
fun RuletaGrid(
    monedaSeleccionada: Int,
    apuestas: List<Apuesta>,
    onApuestaRealizada: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(0.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val spacing = 0.dp
        val zeroFactor = 0.9f       // reducir ancho del 0 para ganar celda
        val sectionFactor = 1.0f    // bandas algo más compactas

        val cellFromWidth  = (maxWidth  - spacing * 12) / (12f + zeroFactor)
        val cellFromHeight = (maxHeight - spacing * 5)  / (3f + 2f * sectionFactor)
        var cellSize = if (cellFromWidth < cellFromHeight) cellFromWidth else cellFromHeight

        val minCell = 26.dp
        val maxCell = when {
            maxWidth < 400.dp -> 60.dp
            maxWidth < 600.dp -> 68.dp
            else -> 84.dp
        }
        cellSize = cellSize.coerceIn(minCell, maxCell)

        val zeroWidth = cellSize * zeroFactor
        val sectionHeight: Dp = cellSize * sectionFactor

        val boardWidth: Dp   = (cellSize * (12f + zeroFactor)) + spacing * 12
        val numbersWidth: Dp = (cellSize * 12) + spacing * 11
        val borderWidth = 1.dp

        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            // ====== 0 + 12x3 ======
            Row(
                modifier = Modifier.width(boardWidth),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Columna del 0
                Box(
                    modifier = Modifier
                        .width(zeroWidth)
                        .height((cellSize * 3) + (spacing * 2))
                        .background(PocketGreen)
                        .border(borderWidth, Gold)
                        .clickable { onApuestaRealizada(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("0", color = IvoryText, fontSize = (cellSize.value * 0.4f).sp, fontWeight = FontWeight.ExtraBold)
                    val apuestasCero = apuestas.filter { it.numero == 0 }
                    CoinsOverlay(apuestasCero, coinSize = cellSize, scale = 1.12f)
                }

                // Cuadrícula 12x3
                Column(
                    modifier = Modifier.width(numbersWidth),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    NumberRow(TopRow, apuestas, onApuestaRealizada, cellSize, spacing, borderWidth)
                    NumberRow(MiddleRow, apuestas, onApuestaRealizada, cellSize, spacing, borderWidth)
                    NumberRow(BottomRow, apuestas, onApuestaRealizada, cellSize, spacing, borderWidth)
                }
            }

            Spacer(Modifier.height(spacing * 2))

            // ====== Docenas ======
            Row(
                modifier = Modifier
                    .width(boardWidth)
                    .height(sectionHeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Spacer(modifier = Modifier.width(zeroWidth))
                DozenBox("1-12",  -101, apuestas, sectionHeight, cellSize, borderWidth) { onApuestaRealizada(-101) }
                DozenBox("13-24", -102, apuestas, sectionHeight, cellSize, borderWidth) { onApuestaRealizada(-102) }
                DozenBox("25-36", -103, apuestas, sectionHeight, cellSize, borderWidth) { onApuestaRealizada(-103) }
            }

            Spacer(Modifier.height(spacing))

            // ====== Apuestas externas ======
            Row(
                modifier = Modifier
                    .width(boardWidth)
                    .height(sectionHeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Spacer(modifier = Modifier.width(zeroWidth))
                OutsideBetBox(label = "1 a 18", codigo = -201, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-201) }
                OutsideBetBox(label = "Par",    codigo = -202, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-202) }
                OutsideBetBox(imagenRes = R.drawable.diamante_rojo, codigo = -203, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-203) }
                OutsideBetBox(imagenRes = R.drawable.diamante_negro, codigo = -204, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-204) }
                OutsideBetBox(label = "Impar",  codigo = -205, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-205) }
                OutsideBetBox(label = "19 a 36",codigo = -206, apuestas = apuestas, sectionHeight = sectionHeight, cellSize = cellSize, borderWidth = borderWidth) { onApuestaRealizada(-206) }
            }
        }
    }
}

/* ====== Filas / Celdas ====== */

@Composable
private fun NumberRow(
    numbers: List<Int>,
    apuestas: List<Apuesta>,
    onApuestaRealizada: (Int) -> Unit,
    cellSize: Dp,
    cellSpacing: Dp,
    borderWidth: Dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
        numbers.forEach { numero ->
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .background(colorForNumber(numero))
                    .border(borderWidth, Gold)
                    .clickable { onApuestaRealizada(numero) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = IvoryText,
                    fontSize = (cellSize.value * 0.33f).sp,
                    fontWeight = FontWeight.Bold
                )
                val apuestasNumero = apuestas.filter { it.numero == numero }
                CoinsOverlay(apuestasNumero, coinSize = cellSize, scale = 1.12f)
            }
        }
    }
}

@Composable
private fun RowScope.DozenBox(
    text: String,
    codigo: Int,
    apuestas: List<Apuesta>,
    sectionHeight: Dp,
    cellSize: Dp,
    borderWidth: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(TableGreen.copy(alpha = 0.25f))
            .border(borderWidth, Gold)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Gold,
            fontSize = (cellSize.value * 0.36f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        val apuestasDozen = apuestas.filter { it.numero == codigo }
        CoinsOverlay(apuestasDozen, coinSize = sectionHeight * 0.98f, scale = 1.06f)
    }
}

@Composable
private fun RowScope.OutsideBetBox(
    label: String? = null,
    imagenRes: Int? = null,
    codigo: Int,
    apuestas: List<Apuesta>,
    sectionHeight: Dp,
    cellSize: Dp,
    borderWidth: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(TableGreen.copy(alpha = 0.25f))
            .border(borderWidth, Gold)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            imagenRes != null -> Image(
                painter = painterResource(id = imagenRes),
                contentDescription = "Apuesta color",
                modifier = Modifier.size(sectionHeight * 0.98f)
            )
            label != null -> Text(
                text = label,
                color = Gold,
                fontSize = (cellSize.value * 0.36f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        val apuestasOutside = apuestas.filter { it.numero == codigo }
        CoinsOverlay(apuestasOutside, coinSize = sectionHeight * 0.98f, scale = 1.06f)
    }
}

/* ====== Overlay de fichas ====== */

@Composable
private fun BoxScope.CoinsOverlay(apuestas: List<Apuesta>, coinSize: Dp, scale: Float = 1f) {
    if (apuestas.isEmpty()) return

    val total = apuestas.sumOf { it.valorMoneda }
    val counts = apuestas.groupingBy { it.valorMoneda }.eachCount().toList().sortedBy { it.first }
    val maxTypesToShow = 3
    val display = counts.take(maxTypesToShow)

    // Total
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(2.dp)
            .background(Gold, RoundedCornerShape(6.dp))
            .padding(horizontal = (coinSize * 0.12f), vertical = (coinSize * 0.06f))
    ) {
        Text(
            text = total.toString(),
            color = Color.Black,
            fontSize = (coinSize.value * 0.27f).sp,
            fontWeight = FontWeight.Bold
        )
    }

    val delta = (coinSize * 0.35f)
    val offsets = when (display.size) {
        1 -> listOf(0.dp)
        2 -> listOf(-delta, delta)
        else -> listOf(-delta, 0.dp, delta)
    }

    display.forEachIndexed { index, (denom, count) ->
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offsets.getOrElse(index) { 0.dp })
                .size(coinSize)
        ) {
            Image(
                painter = painterResource(id = coinRes(denom)),
                contentDescription = "Apuesta $denom",
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
            if (count > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding((coinSize * 0.04f))
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                        .padding(horizontal = (coinSize * 0.09f), vertical = (coinSize * 0.04f))
                ) {
                    Text(
                        text = "x$count",
                        color = Color.White,
                        fontSize = (coinSize.value * 0.24f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    val remainingTypes = counts.size - display.size
    if (remainingTypes > 0) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding((coinSize * 0.04f))
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                .padding(horizontal = (coinSize * 0.09f), vertical = (coinSize * 0.04f))
        ) {
            Text(
                text = "+$remainingTypes",
                color = Color.White,
                fontSize = (coinSize.value * 0.24f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
