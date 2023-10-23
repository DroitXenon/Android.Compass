package com.example.test_compass

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test_compass.ui.theme.TestcompassTheme
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestcompassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainPage()
                }
            }
        }
    }
}

@Composable
fun MainPage() {
    var currentDegree by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val dataManager = SensorDataManager(context)
        dataManager.init()

        val job = scope.launch {
            dataManager.data
                .receiveAsFlow()
                .onEach { currentDegree = it }
                .collect {}
        }
        onDispose {
            dataManager.cancel()
            job.cancel()
        }
    }

    val circleColor = MaterialTheme.colorScheme.onBackground
    val arrowPosColor = MaterialTheme.colorScheme.tertiary
    val arrowNegColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$currentDegree° ${Cardinal.fromDegree(currentDegree.toInt()).letter}")
        Canvas(modifier = Modifier
            .width(128.dp)
            .height(128.dp)) {
            drawCircle(
                color = circleColor,
                style = Stroke(15f),
                center = Offset(x = size.width / 2, y = size.height / 2),
                radius = size.width
            )

            val arrowPath = Path()
            arrowPath.moveTo(size.width / 2, (size.height * .9).toFloat())
            arrowPath.lineTo(size.width / 2 + 30, size.height / 2)
            arrowPath.lineTo(size.width / 2 - 30, size.height / 2)
            arrowPath.lineTo(size.width / 2, (size.height * .9).toFloat())

            rotate(-currentDegree) {
                Cardinal.values().forEach {
                    rotate(it.degree.toFloat()) {
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                it.letter,
                                size.width / 2,
                                -(size.width / 2 + 40),
                                Paint().apply {
                                    textSize = 70f
                                    color = Color.argb(
                                        circleColor.alpha,
                                        circleColor.red,
                                        circleColor.blue,
                                        circleColor.green
                                    )
                                    textAlign = Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }

                for (i in 0..350 step 10) {
                    rotate(i.toFloat()) {
                        drawLine(
                            color = circleColor,
                            start = Offset(x = size.width / 2, ((size.height / 2) - size.width) + 25),
                            end = Offset(x = size.width / 2, (size.height / 2) - size.width),
                            strokeWidth = 5f,
                        )
                    }
                }

                drawPath(arrowPath, SolidColor(arrowNegColor))
                rotate(180f) {
                    drawPath(arrowPath, SolidColor(arrowPosColor))
                }
            }

            drawLine(
                color = arrowNegColor,
                start = Offset(x = size.width / 2, ((size.height / 2) - size.width) + 30),
                end = Offset(x = size.width / 2, ((size.height / 2) - size.width) - 30),
                strokeWidth = 10f,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestcompassTheme {
        MainPage()
    }
}