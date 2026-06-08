package com.mauricior8.enfoque.ui.oasis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.ui.components.tappable
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/** Full-screen editor for a single note page. */
@Composable
fun NoteFullScreen(
    text: String,
    onTextChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Spacer(Modifier.size(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.ArrowBack, "Volver", tint = colors.content,
                modifier = Modifier.size(26.dp).tappable(onBack),
            )
            Spacer(Modifier.width(16.dp))
            Text("Nota", color = colors.content, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.size(20.dp))
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = TextStyle(color = colors.content, fontSize = 18.sp),
            cursorBrush = SolidColor(colors.content),
            modifier = Modifier.fillMaxWidth().fillMaxSize(),
            decorationBox = { inner ->
                if (text.isEmpty()) Text("Escribe aquí...", color = colors.secondaryContent, fontSize = 18.sp)
                inner()
            },
        )
    }
}
