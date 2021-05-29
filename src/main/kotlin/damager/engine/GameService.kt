package damager.engine

import damager.IO
import damager.InternalError
import damager.e
import damager.game.GameState
import damager.game.GameView
import damager.game.LogView
import damager.maze.Coord
import damager.player.Command
import damager.rules.LogStatement
import dev.neeffect.nee.Nee
import dev.neeffect.nee.atomic.AtomicRef
import io.vavr.collection.Seq
import io.vavr.collection.Vector

class GameService(
    private val randomizer: Randomizer,
    private val gameState: AtomicRef<GameState> = AtomicRef(GameState())
) {
    fun resetGame(): IO<GameView> = gameState.updateAndGet {
        GameState()
    }.map {
        it.getView()
    }

    fun getView(): IO<GameView> = gameState.get().map {
        it.getView()
    }

    fun getObjects(token: String): IO<Seq<GameObjectView>> = gameState.get().e().flatMap { state ->
        state.getObjects(token).map { objects ->
            objects.map { obj ->
                obj.toView()
            }
        }
    }

    fun postCommand(token: String, cmd: Command): IO<Boolean> =
        gameState.get().e().flatMap { state ->
            state.registerCommand(token, cmd).flatMap { newState ->
                gameState.compareAndSet(state, newState).e()
            }
        }

    fun registerPlayer(name: String): IO<OwnPlayerView> =
        gameState.get().e().flatMap { game ->
            game.registerPlayer(name, randomizer).flatMap {
                val modified = it.first
                val player = it.second
                gameState.compareAndSet(game, modified).e().flatMap { success ->
                    if (success) {
                        modified.getPlayerObject(name).map {
                            Nee.success { OwnPlayerView(player.name, player.token, it) } as IO<OwnPlayerView>
                        }.getOrElse {
                            Nee.fail(InternalError("no player: $name"))
                        }

                    } else {
                        Nee.fail(InternalError("try again")) as IO<OwnPlayerView>
                    }
                }
            }
        }

    fun getOwnPlayer(token: String): IO<OwnPlayerView> =
        gameState.get().e().flatMap { game ->
            game.getOwnPlayer(token)
        }

    fun tick() = gameState.updateAction { game ->
        game.tick(randomizer) as Nee<Any, Nothing, GameState>
    }
}

data class OwnPlayerView(
    val name: String,
    val token: String,
    val gameObject: PlayerView,
    val commands: Seq<Command> = Vector.empty(),
    val logs: Seq<LogView> = Vector.empty()
)

sealed class GameObjectView() {

}

data class PlayerView(
    val location: Coord,
    val name: String,
    val effectiveStats: StatsView
) : GameObjectView() {
    fun isAlive() = effectiveStats.life > 0
}

data class StatsView(
    val strength: Int,
    val dexterity: Int,
    val health: Int,
    val life: Int = 0
) {

}

