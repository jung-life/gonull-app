package app.gonull.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.gonull.data.local.entity.ImplementationIntentionEntity
import app.gonull.ui.theme.*

@Composable
fun IntentionManagementSection(
    intentions: List<ImplementationIntentionEntity>,
    onAddIntention: (String) -> Unit,
    onDeleteIntention: (ImplementationIntentionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "If-Then Plans",
                style = MaterialTheme.typography.titleMedium,
                color = GoNullGray
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add plan",
                    tint = GoNullGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create plans for when the urge hits. These appear on the blocking screen.",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (intentions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoNullSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No plans yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to create your first if-then plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            intentions.forEach { intention ->
                IntentionItem(
                    intention = intention,
                    onDelete = { onDeleteIntention(intention) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddDialog) {
        AddIntentionDialog(
            onConfirm = { action ->
                onAddIntention(action)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun IntentionItem(
    intention: ImplementationIntentionEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = GoNullGreen.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IF I feel the urge...",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
                Text(
                    text = "THEN ${intention.thenAction}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Medium
                )
                if (intention.packageName != null) {
                    Text(
                        text = "For: ${intention.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoNullGray.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = GoNullGray
                )
            }
        }
    }
}

@Composable
private fun AddIntentionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var actionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GoNullSurface,
        title = {
            Text("New If-Then Plan", color = GoNullWhite)
        },
        text = {
            Column {
                Text(
                    text = "IF I feel the urge to check my phone...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "THEN I will:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actionText,
                    onValueChange = { actionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("e.g., do 10 pushups", color = GoNullGray.copy(alpha = 0.5f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GoNullWhite,
                        unfocusedTextColor = GoNullWhite,
                        cursorColor = GoNullGreen,
                        focusedBorderColor = GoNullGreen,
                        unfocusedBorderColor = GoNullGray
                    ),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (actionText.isNotBlank()) onConfirm(actionText.trim()) },
                enabled = actionText.isNotBlank()
            ) {
                Text("Save", color = GoNullGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoNullGray)
            }
        }
    )
}
