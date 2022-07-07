package cryptography

fun main() {

    do {
        println("Task (hide, show, exit): \n> ")
        val command = readln()
        when(command) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> { println("Bye!"); return }
            else -> println("Wrong task: $command")
        }
    } while(command != "exit")
}

fun hide() {
    println("Hiding message in image.")
}

fun show() {
    println("Obtaining message from image.")
}
