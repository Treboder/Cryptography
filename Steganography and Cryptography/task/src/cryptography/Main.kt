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

        // manipulate byte stream
        val data = bos.toByteArray()
        for(i in 0..data.size-1)
            data[i] = data[i] //0.toByte()

        // create inputstream for output image
        val bis = ByteArrayInputStream(data)
        val bufferedOutputImage = ImageIO.read(bis)

        // perform pixel operations
        for(x in 0..bufferedOutputImage.width-1)
            for(y in 0..bufferedOutputImage.height-1) {
                val color = Color(bufferedInputImage.getRGB(x, y))
                val rgb = Color(
                    modifyBit(color.red),
                    modifyBit(color.green),
                    modifyBit(color.blue)
                ).rgb
                bufferedOutputImage.setRGB(x,y, rgb)
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

fun modifyBit(pixel:Int):Int {
    return if (pixel % 2 == 0) pixel + 1 else pixel
}

fun show() {
    println("Obtaining message from image.")
}