package com.example.shoppingcenternavigator

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmoothLineGraph(navController: NavController) {
    Box(
        modifier = Modifier
            .background(colorResource(id = R.color.isabelline))
            .fillMaxSize()
    ) {
        Image(painter = painterResource(id = R.drawable.carousel_0),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize())
        // animation starting time
        val animationProgress = remember {
            Animatable(0f)
        }
        // animation speed
        LaunchedEffect(key1 = coordinateSystem, block = {
            animationProgress.animateTo(1f, tween(3000))
        })
        val coroutineScope = rememberCoroutineScope()
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .clickable {
                    coroutineScope.launch {
                        animationProgress.snapTo(0f)
                        animationProgress.animateTo(1f, tween(3000))
                    }
                }
                .drawWithCache {
                    val path = generatePath(carouselShops, carouselPoints, coordinateSystem, size)

                    onDrawBehind {
                        // drawing the line
                        drawPath(path, Color.Black, style = Stroke(3.dp.toPx()))

                        /*
                        clipRect(right = size.width * animationProgress.value) {

                        }

                         */

                    }
                })
    }
}

fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
    return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
}

fun calculateDistance(point1: Point, point2: Point): Float {
    return distance(point1.x, point2.x, point1.y, point2.y)
}
fun closestPoints(x: Float, y: Float, list:MutableList<Coordinate>): MutableList<Float> {
    var closest1 = 1414.2136f
    var closestPoint1 = Coordinate(0f,0f)
    var closest2 = 1414.2136f
    var closestPoint2 = Coordinate(0f,0f)


    list.forEachIndexed{ i, points ->
        if ((distance(x, points.x, y, points.y) < closest1) and (distance(x, points.x, y, points.y) < closest2)) {
            closest2 = closest1
            closestPoint2 = closestPoint1
            closest1 = distance(x, points.x, y, points.y)
            closestPoint1 = Coordinate(points.x, points.y)

        }
        else if ((distance(x, points.x, y, points.y) >= closest1) and (distance(x, points.x, y, points.y) < closest2)){
            closest2 = (distance(x, points.x, y, points.y))
            closestPoint2 = Coordinate(points.x, points.y)
        }
    }
    val returnList = mutableListOf<Float>()
    returnList.add(closestPoint1.x)
    returnList.add(closestPoint1.y)
    returnList.add(closestPoint2.x)
    returnList.add(closestPoint2.y)
    returnList.add(closest1)
    returnList.add(closest2)

    return returnList
}


