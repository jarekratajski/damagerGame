package damager.game

import damager.IO
import damager.InternalError
import damager.e
import damager.engine.OwnPlayerView
import damager.engine.PlayerView
import damager.engine.Randomizer
import damager.maze.Coord
import damager.maze.LocatedCell
import damager.maze.Maze
import damager.player.Command
import damager.player.GameObject
import damager.player.MoveCommand
import damager.player.MoveDirection
import damager.player.Player
import damager.player.PlayerCharacter
import damager.player.PlayerObject
import damager.rules.Fight
import damager.rules.FightData
import damager.rules.Rules
import damager.rules.Stat
import damager.rules.Stats
import dev.neeffect.nee.Nee
import io.vavr.collection.Seq
import io.vavr.collection.Vector
import io.vavr.control.Option
import io.vavr.collection.List

typealias Token = String


data class GameState(
    private val playfield: Maze = defaultMaze,
    private val objects: Seq<GameObject> = Vector.empty(),
    private val players: Seq<Player> = Vector.empty()
) {

    fun registerPlayer(uniqueName: String, randomizer: Randomizer): IO<Pair<GameState, Player>> =
        findPlayer(uniqueName).map {
            Nee.fail(InternalError("Player: $uniqueName exists")) as IO<Pair<GameState, Player>>
        }.getOrElse {
            randomizer.token().flatMap { randomToken ->
                randomizer.nextInt(playfield.width()).flatMap { x ->
                    randomizer.nextInt(playfield.height()).map { y ->
                        val stats = Stats(
                            strength = Stat.level(5),
                            dexterity = Stat.level(5),
                            health = Stat.level(5)
                        ).maxLife()
                        val character = PlayerCharacter(uniqueName, stats)
                        val playerObj = PlayerObject(Coord(x, y), character)
                        val player = Player(randomToken, uniqueName)
                        val newGame = this.copy(
                            objects = objects.append(playerObj),
                            players = players.append(player)
                        )
                        Pair(newGame, player)
                    }
                }
            }
        }

    fun findPlayer(name: String): Option<Player> = players.find { it.name == name }

    fun findPlayerByToken(token: Token): IO<Player> = players.find { it.token == token }.map {
        Nee.success { it } as IO<Player>
    }.getOrElse {
        Nee.fail(InternalError("Unauthorized")) as IO<Player>
    }

    fun getView() = GameView(this.playfield, this.objects.map { obj -> obj.toView() })

    fun getMaze(token: Token): IO<Maze> = findPlayerByToken(token).map {
        this.playfield
    }

    fun getObjects(token: Token): IO<Seq<GameObject>> = findPlayerByToken(token).map {
        this.objects
    }

    fun registerCommand(token: Token, cmd: Command): IO<GameState> = findPlayerByToken(token).map { player ->
        getPlayerObject(player.name).map { obj ->
            if (obj.isAlive()) {
                val playerWithCommand = player.addCommand(cmd)
                this.copy(players = players.replace(player, playerWithCommand))
            } else {
                this
            }
        }.getOrElse {
            this
        }
    }

    fun tick(randomizer: Randomizer): IO<GameState> = run {
        val coords = List.range(0, this.playfield.height()).flatMap { y ->
            List.range(0, this.playfield.width()).map { x ->
                Coord(x, y)
            }
        }
        val afterFights = coords.fold(Nee.success { this } as IO<GameState>) { prev, location ->
            prev.flatMap { state ->
                state.resolveCellTick(location, randomizer)
            }
        }
        afterFights.map { state ->
            state.movePlayers()
        }.flatMap { state ->
            state.relife(randomizer)
        }
    }

    private fun relife(randomizer: Randomizer): IO<GameState> = run {
        this.objects.fold(Nee.success { this }) { prev, obj ->
            when (obj) {
                is PlayerObject -> {
                    val playerObject = obj
                    if (playerObject.isAlive()) {
                        val stats = playerObject.character.effectiveStats()
                        randomizer.k20().flatMap { rng ->
                            val newPlayer = if (Rules.lifeRestore(rng, stats.life, stats.health)) {
                                val gameChar = playerObject.character.lifeIncrease(1)
                                playerObject.update(gameChar)
                            } else {
                                obj
                            }
                            prev.map { state ->
                                state.copy(objects = state.objects.replace(obj, newPlayer))
                            }
                        }
                    } else {
                        prev
                    }
                }
            }
        }
    }

    private fun movePlayers(): GameState =
        this.players.fold(this) { state, player ->
            val cmd = player.commands.headOption()
            val leftOverCmds = if (player.commands.size() > 0) {
                player.commands.tail()
            } else {
                Vector.empty()
            }
            cmd.map {
                when (it) {
                    is MoveCommand -> state.movePlayer(player, it, leftOverCmds)
                }
            }.getOrElse(state)
        }

    private fun movePlayer(player: Player, cmd: MoveCommand, otherCommands: Seq<Command>): GameState = run {
        this.getPlayerObject(player.name).map { playerObject ->
            val location = playerObject.location
            val moved = when (cmd.direction) {
                MoveDirection.UP -> location.up()
                MoveDirection.DOWN -> location.down()
                MoveDirection.LEFT -> location.left()
                MoveDirection.RIGHT -> location.right()
            }.cap(this.playfield.width(), this.playfield.height())
            val newPlayer = player.copy(commands = otherCommands)
            if (moved != location
                && canMove(playfield.getLocatedCell(location),
                    playfield.getLocatedCell(moved))
            ) {
                updatePlayerLocation(player, moved).copy(players = players.replace(player, newPlayer))
            } else {
                this.copy(players = players.replace(player, newPlayer))
            }
        }.getOrElse(this)
    }

    private fun canMove(fromLocation: LocatedCell, toLocation: LocatedCell): Boolean = run {
        val diffx = toLocation.coord.x - fromLocation.coord.x
        val diffy = toLocation.coord.y - fromLocation.coord.y
        when (diffx) {
            1 -> fromLocation.cell.doorRight && toLocation.cell.doorLeft
            -1 -> fromLocation.cell.doorLeft && toLocation.cell.doorRight
            else -> true
        } && when (diffy) {
            1 -> fromLocation.cell.doorDown && toLocation.cell.doorUp
            -1 -> fromLocation.cell.doorUp && toLocation.cell.doorDown
            else -> true
        }
    }

    private fun updatePlayerLocation(player: Player, moved: Coord): GameState = this.copy(
        objects = objects.map { obj ->
            when (obj) {
                is PlayerObject -> if (obj.character.name == player.name) {
                    obj.copy(location = moved)
                } else {
                    obj
                }
            }
        })

    private fun resolveCellTick(location: Coord, randomizer: Randomizer): IO<GameState> = run {
        val cellObjects = this.getCellObjects(location)

        val players = getAlivePlayers(cellObjects)

        if (players.size() > 1) {
            randomizer.nextInt(players.size()).flatMap { pl1 ->
                val pl2 = if (pl1 + 1 >= players.size()) 0 else pl1 + 1
                val player1 = players[pl1] as PlayerObject
                val player2 = players[pl2] as PlayerObject
                val fight = Fight(randomizer)
                val fighData = FightData(player1.character, player2.character)

                fight.round(fighData).map { result ->
                    val objects1 = objects.replace(player1, player1.update(result.attacker as PlayerCharacter))
                        .replace(player2, player2.update(result.defender as PlayerCharacter))

                    this.copy(objects = objects1)
                }
            }

        } else {
            Nee.success { this }.e() as IO<GameState>
        }
    }

    private fun getAlivePlayers(cellObjects: Seq<GameObject>): Seq<PlayerObject> =
        cellObjects.filter { obj ->
            when (obj) {
                is PlayerObject -> obj.isAlive()
            }
        }.map {
            it as PlayerObject
        }

    private fun getCellObjects(location: Coord): Seq<GameObject> =
        this.objects.filter { obj -> obj.location() == location }


    fun getPlayerObject(name: String): Option<PlayerView> =
        this.objects.find { it is PlayerObject && it.character.name == name }
            .map {
                val playerObject = it as PlayerObject
                PlayerView(
                    location = playerObject.location,
                    name = playerObject.character.name,
                    effectiveStats = playerObject.character.effectiveStats().toView()
                )
            }

    fun getOwnPlayer(token: String): IO<OwnPlayerView> =
        this.findPlayerByToken(token).flatMap { player ->
            getPlayerObject(player.name).map {
                Nee.success {
                    OwnPlayerView(
                        name = player.name,
                        token = player.token,
                        gameObject = it,
                        commands = player.commands
                    )
                }.e()
            }.getOrElse {
                Nee.fail(Error("No player with name : ${player.name}")) as IO<OwnPlayerView>
            }
        }

    companion object {
        val defaultMaze = Maze.generateEmptyMaze(5, 3)
    }
}


