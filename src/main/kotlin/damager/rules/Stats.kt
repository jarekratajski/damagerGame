package damager.rules

data class Stats(
    val strength: Stat = Stat(),
    val dexterity: Stat = Stat(),
    val health: Stat= Stat(),
    val life: Int = 0
)  {

    fun damage(hit: Int): Stats =
        this.copy(life = life - hit)

    fun maxLife(): Stats = copy(life = health.value())

    operator fun plus(increase:Stats) =
        this.copy(strength = this.strength+increase.strength,
            dexterity = this.dexterity + increase.dexterity,
            health = this.dexterity + increase.health,
            life = this.life + increase.life
        )

    fun healthIncrease(value:Int) = this.copy(health = Stat(value))

    fun strengthIncrease(value:Int) = this.copy(strength = Stat(value))

    fun dexterityIncrease(value:Int) = this.copy(dexterity = Stat(value))

    fun lifeIncrease(value: Int) = this.copy(life = value)

    companion object {
        val empty = Stats()
    }
}
