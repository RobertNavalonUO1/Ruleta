package com.api.ruletaeuropea.componentes

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*
import android.util.Log

/**
 * Representa el estado físico de la bola de la ruleta
 */
data class BallState(
    val position: Offset = Offset.Zero,
    val velocity: Offset = Offset.Zero,
    val isMoving: Boolean = false,
    val finalSection: Int? = null
)

/**
 * Clase que gestiona la física de la bola de la ruleta
 */
class BallPhysicsSimulator(
    private val wheelRadius: Float,
    private val wheelCenter: Offset
) {
    // Constantes físicas
    private val gravity = 980f // pixels/s²
    private val friction = 0.98f // factor de fricción
    private val bounceDamping = 0.7f // reducción de velocidad en rebote
    private val minVelocity = 20f // velocidad mínima antes de detenerse
    
    // Secciones de la ruleta europea (0-36)
    // Orden en una ruleta europea: 0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    private val rouletteOrder = listOf(
        0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 
        24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    )
    
    /**
     * Calcula la sección de la ruleta en la que cae la bola basándose en el ángulo
     */
    fun calculateSection(position: Offset): Int {
        val dx = position.x - wheelCenter.x
        val dy = position.y - wheelCenter.y
        
        // Calcular ángulo en radianes (0 a 2π)
        var angle = atan2(dy, dx)
        if (angle < 0) angle += 2 * PI.toFloat()
        
        // Convertir ángulo a índice de sección (0-36)
        val sectionAngle = (2 * PI / 37).toFloat()
        val sectionIndex = ((angle / sectionAngle).toInt()) % 37
        
        // Obtener el número de la ruleta según el orden europeo
        return rouletteOrder[sectionIndex]
    }
    
    /**
     * Actualiza la posición y velocidad de la bola según las leyes físicas
     */
    fun updateBallPhysics(
        currentPosition: Offset,
        currentVelocity: Offset,
        deltaTime: Float
    ): Pair<Offset, Offset> {
        var newVelocity = currentVelocity
        var newPosition = currentPosition
        
        // Aplicar gravedad hacia el centro (fuerza centrípeta)
        val dx = wheelCenter.x - currentPosition.x
        val dy = wheelCenter.y - currentPosition.y
        val distanceFromCenter = sqrt(dx * dx + dy * dy)
        
        // Si está fuera del radio de la ruleta, aplicar fuerza hacia el centro
        if (distanceFromCenter > wheelRadius * 0.8f) {
            val gravityForce = gravity * deltaTime
            val normalizedDx = dx / distanceFromCenter
            val normalizedDy = dy / distanceFromCenter
            
            newVelocity = Offset(
                newVelocity.x + normalizedDx * gravityForce,
                newVelocity.y + normalizedDy * gravityForce
            )
        }
        
        // Aplicar fricción
        newVelocity = Offset(
            newVelocity.x * friction,
            newVelocity.y * friction
        )
        
        // Actualizar posición
        newPosition = Offset(
            currentPosition.x + newVelocity.x * deltaTime,
            currentPosition.y + newVelocity.y * deltaTime
        )
        
        // Comprobar rebote con los bordes de la ruleta
        val newDx = newPosition.x - wheelCenter.x
        val newDy = newPosition.y - wheelCenter.y
        val newDistance = sqrt(newDx * newDx + newDy * newDy)
        
        if (newDistance > wheelRadius * 0.85f) {
            // Rebote: reflejar la velocidad y aplicar damping
            val normalX = newDx / newDistance
            val normalY = newDy / newDistance
            
            val dotProduct = newVelocity.x * normalX + newVelocity.y * normalY
            
            newVelocity = Offset(
                (newVelocity.x - 2 * dotProduct * normalX) * bounceDamping,
                (newVelocity.y - 2 * dotProduct * normalY) * bounceDamping
            )
            
            // Reposicionar la bola dentro del límite
            newPosition = Offset(
                wheelCenter.x + normalX * wheelRadius * 0.85f,
                wheelCenter.y + normalY * wheelRadius * 0.85f
            )
            
            Log.d("BallPhysics", "Bounce! Position: $newPosition, Velocity: $newVelocity")
        }
        
        return Pair(newPosition, newVelocity)
    }
    
    /**
     * Verifica si la bola debe detenerse
     */
    fun shouldStop(velocity: Offset): Boolean {
        val speed = sqrt(velocity.x * velocity.x + velocity.y * velocity.y)
        return speed < minVelocity
    }
}

