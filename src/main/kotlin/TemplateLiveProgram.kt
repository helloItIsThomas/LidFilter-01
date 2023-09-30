import GLOBAL.cellHeight
import GLOBAL.cellWidth
import GLOBAL.sceneInterval
import GLOBAL.shapeScaler
import classes.Cell
import classes.Flag
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.presets.LIGHT_GREEN
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.math.mix
import org.openrndr.shape.Circle
import java.io.File

object GLOBAL {
    lateinit var cellArray: Array<Array<Cell>>
    var width: Int = 0
    var height: Int = 0
    var shapeScaler: Double = 0.0
    var sceneInterval: Int = 0
    var brightnessThreshold: Double = 0.0
    var cellWidth: Double = 0.0
    var cellHeight: Double = 0.0
}

fun main() = application {
    configure {
        width = 426
        height = 240
        hideWindowDecorations = true
        windowAlwaysOnTop = true
        windowTransparent = true
        position = IntVector2(100,100)
    }

    oliveProgram {
        GLOBAL.width = width
        GLOBAL.height = height
        val imgCount = 467
        GLOBAL.brightnessThreshold = 0.0
        GLOBAL.shapeScaler = 0.1

        var thisClock: Double
        // I think adjusting clockDiv adjusts the framerate
//        val clockDiv = 0.25  // clockDiv of 0.05 means 20 frames between scenes I think
        val clockDiv = 0.025  // clockDiv of 0.05 means 20 frames between scenes I think
        val framesBtwnScenes = ((1.0 / clockDiv)) // this should mean how many frames between new scene
        // I think adjusting sceneInterval adjusts the sample rate
        GLOBAL.sceneInterval = 4  // this should mean how many intervals should pass between drawing a scene
        val framesBetweenSceneIntervals = framesBtwnScenes * sceneInterval

        // "fire scene interval" when newSceneCounter >= sceneInterval
        val ratio = width.toDouble() / height.toDouble()
        val imgs = mutableListOf<ColorBuffer>()
        for(i in 1 until imgCount) {
            imgs.add(loadImage("data/images/frames4/$i.png"))
        }

//        val imageFiles = File("data/images/lidFrames").listFiles { _, name -> name.endsWith(".png") }?.sorted()
        val imageFiles = File("data/images/frames7").listFiles { _, name -> name.endsWith(".png") }?.sorted()
        val lidImgs: List<ColorBuffer> = imageFiles!!.map { loadImage(it) }

        val gridWidth = 20.0      // Number of grid units in width
        val gridHeight = (gridWidth / ratio)      // Number of grid units in height

        GLOBAL.cellWidth = width / gridWidth
        GLOBAL.cellHeight = height / gridHeight

        GLOBAL.cellArray = Array(gridWidth.toInt()) { Array(gridHeight.toInt()) { Cell() } }

        var testFlag = Flag()
        var currentImg: ColorBuffer
        var currentColor: ColorRGBa
        imgs.forEach{it.shadow.download()}

        val indices = (0 until gridWidth.toInt()).flatMap { i ->
            (0 until gridHeight.toInt()).map { j -> Pair(i, j) }
        }

        extend {
            thisClock = frameCount * clockDiv
            currentImg = imgs[(thisClock).toInt() % imgs.size]
            drawer.clear(ColorRGBa.BLACK)
//            drawer.image(currentImg, 0.0, 0.0)

            var idIncrementor = 0
            indices.forEach { (i, j) ->
                val cell = GLOBAL.cellArray[i][j]
                var prevBrightness = cell.brightness // Store previous brightness

                currentColor = currentImg.shadow.read(
                    i * cellWidth.toInt(), j * cellHeight.toInt()
                )
                val brightness =
                    0.2126 * currentColor.r
                + 0.7152 * currentColor.g
                + 0.0722 * currentColor.b

                var tValue = mix(prevBrightness, brightness, (thisClock) % 1.0)

                cell.id = idIncrementor++.toString()
                prevBrightness = prevBrightness // Set previous brightness
                // cell.brightness = // brightness
                cell.brightness = tValue
                cell.color = currentColor.toSRGB()
                cell.position = Vector2((i * cellWidth), (j * cellHeight))
            }
            testFlag.check(thisClock, sceneInterval)


            var circleList: MutableList<Circle> = mutableListOf()
            var circleListDark: MutableList<Circle> = mutableListOf()
            var circleListLight: MutableList<Circle> = mutableListOf()
            var imgList: MutableList<ColorBuffer> = mutableListOf()

            indices.forEach { (i, j) ->
//                circleList.add(
//                    Circle(
//                        GLOBAL.cellArray[i][j].position.x,
//                        GLOBAL.cellArray[i][j].position.y,
//                        GLOBAL.cellArray[i][j].renderW * shapeScaler
//                    )
//                )


                // I need localImgNum to interpolate between this brightness and the previous brightness.

                var localImgNum = GLOBAL.cellArray[i][j].brightness.map(
//                var localImgNum = GLOBAL.cellArray.flatten()[i*j].brightness.map(
                    0.0,
                    0.3,
                    0.0,
                    lidImgs.size.toDouble()
                ).toInt()

                // This seems to be the list that is being
                // updated
                // and
                // drawn
                // every frame.
                imgList.add(
                    lidImgs[localImgNum.toInt()]
                )
//                if(GLOBAL.cellArray[i][j].brightness > 0.185){
//                    circleListLight.add(
//                        Circle(
//                            GLOBAL.cellArray[i][j].position.x,
//                            GLOBAL.cellArray[i][j].position.y,
//                            GLOBAL.cellArray[i][j].renderW * shapeScaler
//                        )
//                    )
//                } else {
//                    circleListDark.add(
//                        Circle(
//                            GLOBAL.cellArray[i][j].position.x,
//                            GLOBAL.cellArray[i][j].position.y,
//                            GLOBAL.cellArray[i][j].renderW * shapeScaler
//                        )
//                    )
//                }
            }

            drawer.stroke = null
            drawer.pushTransforms()
            drawer.fill = ColorRGBa.LIGHT_GREEN
//            drawer.circles( circleListLight )
            drawer.circles( circleListDark )
            drawer.popTransforms()
//            println(GLOBAL.cellArray.flatten()[i].renderW)


            imgList.forEachIndexed{ i, n ->
                drawer.image(
                    n,
                    GLOBAL.cellArray.flatten()[i].position.x,
                    GLOBAL.cellArray.flatten()[i].position.y,
                    GLOBAL.cellWidth,
                    GLOBAL.cellHeight,
                )
            }
        }
    }
}

