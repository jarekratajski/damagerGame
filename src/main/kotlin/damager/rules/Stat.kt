package damager.rules

/**
 * base stat (0-20) included
 */
data class Stat(private val internalValue: Int) {
    fun value() : Int =
        (Math.log(internalValue/10.0)/base).toInt()

    operator fun minus(other:Stat) =
        Stat((this.internalValue - other.internalValue).coerceAtLeast(0))

    fun experience(value: Int): Stat = this.copy(internalValue = internalValue+ value)

    companion object {
        val base = Math.log(1.6)
        fun level(level: Int) = Stat((Math.pow(1.6,level.toDouble())*10).toInt() + 1);
    }
}


fun main() {

    (0 until  20).forEach { i ->

        println(i)
        val z = Math.pow(1.6, i.toDouble())*10
        println(z)
        val x1= Math.log(z/10)
        val x2 = Math.log(1.6)
        val  y = x1/x2
        val s = Stat.level(i)

        println("$i => $z => $y => ${s.value()}")
    }
}
