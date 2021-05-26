package damager.player

import damager.rules.CombatRoundResult
import damager.rules.Stat
import damager.rules.Stats

sealed class GameCharacter {

    abstract fun name(): String

    abstract fun effectiveStats(): Stats

    abstract fun damage(hit: Int): GameCharacter
    abstract fun dexterity(): Stat
    abstract fun strength(): Stat

    abstract fun exp(increase: Stats): GameCharacter

}

data class PlayerCharacter(val name: String, private val baseStats: Stats) : GameCharacter() {
    override fun name(): String =this.name

    override fun effectiveStats(): Stats = this.baseStats
    override fun damage(hit: Int) = this.copy(baseStats = baseStats.damage(hit))

    override fun dexterity(): Stat = effectiveStats().dexterity
    override fun strength(): Stat = effectiveStats().strength
    override fun exp(increase: Stats): GameCharacter =
        copy(baseStats = baseStats + increase)

}

