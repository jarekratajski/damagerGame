package damager.player

import damager.maze.Coord

sealed class GameObject {
    abstract fun location() : Coord
}

data class PlayerObject(val location:Coord, val character: PlayerCharacter)  : GameObject() {
    override fun location(): Coord  = location
}
