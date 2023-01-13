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
    imageInfoToConsole(filename,bufferedImage)

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