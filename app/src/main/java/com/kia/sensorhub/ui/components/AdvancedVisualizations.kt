package com.kia.sensorhub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Advanced 3D Cube visualization for accelerometer
 */
@Composable
fun Cube3DVisualization(
    x: Float,
    y: Float,
    z: Float,
    modifier: Modifier = Modifier
) {
    val animatedX by animateFloatAsState(targetValue = x, label = "x")
    val animatedY by animateFloatAsState(targetValue = y, label = "y")
    val animatedZ by animateFloatAsState(targetValue = z, label = "z")
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val cubeSize = minOf(size.width, size.height) / 4
            
            // Calculate rotation angles from accelerometer data
            val rotX = animatedX * 0.1f
            val rotY = animatedY * 0.1f
            animatedZ * 0.1f
            
            // Draw 3D cube with perspective
            val vertices = listOf(
                Pair(-1f, -1f), Pair(1f, -1f), Pair(1f, 1f), Pair(-1f, 1f), // front
                Pair(-0.7f, -0.7f), Pair(1.3f, -0.7f), Pair(1.3f, 1.3f), Pair(-0.7f, 1.3f) // back
            )
            
            // Front face
            val frontPath = Path().apply {
                moveTo(
                    centerX + vertices[0].first * cubeSize,
                    centerY + vertices[0].second * cubeSize
                )
                vertices.take(4).forEach { (vx, vy) ->
                    lineTo(
                        centerX + vx * cubeSize * cos(rotY),
                        centerY + vy * cubeSize * cos(rotX)
                    )
                }
                close()
            }
            
            drawPath(
                path = frontPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00D4FF),
                        Color(0xFF0088FF)
                    )
                ),
                alpha = 0.8f
            )
            
            // Draw edges
            drawPath(
                path = frontPath,
                color = Color.White,
                style = Stroke(width = 2f)
            )
            
            // Draw axes indicators
            drawLine(
                color = Color.Red,
                start = Offset(centerX, centerY),
                end = Offset(centerX + animatedX * 20, centerY),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.Green,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY + animatedY * 20),
                strokeWidth = 3f
            )
        }
    }
}

/**
 * Waveform visualization for sensor data
 */
@Composable
fun WaveformVisualization(
    dataPoints: List<Float>,
    color: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            phase = (phase + 0.05f) % (2 * PI.toFloat())
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val points = dataPoints.takeLast(100)
            if (points.isEmpty()) return@Canvas
            
            val stepX = size.width / points.size
            val midY = size.height / 2
            val amplitude = size.height / 4
            
            val path = Path()
            path.moveTo(0f, midY)
            
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = midY - (value * amplitude)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (index - 1) * stepX
                    val prevY = midY - (points[index - 1] * amplitude)
                    
                    // Smooth curves using quadratic bezier
                    val controlX = (prevX + x) / 2
                    val controlY = (prevY + y) / 2
                    path.quadraticBezierTo(controlX, controlY, x, y)
                }
            }
            
            // Draw gradient fill
            drawPath(
                path = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                },
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.5f),
                        color.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            
            // Draw line
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Circular gauge visualization
 */
@Composable
fun CircularGauge(
    value: Float,
    maxValue: Float = 100f,
    label: String,
    unit: String,
    color: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "value"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 20f
                val radius = (minOf(size.width, size.height) - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                
                // Background arc
                drawArc(
                    color = color.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Value arc
                val sweepAngle = (animatedValue / maxValue) * 270f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f),
                            color,
                            color.copy(alpha = 0.8f)
                        ),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Draw marker at current value
                val angle = Math.toRadians((135 + sweepAngle).toDouble())
                val markerX = center.x + radius * cos(angle).toFloat()
                val markerY = center.y + radius * sin(angle).toFloat()
                
                drawCircle(
                    color = color,
                    radius = strokeWidth / 2 + 5,
                    center = Offset(markerX, markerY),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth / 2,
                    center = Offset(markerX, markerY)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format("%.1f", animatedValue),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Radar chart for multi-dimensional data
 */
@Composable
fun RadarChart(
    values: List<Float>,
    labels: List<String>,
    color: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    val animatedValues = values.map { value ->
        animateFloatAsState(
            targetValue = value,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "value"
        ).value
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = minOf(size.width, size.height) / 2 * 0.8f
            val angleStep = 360f / values.size
            
            // Draw background circles
            for (i in 1..5) {
                val r = radius * i / 5
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.1f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }
            
            // Draw axes
            values.indices.forEach { i ->
                val angle = Math.toRadians((i * angleStep - 90).toDouble())
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()
                
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1f
                )
            }
            
            // Draw data polygon
            val path = Path()
            animatedValues.forEachIndexed { i, value ->
                val angle = Math.toRadians((i * angleStep - 90).toDouble())
                val r = radius * value
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            
            // Fill
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.4f),
                        color.copy(alpha = 0.1f)
                    ),
                    center = center
                )
            )
            
            // Stroke
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
            
            // Draw points
            animatedValues.forEachIndexed { i, value ->
                val angle = Math.toRadians((i * angleStep - 90).toDouble())
                val r = radius * value
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = color,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Heatmap visualization for sensor patterns
 */
@Composable
fun HeatmapVisualization(
    data: List<List<Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            if (data.isEmpty() || data[0].isEmpty()) return@Canvas
            
            val cellWidth = size.width / data[0].size
            val cellHeight = size.height / data.size
            
            data.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, value ->
                    val color = getHeatmapColor(value)
                    
                    drawRect(
                        color = color,
                        topLeft = Offset(
                            colIndex * cellWidth,
                            rowIndex * cellHeight
                        ),
                        size = Size(cellWidth - 2f, cellHeight - 2f)
                    )
                }
            }
        }
    }
}

private fun getHeatmapColor(value: Float): Color {
    return when {
        value < 0.2f -> Color(0xFF0D47A1) // Dark Blue
        value < 0.4f -> Color(0xFF1976D2) // Blue
        value < 0.6f -> Color(0xFF4CAF50) // Green
        value < 0.8f -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
}

/**
 * Line chart with multiple series
 */
@Composable
fun MultiSeriesLineChart(
    series: List<Pair<String, List<Float>>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                series.forEachIndexed { index, (name, _) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors[index], RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Chart
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxPoints = series.maxOf { it.second.size }
                val stepX = size.width / maxPoints.coerceAtLeast(1)
                val midY = size.height / 2
                val amplitude = size.height / 4
                
                series.forEachIndexed { seriesIndex, (_, data) ->
                    val path = Path()
                    
                    data.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = midY - (value * amplitude)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = colors[seriesIndex],
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

/**
 * Animated progress ring with label
 */
@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progress"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
