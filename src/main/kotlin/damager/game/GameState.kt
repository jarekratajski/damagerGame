package damager.game

import damager.IO
import damager.InternalError
import damager.engine.Randomizer
import damager.maze.Coord
import damager.maze.Maze
import damager.player.GameObject
import damager.player.Player
import damager.player.PlayerCharacter
import damager.player.PlayerObject
import damager.rules.Stat
import damager.rules.Stats
import dev.neeffect.nee.Nee
import io.vavr.collection.Seq
import io.vavr.control.Option


typealias Token = String


data class GameState(
    private val playfield: Maze,
    private val objects: Seq<GameObject>,
    private val players: Seq<Player>,
    val randomizer: Randomizer
) {

    fun registerPlayer(uniqueName:String) : IO<Pair<GameState, Player>>
     = findPlayer(uniqueName).map {
         Nee.fail(InternalError("Player: $uniqueName exists")) as IO<Pair<GameState, Player>>
    }.getOrElse {
        randomizer.token().map { randomToken ->
            val stats =  Stats(
                strength =  Stat(5),
                dexterity = Stat(5),
                health = Stat(5)
            ).maxLife()
            val character = PlayerCharacter(uniqueName,stats)
            val playerObj = PlayerObject(Coord(1,1),character)
            val player  = Player(randomToken, uniqueName)
            val newGame = this.copy(
                objects = objects.append(playerObj),
                players =  players.append(player)
            )
            Pair(newGame, player)
        }
    }

    fun findPlayer(name:String) : Option<Player> = players.find { it.name == name }

    fun findPlayerByToken(token:Token) : IO<Player> = players.find {it.token == token}.map {
        Nee.success { it } as IO<Player>
    } .getOrElse {
        Nee.fail(InternalError("Unauthorized")) as IO<Player>
    }

   fun getMaze(token:Token)  : IO<Maze> = findPlayerByToken(token).map {
       this.playfield
   }

    fun getObjects(token:Token)  : IO<Seq<GameObject>> = findPlayerByToken(token).map {
        this.objects
    }

    fun tick() : IO<GameState> = TODO()
}
