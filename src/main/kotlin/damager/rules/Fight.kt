package damager.rules

import damager.engine.Randomizer
import damager.player.GameCharacter
import dev.neeffect.nee.Nee
import io.vavr.collection.List.empty
import io.vavr.collection.Seq

class Fight(private val randomizer: Randomizer) {

    fun round(characters: FightData): Nee<Any, Nothing, FightData> = run {
        calcDamage(characters).flatMap { afterAttack ->
            calcDamage(afterAttack.swap()).map {
                it.swap()
            }
        }
    }

    private fun calcDamage(fight: FightData): Nee<Any, Nothing, FightData> = run {
        hit(fight.attacker, fight.defender).map { result ->
            val defenderStats = Rules.defenderExperience(fight.defender.effectiveStats(), result)

            fight.applyDamage(result)
                .expDefender(defenderStats)
                .expAttacker(Rules.expAttack(fight.attacker.effectiveStats(),result))

                //.expAttacker(result)

        }
    }

    private fun hit(attacker: GameCharacter, defender: GameCharacter): Nee<Any, Nothing, CombatRoundResult> = run {
        randomizer.k20().flatMap { hitRng ->
            val hitting = Rules.success(hitRng, attacker.dexterity())
            randomizer.k20().flatMap { dodgeRng ->
                val dodged = Rules.success(dodgeRng, defender.dexterity() - attacker.dexterity())
                if (hitting && !dodged) {
                    randomizer.k20().map { powerRng ->
                        val hit = Rules.hitPower(powerRng, attacker.strength())
                        CombatRoundResult(hit, attacker, false)
                    }
                } else {
                    Nee.success { CombatRoundResult(0, attacker, dodged) }
                }
            }
        }
    }
}


data class FightData(val attacker: GameCharacter, val defender: GameCharacter,
                     val log: FightLog = FightLog()
) {
    fun applyDamage(result: CombatRoundResult) : FightData =
        copy(defender =
        this.defender.damage(result.hit))
//            .defenderExperience(result), log = log.append(result))

    fun swap() = this.copy(attacker = defender, defender = attacker)

    fun expDefender(increase:Stats)  = this.copy(defender = defender.exp(increase))
    fun expAttacker(increase:Stats)  = this.copy(attacker = attacker.exp(increase))

}

data class FightLog(val logs: Seq<LogStatement> = empty()) {
    fun append(result: CombatRoundResult): FightLog =
        this.copy(logs = logs.append(result))
}

sealed class LogStatement {
    abstract fun message(): String
}

data class CombatRoundResult(
    val hit: Int,
    val attacker: GameCharacter,
    val defenderDodged: Boolean) : LogStatement() {
    override fun message(): String = if (hit > 0) {
        "${attacker.name()} hits with power: $hit"
    } else {
        "${attacker.name()} misses"
    }
}

data class RoundData(val hitPower: Int, val log: LogStatement)
