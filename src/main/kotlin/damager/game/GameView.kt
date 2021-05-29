package damager.game

import damager.engine.GameObjectView
import damager.maze.Maze
import io.vavr.collection.Seq

data class GameView(val playfield: Maze, val objects: Seq<GameObjectView>)


