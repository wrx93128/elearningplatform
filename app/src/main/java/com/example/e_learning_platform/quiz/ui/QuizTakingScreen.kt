package com.example.e_learning_platform.quiz.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_learning_platform.quiz.model.Question
import com.example.e_learning_platform.quiz.model.QuestionResult
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.QuizOption
import com.example.e_learning_platform.quiz.model.QuizResult
import com.example.e_learning_platform.quiz.state.QuizTakingState
import com.example.e_learning_platform.quiz.state.QuizTakingUiState
import com.example.e_learning_platform.quiz.viewmodel.QuizTakingViewModel

@Composable
fun QuizTakingScreen(
    viewModel: QuizTakingViewModel,
    uiState: QuizTakingUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is QuizTakingUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Ładowanie pytania i opcji...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is QuizTakingUiState.InProgress -> {
            QuizTakingContent(
                takingState = uiState.takingState,
                timeRemainingSeconds = uiState.timeRemainingSeconds,
                isSubmitDialogOpen = uiState.isSubmitDialogOpen,
                onOptionSelected = { questionId, optionId ->
                    viewModel.selectOption(questionId, optionId)
                },
                onNextQuestion = { viewModel.nextQuestion() },
                onPreviousQuestion = { viewModel.previousQuestion() },
                onGoToQuestion = { index -> viewModel.goToQuestion(index) },
                onShowSubmitDialog = { viewModel.showSubmitDialog() },
                onHideSubmitDialog = { viewModel.hideSubmitDialog() },
                onSubmitQuiz = { viewModel.submitQuiz() },
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }

        is QuizTakingUiState.Submitting -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Sprawdzanie odpowiedzi i obliczanie wyniku...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is QuizTakingUiState.Completed -> {
            QuizResultView(
                result = uiState.result,
                onRestartQuiz = { viewModel.restartQuiz() },
                onFinish = onNavigateBack,
                modifier = modifier
            )
        }

        is QuizTakingUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Wystąpił błąd",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Powrót")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingContent(
    takingState: QuizTakingState,
    timeRemainingSeconds: Int?,
    isSubmitDialogOpen: Boolean,
    onOptionSelected: (String, String) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onShowSubmitDialog: () -> Unit,
    onHideSubmitDialog: () -> Unit,
    onSubmitQuiz: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuestion = takingState.currentQuestion

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = takingState.quiz.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Wyjdź")
                        }
                    },
                    actions = {
                        if (timeRemainingSeconds != null) {
                            QuizTimerBadge(timeRemainingSeconds = timeRemainingSeconds)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                LinearProgressIndicator(
                    progress = {
                        if (takingState.questionCount > 0) {
                            (takingState.currentQuestionIndex + 1).toFloat() / takingState.questionCount.toFloat()
                        } else 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            QuizBottomBar(
                hasPrevious = takingState.hasPreviousQuestion,
                hasNext = takingState.hasNextQuestion,
                isAllAnswered = takingState.isAllAnswered,
                onPrevious = onPreviousQuestion,
                onNext = onNextQuestion,
                onSubmitClick = onShowSubmitDialog
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            QuestionJumpBar(
                questionCount = takingState.questionCount,
                currentIndex = takingState.currentQuestionIndex,
                selectedOptions = takingState.selectedOptions,
                questions = takingState.quiz.questions,
                onSelectQuestion = onGoToQuestion
            )

            if (currentQuestion != null) {
                QuestionView(
                    questionIndex = takingState.currentQuestionIndex,
                    totalQuestions = takingState.questionCount,
                    question = currentQuestion,
                    selectedOptionIds = takingState.getSelectedOptions(currentQuestion.id),
                    onOptionClick = { optionId ->
                        onOptionSelected(currentQuestion.id, optionId)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (isSubmitDialogOpen) {
        ConfirmSubmitDialog(
            answeredCount = takingState.answeredCount,
            totalCount = takingState.questionCount,
            onConfirm = onSubmitQuiz,
            onDismiss = onHideSubmitDialog
        )
    }
}

@Composable
fun QuizTimerBadge(
    timeRemainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val isUrgent = timeRemainingSeconds < 60

    val backgroundColor = if (isUrgent) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (isUrgent) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier.padding(end = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Pozostały czas",
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionJumpBar(
    questionCount: Int,
    currentIndex: Int,
    selectedOptions: Map<String, Set<String>>,
    questions: List<Question>,
    onSelectQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(questionCount) { index ->
            val question = questions.getOrNull(index)
            val isAnswered = question?.let { !selectedOptions[it.id].isNullOrEmpty() } == true
            val isSelected = index == currentIndex

            val chipColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isAnswered -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val textColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isAnswered -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(chipColor)
                    .clickable { onSelectQuestion(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun QuestionView(
    questionIndex: Int,
    totalQuestions: Int,
    question: Question,
    selectedOptionIds: Set<String>,
    onOptionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Text(
                            text = "Pytanie ${questionIndex + 1} z $totalQuestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (question.points == 1) "1 pkt" else "${question.points} pkt",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (question.type) {
                                    QuestionType.SINGLE_CHOICE -> "Jednokrotny wybór"
                                    QuestionType.MULTIPLE_CHOICE -> "Wielokrotny wybór"
                                }
                            )
                        }
                    )

                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        itemsIndexed(question.options, key = { _, opt -> opt.id }) { _, option ->
            val isSelected = selectedOptionIds.contains(option.id)
            OptionCard(
                option = option,
                questionType = question.type,
                isSelected = isSelected,
                onClick = { onOptionClick(option.id) }
            )
        }
    }
}

@Composable
fun OptionCard(
    option: QuizOption,
    questionType: QuestionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (questionType == QuestionType.SINGLE_CHOICE) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
            } else {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuizBottomBar(
    hasPrevious: Boolean,
    hasNext: Boolean,
    isAllAnswered: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = hasPrevious
            ) {
                Text("Poprzednie")
            }

            if (hasNext) {
                Button(onClick = onNext) {
                    Text("Następne")
                }
            } else {
                Button(
                    onClick = onSubmitClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAllAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Zakończ i wyślij")
                }
            }
        }
    }
}

@Composable
fun ConfirmSubmitDialog(
    answeredCount: Int,
    totalCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val unAnsweredCount = totalCount - answeredCount
    val warningText = if (unAnsweredCount > 0) {
        "Uwaga: Nie odpowiedziałeś na $unAnsweredCount z $totalCount pytań. Czy na pewno chcesz zakończyć quiz?"
    } else {
        "Odpowiedziałeś na wszystkie pytania ($totalCount z $totalCount). Czy chcesz wysłać swoje odpowiedzi?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wysyłanie quizu", fontWeight = FontWeight.Bold) },
        text = { Text(warningText) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Wyślij quiz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Wróć do pytań")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultView(
    result: QuizResult,
    onRestartQuiz: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passedColor = if (result.passed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val statusText = if (result.passed) "ZALICZONY" else "NIEZALICZONY"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wynik quizu", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = passedColor,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (result.passed) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = passedColor
                        )

                        Text(
                            text = result.quizTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${result.score} / ${result.maxScore} pkt (${String.format("%.1f", result.percentage)}%)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Przegląd pytań:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(result.questionResults, key = { _, qr -> qr.questionId }) { index, qResult ->
                QuestionResultCard(index = index, result = qResult)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestartQuiz,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rozwiąż ponownie")
                    }

                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Strona główna")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuestionResultCard(
    index: Int,
    result: QuestionResult,
    modifier: Modifier = Modifier
) {
    val cardBorder = if (result.isCorrect) {
        BorderStroke(1.dp, Color(0xFF4CAF50))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pytanie #${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (result.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${result.scoreAwarded} / ${result.maxScore} pkt",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!result.explanation.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Wyjaśnienie: ${result.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
