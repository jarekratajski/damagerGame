package damager.engine

import damager.IO
import dev.neeffect.nee.Nee
import dev.neeffect.nee.effects.test.get
import java.nio.charset.Charset
import java.util.Base64
import java.util.Random

interface Randomizer {
    fun k20(): Nee<Any, Nothing, Int>

    fun token(): IO<String>
}

class JvmRandomizer(private val rng: Random) : Randomizer {
    /**
     * returns number from 1 to 20
     */
    override fun k20(): Nee<Any, Nothing, Int> = Nee.success { rng.nextInt(20) + 1 }

    override fun token(): IO<String> = Nee.success {
        val bytes = ByteArray(6)
        rng.nextBytes(bytes)
        val res = Base64.getEncoder().encode(bytes)
        res.toString(Charset.defaultCharset())
    }
}


fun main() {
    val rng = JvmRandomizer(Random())
    val token = rng.token().perform(Unit).get()
    println(token)
}
