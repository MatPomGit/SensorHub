package com.kia.sensorhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Onboarding Screen with Tutorial
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Welcome to SensorHub",
                description = "Your comprehensive platform for learning about mobile sensors and affective computing",
                icon = Icons.Default.Sensors,
                color = Color(0xFF2196F3)
            ),
            OnboardingPage(
                title = "7 Powerful Sensors",
                description = "Explore accelerometer, gyroscope, magnetometer, light, GPS, proximity, and barometer sensors in real-time",
                icon = Icons.Default.Speed,
                color = Color(0xFF4CAF50)
            ),
            OnboardingPage(
                title = "Emotion Analysis",
                description = "Advanced affective computing module analyzes your emotional state using sensor data",
                icon = Icons.Default.Psychology,
                color = Color(0xFF9C27B0)
            ),
            OnboardingPage(
                title = "Track Your Progress",
                description = "Earn achievements, complete daily challenges, and level up as you explore",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFFD700)
            ),
            OnboardingPage(
                title = "Ready to Start?",
                description = "Let's begin your journey into the world of sensors and data science",
                icon = Icons.Default.Rocket,
                color = Color(0xFFFF5722)
            )
        )
    }
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onComplete) {
                    Text("Skip")
                }
            }
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    currentPage = page,
                    totalPages = pages.size
                )
            }
            
            // Page indicator and buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Page dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        PageIndicatorDot(
                            isActive = index == pagerState.currentPage
                        )
                    }
                }
                
                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    
                    // Next/Get Started button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].color
                        )
                    ) {
                        Text(
                            if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started"
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (pagerState.currentPage < pages.size - 1)
                                Icons.Default.ArrowForward
                            else
                                Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    currentPage: Int,
    totalPages: Int
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                page.color.copy(alpha = 0.3f),
                                page.color.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Icon
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = page.color.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp),
                    tint = page.color
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = page.color
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Features list (for some pages)
        if (currentPage == 1) {
            FeaturesList()
        }
    }
}

@Composable
fun FeaturesList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FeatureItem(Icons.Default.Speed, "Real-time data visualization")
        FeatureItem(Icons.Default.Save, "Automatic data logging")
        FeatureItem(Icons.Default.BarChart, "Advanced analytics")
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PageIndicatorDot(
    isActive: Boolean
) {
    val width by animateDpAsState(
        targetValue = if (isActive) 32.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "width"
    )
    
    val color by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        label = "color"
    )
    
    Box(
        modifier = Modifier
            .width(width)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

/**
 * Interactive Tutorial Overlay
 */
@Composable
fun TutorialOverlay(
    step: TutorialStep,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        // Spotlight effect on target area
        // (Would need actual coordinates in real implementation)
        
        // Tutorial card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close tutorial")
                    }
                }
                
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step ${step.stepNumber}/${step.totalSteps}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(onClick = onNext) {
                        Text(if (step.stepNumber < step.totalSteps) "Next" else "Finish")
                    }
                }
            }
        }
    }
}

/**
 * Quick Tips Card
 */
@Composable
fun QuickTipCard(
    tip: QuickTip,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFFEB3B),
                modifier = Modifier.size(32.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💡 Pro Tip",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = tip.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Data classes
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

data class TutorialStep(
    val stepNumber: Int,
    val totalSteps: Int,
    val title: String,
    val description: String,
    val targetId: String
)

data class QuickTip(
    val id: String,
    val text: String,
    val category: String
)

/**
 * Tutorial Manager to show contextual help
 */
class TutorialManager {
    private val _currentStep = MutableStateFlow<TutorialStep?>(null)
    val currentStep: StateFlow<TutorialStep?> = _currentStep.asStateFlow()
    
    private val tips = listOf(
        QuickTip(
            id = "tip_1",
            text = "Tap on any sensor card to see detailed real-time data and visualizations",
            category = "navigation"
        ),
        QuickTip(
            id = "tip_2",
            text = "Enable auto-save in settings to automatically store all sensor readings",
            category = "features"
        ),
        QuickTip(
            id = "tip_3",
            text = "Complete daily challenges to earn XP and level up faster",
            category = "gamification"
        ),
        QuickTip(
            id = "tip_4",
            text = "Export your data to CSV or JSON for analysis in external tools",
            category = "export"
        ),
        QuickTip(
            id = "tip_5",
            text = "The affective computing module works best when you move naturally",
            category = "affective"
        )
    )
    
    fun startTutorial() {
        _currentStep.value = TutorialStep(
            stepNumber = 1,
            totalSteps = 5,
            title = "Welcome!",
            description = "Let's take a quick tour of SensorHub",
            targetId = "home"
        )
    }
    
    fun nextStep() {
        val current = _currentStep.value ?: return
        if (current.stepNumber < current.totalSteps) {
            _currentStep.value = current.copy(stepNumber = current.stepNumber + 1)
        } else {
            _currentStep.value = null
        }
    }
    
    fun dismissTutorial() {
        _currentStep.value = null
    }
    
    fun getRandomTip(): QuickTip {
        return tips.random()
    }
}
