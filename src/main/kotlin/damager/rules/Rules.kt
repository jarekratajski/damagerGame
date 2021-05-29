package damager.rules

object Rules {

    fun success(k20Result: Int, stat: Stat): Boolean =
        when (k20Result) {
            1 -> true
            20 -> false
            else -> k20Result <= stat.value()
        }

    fun hitPower(k20Result: Int, strength: Stat) =
        ((k20Result + strength.value() - 12) / 4) + 1


    fun lifeRestore(k20Result: Int,  life: Int, health:Stat) : Boolean =
        when (k20Result) {
            1 -> true
            20 -> false
            else -> k20Result*1.8 <= health.value()
        }


    fun defenderExperience(defender: Stats, result: CombatRoundResult): Stats =
        Stats.empty.healthIncrease(expHealth(result.hit)) +
                expDodging(defender, result)

    private fun expDodging(defender: Stats, result: CombatRoundResult): Stats =
        if (result.defenderDodged) {
            Stats.empty.dexterityIncrease(result.attacker.effectiveStats().dexterity.value() * 2)
        } else {
            Stats.empty
        }

    private fun expHealth(lifeLoss: Int): Int = lifeLoss * 5


    fun expAttack(attacker: Stats, result: CombatRoundResult): Stats =
        if (result.hit > 0) {
            Stats.empty.dexterityIncrease(result.hit + 10).strengthIncrease(result.hit*5)
        } else {
            Stats.empty
        }

}
