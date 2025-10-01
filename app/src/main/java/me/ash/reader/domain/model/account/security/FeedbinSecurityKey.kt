package me.ash.reader.domain.model.account.security

class FeedbinSecurityKey private constructor() : SecurityKey() {

    var username: String? = null
    var password: String? = null

    constructor(username: String?, password: String?) : this() {
        this.username = username
        this.password = password
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, FeedbinSecurityKey::class.java).let {
            username = it.username
            password = it.password
        }
    }
}
