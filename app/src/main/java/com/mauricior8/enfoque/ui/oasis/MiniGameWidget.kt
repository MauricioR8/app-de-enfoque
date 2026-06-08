package com.mauricior8.enfoque.ui.oasis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.data.model.MiniGameType
import com.mauricior8.enfoque.data.model.TriviaItem
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.ui.components.OasisCard
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import kotlinx.coroutines.delay
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
                    text = config.title.ifBlank { "Minijuegos" },
                    color = colors.content,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.Close, "Quitar", tint = colors.secondaryContent,
                    modifier = Modifier.size(20.dp).clickable { onRemove() },
                )
            }
            Spacer(Modifier.height(10.dp))
            // Game selector (only the games requested for this build).
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GameTab("2048", config.game == MiniGameType.GAME_2048) { onChange(config.copy(game = MiniGameType.GAME_2048)) }
                GameTab("Serpiente", config.game == MiniGameType.SNAKE) { onChange(config.copy(game = MiniGameType.SNAKE)) }
                GameTab("Ladrillos", config.game == MiniGameType.BRICKS) { onChange(config.copy(game = MiniGameType.BRICKS)) }
                GameTab("Trivia", config.game == MiniGameType.TRIVIA) { onChange(config.copy(game = MiniGameType.TRIVIA)) }
            }
            Spacer(Modifier.height(12.dp))
            when (config.game) {
                MiniGameType.GAME_2048 -> Game2048()
                MiniGameType.SNAKE -> SnakeGame()
                MiniGameType.BRICKS -> BricksGame()
                MiniGameType.TRIVIA -> TriviaGame(config, onChange)
                else -> GamePlaceholder(gameName(config.game))
            }
        }
    }
}

@Composable
private fun GameTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(if (selected) Modifier.background(colors.subtleFill) else Modifier.border(1.dp, colors.stroke, RoundedCornerShape(10.dp)))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(label, color = if (selected) colors.content else colors.secondaryContent, style = MaterialTheme.typography.labelLarge)
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



/* ============================ Snake (retro) ============================ */

