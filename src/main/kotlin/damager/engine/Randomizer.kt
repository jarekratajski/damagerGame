package damager.engine

import dev.neeffect.nee.Nee
import java.util.Random

interface Randomizer {
    fun k20(): Nee<Any, Nothing, Int>
}

class JvmRandomizer(private val rng: Random) : Randomizer{
    /**
     * returns number from 1 to 20
     */
    override fun k20(): Nee<Any, Nothing, Int> = Nee.success { rng.nextInt(20) +1 }
}
