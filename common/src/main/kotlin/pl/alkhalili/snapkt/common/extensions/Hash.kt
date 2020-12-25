package pl.alkhalili.snapkt.common.extensions

import java.security.MessageDigest

fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}