@Composable
private fun SnakeGame() {
    val colors = LocalEnfoqueColors.current
    val cols = 16
    val rows = 16
    // Retro LCD palette.
    val boardColor = Color(0xFF9BBC0F)
    val pixelColor = Color(0xFF0F380F)

    var snake by remember { mutableStateOf(listOf(8 to 8, 7 to 8, 6 to 8)) }
    var dir by remember { mutableStateOf(1 to 0) }
    var food by remember { mutableStateOf(11 to 8) }
    var alive by remember { mutableStateOf(true) }
    var running by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    fun reset() {
        snake = listOf(8 to 8, 7 to 8, 6 to 8)
        dir = 1 to 0
        food = 11 to 8
        alive = true
        score = 0
        running = true
    }

    LaunchedEffect(running, alive) {
        while (running && alive) {
            delay(170L)
            val head = snake.first()
            val nh = ((head.first + dir.first + cols) % cols) to ((head.second + dir.second + rows) % rows)
            if (snake.contains(nh)) {
                alive = false
                running = false
            } else {
                val grew = nh == food
                snake = (listOf(nh) + snake).let { if (grew) it else it.dropLast(1) }
                if (grew) {
                    score++
                    var newFood: Pair<Int, Int>
                    do { newFood = (0 until cols).random() to (0 until rows).random() } while (snake.contains(newFood))
                    food = newFood
                }
            }
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Puntos: $score", color = colors.content, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Refresh, "Reiniciar", tint = colors.content, modifier = Modifier.size(22.dp).clickable { reset() })
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(boardColor)
                .pointerInput(Unit) {
                    var dx = 0f; var dy = 0f
                    detectDragGestures(
                        onDragStart = { dx = 0f; dy = 0f },
                        onDragEnd = {
                            if (abs(dx) > abs(dy)) {
                                if (dx > 30f && dir != (-1 to 0)) dir = 1 to 0
                                else if (dx < -30f && dir != (1 to 0)) dir = -1 to 0
                            } else {
                                if (dy > 30f && dir != (0 to -1)) dir = 0 to 1
                                else if (dy < -30f && dir != (0 to 1)) dir = 0 to -1
                            }
                            if (!running) { running = true; alive = true }
                        },
                        onDrag = { change, amount -> change.consume(); dx += amount.x; dy += amount.y },
                    )
                }
                .padding(4.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cell = size.width / cols
                food.let { drawRect(pixelColor, Offset(it.first * cell, it.second * cell), Size(cell, cell)) }
                snake.forEach { (x, y) ->
                    drawRect(pixelColor, Offset(x * cell + cell * 0.06f, y * cell + cell * 0.06f), Size(cell * 0.88f, cell * 0.88f))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (!alive) "¡Chocaste! Toca reiniciar o desliza." else if (!running) "Desliza para empezar." else "Desliza para girar.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/* ============================ Bricks (breakout) ============================ */

@Composable
private fun BricksGame() {
    val colors = LocalEnfoqueColors.current
    val brickCols = 6
    val brickRows = 4

    // Logical coordinates in 0f..1f.
    var paddleX by remember { mutableStateOf(0.5f) }
    var ballX by remember { mutableStateOf(0.5f) }
    var ballY by remember { mutableStateOf(0.6f) }
    var velX by remember { mutableStateOf(0.012f) }
    var velY by remember { mutableStateOf(-0.016f) }
    var bricks by remember { mutableStateOf(List(brickRows * brickCols) { true }) }
    var running by remember { mutableStateOf(false) }
    var lost by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    val paddleW = 0.24f
    val ballR = 0.022f
    val brickH = 0.07f
    val topOffset = 0.06f

    fun reset() {
        paddleX = 0.5f; ballX = 0.5f; ballY = 0.6f
        velX = 0.012f; velY = -0.016f
        bricks = List(brickRows * brickCols) { true }
        running = true; lost = false; won = false
    }

    LaunchedEffect(running) {
        while (running && !lost && !won) {
            delay(16L)
            var nx = ballX + velX
            var ny = ballY + velY
            if (nx < ballR) { nx = ballR; velX = -velX }
            if (nx > 1f - ballR) { nx = 1f - ballR; velX = -velX }
            if (ny < ballR) { ny = ballR; velY = -velY }

            // Paddle collision (bottom).
            if (ny > 1f - ballR - 0.04f && ny < 1f) {
                if (nx in (paddleX - paddleW / 2)..(paddleX + paddleW / 2)) {
                    ny = 1f - ballR - 0.04f
                    velY = -abs(velY)
                    velX += (nx - paddleX) * 0.03f
                }
            }
            if (ny > 1f) { lost = true; running = false }

            // Brick collisions.
            val updated = bricks.toMutableList()
            var hit = false
            for (r in 0 until brickRows) {
                for (c in 0 until brickCols) {
                    val idx = r * brickCols + c
                    if (!updated[idx]) continue
                    val bx = c.toFloat() / brickCols
                    val bx2 = (c + 1f) / brickCols
                    val by = topOffset + r * brickH
                    val by2 = by + brickH
                    if (nx in bx..bx2 && ny in by..by2) {
                        updated[idx] = false
                        velY = -velY
                        hit = true
                    }
                }
            }
            if (hit) bricks = updated
            if (updated.none { it }) { won = true; running = false }

            ballX = nx; ballY = ny
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (won) "¡Ganaste!" else if (lost) "Perdiste" else "Ladrillos", color = colors.content, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Refresh, "Reiniciar", tint = colors.content, modifier = Modifier.size(22.dp).clickable { reset() })
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.stroke, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { if (!running && !lost && !won) running = true },
                        onDrag = { change, amount ->
                            change.consume()
                            paddleX = (paddleX + amount.x / size.width).coerceIn(paddleW / 2, 1f - paddleW / 2)
                        },
                    )
                }
                .padding(2.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                // Bricks
                for (r in 0 until brickRows) for (c in 0 until brickCols) {
                    if (!bricks[r * brickCols + c]) continue
                    val left = c.toFloat() / brickCols * w
                    val top = (topOffset + r * brickH) * h
                    drawRect(
                        colors.content.copy(alpha = 0.85f),
                        Offset(left + 2f, top + 2f),
                        Size(w / brickCols - 4f, brickH * h - 4f),
                    )
                }
                // Paddle
                drawRect(
                    colors.content,
                    Offset((paddleX - paddleW / 2) * w, h - 0.04f * h),
                    Size(paddleW * w, 0.025f * h),
                )
                // Ball
                drawCircle(colors.content, ballR * w, Offset(ballX * w, ballY * h))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (!running && !lost && !won) "Arrastra para mover la barra y empezar." else "Arrastra para mover la barra.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/* ============================ Trivia (editable) ============================ */

@Composable
private fun TriviaGame(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    val colors = LocalEnfoqueColors.current
    val questions = config.trivia
    var editing by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var qDraft by remember { mutableStateOf("") }
    var aDraft by remember { mutableStateOf("") }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (editing) "Editar preguntas" else "Trivia", color = colors.content, modifier = Modifier.weight(1f))
            Text(
                if (editing) "Jugar" else "Editar",
                color = colors.content,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, colors.stroke, RoundedCornerShape(50))
                    .clickable { editing = !editing }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        if (editing) {
            // Add Q/A
            BasicTextField(
                value = qDraft, onValueChange = { qDraft = it },
                textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                cursorBrush = SolidColor(colors.content),
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.stroke, RoundedCornerShape(8.dp)).padding(10.dp),
                decorationBox = { inner -> if (qDraft.isEmpty()) Text("Pregunta", color = colors.secondaryContent); inner() },
            )
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = aDraft, onValueChange = { aDraft = it },
                textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                cursorBrush = SolidColor(colors.content),
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.stroke, RoundedCornerShape(8.dp)).padding(10.dp),
                decorationBox = { inner -> if (aDraft.isEmpty()) Text("Respuesta", color = colors.secondaryContent); inner() },
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.subtleFill)
                    .clickable {
                        if (qDraft.isNotBlank() && aDraft.isNotBlank()) {
                            onChange(config.copy(trivia = config.trivia + TriviaItem(question = qDraft.trim(), answer = aDraft.trim())))
                            qDraft = ""; aDraft = ""
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) { Text("Añadir pregunta", color = colors.content) }

            Spacer(Modifier.height(12.dp))
            questions.forEach { q ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(q.question, color = colors.content, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Outlined.Close, "Eliminar", tint = colors.secondaryContent,
                        modifier = Modifier.size(18.dp).clickable { onChange(config.copy(trivia = config.trivia.filterNot { it.id == q.id })) },
                    )
                }
            }
        } else {
            if (questions.isEmpty()) {
                Text("No hay preguntas todavía. Pulsa \"Editar\" para crear las tuyas.", color = colors.secondaryContent)
            } else {
                val safeIndex = index.coerceIn(0, questions.size - 1)
                val q = questions[safeIndex]
                Box(
                    modifier = Modifier.fillMaxWidth().border(1.dp, colors.stroke, RoundedCornerShape(12.dp)).padding(16.dp),
                ) {
                    Column {
                        Text(q.question, color = colors.content, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        if (revealed) {
                            Text("Respuesta: ${q.answer}", color = colors.content)
                        } else {
                            Text(
                                "Mostrar respuesta",
                                color = colors.secondaryContent,
                                modifier = Modifier.clickable { revealed = true },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${safeIndex + 1}/${questions.size}", color = colors.secondaryContent, modifier = Modifier.weight(1f))
                    Text(
                        "Siguiente",
                        color = colors.content,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.subtleFill)
                            .clickable {
                                revealed = false
                                index = if (safeIndex + 1 >= questions.size) 0 else safeIndex + 1
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
