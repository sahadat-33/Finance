package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun <T> ReorderableColumnList(
    items: List<T>,
    key: (T) -> Any,
    onReorderFinished: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    itemContent: @Composable (item: T, isDragging: Boolean, dragHandleModifier: Modifier) -> Unit
) {
    var localItems by remember(items) { mutableStateOf(items) }
    var draggingItemKey by remember { mutableStateOf<Any?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    // Store item heights and parent Y coordinates
    val itemHeights = remember { mutableStateMapOf<Any, Float>() }

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        localItems.forEachIndexed { index, item ->
            val itemKey = key(item)
            val isDragging = draggingItemKey == itemKey

            val animatedElevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp,
                label = "reorder_elevation"
            )

            val animatedScale by animateFloatAsState(
                targetValue = if (isDragging) 1.03f else 1.0f,
                label = "reorder_scale"
            )

            val dragHandleModifier = Modifier.pointerInput(itemKey, localItems) {
                detectDragGestures(
                    onDragStart = {
                        draggingItemKey = itemKey
                        dragOffsetY = 0f
                    },
                    onDragEnd = {
                        draggingItemKey = null
                        dragOffsetY = 0f
                        onReorderFinished(localItems)
                    },
                    onDragCancel = {
                        draggingItemKey = null
                        dragOffsetY = 0f
                        localItems = items // revert if cancelled
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount.y

                        val currentIndex = localItems.indexOfFirst { key(it) == itemKey }
                        if (currentIndex != -1) {
                            val currentItemHeight = itemHeights[itemKey] ?: 60f
                            val threshold = currentItemHeight * 0.6f

                            if (dragOffsetY > threshold && currentIndex < localItems.size - 1) {
                                // Move down
                                val targetIndex = currentIndex + 1
                                val mutable = localItems.toMutableList()
                                val moved = mutable.removeAt(currentIndex)
                                mutable.add(targetIndex, moved)
                                localItems = mutable
                                dragOffsetY -= currentItemHeight
                            } else if (dragOffsetY < -threshold && currentIndex > 0) {
                                // Move up
                                val targetIndex = currentIndex - 1
                                val mutable = localItems.toMutableList()
                                val moved = mutable.removeAt(currentIndex)
                                mutable.add(targetIndex, moved)
                                localItems = mutable
                                dragOffsetY += currentItemHeight
                            }
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        itemHeights[itemKey] = coordinates.size.height.toFloat()
                    }
                    .zIndex(if (isDragging) 10f else 1f)
                    .offset {
                        if (isDragging) {
                            IntOffset(0, dragOffsetY.roundToInt())
                        } else {
                            IntOffset(0, 0)
                        }
                    }
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        shadowElevation = animatedElevation.toPx()
                    }
            ) {
                itemContent(item, isDragging, dragHandleModifier)
            }
        }
    }
}
