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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.ui.components.DrawableImage
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import java.io.File

/**
 * Create / edit a customizable folder: name, up to 10 apps, and an optional
 * custom image (already cropped to a square by [com.mauricior8.enfoque.data.ImageStorage]).
 */
@Composable
fun FolderEditorDialog(
    initial: Folder,
    allItems: List<LaunchableItem>,
    pendingImagePath: String?,
    onPickImage: () -> Unit,
    onSave: (Folder) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    var name by remember { mutableStateOf(initial.name) }
    var imagePath by remember { mutableStateOf(initial.customImagePath) }
    val selected: SnapshotStateList<String> = remember { initial.itemKeys.toMutableStateList() }

    // When MainActivity finishes picking & cropping an image, adopt it.
    LaunchedEffect(pendingImagePath) {
        if (pendingImagePath != null) imagePath = pendingImagePath
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column {
                Text(
                    if (onDelete == null) "Nueva carpeta" else "Editar carpeta",
                    color = colors.content,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(16.dp))

                // Image + name row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(shape)
                            .border(1.dp, colors.stroke, shape)
                            .clickable { onPickImage() },
                        contentAlignment = Alignment.Center,
                    ) {
                        val path = imagePath
                        if (!path.isNullOrBlank() && File(path).exists()) {
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Icono de carpeta",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().clip(shape),
                            )
                        } else {
                            Icon(Icons.Outlined.Image, "Elegir imagen", tint = colors.secondaryContent, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, colors.stroke, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                singleLine = true,
                                textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                                cursorBrush = SolidColor(colors.content),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (name.isEmpty()) Text("Nombre", color = colors.secondaryContent)
                                    inner()
                                },
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Toca el cuadro para elegir una imagen (se recorta automáticamente).",
                            color = colors.secondaryContent,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Apps (${selected.size}/${Folder.MAX_APPS})",
                    color = colors.content,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allItems, key = { it.key }) { item ->
                        val isChecked = selected.contains(item.key)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selected.remove(item.key)
                                    } else if (selected.size < Folder.MAX_APPS) {
                                        selected.add(item.key)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(1.dp, colors.stroke, RoundedCornerShape(4.dp))
                                    .background(
                                        if (isChecked) colors.subtleFill else androidx.compose.ui.graphics.Color.Transparent,
                                        RoundedCornerShape(4.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isChecked) Text("✓", color = colors.content, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            DrawableImage(item.icon, item.label, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(item.label, color = colors.content, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onDelete != null) {
                        Text(
                            "Eliminar",
                            color = colors.secondaryContent,
                            modifier = Modifier.clickable { onDelete() }.padding(12.dp),
                        )
                        Spacer(Modifier.weight(1f))
                    }
                    Text("Cancelar", color = colors.secondaryContent, modifier = Modifier.clickable { onDismiss() }.padding(12.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Guardar",
                        color = colors.content,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.subtleFill)
                            .clickable {
                                onSave(
                                    initial.copy(
                                        name = name.ifBlank { "Carpeta" },
                                        itemKeys = selected.toList(),
                                        customImagePath = imagePath,
                                    )
                                )
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
