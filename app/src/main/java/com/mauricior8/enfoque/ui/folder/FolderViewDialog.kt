package com.mauricior8.enfoque.ui.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.ui.components.IconCell
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * Opens a folder so the user can launch the apps inside it. An edit (pencil)
 * button switches to the folder editor.
 */
@Composable
fun FolderViewDialog(
    folder: Folder,
    items: List<LaunchableItem>,
    onLaunch: (LaunchableItem) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    val contained = items.filter { folder.itemKeys.contains(it.key) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = folder.name.ifBlank { "Carpeta" },
                        color = colors.content,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar carpeta",
                        tint = colors.content,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEdit() },
                    )
                }
                Spacer(Modifier.height(16.dp))

                if (contained.isEmpty()) {
                    Text(
                        "Esta carpeta está vacía. Toca el lápiz para añadir apps.",
                        color = colors.secondaryContent,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 76.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        items(contained, key = { it.key }) { item ->
                            IconCell(
                                label = item.label,
                                icon = item.icon,
                                onClick = {
                                    onLaunch(item)
                                    onDismiss()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