fun generatePath(shop: List<Shops>, point: List<Point>, data: List<Coordinate>, size: Size): Path {
    val path = Path()
    val xMax = data.maxBy { it.x }
    val xMin = data.minBy { it.x }
    val yMax = data.maxBy { it.y }
    val yMin = data.minBy { it.y }
    val rangeX = xMax.x - xMin.x
    val rangeY = yMax.y - yMin.y
    val widthPerCoordinate = size.width / rangeX
    val heightPerCoordinate = size.height / rangeY


    var distance = 1414.2136f
    var nextPoint = Coordinate(0f,0f)
    var lastPoint = Coordinate(0f,0f)
    var index = 0
    val fromIndex = SelectedShops.selectedOptionFromIndex
    val toIndex = SelectedShops.selectedOptionToIndex

    Log.d("fromIndex", "$fromIndex")
    Log.d("toIndex", "$toIndex")

    var pointList: MutableList<Coordinate> = mutableListOf<Coordinate>()
    for (point in carouselPoints) {
        pointList.add(Coordinate(point.x, point.y))
    }

    path.moveTo(
        carouselShops[fromIndex].x * widthPerCoordinate,
        carouselShops[fromIndex].y * heightPerCoordinate)
    path.lineTo(
        carouselPrime[fromIndex].x * widthPerCoordinate,
        carouselPrime[fromIndex].y * heightPerCoordinate)
    index = pointList.indexOf(Coordinate(
        carouselPrime[fromIndex].x,
        carouselPrime[fromIndex].y))
    pointList.removeAt(index)

    distance = distance(
        carouselShops[toIndex].x,
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[0],
        carouselShops[toIndex].y,
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[1])
    nextPoint = Coordinate(
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[0],
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[1])

    Log.d("bulduğu nokta 1","${nextPoint}]")
    Log.d("bulduğu nokta 2","${Coordinate(
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[2],
        closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[3])}]")

    if (distance > (distance(
            carouselShops[toIndex].x,
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[2] ,
            carouselShops[toIndex].y,
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[3]))){
        distance = distance(
            carouselShops[toIndex].x,
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[2] ,
            carouselShops[toIndex].y,
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[3])
        nextPoint = Coordinate(
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[2],
            closestPoints(carouselPrime[fromIndex].x,carouselPrime[fromIndex].y,pointList)[3])
    }

    path.lineTo(nextPoint.x * widthPerCoordinate,
        nextPoint.y * heightPerCoordinate)
    index = pointList.indexOf(Coordinate(
        nextPoint.x,
        nextPoint.y))
    Log.d("gittiği nokta","${pointList[index]}]")
    pointList.removeAt(index)

    while (!((nextPoint.x == carouselPrime[toIndex].x) and (nextPoint.y == carouselPrime[toIndex].y))){
        lastPoint = nextPoint

        distance = distance(
            carouselShops[toIndex].x,
            closestPoints(nextPoint.x,nextPoint.y,pointList)[0] ,
            carouselShops[toIndex].y,
            closestPoints(nextPoint.x,nextPoint.y,pointList)[1])
        nextPoint = Coordinate(
            closestPoints(nextPoint.x,nextPoint.y,pointList)[0],
            closestPoints(nextPoint.x,nextPoint.y,pointList)[1])

        Log.d("bulduğu nokta 1","${nextPoint}]")
        Log.d("bulduğu nokta 2","${Coordinate(
            closestPoints(lastPoint.x,lastPoint.y,pointList)[2],
            closestPoints(lastPoint.x,lastPoint.y,pointList)[3])}]")

        if (distance > (distance(
                carouselShops[toIndex].x,
                closestPoints(lastPoint.x,lastPoint.y,pointList)[2] ,
                carouselShops[toIndex].y,
                closestPoints(lastPoint.x,lastPoint.y,pointList)[3]))){
            distance = distance(
                carouselShops[toIndex].x,
                closestPoints(lastPoint.x,lastPoint.y,pointList)[2] ,
                carouselShops[toIndex].y,
                closestPoints(lastPoint.x,lastPoint.y,pointList)[3])
            nextPoint = Coordinate(
                closestPoints(lastPoint.x,lastPoint.y,pointList)[2],
                closestPoints(lastPoint.x,lastPoint.y,pointList)[3])
        }


        path.lineTo(nextPoint.x * widthPerCoordinate,
            nextPoint.y * heightPerCoordinate)
        index = pointList.indexOf(Coordinate(
            nextPoint.x,
            nextPoint.y))
        Log.d("gittiği nokta","${pointList[index]}]")
        pointList.removeAt(index)
    }
    path.lineTo(carouselShops[toIndex].x * widthPerCoordinate, carouselShops[toIndex].y * heightPerCoordinate)

    return path
}
// val PurpleBackgroundColor = Color(0xff322049)
val BarColor = Color.White.copy(alpha = 0.3f)

@Preview
@Composable
fun CoordinateSystem(){
    Box(modifier = Modifier
        .fillMaxSize()
        .drawBehind {
            val barWidthPx = 1.dp.toPx()

            drawRect(Color.White)
            val verticalLines = size.width / 80.dp.toPx()
            val verticalSize = size.width / (verticalLines + 1)
            repeat(verticalLines.roundToInt()) { i ->
                val startX = verticalSize * (i + 1)
                drawLine(
                    Color.Gray,
                    start = Offset(startX, 0f),
                    end = Offset(startX, size.height),
                    strokeWidth = barWidthPx
                )
            }

            val horizontalLines = size.height / 80.dp.toPx()
            val sectionSize = size.height / (horizontalLines + 1)
            repeat(horizontalLines.roundToInt()) { i ->
                val startY = sectionSize * (i + 1)
                drawLine(
                    BarColor,
                    start = Offset(0f, startY),
                    end = Offset(size.width, startY),
                    strokeWidth = barWidthPx
                )
            }
        })
}
/*
fun generateSmoothPath(data: List<Balance>, size: Size): Path {
    val path = Path()
    val numberEntries = data.size - 1
    val weekWidth = size.width / numberEntries

    val max = data.maxBy { it.y }
    val min = data.minBy { it.y } // will map to x= 0, y = height
    val range = max.y - min.y
    val heightPxPerAmount = size.height / range

    var previousBalanceX = 0f
    var previousBalanceY = size.height
    data.forEachIndexed { i, balance ->
        if (i == 0) {
            path.moveTo(
                0f,
                size.height - (balance.y - min.y) *
                        heightPxPerAmount
            )
        }

        val balanceX = i * weekWidth
        val balanceY = size.height - (balance.y - min.y) *
                heightPxPerAmount


        // smoothing the curve
        val controlPoint1 = PointF((balanceX + previousBalanceX) / 2f, previousBalanceY)
        val controlPoint2 = PointF((balanceX + previousBalanceX) / 2f, balanceY)
        path.cubicTo(
            controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y,
            balanceX, balanceY
        )
        previousBalanceX = balanceX
        previousBalanceY = balanceY
    }
    return path
}
*/
