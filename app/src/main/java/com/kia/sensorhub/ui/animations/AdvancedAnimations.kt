package com.kia.sensorhub.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Advanced animation effects library for SensorHub
 */

/**
 * Particle system for creating dynamic visual effects
 */
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float = 1f,
    val size: Float = 4f,
    val color: Color = Color.White
)

/**
 * Particle effect composable
 */
@Composable
fun ParticleEffect(
    isActive: Boolean,
    particleCount: Int = 50,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.6f)
) {
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            particles = List(particleCount) {
                Particle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 0.02f,
                    vy = (Random.nextFloat() - 0.5f) * 0.02f,
                    life = Random.nextFloat(),
                    size = Random.nextFloat() * 6f + 2f,
                    color = color
                )
            }
        }
    }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                kotlinx.coroutines.delay(16) // ~60 FPS
                particles = particles.map { particle ->
                    particle.copy(
                        x = (particle.x + particle.vx).coerceIn(0f, 1f),
                        y = (particle.y + particle.vy).coerceIn(0f, 1f),
                        life = (particle.life - 0.01f).coerceAtLeast(0f)
                    )
                }.filter { it.life > 0 } + if (particles.size < particleCount) {
                    listOf(
                        Particle(
                            x = Random.nextFloat(),
                            y = Random.nextFloat(),
                            vx = (Random.nextFloat() - 0.5f) * 0.02f,
                            vy = (Random.nextFloat() - 0.5f) * 0.02f,
                            life = 1f,
                            size = Random.nextFloat() * 6f + 2f,
                            color = color
                        )
                    )
                } else emptyList()
            }
        }
    }
    
    Box(
        modifier = modifier.drawBehind {
            particles.forEach { particle ->
                drawCircle(
                    color = particle.color.copy(alpha = particle.life * 0.6f),
                    radius = particle.size,
                    center = Offset(
                        particle.x * size.width,
                        particle.y * size.height
                    )
                )
            }
        }
    )
}

/**
 * Pulsating glow effect
 */
@Composable
fun PulsatingGlow(
    isActive: Boolean,
    color: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    if (isActive) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Wave animation effect
 */
@Composable
fun WaveEffect(
    isActive: Boolean,
    waveColor: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "phase"
    )
    
    if (isActive) {
        Box(
            modifier = modifier.drawBehind {
                val waveHeight = size.height / 4
                val waveLength = size.width / 2
                
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    
                    for (x in 0..size.width.toInt() step 5) {
                        val y = size.height / 2 + waveHeight * sin(
                            Math.toRadians((x / waveLength * 360 + phase).toDouble())
                        ).toFloat()
                        lineTo(x.toFloat(), y)
                    }
                    
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            waveColor.copy(alpha = 0.4f),
                            waveColor.copy(alpha = 0.1f)
                        )
                    )
                )
            }
        )
    }
}

/**
 * Ripple effect for touch interactions
 */
@Composable
fun AnimatedRipple(
    onClick: () -> Unit,
    rippleColor: Color = Color.Blue,
    content: @Composable () -> Unit
) {
    var ripples by remember { mutableStateOf(listOf<RippleData>()) }
    
    LaunchedEffect(ripples) {
        if (ripples.isNotEmpty()) {
            kotlinx.coroutines.delay(16)
            ripples = ripples.map { ripple ->
                ripple.copy(
                    radius = ripple.radius + 2f,
                    alpha = (ripple.alpha - 0.02f).coerceAtLeast(0f)
                )
            }.filter { it.alpha > 0 }
        }
    }
    
    Box(
        modifier = Modifier
            .drawBehind {
                ripples.forEach { ripple ->
                    drawCircle(
                        color = rippleColor.copy(alpha = ripple.alpha),
                        radius = ripple.radius,
                        center = ripple.center,
                        style = Stroke(width = 2f)
                    )
                }
            }
    ) {
        content()
    }
}

data class RippleData(
    val center: Offset,
    val radius: Float,
    val alpha: Float
)

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = 0.5f),
                    Color.LightGray.copy(alpha = 0.3f)
                ),
                start = Offset(offset - 500f, 0f),
                end = Offset(offset, 1000f)
            )
        )
    )
}

/**
 * Rotating border effect
 */
@Composable
fun RotatingBorder(
    isActive: Boolean,
    color: Color = Color.Blue,
    strokeWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    
    if (isActive) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    rotationZ = rotation
                }
                .drawBehind {
                    val gradient = Brush.sweepGradient(
                        colors = listOf(
                            color.copy(alpha = 0f),
                            color.copy(alpha = 1f),
                            color.copy(alpha = 0f)
                        )
                    )
                    
                    drawCircle(
                        brush = gradient,
                        style = Stroke(width = strokeWidth.toPx())
                    )
                }
        )
    }
}

/**
 * Bouncing animation
 */
@Composable
fun BouncingBox(
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    Box(
        modifier = Modifier.offset(y = offsetY.dp)
    ) {
        content()
    }
}

/**
 * 3D Card flip animation
 */
@Composable
fun FlipCard(
    isFlipped: Boolean,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(600),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            frontContent()
        } else {
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            ) {
                backContent()
            }
        }
    }
}

/**
 * Radar scanning effect
 */
@Composable
fun RadarScan(
    isActive: Boolean,
    color: Color = Color.Green,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "angle"
    )
    
    if (isActive) {
        Box(
            modifier = modifier.drawBehind {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2
                
                // Draw radar circles
                for (i in 1..3) {
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = radius * i / 3,
                        center = center,
                        style = Stroke(width = 1f)
                    )
                }
                
                // Draw scanning line
                val scanAngle = Math.toRadians(angle.toDouble())
                val endX = center.x + radius * cos(scanAngle).toFloat()
                val endY = center.y + radius * sin(scanAngle).toFloat()
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0f),
                            color.copy(alpha = 0.8f)
                        ),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
            }
        )
    }
}

/**
 * Morphing shape animation
 */
@Composable
fun MorphingShape(
    targetShape: Float,
    color: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    val animatedShape by animateFloatAsState(
        targetValue = targetShape,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shape"
    )
    
    Box(
        modifier = modifier.drawBehind {
            val path = Path()
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(size.width, size.height) / 2 * 0.8f
            
            // Create morphing shape
            val sides = (3 + animatedShape * 5).toInt() // 3 to 8 sides
            val angleStep = 360f / sides
            
            for (i in 0..sides) {
                val angle = Math.toRadians((i * angleStep).toDouble())
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3f)
            )
        }
    )
}

/**
 * Gradient animation
 */
@Composable
fun AnimatedGradient(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(1000f * offset, 1000f * offset)
            )
        )
    )
}
