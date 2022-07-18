package cryptography

import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

fun main() {

    do {
        print("Task (hide, show, exit): \n> ")
        val command = readln()
        when(command) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> println("Bye!")
            else -> println("Wrong task: $command")
        }
    } while(command != "exit")

}

fun hide() {

    print("Input image file: \n> ")
    var inputFile = readLine()!!

    print("Output image file: \n> ")
    var outputFile = readLine()!!

    try {
        // create outputstream from input image (read image)
        val bufferedInputImage = ImageIO.read(File(inputFile))
        val bos = ByteArrayOutputStream()
        ImageIO.write(bufferedInputImage, "png", bos)
        println("Input Image: $inputFile")

        // create inputstream for output image
        val data = bos.toByteArray()
        val bis = ByteArrayInputStream(data)
        val bufferedOutputImage = ImageIO.read(bis)

        // perform pixel operations
        for(x in 0..bufferedOutputImage.width-1)
            for(y in 0..bufferedOutputImage.height-1) {
                val modifyBit = {pixel:Int -> if (pixel % 2 == 0) pixel + 1 else pixel}
                val originalColor = Color(bufferedInputImage.getRGB(x, y))
                val modifiedColor = Color(modifyBit(originalColor.red), modifyBit(originalColor.green), modifyBit(originalColor.blue)).rgb
                bufferedOutputImage.setRGB(x, y, modifiedColor)
            }

        // write image from output image
        ImageIO.write(bufferedOutputImage, "png", File(outputFile))
        println("Output Image: $outputFile")
        println("Image $outputFile is saved.")
    }
    catch (e:IOException)  {
        println("Can't read input file!")
    }

}

fun show() {
    println("Obtaining message from image.")
}