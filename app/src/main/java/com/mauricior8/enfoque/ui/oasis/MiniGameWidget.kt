package com.mauricior8.enfoque.ui.oasis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.data.model.MiniGameType
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.ui.components.OasisCard
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import kotlin.math.abs

@Composable
fun MiniGameWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    OasisCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = config.title.ifBlank { gameName(config.game) },
                    color = colors.content,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.Close, "Quitar", tint = colors.secondaryContent,
                    modifier = Modifier.height(20.dp).clickable { onRemove() },
                )
            }
            Spacer(Modifier.height(12.dp))
            when (config.game) {
                MiniGameType.GAME_2048 -> Game2048()
                else -> GamePlaceholder(gameName(config.game))
            }
        }
    }
}

private fun gameName(type: MiniGameType): String = when (type) {
    MiniGameType.GAME_2048 -> "2048"
    MiniGameType.SUDOKU -> "Sudoku"
    MiniGameType.SNAKE -> "Serpiente"
    MiniGameType.BRICKS -> "Ladrillos"
    MiniGameType.TRIVIA -> "Trivia"
}

@Composable
private fun GamePlaceholder(name: String) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .border(1.dp, colors.stroke, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$name\n(próximamente)",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/* ============================ 2048 ============================ */

@Composable
private fun Game2048() {
    val colors = LocalEnfoqueColors.current
    var grid by remember { mutableStateOf(newGame()) }
    var score by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    fun apply(direction: Direction) {
        if (gameOver) return
        val (moved, newGrid, gained) = move(grid, direction)
        if (moved) {
            val withNew = spawn(newGrid)
            grid = withNew
            score += gained
            if (!canMove(withNew)) gameOver = true
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Puntos: $score", color = colors.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.Refresh, "Reiniciar", tint = colors.content,
                modifier = Modifier.height(22.dp).clickable {
                    grid = newGame(); score = 0; gameOver = false
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.stroke, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    var dx = 0f
                    var dy = 0f
                    detectDragGestures(
                        onDragStart = { dx = 0f; dy = 0f },
                        onDragEnd = {
                            if (abs(dx) > abs(dy)) {
                                if (dx > 40f) apply(Direction.RIGHT) else if (dx < -40f) apply(Direction.LEFT)
                            } else {
                                if (dy > 40f) apply(Direction.DOWN) else if (dy < -40f) apply(Direction.UP)
                            }
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            dx += amount.x; dy += amount.y
                        },
                    )
                }
                .padding(6.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                for (r in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
                        for (c in 0 until 4) {
                            val value = grid[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (value == 0) colors.subtleFill else colors.content.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = value.toString(),
                                        color = colors.background,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (gameOver) {
            Spacer(Modifier.height(8.dp))
            Text("¡Juego terminado! Desliza para reiniciar.", color = colors.secondaryContent)
        } else {
            Spacer(Modifier.height(8.dp))
            Text("Desliza para mover las fichas.", color = colors.secondaryContent, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private enum class Direction { LEFT, RIGHT, UP, DOWN }

private fun newGame(): Array<IntArray> {
    val g = Array(4) { IntArray(4) }
    return spawn(spawn(g))
}

private fun spawn(grid: Array<IntArray>): Array<IntArray> {
    val empties = mutableListOf<Pair<Int, Int>>()
    for (r in 0 until 4) for (c in 0 until 4) if (grid[r][c] == 0) empties += r to c
    if (empties.isEmpty()) return grid
    val (r, c) = empties.random()
    val copy = grid.map { it.copyOf() }.toTypedArray()
    copy[r][c] = if (Math.random() < 0.9) 2 else 4
    return copy
}

private data class MoveResult(val moved: Boolean, val grid: Array<IntArray>, val gained: Int)

private fun move(grid: Array<IntArray>, direction: Direction): MoveResult {
    // Normalize to "move left" by rotating, then rotate back.
    var rotated = when (direction) {
        Direction.LEFT -> grid.map { it.copyOf() }.toTypedArray()
        Direction.RIGHT -> grid.map { it.reversedArray() }.toTypedArray()
        Direction.UP -> transpose(grid)
        Direction.DOWN -> transpose(grid).map { it.reversedArray() }.toTypedArray()
    }

    var gained = 0
    var moved = false
    for (r in 0 until 4) {
        val (newRow, rowGain, rowMoved) = collapseLeft(rotated[r])
        rotated[r] = newRow
        gained += rowGain
        if (rowMoved) moved = true
    }

    val restored = when (direction) {
        Direction.LEFT -> rotated
        Direction.RIGHT -> rotated.map { it.reversedArray() }.toTypedArray()
        Direction.UP -> transpose(rotated)
        Direction.DOWN -> transpose(rotated.map { it.reversedArray() }.toTypedArray())
    }
    return MoveResult(moved, restored, gained)
}

private fun collapseLeft(row: IntArray): Triple<IntArray, Int, Boolean> {
    val nums = row.filter { it != 0 }.toMutableList()
    val result = mutableListOf<Int>()
    var gained = 0
    var i = 0
    while (i < nums.size) {
        if (i + 1 < nums.size && nums[i] == nums[i + 1]) {
            val merged = nums[i] * 2
            result += merged
            gained += merged
            i += 2
        } else {
            result += nums[i]
            i += 1
        }
    }
    while (result.size < 4) result += 0
    val newRow = result.toIntArray()
    val moved = !newRow.contentEquals(row)
    return Triple(newRow, gained, moved)
}

private fun transpose(grid: Array<IntArray>): Array<IntArray> {
    val t = Array(4) { IntArray(4) }
    for (r in 0 until 4) for (c in 0 until 4) t[c][r] = grid[r][c]
    return t
}

private fun canMove(grid: Array<IntArray>): Boolean {
    for (r in 0 until 4) for (c in 0 until 4) {
        if (grid[r][c] == 0) return true
        if (c + 1 < 4 && grid[r][c] == grid[r][c + 1]) return true
        if (r + 1 < 4 && grid[r][c] == grid[r + 1][c]) return true
    }
    return false
}

/* ============================ Custom widget ============================ */

@Composable
fun CustomWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    OasisCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.text.BasicTextField(
                    value = config.title,
                    onValueChange = { onChange(config.copy(title = it)) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = colors.content,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.content),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (config.title.isEmpty()) {
                            Text("Título del módulo", color = colors.secondaryContent, fontSize = 20.sp)
                        }
                        inner()
                    },
                )
                Icon(
                    Icons.Outlined.Close, "Quitar", tint = colors.secondaryContent,
                    modifier = Modifier.height(20.dp).clickable { onRemove() },
                )
            }
            Spacer(Modifier.height(12.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = config.customBody,
                onValueChange = { onChange(config.copy(customBody = it)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = colors.content, fontSize = 16.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.content),
                modifier = Modifier.fillMaxWidth().height(80.dp),
                decorationBox = { inner ->
                    if (config.customBody.isEmpty()) {
                        Text(
                            "Escribe lo que quieras: ej. \"Ejercicio diario de ajedrez\", una rutina, recordatorios...",
                            color = colors.secondaryContent, fontSize = 16.sp,
                        )
                    }
                    inner()
                },
            )
        }
    }
}
