package com.api.ruletaeuropea.fisica

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RoulettePhysicsTest {
    // Carga el layout completo desde res/raw
    private fun loadFullLayout(): JSONObject {
        val file = File("src/main/res/raw/ruleta_layout_extended.json")
        require(file.exists()) { "No se encontró el archivo de layout en ${file.absolutePath}" }
        val text = file.readText(Charsets.UTF_8)
        return JSONObject(text)
    }

    @Test
    fun testJsonHasAll37UniqueNumbers() {
        val layout = loadFullLayout()
        val pockets = layout.getJSONArray("upper_pockets")
        assertEquals("Debe haber 37 pockets",37, pockets.length())
        val set = HashSet<Int>()
        for (i in 0 until pockets.length()) {
            val num = pockets.getJSONObject(i).getInt("number")
            set.add(num)
        }
        assertEquals("Los números deben ser únicos", 37, set.size)
        // Comprobar que contiene 0..36
        for (n in 0..36) {
            assertTrue("Falta el número $n en el layout", set.contains(n))
        }
    }

    @Test
    fun testPhysicsStopsWithFullLayout() {
        val layout = loadFullLayout()
        val physics = RoulettePhysics(layout, seed = 123)
        physics.launchBall()
        var steps = 0
        // Permitir más steps porque 37 casillas y dinámica completa
        while (physics.rolling && steps < 120000) {
            physics.updatePhysics()
            steps++
        }
        assertTrue("La simulación debería haberse detenido (steps=$steps)", !physics.rolling)
        val pocket = physics.resultPocketNumber()
        assertTrue("Pocket debe ser 0..36, fue $pocket", pocket in 0..36)
    }

    @Test
    fun testStopsInMidSectionFullLayout() {
        val layout = loadFullLayout()
        val physics = RoulettePhysics(layout, seed = 321, initialConfig = RoulettePhysics.Config(requireMidForStop = true))
        physics.launchBall()
        var steps = 0
        while (physics.rolling && steps < 150000) {
            physics.updatePhysics()
            steps++
        }
        assertTrue("La simulación debe haber parado (steps=$steps)", !physics.rolling)
        val status = physics.debugStatus()
        assertTrue("El estado final debe indicar mid=true: $status", status.contains("mid=true"))
    }
}
