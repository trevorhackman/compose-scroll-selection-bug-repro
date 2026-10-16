import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Minimal reproduction: BasicTextField(state = TextFieldState(...), scrollState = ScrollState(...))
 * ignores the mouse wheel while the left mouse button is still held down mid-drag-selection - so
 * there's no way to wheel-scroll the view to extend a selection out to text that's currently
 * outside the visible viewport.
 *
 * Steps to reproduce:
 *  1. Press and hold the left mouse button inside the bordered text field below, on some text, and
 *     drag a little to start a selection (don't release the button).
 *  2. Still holding the button down, scroll the mouse wheel - trying to scroll the view so the
 *     selection can be extended to text currently above or below the visible area.
 *
 * Expected: the view scrolls (as it does when the button isn't held - see the "Scroll offset"
 * label), letting the selection be extended to off-screen text while still dragging.
 * Actual: the wheel does nothing at all while the button is held - "Scroll offset" doesn't change
 * no matter how much the wheel is scrolled. Only once the button is released does wheel scrolling
 * work again.
 *
 * Immediately below is a second, comparable scrollable+selectable component - plain [Text] lines in
 * a [Column] wrapped in [SelectionContainer], scrolled via [Modifier.verticalScroll] (the same shape
 * as a read-only log view) - that does NOT have this bug: its wheel keeps scrolling the view even
 * while the button is held down mid-drag-selection. Included so a report can point at a second,
 * working component built on the *same* Compose Foundation selection/scroll primitives, rather than
 * relying on a description of what "should" happen.
 */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "BasicTextField wheel-scroll-during-drag-selection repro") {
        App()
    }
}

@Composable
private fun App() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "For both components below: press and hold the left mouse button on some text and drag a little " +
                "to start a selection - don't release. Still holding the button, scroll the wheel, as if trying " +
                "to reach off-screen text to extend the selection to.",
        )

        BrokenTextField()
        WorkingLogStyleList()
    }
}

/** The broken component - see this file's own top-level KDoc. */
@Composable
private fun BrokenTextField() {
    val lineCount = 200
    val initialText = (1..lineCount).joinToString("\n") { "Line $it - scroll me, then select some of this text, then try scrolling again." }
    val textFieldState = rememberTextFieldState(initialText = initialText)
    val scrollState = rememberScrollState()
    var lastScrollValue by remember { mutableStateOf(0) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect { lastScrollValue = it }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("BROKEN: BasicTextField(state, scrollState) - wheel does nothing while the button is held.", bold = true)
        Text("Scroll offset: $lastScrollValue", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                .padding(8.dp),
        ) {
            BasicTextField(
                state = textFieldState,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                scrollState = scrollState,
            )
        }
    }
}

/**
 * The working comparison - the same shape as a read-only log view: plain [Text] lines in a
 * [SelectionContainer], scrolled via the ordinary [Modifier.verticalScroll] rather than a
 * [BasicTextField]'s own internal scroll handling. Scrolls correctly with the wheel even mid-drag.
 */
@Composable
private fun WorkingLogStyleList() {
    val lineCount = 200
    val scrollState = rememberScrollState()
    var lastScrollValue by remember { mutableStateOf(0) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect { lastScrollValue = it }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("WORKING: SelectionContainer + Column.verticalScroll (like a log view) - wheel keeps working while the button is held.", bold = true)
        Text("Scroll offset: $lastScrollValue", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                .padding(8.dp),
        ) {
            SelectionContainer {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    for (i in 1..lineCount) {
                        Text(
                            "Line $i - scroll me, then select some of this text, then try scrolling again.",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Text(text: String, style: TextStyle = TextStyle(fontSize = 14.sp), bold: Boolean = false, modifier: Modifier = Modifier) {
    androidx.compose.foundation.text.BasicText(
        text,
        style = if (bold) style.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else style,
        modifier = modifier,
    )
}
