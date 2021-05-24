package damager.rules

import damager.engine.Randomizer
import dev.neeffect.nee.Nee
import dev.neeffect.nee.effects.test.get
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FightTest : DescribeSpec({
    describe("round of fight") {
        it ("should damage character b"){
            val randomSeq = TestRandomizer(listOf(4, 10, 20, 4, 10, 20))
            val fight = Fight(randomSeq)
            val result  = fight.round(initidalFightData).perform(Unit).get()
            println(result)
        }
    }

})  {
    companion object {
        val chara = Character(
            name = "a",
            strength = Stat.level(5),
            dexterity = Stat.level(5),
            health = Stat.level(5),
            life = 1
        ).maxLife()
        val charb = Character(
            name = "b",
            strength = Stat.level(5),
            dexterity = Stat.level(5),
            health = Stat.level(5),
            life = 1
        ).maxLife()

        val initidalFightData = FightData(chara, charb)
    }
}

class TestRandomizer(k20Sequences: List<Int>) : Randomizer {
    private val mutableSeq = k20Sequences.toMutableList()
    override fun k20(): Nee<Any, Nothing, Int>  =
        Nee.success {
            val nextVal = mutableSeq.removeAt(0)
            nextVal
        }


}
