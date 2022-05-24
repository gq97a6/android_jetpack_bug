package com.alteratom.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var angleA by remember { mutableStateOf(180.0) }
            var angleB by remember { mutableStateOf(0.0) }
            var fill by remember { mutableStateOf(1.0) }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ArcSlider(
                    modifier = Modifier
                        .fillMaxWidth(.6f)
                        .aspectRatio(1f),
                    name ="A",
                    angle = angleA,
                    startAngle = 100.0,
                    sweepAngle = 160.0,
                    strokeWidth = 15.dp.toPx(),
                    pointerRadius = 15.dp.toPx(),
                    pointerStyle = Stroke(width = 1.dp.toPx()),
                    pointerColor = Color.Gray,
                    colorList = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    ),
                    onChange = { a, v ->
                        angleA = a
                        fill = v
                    }
                )

                ArcSlider(
                    modifier = Modifier
                        .fillMaxSize(.6f)
                        .aspectRatio(1f),
                    name ="B",
                    angle = angleB,
                    startAngle = 280.0,
                    sweepAngle = (160.0 * fill),
                    strokeWidth = 15.dp.toPx(),
                    pointerRadius = 15.dp.toPx(),
                    pointerStyle = Stroke(width = 1.dp.toPx()),
                    pointerColor = Color.Gray,
                    colorList = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    ),
                    onChange = { a, _ ->
                        angleB = a
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize(.4f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }
    }
}

@Composable
fun Dp.toPx() = LocalDensity.current.run { this@toPx.toPx() }

@Composable
fun Float.toDp() = LocalDensity.current.run { this@toDp.toDp() }

@Composable
fun ArcSlider(
    modifier: Modifier = Modifier,
    name: String = "",
    @FloatRange(from = 0.0, to = 360.0)
    angle: Double,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Double = 0.0,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Double = 180.0,
    onChange: (Double, Double) -> Unit = { _, _ -> },
    strokeCap: StrokeCap = StrokeCap.Round,
    strokeWidth: Float = 10.dp.toPx(),
    pointerDraw: ((Double) -> Unit)? = null,
    pointerStyle: DrawStyle = Fill,
    pointerColor: Color = Color.Black,
    pointerRadius: Float = 15.dp.toPx(),
    colorList: List<Color>
) {
    val brush by remember {
        mutableStateOf(
            if (startAngle > (startAngle + sweepAngle) % 360) {
                var start = startAngle / 360
                val range = sweepAngle / 360
                val step = range / (colorList.size - 1)

                val colorList = colorList.toMutableList()
                val colors: MutableList<Pair<Float, Color>> = mutableListOf()

                while (start < 1 && colorList.isNotEmpty()) {
                    colors.add(Pair(start.toFloat(), colorList[0]))
                    colorList.removeAt(0)
                    start += step
                }

                if (colorList.isNotEmpty()) {
                    colors.add(Pair(1f, colorList[0]))

                    start -= 1

                    for (i in colorList.size - 1 downTo 0) {
                        colors.add(0, Pair((start + step * i).toFloat(), colorList.last()))
                        colorList.removeLast()
                    }
                }

                Brush.sweepGradient(*colors.toTypedArray())
            } else {
                val start = startAngle / 360
                val range = sweepAngle / 360
                val step = range / (colorList.size - 1)

                val colors = Array(colorList.size) {
                    Pair((start + step * it).toFloat(), colorList[it])
                }

                Brush.sweepGradient(*colors)
            }
        )
    }

    ArcSlider(
        name,
        modifier,
        angle,
        startAngle,
        sweepAngle,
        onChange,
        strokeCap,
        strokeWidth,
        pointerDraw,
        pointerStyle,
        pointerColor,
        pointerRadius,
        brush
    )
}

