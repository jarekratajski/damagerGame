package damager.rules

data class Character(
    val name: String,
    val strength: Stat,
    val dexterity: Stat,
    val health: Stat,
    val life: Int
)  {
    fun damage(hit: Int): Character =
        this.copy(life = life - hit)

    fun defenderExperience(result: CombatRoundResult): Character  =
        this.copy(
            health = Rules.expHealth(result.hit,health),
            dexterity = Rules.expDodging(dexterity, result))

    fun maxLife(): Character = copy(life = health.value())


}
