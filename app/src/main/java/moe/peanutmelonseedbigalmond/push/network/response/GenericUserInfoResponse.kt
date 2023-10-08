package moe.peanutmelonseedbigalmond.push.network.response

data class GenericUserInfoResponse(
    val token: LoginToken,
) {
    data class LoginToken(
        val user: User,
    ) {
        data class User(
            val username: String,
        )
    }
}