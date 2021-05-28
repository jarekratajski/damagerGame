package damager

import dev.neeffect.nee.Nee

typealias IO<A> = Nee<Any, Error, A>

sealed class Error(val msg:String)  {
    open fun message() : String = "${javaClass.name}:$msg"
}

class InternalError(msg:String) : Error(msg)
