package moe.peanutmelonseedbigalmond.push.exception

class ApiException(val errorCode:String,val errorMessage:String):Exception() {
    override val message: String
        get() = errorMessage
}