/**
 * Composable que gestiona el estado y la animación de la bola
 */
@Composable
fun rememberBallPhysicsState(
    wheelRadius: Dp,
    wheelCenter: Offset,
    onBallStopped: (Int) -> Unit
): BallPhysicsState {
    val state = remember {
        BallPhysicsState(
            wheelRadius = wheelRadius.value,
            wheelCenter = wheelCenter,
            onBallStopped = onBallStopped
        )
    }
    
    LaunchedEffect(state.isAnimating) {
        if (state.isAnimating) {
            var lastTime = System.currentTimeMillis()
            
            while (isActive && state.isAnimating) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f // convertir a segundos
                lastTime = currentTime
                
                state.update(deltaTime)
                
                delay(16) // ~60 FPS
            }
        }
    }
    
    return state
}

/**
 * Clase que mantiene el estado de la física de la bola
 */
class BallPhysicsState(
    private val wheelRadius: Float,
    private val wheelCenter: Offset,
    private val onBallStopped: (Int) -> Unit
) {
    private val simulator = BallPhysicsSimulator(wheelRadius, wheelCenter)
    
    var ballState by mutableStateOf(BallState())
        private set
    
    var isAnimating by mutableStateOf(false)
        private set
    
    var debugInfo by mutableStateOf("")
        private set
    
    /**
     * Lanza la bola con una velocidad y dirección inicial
     */
    fun throwBall(initialVelocity: Offset = Offset(600f, -400f)) {
        Log.d("BallPhysics", "Throwing ball with velocity: $initialVelocity")
        
        // Posición inicial en el borde superior de la ruleta
        val startAngle = -PI / 2 // arriba
        val startRadius = wheelRadius * 0.7f
        
        ballState = BallState(
            position = Offset(
                wheelCenter.x + cos(startAngle).toFloat() * startRadius,
                wheelCenter.y + sin(startAngle).toFloat() * startRadius
            ),
            velocity = initialVelocity,
            isMoving = true,
            finalSection = null
        )
        
        isAnimating = true
        updateDebugInfo()
    }
    
    /**
     * Actualiza la física de la bola
     */
    fun update(deltaTime: Float) {
        if (!isAnimating) return
        
        val (newPosition, newVelocity) = simulator.updateBallPhysics(
            ballState.position,
            ballState.velocity,
            deltaTime
        )
        
        ballState = ballState.copy(
            position = newPosition,
            velocity = newVelocity
        )
        
        // Verificar si debe detenerse
        if (simulator.shouldStop(newVelocity)) {
            val finalSection = simulator.calculateSection(newPosition)
            
            ballState = ballState.copy(
                velocity = Offset.Zero,
                isMoving = false,
                finalSection = finalSection
            )
            
            isAnimating = false
            
            Log.d("BallPhysics", "Ball stopped at section: $finalSection, position: $newPosition")
            
            onBallStopped(finalSection)
        }
        
        updateDebugInfo()
    }
    
    /**
     * Actualiza la información de debug
     */
    private fun updateDebugInfo() {
        val speed = sqrt(
            ballState.velocity.x * ballState.velocity.x + 
            ballState.velocity.y * ballState.velocity.y
        )
        
        val distanceFromCenter = sqrt(
            (ballState.position.x - wheelCenter.x).pow(2) + 
            (ballState.position.y - wheelCenter.y).pow(2)
        )
        
        val currentSection = simulator.calculateSection(ballState.position)
        
        debugInfo = """
            Position: (${ballState.position.x.toInt()}, ${ballState.position.y.toInt()})
            Velocity: (${ballState.velocity.x.toInt()}, ${ballState.velocity.y.toInt()})
            Speed: ${speed.toInt()} px/s
            Distance from center: ${distanceFromCenter.toInt()} px
            Current section: $currentSection
            Is moving: ${ballState.isMoving}
        """.trimIndent()
        
        if (ballState.isMoving) {
            Log.d("BallPhysics", debugInfo.replace("\n", " | "))
        }
    }
    
    /**
     * Resetea el estado de la bola
     */
    fun reset() {
        ballState = BallState()
        isAnimating = false
        debugInfo = ""
        Log.d("BallPhysics", "Ball physics reset")
    }
}
