# BasicTextField: mouse wheel does nothing while dragging a selection

## Summary

`BasicTextField(state = TextFieldState, scrollState = ScrollState, ...)` ignores the mouse wheel
entirely while the left mouse button is held down mid-drag-selection. There's no way to scroll the
view with the wheel to extend a selection out to text that's currently outside the field's visible
viewport while still holding the button - the wheel only works again once the button is released.

## Steps to reproduce

Run this project (`./gradlew run`) and, in the window that opens, in the **red-bordered** box
(labeled "BROKEN"):

1. Press and hold the left mouse button inside the bordered text field, on some text, and drag a
   little to start a selection - don't release the button.
2. Still holding the button down, scroll the mouse wheel, as if trying to scroll the view so the
   selection could be extended to text currently above or below the visible area.

## Expected behavior

The view scrolls with the wheel while the button is held, the same way it does when the button
isn't held (try step 2 again after releasing the button - the "Scroll offset" label updates
normally). This would let a selection be extended to off-screen text by scrolling manually, as an
alternative or supplement to edge-triggered auto-scroll.

## Actual behavior

The wheel does nothing at all while the button is down. "Scroll offset" doesn't change no matter
how much the wheel is scrolled during the drag. Releasing the button restores normal wheel
scrolling immediately.

## Working comparison

The **green-bordered** box below it (labeled "WORKING") is a second, comparable scrollable and
selectable component built from the same Compose Foundation primitives - plain `Text` lines in a
`Column`, wrapped in `SelectionContainer`, scrolled via the ordinary `Modifier.verticalScroll`
(the same shape a read-only log/output view would use) instead of a `BasicTextField`'s own internal
scroll handling. Running through the exact same repro steps against it, the wheel keeps scrolling
the view the whole time the button is held down. It's included so the report can point at a second,
working component rather than just a description of what "should" happen.

## Secondary observation: inconsistent auto-scroll speed/control while dragging past the edge

Separate from the wheel issue above: drag a selection past the *edge* of each box (rather than
scrolling the wheel) to trigger each component's own auto-scroll. Both components do auto-scroll
this way, but noticeably differently - the red "BROKEN" `BasicTextField` auto-scrolls fast and feels
hard to control precisely, while the green "WORKING" `SelectionContainer`/`verticalScroll` one
auto-scrolls slower and more smoothly, easier to stop exactly where intended.

This is a different mechanism from the wheel-ignored bug above (both of these *do* scroll; only the
speed/gain curve differs), and arguably a separate report: the two are independent auto-scroll
implementations inside Compose Foundation for what's conceptually the same gesture - drag a selection
past the visible bounds, keep extending it - so there's no obvious reason they should feel different.
If anything, the editable field overshooting a selection is more costly than a read-only log view
doing so, so it's not simply "editable fields get to be faster" by design either. Worth reporting as
"these two should share one auto-scroll speed/gain curve" rather than "X is slow" or "Y is fast".

## Environment

- Kotlin: 2.4.10
- Compose Multiplatform: 1.12.1 (`compose.foundation`, `compose.ui`, `compose.desktop`)
- OS: Windows 11 Pro (10.0.26100)
- JDK: JetBrains Runtime 21.0.11

## Notes

Possibly related to [#2689](https://github.com/JetBrains/compose-multiplatform/issues/2689)
(dragging a selection past the field's edge didn't auto-scroll) - that issue is closed as fixed, and
this field does already auto-scroll at the edge while dragging, which is presumably that fix. This
report is about a *different* input during that same drag: the mouse wheel itself isn't read at all
while the button is held, whether or not it shares #2689's underlying cause.

## Project layout

Single-file minimal repro - `src/main/kotlin/Main.kt` is the entire reproduction, no other
dependencies beyond `compose.foundation`/`compose.ui`/`compose.desktop.currentOs`.
