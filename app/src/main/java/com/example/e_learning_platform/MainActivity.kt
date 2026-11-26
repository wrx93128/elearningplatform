package com.example.e_learning_platform

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.e_learning_platform.auth.ui.AuthContainerScreen
import com.example.e_learning_platform.auth.viewmodel.AuthSessionState
import com.example.e_learning_platform.auth.viewmodel.AuthViewModel
import com.example.e_learning_platform.quiz.api.QuizApiService
import com.example.e_learning_platform.quiz.model.QuizSummary
import com.example.e_learning_platform.quiz.ui.CreateQuizActivity
import com.example.e_learning_platform.quiz.ui.QuizTakingActivity
import com.example.e_learning_platform.ui.theme.ElearningplatformTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElearningplatformTheme {
                val authUiState by authViewModel.uiState.collectAsState()

                when (val sessionState = authUiState.sessionState) {
                    AuthSessionState.Checking -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    AuthSessionState.Unauthenticated -> {
                        AuthContainerScreen(
                            authViewModel = authViewModel,
                            uiState = authUiState
                        )
                    }
                    is AuthSessionState.Authenticated -> {
                        MainScreen(
                            sessionState = sessionState,
                            onLogout = authViewModel::logout,
                            onNavigateToCreateQuiz = {
                                startActivity(Intent(this, CreateQuizActivity::class.java))
                            },
                            onNavigateToTakeQuiz = { quizId ->
                                startActivity(QuizTakingActivity.createIntent(this, quizId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sessionState: AuthSessionState.Authenticated,
    onLogout: () -> Unit,
    onNavigateToCreateQuiz: () -> Unit,
    onNavigateToTakeQuiz: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quizzes by remember { mutableStateOf<List<QuizSummary>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun loadQuizzes() {
        coroutineScope.launch {
            val result = QuizApiService.default.getQuizzes()
            quizzes = result.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(Unit) {
        loadQuizzes()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "E-Learning Platform",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onLogout
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Wyloguj się"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Witaj, ${sessionState.user.name}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text(sessionState.user.email) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    Text(
                        text = "Twórz i rozwiązuj interaktywne quizy z pytaniami jednokrotnego i wielokrotnego wyboru.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dostępne quizy (${quizzes.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onNavigateToCreateQuiz
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nowy quiz")
                    }
                }
            }

            items(quizzes, key = { it.id }) { quizSummary ->
                QuizCardItem(
                    quiz = quizSummary,
                    onSolveClick = { onNavigateToTakeQuiz(quizSummary.id) }
                )
            }
        }
    }
}

@Composable
fun QuizCardItem(
    quiz: QuizSummary,
    onSolveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (quiz.timeLimitMinutes != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${quiz.timeLimitMinutes} min") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.height(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            if (quiz.description.isNotBlank()) {
                Text(
                    text = quiz.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pytania: ${quiz.questionCount} • Punkty: ${quiz.totalPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(onClick = onSolveClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rozwiąż quiz")
                }
            }
        }
    }
}
