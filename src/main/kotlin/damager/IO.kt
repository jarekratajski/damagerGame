package damager

import dev.neeffect.nee.Nee
import dev.neeffect.nee.withErrorType

typealias IO<A> = Nee<Any, Error, A>

sealed class Error(val msg: String) {
    open fun message(): String = "${javaClass.name}:$msg"
}

class InternalError(msg: String) : Error(msg)

fun <A> Nee<Any, Nothing, A>.e() = this.withErrorType<Any, Error, A>()
