package damager.player

import com.fasterxml.jackson.annotation.JsonTypeInfo
import damager.web.webContextProvider

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class Command {

}


data class MoveCommand(val direction: MoveDirection) : Command()

enum class MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

fun main() {
    val up =MoveCommand(MoveDirection.RIGHT)
    val string = webContextProvider.jacksonMapper.writeValueAsString(up)
    println(string)

    val cmd = webContextProvider.jacksonMapper.readValue(string,Command::class.java)
    println(cmd)
}
