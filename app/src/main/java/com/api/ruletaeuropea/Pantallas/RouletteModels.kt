package com.api.ruletaeuropea.pantallas

import androidx.compose.ui.geometry.Offset
import org.json.JSONArray
import org.json.JSONObject

/* -------------------- Modelos y parseo JSON -------------------- */

data class ImageSize(val width: Float, val height: Float)
data class CenterPx(val cx: Float, val cy: Float)
data class RingsPx(
    val outer_r: Float,
    val track_outer_r: Float,
    val track_inner_r: Float,
    val cone_outer_r: Float,
    val cone_inner_r: Float,
    val hub_r: Float
)
data class Pocket(
    val index_clockwise: Int,
    val number: Int,
    val color: String,
    val thetaCenter: Float,
    val thetaStart: Float,
    val thetaEnd: Float,
    val centerX: Float,
    val centerY: Float,
    val polygon: List<Offset>,
    val id1: String = "",
    val id2: String = ""
)
data class RouletteLayoutJson(
    val imageSize: ImageSize,
    val centerPx: CenterPx,
    val ringsPx: RingsPx,
    val separators: List<Float>,
    val upper: List<Pocket>,
    val middle: List<Pocket>,
    val lower: List<Pocket>
)

/* -------------------- Parse helpers -------------------- */

private fun JSONArray.toFloatList(): List<Float> = (0 until length()).map { getDouble(it).toFloat() }

private fun readPolygon(arr: JSONArray): List<Offset> {
    val out = ArrayList<Offset>(arr.length())
    for (i in 0 until arr.length()) {
        val p = arr.getJSONArray(i)
        out += Offset(p.getDouble(0).toFloat(), p.getDouble(1).toFloat())
    }
    return out
}

private fun readPockets(arr: JSONArray, hasColor: Boolean, id1: String, id2: String): List<Pocket> {
    val out = ArrayList<Pocket>(arr.length())
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        out += Pocket(
            index_clockwise = o.getInt("index_clockwise"),
            number = o.optInt("number", -1),
            color = if (hasColor) o.getString("color") else "",
            thetaCenter = o.getDouble("theta_center_rad").toFloat(),
            thetaStart = o.getDouble("theta_start_rad").toFloat(),
            thetaEnd = o.getDouble("theta_end_rad").toFloat(),
            centerX = o.getDouble("center_x").toFloat(),
            centerY = o.getDouble("center_y").toFloat(),
            polygon = readPolygon(o.getJSONArray("polygon")),
            id1 = o.optString(id1, ""),
            id2 = o.optString(id2, "")
        )
    }
    return out
}

fun parseRouletteLayout(json: String): RouletteLayoutJson {
    val root = JSONObject(json)
    val img = root.getJSONObject("image_size")
    val center = root.getJSONObject("center_px")
    val rings = root.getJSONObject("rings_px")

    val imageSize = ImageSize(
        width = img.getInt("width").toFloat(),
        height = img.getInt("height").toFloat()
    )
    val centerPx = CenterPx(
        cx = center.getDouble("cx").toFloat(),
        cy = center.getDouble("cy").toFloat()
    )
    val ringsPx = RingsPx(
        outer_r = rings.getDouble("outer_r").toFloat(),
        track_outer_r = rings.getDouble("track_outer_r").toFloat(),
        track_inner_r = rings.getDouble("track_inner_r").toFloat(),
        cone_outer_r = rings.getDouble("cone_outer_r").toFloat(),
        cone_inner_r = rings.getDouble("cone_inner_r").toFloat(),
        hub_r = rings.getDouble("hub_r").toFloat()
    )

    val seps = root.getJSONArray("separators_theta_rad_clockwise").toFloatList()
    val upper = readPockets(root.getJSONArray("upper_pockets"), true, "id_s1", "id_s2")
    val middle = readPockets(root.getJSONArray("middle_pockets"), false, "id_m1", "id_m2")
    val lower = readPockets(root.getJSONArray("lower_pockets"), false, "id_inf1", "id_inf2")

    return RouletteLayoutJson(imageSize, centerPx, ringsPx, seps, upper, middle, lower)
}

