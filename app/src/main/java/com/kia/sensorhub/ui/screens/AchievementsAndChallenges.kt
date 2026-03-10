package com.kia.sensorhub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kia.sensorhub.ui.animations.PulsatingGlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Achievements and Gamification Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Achievements")
                        Text(
                            text = "Level ${uiState.userLevel} • ${uiState.totalPoints} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Level Progress
            LevelProgressCard(
                currentLevel = uiState.userLevel,
                currentXP = uiState.currentLevelXP,
                nextLevelXP = uiState.nextLevelXP,
                modifier = Modifier.padding(16.dp)
            )
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Unlocked") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Locked") }
                )
            }
            
            // Achievements Grid
            val filteredAchievements = when (selectedTab) {
                1 -> uiState.achievements.filter { it.isUnlocked }
                2 -> uiState.achievements.filter { !it.isUnlocked }
                else -> uiState.achievements
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        onClick = { /* Show details */ }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelProgressCard(
    currentLevel: Int,
    currentXP: Int,
    nextLevelXP: Int,
    modifier: Modifier = Modifier
) {
    val progress = currentXP.toFloat() / nextLevelXP
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated ring
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFA500),
                                        Color(0xFFFFD700)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LVL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "$currentLevel",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                ) {
                    Text(
                        text = "Level $currentLevel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentXP / $nextLevelXP XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                )
                
                Text(
                    text = "${(animatedProgress * 100).toInt()}% to next level",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (achievement.isUnlocked) 1f else 0.95f,
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                achievement.color.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 4.dp else 0.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (achievement.isUnlocked) {
                        PulsatingGlow(
                            isActive = true,
                            color = achievement.color,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Surface(
                        shape = CircleShape,
                        color = if (achievement.isUnlocked)
                            achievement.color.copy(alpha = 0.3f)
                        else
                            Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = achievement.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = if (achievement.isUnlocked)
                                achievement.color
                            else
                                Color.Gray
                        )
                    }
                }
                
                // Title and XP
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = if (achievement.isUnlocked)
                            MaterialTheme.colorScheme.onSurface
                        else
                            Color.Gray
                    )
                    
                    if (achievement.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = achievement.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+${achievement.xpReward} XP",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = achievement.color
                            )
                        }
                    } else {
                        // Progress
                        if (achievement.progress < achievement.target) {
                            Text(
                                text = "${achievement.progress}/${achievement.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // "NEW" badge for recently unlocked
            if (achievement.isUnlocked && achievement.isNew) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFF5722)
                ) {
                    Text(
                        text = "NEW",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Daily Challenges Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengesScreen(
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenges") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak Card
            item {
                StreakCard(streak = uiState.currentStreak)
            }
            
            // Challenges
            items(uiState.challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onClaim = { viewModel.claimReward(challenge.id) }
                )
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep it going!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFFF5722)
                )
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = challenge.icon,
                        contentDescription = null,
                        tint = challenge.color,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (challenge.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
            
            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${challenge.progress}/${challenge.target}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "+${challenge.xpReward} XP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = challenge.color
                    )
                }
                
                LinearProgressIndicator(
                    progress = challenge.progress.toFloat() / challenge.target,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = challenge.color,
                    trackColor = challenge.color.copy(alpha = 0.2f)
                )
            }
            
            // Claim button
            if (challenge.isCompleted && !challenge.isClaimed) {
                Button(
                    onClick = onClaim,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = challenge.color
                    )
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Claim Reward")
                }
            }
        }
    }
}

// Data classes
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val isNew: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val progress: Int,
    val target: Int,
    val xpReward: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean = false
)

// ViewModels
@HiltViewModel
class AchievementsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()
    
    init {
        loadAchievements()
    }
    
    private fun loadAchievements() {
        val achievements = listOf(
            Achievement(
                id = "first_sensor",
                title = "First Steps",
                description = "Use your first sensor",
                icon = Icons.Default.Sensors,
                color = Color(0xFF2196F3),
                xpReward = 100,
                isUnlocked = true,
                isNew = true
            ),
            Achievement(
                id = "data_collector",
                title = "Data Collector",
                description = "Collect 1000 readings",
                icon = Icons.Default.DataUsage,
                color = Color(0xFF4CAF50),
                xpReward = 250,
                isUnlocked = true
            ),
            Achievement(
                id = "sensor_master",
                title = "Sensor Master",
                description = "Use all 7 sensors",
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700),
                xpReward = 500,
                isUnlocked = false,
                progress = 5,
                target = 7
            ),
            Achievement(
                id = "affective_analyst",
                title = "Emotion Expert",
                description = "Analyze emotions 50 times",
                icon = Icons.Default.Psychology,
                color = Color(0xFF9C27B0),
                xpReward = 300,
                isUnlocked = false,
                progress = 23,
                target = 50
            )
        )
        
        _uiState.value = _uiState.value.copy(
            achievements = achievements,
            userLevel = 5,
            totalPoints = 2350,
            currentLevelXP = 350,
            nextLevelXP = 1000
        )
    }
}

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val userLevel: Int = 1,
    val totalPoints: Int = 0,
    val currentLevelXP: Int = 0,
    val nextLevelXP: Int = 1000
)

@HiltViewModel
class ChallengesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()
    
    init {
        loadChallenges()
    }
    
    private fun loadChallenges() {
        val challenges = listOf(
            Challenge(
                id = "daily_1",
                title = "Morning Movement",
                description = "Record 100 accelerometer readings",
                icon = Icons.Default.DirectionsRun,
                color = Color(0xFF2196F3),
                progress = 100,
                target = 100,
                xpReward = 50,
                isCompleted = true
            ),
            Challenge(
                id = "daily_2",
                title = "Compass Navigator",
                description = "Use magnetometer for 5 minutes",
                icon = Icons.Default.Explore,
                color = Color(0xFF9C27B0),
                progress = 3,
                target = 5,
                xpReward = 75,
                isCompleted = false
            ),
            Challenge(
                id = "daily_3",
                title = "Data Export",
                description = "Export your sensor data",
                icon = Icons.Default.FileDownload,
                color = Color(0xFF4CAF50),
                progress = 0,
                target = 1,
                xpReward = 100,
                isCompleted = false
            )
        )
        
        _uiState.value = _uiState.value.copy(
            challenges = challenges,
            currentStreak = 7
        )
    }
    
    fun claimReward(challengeId: String) {
        // Claim reward logic
    }
}

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val currentStreak: Int = 0
)
