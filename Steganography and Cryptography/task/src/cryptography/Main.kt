package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
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

fun test() {
    var messageString = "qwfwqfq"
    var messageBytes = messageString.toByteArray(Charsets.UTF_8)
    var messageBits = ""

    println("11001".toInt(2))
    println("11001".toLong(2))
    println("110010000".toInt(2).toChar())
    println("110010000".toLong(2).toChar())
    println("11111".encodeToByteArray().joinToString { " " })
    println("11111".toByteArray().joinToString { " " })


    println("a".toInt(2))
}

fun convertBinaryToDecimal(num: Long): Int {
    var num = num
    var decimalNumber = 0
    var i = 0
    var remainder: Long

    while (num.toInt() != 0) {
        remainder = num % 10
        num /= 10
        decimalNumber += (remainder * Math.pow(2.0, i.toDouble())).toInt()
        ++i
    }
    return decimalNumber
}

fun hide() {

    print("Input image file: \n> ")
    var inputFile = readLine()!!

    print("Output image file: \n> ")
    var outputFile = readLine()!!

    print("Message to hide: \n> ")
    var message = readLine()!!

    try {
        // create outputstream from input image (read image)
        val bufferedInputImage = ImageIO.read(File(inputFile))
        val bos = ByteArrayOutputStream()
        ImageIO.write(bufferedInputImage, "png", bos)

        // create inputstream for output image
        val data = bos.toByteArray()
        val bis = ByteArrayInputStream(data)
        val bufferedOutputImage = ImageIO.read(bis)

        // perform pixel operations in order to hide the message
        //performPixelOperations(bufferedInputImage, bufferedOutputImage)
        setMessage(bufferedOutputImage, message)

        // write image from output image
        ImageIO.write(bufferedOutputImage, "png", File(outputFile))
        println("Message saved in $outputFile image.")
    }
    catch (e:IOException)  {
        println("Can't read input file!")
    }

}

fun setMessage(bufferedImage: BufferedImage, message: String) {

    var messageByteArray = message.encodeToByteArray() + byteArrayOf(0, 0, 3)

    var messageBitSequence = ""
    for (byte in messageByteArray)
    {
        var bits = byte.toUInt().toString(radix = 2)
        bits = bits.padStart(8,'0')
        messageBitSequence += bits
    }

    if((bufferedImage.width * bufferedImage.height) < messageBitSequence.length) {
        println("The input image is not large enough to hold this message.")
        return
    }

    var index = 0
    for(y in 0..bufferedImage.height-1)
        for(x in 0..bufferedImage.width-1) {
            var sourcePixel = Color(bufferedImage.getRGB(x,y))
            var bitToHide = messageBitSequence[index].digitToInt()

            var modifiedBlueChannel = sourcePixel.blue

            var blueBitsBefore = modifiedBlueChannel.toUInt().toString(radix = 2)
            var blueBitsBefore2 = Integer.toBinaryString(modifiedBlueChannel)
            var blueBitsBefore3 = modifiedBlueChannel.toDouble().toBits()

            val lastBitBefore = sourcePixel.blue % 2
            if(lastBitBefore != bitToHide) {
                modifiedBlueChannel +=1
                if(modifiedBlueChannel == 256)
                    modifiedBlueChannel = 254
            }
            val blueBitsAfter = modifiedBlueChannel.toUInt().toString(radix = 2)

            var modifiedPixel = Color(sourcePixel.red, sourcePixel.green, modifiedBlueChannel)
            bufferedImage.setRGB(x,y, modifiedPixel.rgb)

            bufferedImage.setRGB(x, y, Color(sourcePixel.red, sourcePixel.green, sourcePixel.blue.and(254).or(bitToHide) % 256).rgb)

            index++
            if(messageBitSequence.length == index)
                return
        }
}

fun getMessage(bufferedImage: BufferedImage):String {

    var message = ""
    var hiddenBitSequence = getHiddenBitSequence(bufferedImage)
    var byteCount = hiddenBitSequence.length / 8
    for(i in 0..byteCount-4)
    {
        var start = i*8
        var end = i*8+8
        var string = hiddenBitSequence.substring(start, end)

        // transform bit sequence to corresponding character
        var long = string.toLong()
        var decimal = convertBinaryToDecimal(long)
        var char1 = decimal.toChar()
        var char2 = string.toInt().toChar()

        message += char1
    }

    return message
}

fun getHiddenBitSequence(bufferedImage: BufferedImage):String {
    var hiddenBitSequence = ""
    for(y in 0..bufferedImage.height-1)
        for(x in 0..bufferedImage.width-1){
            hiddenBitSequence += Color(bufferedImage.getRGB(x,y)).blue % 2
            if(hiddenBitSequence.endsWith("000000000000000000000011")) // 00000000 00000000 00000011 [0, 0, 3]
                return hiddenBitSequence
        }
    return hiddenBitSequence
}


fun show() {
    print("Input image file: \n> ")
    var inputFile = readLine()!!

    try {
        // create outputstream from input image (read image)
        val bufferedInputImage = ImageIO.read(File(inputFile))
        var message = getMessage(bufferedInputImage)
        println("Message:")
        println(message)

    }
    catch (e:IOException)  {
        println("Can't read input file!")
    }

}