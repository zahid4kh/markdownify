import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*


@Composable
private fun TabDropdown(
    openFiles: List<OpenFile>,
    activeFileIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activeFile = openFiles.getOrNull(activeFileIndex)

    Box {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.widthIn(min = 150.dp, max = 300.dp)
        ) {
            Text(
                text = activeFile?.displayTitle ?: "No files",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Open tabs",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 200.dp, max = 400.dp)
        ) {
            openFiles.forEachIndexed { index, file ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.displayTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                color = if (index == activeFileIndex)
                                    MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                            if (openFiles.size > 1) {
                                IconButton(
                                    onClick = {
                                        onTabClosed(index)
                                        expanded = false
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close file",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onTabSelected(index)
                        expanded = false
                    },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (index == activeFileIndex)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                )
            }
        }
    }
}