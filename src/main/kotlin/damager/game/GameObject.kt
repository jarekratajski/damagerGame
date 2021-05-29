package damager.player

import damager.engine.GameObjectView
import damager.engine.PlayerView
import damager.maze.Coord

sealed class GameObject {
    abstract fun location() : Coord
    abstract fun toView():GameObjectView
    abstract fun isAlive(): Boolean
}

data class PlayerObject(val location:Coord, val character: PlayerCharacter)  : GameObject() {
    override fun location(): Coord  = location
    override fun isAlive(): Boolean  = character.effectiveStats().life > 0

    override fun toView(): GameObjectView =
        PlayerView(location, character.name,character.effectiveStats().toView())

    fun update(ch: PlayerCharacter): GameObject =
        this.copy(character = ch)

}
