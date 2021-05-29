package damager.player

import io.vavr.collection.Seq
import io.vavr.collection.Vector


data class Player(
    val token:String,
    val name: String,
    val commands: Seq<Command> = Vector.empty()
    ) {
    fun addCommand(cmd: Command): Player  =
        copy(commands = commands.prepend(cmd))
}

