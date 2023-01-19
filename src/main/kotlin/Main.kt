import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Position(val type:String, val pair: Pair<Int, Int>? = null)

fun main() {
    println("Input the image filename:")
    val filename = readln()

    val image = File(filename)
    if (!image.exists()) {
        println("The file $filename doesn't exist.")
        return
    }

    val bufferedImage = ImageIO.read(image)
    if (bufferedImage.colorModel.numColorComponents != 3){
        println("The number of image color components isn't 3.")
        return
    }
    if (bufferedImage.colorModel.pixelSize !in arrayOf(24,32)){
        println("The image isn't 24 or 32-bit.")
        return
    }

    println("Input the watermark image filename:")
    val watermarkfilename = readln()

    val watermark = File(watermarkfilename)
    if (!watermark.exists()) {
        println("The file $watermarkfilename doesn't exist.")
        return
    }

    val bufferedWatermarkImage = ImageIO.read(watermark)

    if (bufferedWatermarkImage.height > bufferedImage.height || bufferedWatermarkImage.width > bufferedImage.width){
        println("The watermark's dimensions are larger.")
        return
    }

    val watermarkTransparency = askWatermarkTransparency(bufferedWatermarkImage)

    if (bufferedWatermarkImage.colorModel.numColorComponents != 3){
        println("The number of watermark color components isn't 3.")
        return
    }
    if (bufferedWatermarkImage.colorModel.pixelSize !in arrayOf(24,32)){
        println("The watermark isn't 24 or 32-bit.")
        return
    }

    val transparencyColor = askTransparencyColor(watermarkTransparency,bufferedWatermarkImage)



    println("Input the watermark transparency percentage (Integer 0-100):")
    val w = readln()
    val weight = checkWeight(w)
    if (weight == -1) {
        return
    }
    val maxDiffX = bufferedImage.width - bufferedWatermarkImage.width
    val maxDiffY = bufferedImage.height - bufferedWatermarkImage.height
    val position = getPosition(maxDiffX,maxDiffY)

    println("Input the output image filename (jpg or png extension):")
    val outputName = readln()
    if (outputName.length < 4){
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }
    if (outputName.substring(outputName.length-4) != ".jpg" && outputName.substring(outputName.length-4) != ".png"){
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }

    val output = imageBlend(bufferedImage,bufferedWatermarkImage,weight,watermarkTransparency,transparencyColor,position)


    ImageIO.write(output, outputName.split(".").last(), File(outputName))
    println("The watermarked image $outputName has been created.")
    //imageInfoToConsole(filename,bufferedImage)

}

fun imageInfoToConsole(filename:String, bufferedImage: BufferedImage) {
    println("Image file: $filename")
    println("Width: ${bufferedImage.width}")
    println("Height: ${bufferedImage.height}")
    println("Number of components: ${bufferedImage.colorModel.numComponents}")
    println("Number of color components: ${bufferedImage.colorModel.numColorComponents}")
    println("Bits per pixel: ${bufferedImage.colorModel.pixelSize}")
    println("Transparency: ${getTransparency(bufferedImage.colorModel.transparency)}")
}

fun getTransparency(tr:Int):String{
    return when (tr) {
        1 -> "OPAQUE"
        2 -> "BITMASK"
        3 -> "TRANSLUCENT"
        else -> throw IllegalArgumentException("Invalid transparency")
    }
}

fun isNumber(s: String): Boolean {
    return Regex("^\\d+\$").matches(s)
}

fun checkWeight(w:String):Int {
    return if (isNumber(w)){
        if (w.toInt() in 0..100){
            w.toInt()
        } else {
            println("The transparency percentage is out of range.")
            -1
        }
    } else {
        println("The transparency percentage isn't an integer number.")
        -1
    }
}

fun imageBlend(image:BufferedImage,watermark:BufferedImage, weight:Int,watermarkTransparency:Boolean,transparencyColor:Color?,position: Position):BufferedImage {
    val output = BufferedImage(image.width,image.height,BufferedImage.TYPE_INT_RGB)
    for (x in 0 until image.width){
        for (y in 0 until image.height){
            val a = Color(image.getRGB(x,y)) //image
            val b = if (position.type == "grid"){ // watermark
                Color(watermark.getRGB(x % watermark.width, y % watermark.height), watermarkTransparency)
            } else {
                if (x in (position.pair!!.first .. (position.pair.first + watermark.width)) &&
                    y in (position.pair.second .. (position.pair.second + watermark.height))){
                    Color(watermark.getRGB(x - position.pair.first, y - position.pair.second), watermarkTransparency)
                } else {
                    Color(image.getRGB(x,y))
                }
            }

            val color = if (b.alpha == 0 || (b.red == transparencyColor?.red && b.green == transparencyColor?.green && b.blue == transparencyColor?.blue)) {
                Color(a.red, a.green, a.blue)
            } else {
                Color(
                    (weight * b.red + (100 - weight) * a.red) / 100,
                    (weight * b.green + (100 - weight) * a.green) / 100,
                    (weight * b.blue + (100 - weight) * a.blue) / 100)
            }
            output.setRGB(x,y,color.rgb)
        }
    }
    return output
}

fun askWatermarkTransparency(bufferedWatermarkImage:BufferedImage):Boolean {
    if (getTransparency(bufferedWatermarkImage.colorModel.transparency) == "TRANSLUCENT"){
        println("Do you want to use the watermark's Alpha channel?")
        val input = readln()
        if (input.lowercase() == "yes") {
            return true
        }
    }
    return false
}

fun askTransparencyColor(alpha:Boolean, watermark: BufferedImage): Color?{
    if (alpha) return null
    if (watermark.colorModel.pixelSize == 24) {
        println("Do you want to set a transparency color?")
        val input = readln()
        if (input != "yes") {
            return null
        }
        println("Input a transparency color ([Red] [Green] [Blue]):")
        val colorInput = readln()
        if ("(\\d+) (\\d+) (\\d+)".toRegex().matches(colorInput)) {
            val r = colorInput.split("\\s".toRegex())[0].toInt()
            val g = colorInput.split("\\s".toRegex())[1].toInt()
            val b = colorInput.split("\\s".toRegex())[2].toInt()
            if (r in 0..255 && b in 0..255 && g in 0..255){
                return Color(r, g, b)
            } else {
                throw Exception("The transparency color input is invalid.")
            }
        } else {
            throw Exception("The transparency color input is invalid.")
        }
    }
    return null
}

fun getPosition(maxDiffX:Int, maxDiffY:Int):Position{
    println("Choose the position method (single, grid):")
    return when (readln().lowercase()) {
        "single" -> Position("single", getSinglePosition(maxDiffX, maxDiffY))
        "grid" -> Position("grid")
        else -> throw Exception("The position method input is invalid.")
    }
}

fun getSinglePosition(maxDiffX:Int, maxDiffY:Int):Pair<Int,Int>{
    println("Input the watermark position ([x 0-$maxDiffX] [y 0-$maxDiffY]):")
    val pos = readln()

    return if ("(-?\\d+) (-?\\d+)".toRegex().matches(pos)){
        val x = pos.split("\\s".toRegex())[0].toInt()
        val y = pos.split("\\s".toRegex())[1].toInt()

        if (x in 0..maxDiffX && y in 0..maxDiffY){
            Pair(x,y)
        } else {
            throw Exception("The position input is out of range.")
        }
    } else {
        throw Exception("The position input is invalid.")
    }
}