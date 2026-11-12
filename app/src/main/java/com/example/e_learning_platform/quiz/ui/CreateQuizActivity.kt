package com.example.e_learning_platform.quiz.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.e_learning_platform.quiz.api.MockQuizApiService
import com.example.e_learning_platform.quiz.model.CreateQuestionRequest
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizValidationResult
import com.example.e_learning_platform.quiz.state.QuizCreationState
import com.example.e_learning_platform.ui.theme.ElearningplatformTheme
import kotlinx.coroutines.launch

class CreateQuizActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElearningplatformTheme {
                CreateQuizScreen(
                    onNavigateBack = { finish() },
                    onQuizCreated = { createdQuiz ->
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    onNavigateBack: () -> Unit,
    onQuizCreated: (Quiz) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(QuizCreationState()) }
    var validationErrors by remember { mutableStateOf<List<String>?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalPoints = state.questions.sumOf { it.points }

    fun submitQuiz() {
        when (val validation = state.validate()) {
            is QuizValidationResult.Invalid -> {
                validationErrors = validation.errors
            }
            is QuizValidationResult.Valid -> {
                coroutineScope.launch {
                    isSubmitting = true
                    val result = MockQuizApiService.default.createQuiz(state.toCreateQuizRequest())
                    isSubmitting = false
                    result.onSuccess { createdQuiz ->
                        Toast.makeText(context, "Quiz '${createdQuiz.title}' został utworzony!", Toast.LENGTH_SHORT).show()
                        onQuizCreated(createdQuiz)
                    }.onFailure { error ->
                        Toast.makeText(context, "Błąd tworzenia quizu: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kreator quizu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .height(24.dp)
                                .width(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = { submitQuiz() }
                        ) {
                            Text("Zapisz", fontWeight = FontWeight.Bold)
                        }
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
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                QuizMetadataCard(
                    title = state.title,
                    description = state.description,
                    timeLimitMinutes = state.timeLimitMinutes,
                    passingPercentage = state.passingPercentage,
                    onTitleChange = { state = state.updateTitle(it) },
                    onDescriptionChange = { state = state.updateDescription(it) },
                    onTimeLimitChange = { state = state.updateTimeLimit(it) },
                    onPassingPercentageChange = { state = state.updatePassingPercentage(it) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pytania (${state.questions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Suma punktów: $totalPoints",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { state = state.addQuestion() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dodaj pytanie")
                    }
                }
            }

            itemsIndexed(state.questions, key = { _, q -> q.id }) { index, question ->
                QuestionEditorCard(
                    index = index,
                    question = question,
                    canDelete = state.questions.size > 1,
                    onTextChange = { text ->
                        state = state.updateQuestionText(question.id, text)
                    },
                    onTypeChange = { newType ->
                        state = state.updateQuestionType(question.id, newType)
                    },
                    onPointsChange = { points ->
                        state = state.updateQuestionPoints(question.id, points)
                    },
                    onExplanationChange = { explanation ->
                        state = state.updateQuestionExplanation(question.id, explanation)
                    },
                    onDeleteQuestion = {
                        state = state.removeQuestion(question.id)
                    },
                    onAddOption = {
                        state = state.addOption(question.id)
                    },
                    onRemoveOption = { optionId ->
                        state = state.removeOption(question.id, optionId)
                    },
                    onOptionTextChange = { optionId, text ->
                        state = state.updateOptionText(question.id, optionId, text)
                    },
                    onToggleCorrectness = { optionId ->
                        state = state.toggleOptionCorrectness(question.id, optionId)
                    }
                )
            }

            item {
                Button(
                    onClick = { submitQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    enabled = !isSubmitting
                ) {
                    Text("Utwórz quiz ($totalPoints pkt)")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (validationErrors != null) {
        AlertDialog(
            onDismissRequest = { validationErrors = null },
            title = { Text("Błędy formularza", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    validationErrors?.forEach { error ->
                        Text("• $error", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { validationErrors = null }) {
                    Text("Rozumiem")
                }
            }
        )
    }
}

@Composable
fun QuizMetadataCard(
    title: String,
    description: String,
    timeLimitMinutes: Int?,
    passingPercentage: Int,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeLimitChange: (Int?) -> Unit,
    onPassingPercentageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Informacje podstawowe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Tytuł quizu *") },
                placeholder = { Text("np. Podstawy programowania w Kotlinie") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis quizu (opcjonalnie)") },
                placeholder = { Text("Czego dotyczy quiz, dla kogo jest przeznaczony...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = timeLimitMinutes?.toString().orEmpty(),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        onTimeLimitChange(digits.toIntOrNull())
                    },
                    label = { Text("Limit (min)") },
                    placeholder = { Text("Brak") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = passingPercentage.toString(),
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        val value = digits.toIntOrNull() ?: 0
                        onPassingPercentageChange(value.coerceIn(0, 100))
                    },
                    label = { Text("Próg zdania (%)") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun QuestionEditorCard(
    index: Int,
    question: CreateQuestionRequest,
    canDelete: Boolean,
    onTextChange: (String) -> Unit,
    onTypeChange: (QuestionType) -> Unit,
    onPointsChange: (Int) -> Unit,
    onExplanationChange: (String) -> Unit,
    onDeleteQuestion: () -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (String) -> Unit,
    onOptionTextChange: (String, String) -> Unit,
    onToggleCorrectness: (String) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = question.points.toString(),
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            val pts = digits.toIntOrNull() ?: 1
                            onPointsChange(pts.coerceAtLeast(1))
                        },
                        label = { Text("Pkt") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(72.dp),
                        singleLine = true
                    )

                    if (canDelete) {
                        IconButton(onClick = onDeleteQuestion) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Usuń pytanie",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.selectableGroup()) {
                Text(
                    text = "Typ pytania:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (question.type == QuestionType.SINGLE_CHOICE),
                                onClick = { onTypeChange(QuestionType.SINGLE_CHOICE) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (question.type == QuestionType.SINGLE_CHOICE),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jednokrotny wybór", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (question.type == QuestionType.MULTIPLE_CHOICE),
                                onClick = { onTypeChange(QuestionType.MULTIPLE_CHOICE) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (question.type == QuestionType.MULTIPLE_CHOICE),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wielokrotny wybór", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedTextField(
                value = question.text,
                onValueChange = onTextChange,
                label = { Text("Treść pytania *") },
                placeholder = { Text("np. Co oznacza słowo kluczowe 'val'?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3
            )

            HorizontalDivider()

            Text(
                text = "Odpowiedzi (zaznacz poprawne):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            question.options.forEachIndexed { optIndex, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (question.type == QuestionType.SINGLE_CHOICE) {
                        RadioButton(
                            selected = option.isCorrect,
                            onClick = { onToggleCorrectness(option.id) }
                        )
                    } else {
                        Checkbox(
                            checked = option.isCorrect,
                            onCheckedChange = { onToggleCorrectness(option.id) }
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedTextField(
                        value = option.text,
                        onValueChange = { text -> onOptionTextChange(option.id, text) },
                        label = { Text("Odpowiedź #${optIndex + 1} *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (question.options.size > 2) {
                        IconButton(onClick = { onRemoveOption(option.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Usuń opcję",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddOption,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj kolejną odpowiedź")
            }

            OutlinedTextField(
                value = question.explanation.orEmpty(),
                onValueChange = onExplanationChange,
                label = { Text("Wyjaśnienie odpowiedzi (opcjonalnie)") },
                placeholder = { Text("Wyjaśnienie pojawi się po rozwiązaniu quizu...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 2
            )
        }
    }
}
