package damager.game

import damager.engine.GameObjectView
import damager.maze.Maze
import io.vavr.collection.Seq

data class GameView(val playfield: Maze, val objects: Seq<GameObjectView>)

sealed class LogView() {
    fun canSee(player: String) = true
}

data class CombatResultView(
    val attacker: String,
    val defender: String,
    val hit: Int,
    val defenderDodged: Boolean
) : LogView()


