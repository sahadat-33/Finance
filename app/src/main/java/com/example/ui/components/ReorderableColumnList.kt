package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()

    // Store item heights
    val itemHeights = remember { mutableStateMapOf<Any, Float>() }
    // Store animated displacement offsets for non-dragging items when swapped
    val itemDisplacements = remember { mutableStateMapOf<Any, Animatable<Float, *>>() }

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        localItems.forEachIndexed { index, item ->
            val itemKey = key(item)
            val isDragging = draggingItemKey == itemKey

            val animatedElevation by animateDpAsState(
                targetValue = if (isDragging) 10.dp else 0.dp,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "reorder_elevation"
            )

            val animatedScale by animateFloatAsState(
                targetValue = if (isDragging) 1.04f else 1.0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
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
                            val threshold = currentItemHeight * 0.5f

                            if (dragOffsetY > threshold && currentIndex < localItems.size - 1) {
                                // Move down
                                val targetIndex = currentIndex + 1
                                val displacedKey = key(localItems[targetIndex])
                                val mutable = localItems.toMutableList()
                                val moved = mutable.removeAt(currentIndex)
                                mutable.add(targetIndex, moved)
                                localItems = mutable
                                dragOffsetY -= currentItemHeight

                                // Animate the displaced item sliding up into its new slot
                                coroutineScope.launch {
                                    val anim = (itemDisplacements[displacedKey] as? Animatable<Float, *>)
                                        ?: Animatable(0f).also { itemDisplacements[displacedKey] = it }
                                    @Suppress("UNCHECKED_CAST")
                                    val floatAnim = anim as Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
                                    floatAnim.snapTo(currentItemHeight)
                                    floatAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            } else if (dragOffsetY < -threshold && currentIndex > 0) {
                                // Move up
                                val targetIndex = currentIndex - 1
                                val displacedKey = key(localItems[targetIndex])
                                val mutable = localItems.toMutableList()
                                val moved = mutable.removeAt(currentIndex)
                                mutable.add(targetIndex, moved)
                                localItems = mutable
                                dragOffsetY += currentItemHeight

                                // Animate the displaced item sliding down into its new slot
                                coroutineScope.launch {
                                    val anim = (itemDisplacements[displacedKey] as? Animatable<Float, *>)
                                        ?: Animatable(0f).also { itemDisplacements[displacedKey] = it }
                                    @Suppress("UNCHECKED_CAST")
                                    val floatAnim = anim as Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
                                    floatAnim.snapTo(-currentItemHeight)
                                    floatAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }

            val nonDragOffset = (itemDisplacements[itemKey]?.value as? Float) ?: 0f

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
                            IntOffset(0, nonDragOffset.roundToInt())
                        }
                    }
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        shadowElevation = animatedElevation.toPx()
                        shape = RoundedCornerShape(12.dp)
                        clip = false
                    }
            ) {
                itemContent(item, isDragging, dragHandleModifier)
            }
        }
    }
}
