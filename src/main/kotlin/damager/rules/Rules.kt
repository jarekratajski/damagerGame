package damager.rules

object Rules {

    fun success(k20Result: Int, stat: Stat): Boolean =
        when (k20Result) {
            1 -> true
            20 -> false
            else -> k20Result <= stat.value()
        }

    fun hitPower(k20Result: Int, strength:Stat) =
        ((k20Result+strength.value() - 15)/4)+1

    fun expHealth(lifeLoss: Int, health:Stat) =
        health.experience(lifeLoss*5)

    fun expDodging(dexterity: Stat, result: CombatRoundResult): Stat =
        if (result.defenderDodged) {
             dexterity.experience(result.attacker.dexterity.value()*2)
        } else {
            dexterity
        }

    fun expAttack(attacker: Character, result: CombatRoundResult) : Character =
        if (result.hit > 0) {
            attacker.copy(
                dexterity = attacker.dexterity.experience(result.hit+10),
                strength = attacker.strength.experience(result.hit*5)
            )
        } else {
            attacker
        }

}
