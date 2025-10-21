package com.api.ruletaeuropea.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.api.ruletaeuropea.Modelo.Apuesta
import com.api.ruletaeuropea.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

import com.api.ruletaeuropea.componentes.SelectorMonedas

// Paleta y dimensiones
private val TableGreen = Color(0xFF157A3E)
private val PocketGreen = Color(0xFF0FA54B)
private val RouletteRed = Color(0xFFB51414)
private val RouletteBlack = Color(0xFF101010)
private val IvoryText = Color(0xFFF3F1E8)
private val Gold = Color(0xFFD4AF37)

private val CellSize: Dp = 44.dp
private val CellSpacing: Dp = 0.dp
private val OuterPadding: Dp = 8.dp
private val SectionHeight: Dp = 42.dp
private val BorderWidth: Dp = 2.dp

private val RedNumbers = setOf(
    1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
)

private val TopRow = listOf(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36)
private val MiddleRow = listOf(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35)
private val BottomRow = listOf(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34)

private fun colorForNumber(n: Int): Color = when {
    n == 0 -> PocketGreen
    RedNumbers.contains(n) -> RouletteRed
    else -> RouletteBlack
}

@Composable
fun RuletaGrid(
    monedaSeleccionada: Int,
    apuestas: List<Apuesta>,
    onApuestaRealizada: (Int) -> Unit
) {
    val gridWidth = (CellSize * 12) + (CellSpacing * 11)

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(OuterPadding)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
            // Columna del 0 (apuesta al 0)
            Box(
                modifier = Modifier
                    .width(CellSize)
                    .height((CellSize * 3) + (CellSpacing * 2))
                    .background(PocketGreen)
                    .border(BorderWidth, Gold)
                    .clickable { onApuestaRealizada(0) },
                contentAlignment = Alignment.Center
            ) {
                Text("0", color = IvoryText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            // Cuadrícula 12x3
            Column(
                modifier = Modifier.width(gridWidth),
                verticalArrangement = Arrangement.spacedBy(CellSpacing)
            ) {
                NumberRow(TopRow, apuestas, onApuestaRealizada)
                NumberRow(MiddleRow, apuestas, onApuestaRealizada)
                NumberRow(BottomRow, apuestas, onApuestaRealizada)
            }
        }

        Spacer(Modifier.height(CellSpacing * 2))

        // Dozens
        Row(
            modifier = Modifier
                .width((CellSize * 12) + (CellSpacing * 11) + CellSize + CellSpacing)
                .height(SectionHeight),
            horizontalArrangement = Arrangement.spacedBy(CellSpacing)
        ) {
            Spacer(modifier = Modifier.width(CellSize))

            DozenBox("1st 12", -101, apuestas) { onApuestaRealizada(-101) }
            DozenBox("2nd 12", -102, apuestas) { onApuestaRealizada(-102) }
            DozenBox("3rd 12", -103, apuestas) { onApuestaRealizada(-103) }

        }

        Spacer(Modifier.height(CellSpacing))

        // Outside bets
        Row(
            modifier = Modifier
                .width((CellSize * 12) + (CellSpacing * 11) + CellSize + CellSpacing)
                .height(SectionHeight),
            horizontalArrangement = Arrangement.spacedBy(CellSpacing)
        ) {
            Spacer(modifier = Modifier.width(CellSize))

            OutsideBetBox(label= "1 to 18", codigo= -201, apuestas = apuestas) { onApuestaRealizada(-201) }
            OutsideBetBox("Even", codigo= -202, apuestas = apuestas) { onApuestaRealizada(-202) }
            OutsideBetBox(imagenRes = R.drawable.diamante_rojo, codigo = -203, apuestas = apuestas) { onApuestaRealizada(-203) }
            OutsideBetBox(imagenRes = R.drawable.diamante_negro, codigo = -204, apuestas = apuestas) { onApuestaRealizada(-204) }
            OutsideBetBox("Odd", codigo= -205, apuestas = apuestas) { onApuestaRealizada(-205) }
            OutsideBetBox("19 to 36", codigo= -206, apuestas = apuestas) { onApuestaRealizada(-206) }

        }
    }
}

@Composable
private fun NumberRow(
    numbers: List<Int>,
    apuestas: List<Apuesta>,
    onApuestaRealizada: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
        numbers.forEach { numero ->
            Box(
                modifier = Modifier
                    .size(CellSize)
                    .background(colorForNumber(numero))
                    .border(BorderWidth, Gold)
                    .clickable { onApuestaRealizada(numero) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = IvoryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // 🪙 Si hay apuesta en este número, mostramos la moneda
                apuestas.filter { it.numero == numero }.forEach { apuesta ->
                    Image(
                        painter = painterResource(
                            id = when (apuesta.valorMoneda) {
                                1 -> R.drawable.moneda1
                                5 -> R.drawable.moneda5
                                10 -> R.drawable.moneda10
                                else -> R.drawable.moneda1
                            }
                        ),
                        contentDescription = "Apuesta ${apuesta.valorMoneda}",
                        modifier = Modifier.size(36.dp) // moneda pequeña encima
                    )
                }
            }

        }
    }
}

@Composable
private fun RowScope.DozenBox(
    text: String,
    codigo: Int,
    apuestas: List<Apuesta>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(TableGreen.copy(alpha = 0.25f))
            .border(BorderWidth, Gold)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        apuestas.filter { it.numero == codigo }.forEach { apuesta ->
            Image(
                painter = painterResource(
                    id = when (apuesta.valorMoneda) {
                        1 -> R.drawable.moneda1
                        5 -> R.drawable.moneda5
                        10 -> R.drawable.moneda10
                        else -> R.drawable.moneda1
                    }
                ),
                contentDescription = "Apuesta ${apuesta.valorMoneda}",
                modifier = Modifier.size(36.dp) // 👈 más grande
            )
    }
}
}
@Composable
private fun RowScope.OutsideBetBox(
    label: String? = null,
    imagenRes: Int? = null,
    codigo: Int,
    apuestas: List<Apuesta>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(TableGreen.copy(alpha = 0.25f))
            .border(BorderWidth, Gold)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            imagenRes != null -> Image(
                painter = painterResource(id = imagenRes),
                contentDescription = "Apuesta color",
                modifier = Modifier.size(50.dp)
            )
            label != null -> Text(
                text = label,
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        apuestas.filter { it.numero == codigo }.forEach { apuesta ->
            Image(
                painter = painterResource(
                    id = when (apuesta.valorMoneda) {
                        1 -> R.drawable.moneda1
                        5 -> R.drawable.moneda5
                        10 -> R.drawable.moneda10
                        else -> R.drawable.moneda1
                    }
                ),
                contentDescription = "Apuesta ${apuesta.valorMoneda}",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}






