import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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
    if (bufferedWatermarkImage.colorModel.numColorComponents != 3){
        println("The number of watermark color components isn't 3.")
        return
    }
    if (bufferedWatermarkImage.colorModel.pixelSize !in arrayOf(24,32)){
        println("The watermark isn't 24 or 32-bit.")
        return
    }
    if (bufferedWatermarkImage.height != bufferedImage.height || bufferedWatermarkImage.width != bufferedImage.width){
        println("The image and watermark dimensions are different.")
        return
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val w = readln()
    val weight = checkWeight(w)
    if (weight == -1) {
        return
    }

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

    val output = imageBlend(bufferedImage,bufferedWatermarkImage,weight)


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
    return Regex("\\d+").matches(s)
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

fun imageBlend(image:BufferedImage,watermark:BufferedImage, weight:Int):BufferedImage {
    val output = BufferedImage(image.width,image.height,BufferedImage.TYPE_INT_RGB)
    for (x in 0 until image.width){
        for (y in 0 until image.height){
            val a = Color(image.getRGB(x,y))
            val b = Color(watermark.getRGB(x,y))

            val color = Color(
                (weight * b.red + (100 - weight) * a.red) / 100,
                (weight * b.green + (100 - weight) * a.green) / 100,
                (weight * b.blue + (100 - weight) * a.blue) / 100)
            output.setRGB(x,y,color.rgb)
        }
    }
    return output
}