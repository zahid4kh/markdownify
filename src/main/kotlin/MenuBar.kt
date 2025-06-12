import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

@Preview
@Composable
fun MenuBar(
    onNewFile: () -> Unit = {},
    onOpenFile: () -> Unit = {},
    onSaveFile: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onShowInfo: () -> Unit = {}
){
    Row(
        modifier = Modifier
            .padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ){
        MenuBarItem(
            itemTitle = FileMenuItem.FILE,
            itemTitleIcon = Icons.Default.MoreHoriz,
            children = listOf(FileMenuItem.NEW_FILE, FileMenuItem.OPEN_FILE, FileMenuItem.SAVE_FILE),
            onItemClick = { item ->
                when (item) {
                    FileMenuItem.NEW_FILE -> onNewFile()
                    FileMenuItem.OPEN_FILE -> onOpenFile()
                    FileMenuItem.SAVE_FILE -> onSaveFile()
                    else -> {}
                }
            }
        )
        Spacer(modifier = Modifier.width(5.dp))
        MenuBarItem(
            itemTitle = FileMenuItem.OPTIONS,
            itemTitleIcon = Icons.Default.Settings,
            children = listOf(FileMenuItem.DARK_MODE, FileMenuItem.INFO),
            onItemClick = { item ->
                when (item) {
                    FileMenuItem.DARK_MODE -> onToggleDarkMode()
                    FileMenuItem.INFO -> onShowInfo()
                    else -> {}
                }
            }
        )
    }
}

@Composable
fun MenuBarItem(
    itemTitle: FileMenuItem,
    itemTitleIcon: ImageVector? = null,
    children: List<FileMenuItem>,
    onItemClick: (FileMenuItem) -> Unit = {}
){
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = !expanded },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
        ) {
            Icon(
                imageVector = itemTitleIcon?: Icons.Default.MoreHoriz,
                contentDescription = "${itemTitle.title} menu",
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = itemTitle.title,
                fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                fontSize = MaterialTheme.typography.labelMedium.fontSize
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(IntrinsicSize.Max),
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            children.forEach { childItem ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = childItem.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        expanded = false
                        onItemClick(childItem)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                )
            }
        }
    }
}

enum class FileMenuItem(val title: String) {
    FILE("File"),
    NEW_FILE("New file"),
    OPEN_FILE("Open existing"),
    SAVE_FILE("Save"),
    OPTIONS("Options"),
    DARK_MODE("Toggle theme"),
    INFO("Info")
}