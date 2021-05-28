package damager.player

import io.vavr.collection.Seq
import io.vavr.collection.Vector


data class Player(
    val token:String,
    val name: String,
    val commands: Seq<Command> = Vector.empty()
    ) {
}

