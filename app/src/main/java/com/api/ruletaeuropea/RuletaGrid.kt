package com.api.ruletaeuropea

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// 🎨 Paleta y dimensiones base
private val TableGreen = Color(0xFF157A3E)       // Verde tapete clásico
private val PocketGreen = Color(0xFF0FA54B)      // Verde más vivo para el 0
private val RouletteRed = Color(0xFFB51414)
private val RouletteBlack = Color(0xFF101010)
private val IvoryText = Color(0xFFF3F1E8)        // Texto marfil
private val Gold = Color(0xFFD4AF37)             // Dorado elegante

private val CellSize: Dp = 44.dp
private val CellSpacing: Dp = 0.dp
private val OuterPadding: Dp = 8.dp
private val CornerRadius = 8.dp
private val ZeroWidth: Dp = CellSize
private val SectionHeight: Dp = 42.dp
private val BorderWidth: Dp = 2.dp

// 🔴 Números rojos en ruleta europea
private val RedNumbers = setOf(
    1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
)

// Filas de la cuadrícula
private val TopRow = listOf(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36)
private val MiddleRow = listOf(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35)
private val BottomRow = listOf(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34)

private fun colorForNumber(n: Int): Color = when {
    n == 0 -> PocketGreen
    RedNumbers.contains(n) -> RouletteRed
    else -> RouletteBlack
}

@Composable
fun RuletaGrid() {
    val gridWidth = (CellSize * 12) + (CellSpacing * 11)

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(OuterPadding)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CellSpacing)
        ) {
            // 🔹 Columna del 0 (ocupa 3 filas)
            Box(
                modifier = Modifier
                    .width(CellSize)
                    .height((CellSize * 3) + (CellSpacing * 2))
                    .background(PocketGreen)
                    .border(BorderWidth, Gold),
                contentAlignment = Alignment.Center
            ) {
                Text("0", color = IvoryText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            // 🔹 Cuadrícula de 12 columnas × 3 filas
            Column(
                modifier = Modifier.width(gridWidth),
                verticalArrangement = Arrangement.spacedBy(CellSpacing)
            ) {
                NumberRow(TopRow)
                NumberRow(MiddleRow)
                NumberRow(BottomRow)
            }
        }

        Spacer(Modifier.height(CellSpacing * 2))

        // 🔹 Dozens (alineados solo con los números, no con el 0)
        Row(
            modifier = Modifier
                .width((CellSize * 12) + (CellSpacing * 11) + CellSize + CellSpacing) // ancho total incluyendo el 0
                .height(SectionHeight),
            horizontalArrangement = Arrangement.spacedBy(CellSpacing)
        ) {
            // Este Spacer representa la columna del "0" vacía debajo
            Spacer(modifier = Modifier.width(CellSize))

            DozenBox("1st 12")
            DozenBox("2nd 12")
            DozenBox("3rd 12")
        }

        Spacer(Modifier.height(CellSpacing))

        // 🔹 Outside bets (también solo debajo de los números)
        Row(
            modifier = Modifier
                .width((CellSize * 12) + (CellSpacing * 11) + CellSize + CellSpacing)
                .height(SectionHeight),
            horizontalArrangement = Arrangement.spacedBy(CellSpacing)
        ) {
            Spacer(modifier = Modifier.width(CellSize))

            OutsideBetBox("1 to 18")
            OutsideBetBox("Even")
            OutsideBetBox("♦", fill = RouletteRed, labelColor = IvoryText, bold = true)
            OutsideBetBox("♦", fill = RouletteBlack, labelColor = IvoryText, bold = true)
            OutsideBetBox("Odd")
            OutsideBetBox("19 to 36")
        }
    }
}



@Composable
private fun NumberRow(numbers: List<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
        numbers.forEach { numero ->
            Box(
                modifier = Modifier
                    .size(CellSize) // ✅ tamaño fijo
                    .background(colorForNumber(numero))
                    .border(BorderWidth, Gold,),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = IvoryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun RowScope.DozenBox(text: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(TableGreen.copy(alpha = 0.25f))
            .border(BorderWidth, Gold),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RowScope.OutsideBetBox(
    label: String,
    fill: Color = TableGreen.copy(alpha = 0.25f),
    labelColor: Color = Gold,
    bold: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(fill)
            .border(BorderWidth, Gold),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 16.sp,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}





