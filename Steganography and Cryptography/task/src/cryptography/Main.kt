package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {

    test()

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
    MessageEncoder.inputFile = readLine()!!

    print("Output image file: \n> ")
    MessageEncoder.outputFile = readLine()!!

    print("Message to hide: \n> ")
    MessageEncoder.message = readLine()!!

    print("Password: \n> ")
    MessageEncoder.password = readLine()!!

    MessageEncoder.readInputFile()
    MessageEncoder.hideMessage()
    MessageEncoder.writeOutputFile()
    MessageEncoder.clearMessage()

    println("Message saved in ${MessageEncoder.outputFile} image.")
}

fun show() {
    print("Input image file: \n> ")
    MessageDecoder.inputFile = readLine()!!

    print("Password: \n> ")
    MessageDecoder.password = readLine()!!

    MessageDecoder.readInputFile()
    MessageDecoder.extractMessage()

    println("Message:")
    println(MessageDecoder.message)

    MessageDecoder.clearMessage()
}

object MessageEncoder {

    // vars initialized with never used default values (but needed to initialize with sth.)
    var inputFile = ""
    var outputFile = ""
    var message = ""
    var password = ""
    var bufferedImage = BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)

    fun readInputFile() {
        try {
            bufferedImage = ImageIO.read(File(MessageEncoder.inputFile))
        }
        catch (e:IOException)  {
            println("Can't read input file!")
        }
    }

    fun hideMessage() {

        var plainMessageByteArray = message.encodeToByteArray() + byteArrayOf(0, 0, 3)
        var encryptedmessageByteArray = encryptMessage(plainMessageByteArray)
        var messageBitString = convertByteArrayToBitString(encryptedmessageByteArray)

        if((bufferedImage.width * bufferedImage.height) < messageBitString.length) {
            println("The input image is not large enough to hold this message.")
            return
        }

        var currentMessageBitIndex = 0
        for(y in 0..bufferedImage.height-1)
            for(x in 0..bufferedImage.width-1) {

                var originalPixel = Color(bufferedImage.getRGB(x,y))
                var bitToHide = messageBitString[currentMessageBitIndex].digitToInt()
                var modifiedBlue = modifyBlueChannel(originalPixel.blue, bitToHide)
                var modifiedPixel = Color(originalPixel.red, originalPixel.green, modifiedBlue)
                bufferedImage.setRGB(x,y, modifiedPixel.rgb)

                // return if all message bits have been hidden
                if(++currentMessageBitIndex == messageBitString.length)
                    return
            }
    }

    fun encryptMessage(messageByteArray:ByteArray): ByteArray {
        var passwordByteArray = password.toByteArray()
        var encryptedMessageByteArray = ByteArray(messageByteArray.size)
        for(i in 0..messageByteArray.size-4) { // last three 003
            var messageByte = messageByteArray[i]
            var passwordByteIndex = i % passwordByteArray.size
            var passwordByte = passwordByteArray[passwordByteIndex]
            var encryptedByte = messageByte.xor(passwordByte)
            encryptedMessageByteArray[i] = encryptedByte
        }
        encryptedMessageByteArray[encryptedMessageByteArray.size-3] = 0
        encryptedMessageByteArray[encryptedMessageByteArray.size-2] = 0
        encryptedMessageByteArray[encryptedMessageByteArray.size-1] = 3
        return encryptedMessageByteArray
    }

    fun convertByteArrayToBitString(messageByteArray: ByteArray): String {
        var messageBitSequence = ""
        for (byte in messageByteArray) {
            var bits = byte.toUInt().toString(radix = 2)
            bits = bits.padStart(8,'0')
            messageBitSequence += bits
        }
        return messageBitSequence
    }

    fun modifyBlueChannel(blue:Int, bitToHide:Int):Int {
        var blueBitsBefore = Integer.toBinaryString(blue)
        var modifiedBlue = blue.and(254).or(bitToHide) % 256 // ToDo: understand whats going on, copied from hints
        val blueBitsAfter = Integer.toBinaryString(modifiedBlue)
        return modifiedBlue
    }

    fun writeOutputFile() {
        ImageIO.write(bufferedImage, "png", File(outputFile))
    }

    fun clearMessage() {
        message = ""
    }
}

object MessageDecoder {

    // vars initialized with never used default values (but needed to initialize with sth.)
    var inputFile = ""
    var message = ""
    var password = ""
    var bufferedImage = BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)

    fun readInputFile() {
        try {
            bufferedImage = ImageIO.read(File(inputFile))
        }
        catch (e:IOException)  {
            println("Can't read input file!")
        }
    }

    fun extractMessage() {
        var hiddenBitString = extractHiddenBitSequenceAsString(bufferedImage)
        var hiddenByteArray = convertBitStringToByteArray(hiddenBitString)
        var decyrptedMessage = decryptMessage(hiddenByteArray)

        for(byte in decyrptedMessage) {
            var char = byte.toChar()
            message += char
        }

    }

    fun convertBitStringToByteArray(bitString: String):ByteArray {
        var numberOfBytes = bitString.length / 8
        var byteArray = ByteArray(numberOfBytes)
        for(i in 0..numberOfBytes-4) {
            var hiddenByteStartIndex = i*8
            var hiddenByteAsBitString = bitString.substring(hiddenByteStartIndex, hiddenByteStartIndex + 8)
            var long = hiddenByteAsBitString.toLong()
            var decimal = convertBinaryToDecimal(long)    // ToDo: replace with some standard functions, but dont know which
            byteArray[i] = decimal.toByte()
        }
        return byteArray
    }

    fun decryptMessage(messageByteArray:ByteArray): ByteArray {
        var passwordByteArray = password.toByteArray()
        var decryptedMessageByteArray = ByteArray(messageByteArray.size-3)
        for(i in 0..messageByteArray.size-4) { // last three 003
            var messageByte = messageByteArray[i]
            var passwordByteIndex = i % passwordByteArray.size
            var passwordByte = passwordByteArray[passwordByteIndex]
            var encryptedByte = messageByte.xor(passwordByte)
            decryptedMessageByteArray[i] = encryptedByte
        }

        return decryptedMessageByteArray
    }

    fun extractHiddenBitSequenceAsString(bufferedImage: BufferedImage):String {
        var hiddenBitSequence = ""
        for(y in 0..bufferedImage.height-1)
            for(x in 0..bufferedImage.width-1){
                hiddenBitSequence += Color(bufferedImage.getRGB(x,y)).blue % 2
                if(hiddenBitSequence.endsWith("000000000000000000000011")) // 00000000 00000000 00000011 [0, 0, 3]
                    return hiddenBitSequence
            }
        return hiddenBitSequence
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

    fun clearMessage() {
        message = ""
    }
}

fun test() {

    var v1 = MessageEncoder.modifyBlueChannel(253, 0)
    var v2 = MessageEncoder.modifyBlueChannel(254, 0)
    var v3 = MessageEncoder.modifyBlueChannel(255, 0)

    var v4 = MessageEncoder.modifyBlueChannel(253, 1)
    var v5 = MessageEncoder.modifyBlueChannel(254, 1)
    var v6 = MessageEncoder.modifyBlueChannel(255, 1)

    var v7 = 253.and(254)
    var v8 = 254.and(254)
    var v9 = 255.and(254)

    var v10 = 252.or(0)
    var v11 = 252.or(1)
    var v12 = 254.or(0)
    var v13 = 254.or(1)

    var v14 = 253 % 256
    var v15 = 254 % 256
    var v16 = 255 % 256

}
