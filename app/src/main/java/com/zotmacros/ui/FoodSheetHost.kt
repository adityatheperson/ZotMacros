package com.zotmacros.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import com.zotmacros.domain.Meal
import kotlinx.coroutines.flow.first

/** Retain content during the native exit; cancellation lets a new selection interrupt safely. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FoodSheetHost(selection: FoodSelection?, saving: Boolean, onDismiss: () -> Unit,
    onSave: (Double, Meal) -> Unit, onDelete: (String) -> Unit) {
    var displayed by remember { mutableStateOf(selection) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(selection?.requestId) {
        if (selection != null) {
            displayed = selection
            snapshotFlow { sheetState.hasExpandedState }.first { it }
            sheetState.show()
        } else if (displayed != null) {
            sheetState.hide()
            displayed = null
        }
    }
    displayed?.let {
        FoodSheet(it, saving, onDismiss, onSave, onDelete, sheetState, closing = selection == null)
    }
}
