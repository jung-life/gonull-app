package app.gonull.ui.screens.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.JournalEntryEntity
import app.gonull.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    database: AppDatabase,
    onNavigateBack: () -> Unit
) {
    val entries by database.journalEntryDao().getAllEntriesFlow().collectAsState(initial = emptyList())
    var totalEntries by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(entries) {
        totalEntries = entries.size
    }

    // Count moods
    val moodCounts = remember(entries) {
        entries.groupBy { it.mood ?: "neutral" }.mapValues { it.value.size }
    }

    if (showAddDialog) {
        AddJournalEntryDialog(
            onSave = { text, mood ->
                scope.launch {
                    database.journalEntryDao().insertEntry(
                        JournalEntryEntity(
                            entryText = text,
                            mood = mood
                        )
                    )
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reclaim Journal",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoNullWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoNullWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoNullBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = GoNullBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Insight card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GoNullGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "\uD83D\uDCD3",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalEntries moments reclaimed",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoNullWhite,
                            fontWeight = FontWeight.Bold
                        )
                        if (moodCounts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                moodCounts.forEach { (mood, count) ->
                                    val emoji = when (mood) {
                                        "great" -> "\uD83D\uDE0A"
                                        "good" -> "\uD83D\uDE42"
                                        "neutral" -> "\uD83D\uDE10"
                                        "meh" -> "\uD83D\uDE11"
                                        else -> "\uD83D\uDE10"
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = emoji, fontSize = 20.sp)
                                        Text(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoNullGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No entries yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GoNullGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add an entry, or they'll appear automatically after a focus mode ends.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullGray.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(entries) { entry ->
                JournalEntryItem(entry = entry)
            }
        }
    }
}

@Composable
fun JournalEntryItem(entry: JournalEntryEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    val moodEmoji = when (entry.mood) {
        "great" -> "\uD83D\uDE0A"
        "good" -> "\uD83D\uDE42"
        "neutral" -> "\uD83D\uDE10"
        "meh" -> "\uD83D\uDE11"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoNullSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (moodEmoji.isNotEmpty()) {
                        Text(text = moodEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = dateFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelMedium,
                        color = GoNullGray
                    )
                }
                if (entry.linkedFocusMode != null) {
                    Text(
                        text = entry.linkedFocusMode,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoNullGreen.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.entryText,
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullWhite
            )

            if (entry.focusDurationMinutes != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Focus session: ${entry.focusDurationMinutes}min",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoNullGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddJournalEntryDialog(
    onSave: (text: String, mood: String) -> Unit,
    onDismiss: () -> Unit
) {
    var entryText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    val moods = listOf(
        "\uD83D\uDE0A" to "great",
        "\uD83D\uDE42" to "good",
        "\uD83D\uDE10" to "neutral",
        "\uD83D\uDE11" to "meh"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDCD3",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "New Journal Entry",
                    color = GoNullWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "How do you feel?",
                    color = GoNullGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { (emoji, mood) ->
                        Surface(
                            modifier = Modifier.clickable { selectedMood = mood },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedMood == mood) GoNullGreen.copy(alpha = 0.2f) else GoNullBlack
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = entryText,
                    onValueChange = { entryText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What's on your mind?", color = GoNullGray.copy(alpha = 0.5f)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GoNullWhite,
                        unfocusedTextColor = GoNullWhite,
                        cursorColor = GoNullGreen,
                        focusedBorderColor = GoNullGreen,
                        unfocusedBorderColor = GoNullBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(entryText, selectedMood ?: "neutral") },
                enabled = entryText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoNullGreen,
                    contentColor = GoNullBlack
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoNullGray)
            }
        }
    )
}
