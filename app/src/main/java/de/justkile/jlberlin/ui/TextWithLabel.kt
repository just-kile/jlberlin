package de.justkile.jlberlin.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TextWithLabel(label: String, value: String) {
    Text(buildAnnotatedString {
        append(label)
        append(": ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(value)
        }
    })
}

@Preview(showBackground = true)
@Composable
fun PreviewTextWithLabel() {
    TextWithLabel(label = "Label", value = "Value")
}