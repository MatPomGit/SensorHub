package com.kia.sensorhub.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kia.sensorhub.affective.*
import com.kia.sensorhub.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Affective Computing Screen - Emotion Analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffectiveComputingScreen(
    viewModel: AffectiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Affective Computing") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleAnalysis() }) {
                        Icon(
                            imageVector = if (uiState.isAnalyzing) 
                                Icons.Default.Stop 
                            else 
                                Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isAnalyzing) "Stop" else "Start"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isAnalyzing)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (uiState.isAnalyzing) "Analyzing..." else "Ready",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current Emotion: ${uiState.currentEmotion?.emotion?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Current Emotion
            if (uiState.currentEmotion != null) {
                EmotionCard(
                    emotion = uiState.currentEmotion!!,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Affective State Visualization
            Text(
                text = "Emotional State",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            AffectiveStateVisualization(
                state = uiState.affectiveState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            // Dimensional Values
            Text(
                text = "Emotional Dimensions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            DimensionalSlider(
                label = "Arousal",
                value = uiState.affectiveState.arousal,
                lowLabel = "Calm",
                highLabel = "Excited",
                color = Color(0xFF2196F3)
            )
            
            DimensionalSlider(
                label = "Valence",
                value = uiState.affectiveState.valence,
                lowLabel = "Negative",
                highLabel = "Positive",
                color = Color(0xFF4CAF50)
            )
            
            DimensionalSlider(
                label = "Stress",
                value = uiState.affectiveState.stress,
                lowLabel = "Relaxed",
                highLabel = "Stressed",
                color = Color(0xFFF44336)
            )
            
            DimensionalSlider(
                label = "Focus",
                value = uiState.affectiveState.focus,
                lowLabel = "Distracted",
                highLabel = "Focused",
                color = Color(0xFF9C27B0)
            )
            
            // Statistics
            if (uiState.emotionHistory.isNotEmpty()) {
                Text(
                    text = "Emotion Distribution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                EmotionDistributionCard(
                    distribution = uiState.emotionDistribution,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleAnalysis() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isAnalyzing)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isAnalyzing)
                            Icons.Default.Stop
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.isAnalyzing) "Stop" else "Start Analysis")
                }
                
                if (uiState.emotionHistory.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionCard(
    emotion: DetectedEmotion,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = getEmotionColor(emotion.emotion).copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getEmotionIcon(emotion.emotion),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = getEmotionColor(emotion.emotion)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = emotion.emotion.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Confidence: ${(emotion.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (emotion.factors.isNotEmpty()) {
                Text(
                    text = "Contributing Factors:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                emotion.factors.forEach { (factor, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = factor.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = String.format("%.2f", value),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AffectiveStateVisualization(
    state: AffectiveState,
    modifier: Modifier = Modifier
) {
    val animatedArousal by animateFloatAsState(targetValue = state.arousal, label = "arousal")
    val animatedValence by animateFloatAsState(targetValue = state.valence, label = "valence")
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = minOf(size.width, size.height) / 2 * 0.8f
            
            // Draw axes
            drawLine(
                color = Color.Gray,
                start = Offset(centerX - maxRadius, centerY),
                end = Offset(centerX + maxRadius, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray,
                start = Offset(centerX, centerY - maxRadius),
                end = Offset(centerX, centerY + maxRadius),
                strokeWidth = 2f
            )
            
            // Calculate current position
            val x = centerX + (animatedValence - 0.5f) * 2f * maxRadius
            val y = centerY - (animatedArousal - 0.5f) * 2f * maxRadius
            
            // Draw current state point
            drawCircle(
                color = Color(0xFF9C27B0),
                radius = 20f,
                center = Offset(x, y)
            )
            
            // Draw glow effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9C27B0).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = 40f
                ),
                radius = 40f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun DimensionalSlider(
    label: String,
    value: Float,
    lowLabel: String,
    highLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(targetValue = value, label = label)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(animatedValue * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            LinearProgressIndicator(
                progress = animatedValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lowLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = highLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmotionDistributionCard(
    distribution: Map<EmotionType, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val total = distribution.values.sum()
            distribution.forEach { (emotion, count) ->
                if (emotion != EmotionType.UNKNOWN) {
                    EmotionDistributionRow(
                        emotion = emotion,
                        count = count,
                        percentage = if (total > 0) count.toFloat() / total else 0f
                    )
                }
            }
        }
    }
}

@Composable
fun EmotionDistributionRow(
    emotion: EmotionType,
    count: Int,
    percentage: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getEmotionIcon(emotion),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getEmotionColor(emotion)
                )
                Text(
                    text = emotion.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "$count (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = getEmotionColor(emotion),
            trackColor = getEmotionColor(emotion).copy(alpha = 0.2f)
        )
    }
}

fun getEmotionIcon(emotion: EmotionType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (emotion) {
        EmotionType.CALM -> Icons.Default.SelfImprovement
        EmotionType.STRESSED -> Icons.Default.Warning
        EmotionType.ACTIVE -> Icons.Default.DirectionsRun
        EmotionType.RESTING -> Icons.Default.BedroomBaby
        EmotionType.ANXIOUS -> Icons.Default.ErrorOutline
        EmotionType.FOCUSED -> Icons.Default.Visibility
        EmotionType.DISTRACTED -> Icons.Default.VisibilityOff
        EmotionType.UNKNOWN -> Icons.Default.Help
    }
}

fun getEmotionColor(emotion: EmotionType): Color {
    return when (emotion) {
        EmotionType.CALM -> Color(0xFF4CAF50)
        EmotionType.STRESSED -> Color(0xFFF44336)
        EmotionType.ACTIVE -> Color(0xFF2196F3)
        EmotionType.RESTING -> Color(0xFF9E9E9E)
        EmotionType.ANXIOUS -> Color(0xFFFF9800)
        EmotionType.FOCUSED -> Color(0xFF9C27B0)
        EmotionType.DISTRACTED -> Color(0xFF607D8B)
        EmotionType.UNKNOWN -> Color(0xFF9E9E9E)
    }
}

// ViewModel
@HiltViewModel
class AffectiveViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AffectiveUiState())
    val uiState: StateFlow<AffectiveUiState> = _uiState.asStateFlow()
    
    private val analyzer = AffectiveAnalyzer()
    private val tracker = EmotionTracker()
    private var analysisJob: Job? = null
    
    fun toggleAnalysis() {
        if (_uiState.value.isAnalyzing) {
            stopAnalysis()
        } else {
            startAnalysis()
        }
    }
    
    private fun startAnalysis() {
        analysisJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            
            // Start collecting accelerometer data
            repository.getAccelerometerFlow().collect { data ->
                val emotion = analyzer.analyzeFromAccelerometer(data)
                tracker.addEmotion(emotion)
                
                val state = analyzer.computeAffectiveState(
                    tracker.getEmotionHistory().takeLast(10)
                )
                tracker.addState(state)
                
                _uiState.value = _uiState.value.copy(
                    currentEmotion = emotion,
                    affectiveState = state,
                    emotionHistory = tracker.getEmotionHistory(),
                    emotionDistribution = tracker.getEmotionDistribution()
                )
            }
        }
    }
    
    private fun stopAnalysis() {
        analysisJob?.cancel()
        _uiState.value = _uiState.value.copy(isAnalyzing = false)
    }
    
    fun clearHistory() {
        analyzer.clearHistory()
        tracker.clear()
        _uiState.value = _uiState.value.copy(
            emotionHistory = emptyList(),
            emotionDistribution = emptyMap()
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAnalysis()
    }
}

data class AffectiveUiState(
    val isAnalyzing: Boolean = false,
    val currentEmotion: DetectedEmotion? = null,
    val affectiveState: AffectiveState = AffectiveState(
        arousal = 0.5f,
        valence = 0.5f,
        stress = 0.5f,
        focus = 0.5f
    ),
    val emotionHistory: List<DetectedEmotion> = emptyList(),
    val emotionDistribution: Map<EmotionType, Int> = emptyMap()
)
