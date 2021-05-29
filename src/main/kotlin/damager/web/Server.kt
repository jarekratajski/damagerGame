package damager.web

import damager.Error
import damager.engine.GameService
import damager.engine.JvmRandomizer
import damager.player.Command
import dev.neeffect.nee.Nee
import dev.neeffect.nee.ctx.web.BaseWebContextProvider
import dev.neeffect.nee.ctx.web.DefaultErrorHandler
import dev.neeffect.nee.ctx.web.DefaultJacksonMapper
import dev.neeffect.nee.ctx.web.ErrorHandler
import dev.neeffect.nee.ctx.web.pure.get
import dev.neeffect.nee.ctx.web.pure.nested
import dev.neeffect.nee.ctx.web.pure.post
import dev.neeffect.nee.ctx.web.pure.startNettyServer
import dev.neeffect.nee.effects.test.get
import dev.neeffect.nee.effects.time.TimeProvider
import dev.neeffect.nee.effects.tx.DummyTxProvider
import dev.neeffect.nee.runNee
import io.ktor.request.receive
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val errorHandler: ErrorHandler = { error ->

    if (error is Throwable) {
        error.printStackTrace()
    }
    when (error) {
        is Error -> DefaultErrorHandler(error.msg)
        else -> DefaultErrorHandler(error).also {
            System.err.println(error)
        }
    }
}

val webContextProvider: BaseWebContextProvider<Nothing, DummyTxProvider> = BaseWebContextProvider.createTransient(
    customErrorHandler = errorHandler
)


object Server {
    fun defineRouting(
        ctx: BaseWebContextProvider<Nothing, DummyTxProvider>,
        randomizer: JvmRandomizer) = run {
        val rb = ctx.routeBuilder()
        val service = GameService(randomizer)
        val scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate({
            println("ticking")
            try {
                service.tick().perform(Unit).get()
            } catch (e:Throwable) {
                e.printStackTrace()
            }
        },
            1,
            3,
            TimeUnit.SECONDS
        )

        rb.nested("/api/game") {
            rb.nested("/admin") {
                rb.get("/view") {
                    service.getView()
                } + rb.post("/reset") {
                    service.resetGame()
                }
            } + rb.nested("/player") {
                rb.nested("/command") {
                    rb.post {
                        val token = it.request.headers["token"]!!
                        runNee {
                            it.receive<Command>()
                        }.anyError().flatMap { cmd ->
                            service.postCommand(token, cmd)
                        }
                    }

                } + rb.post {
                    val name = it.parameters["name"]!!
                    service.registerPlayer(name)
                } + rb.get {
                    val token = it.request.headers["token"]!!
                    service.getOwnPlayer(token)
                }
            } + rb.get("/objects") {
                val token = it.request.headers["token"]!!
                service.getObjects(token)
            }
        }
    }
}

const val SERVER_PORT = 7777

fun main() {
    val randomizer = JvmRandomizer(Random())
    startNettyServer(SERVER_PORT, DefaultJacksonMapper.mapper, webContextProvider) {
       Server.defineRouting(webContextProvider, randomizer)
    }.perform(Unit)
}