@Composable
fun ArcSlider(
    name: String,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 360.0)
    angle: Double,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Double = 0.0,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Double = 360.0,
    onChange: (Double, Double) -> Unit = { _, _ -> },
    strokeCap: StrokeCap = StrokeCap.Round,
    strokeWidth: Float = 10.dp.toPx(),
    pointerDraw: ((Double) -> Unit)? = null,
    pointerStyle: DrawStyle = Fill,
    pointerColor: Color = Color.Black,
    pointerRadius: Float = 15.dp.toPx(),
    brush: Brush
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerOffset by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }

    //val endAngle by remember(startAngle, sweepAngle) { mutableStateOf((startAngle + sweepAngle) % 360) }
    //val midAngle by remember { mutableStateOf(endAngle + (360.0 - sweepAngle) / 2.0) }

    val endAngle = (startAngle + sweepAngle) % 360
    val midAngle = endAngle + (360.0 - sweepAngle) / 2.0

    var isSliding by remember { mutableStateOf(false) }

    fun calculateAngle(d: Offset) {
        if (name == "B") Log.i("OUY", "0. $sweepAngle")
        val x = d.x - radius
        val y = d.y - radius
        val c = sqrt((x * x + y * y).toDouble())

        var angle = Math.toDegrees(acos(x / c))
        if (y < 0) angle = 360 - angle

        //Keep in range
        if (sweepAngle != 360.0) {
            if (name == "B") Log.i("OUY", "1. $sweepAngle")

            if (endAngle < startAngle) {
                when (angle) {
                    in endAngle..midAngle -> angle = endAngle
                    in midAngle..startAngle -> angle = startAngle
                }
            } else if (endAngle > startAngle) {
                if (midAngle > 360) {
                    val correctedMiddle = midAngle - 360
                    when (angle) {
                        in endAngle..360.0, in 0.0..correctedMiddle -> angle = endAngle
                        in correctedMiddle..startAngle -> angle = startAngle
                    }
                } else {
                    when (angle) {
                        in endAngle..midAngle -> angle = endAngle
                        in midAngle..360.0, in 0.0..startAngle -> angle = startAngle
                    }
                }
            }
        }

        onChange(angle, ((angle + 360 - startAngle) % 360) / sweepAngle)
    }

    Box(
        modifier = modifier
    ) {
        //Draw path
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    radius = (it.size.width / 2).toFloat()
                }
        ) {
            if (name == "B") Log.i("OUY", "2. $sweepAngle")
            drawArc(
                brush = brush,
                startAngle = startAngle.toFloat(),
                sweepAngle = sweepAngle.toFloat(),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = strokeCap)
            )
        }

        //Draw pointer hitbox
        Box(
            modifier = Modifier
                .absoluteOffset(-pointerRadius.toDp(), -pointerRadius.toDp())
                .absoluteOffset(dragOffset.x.toDp(), dragOffset.y.toDp())
                .size(pointerRadius.toDp() * 2)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isSliding = true
                        },
                        onDrag = { change, _ ->
                            Log.i("OUY", "3. $sweepAngle")
                            calculateAngle(dragOffset + change.position)
                            change.consumeAllChanges()
                        },
                        onDragEnd = {
                            isSliding = false
                            dragOffset = pointerOffset
                        }
                    )
                }
        )

        //Draw pointer
        Canvas(
            modifier = Modifier
                .absoluteOffset(-pointerRadius.toDp(), -pointerRadius.toDp())
                .absoluteOffset(pointerOffset.x.toDp(), pointerOffset.y.toDp())
                .size(pointerRadius.toDp() * 2)
        ) {
            if (pointerDraw != null) pointerDraw(angle)
            else drawCircle(
                color = pointerColor,
                radius = pointerRadius,
                center = center,
                style = pointerStyle
            )
        }
    }

    val x = (radius + radius * cos(Math.toRadians(angle))).toFloat()
    val y = (radius + radius * sin(Math.toRadians(angle))).toFloat()
    pointerOffset = Offset(x, y)
    if (!isSliding) dragOffset = Offset(x, y)
}
