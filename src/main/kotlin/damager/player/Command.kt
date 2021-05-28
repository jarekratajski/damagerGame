package damager.player

sealed class Command {

}


data class MoveCommand(val direction: MoveDirection) : Command()

enum class MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
