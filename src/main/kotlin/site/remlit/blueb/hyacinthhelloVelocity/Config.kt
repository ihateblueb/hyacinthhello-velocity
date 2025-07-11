package site.remlit.blueb.hyacinthhelloVelocity

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class Config {
    val redis = ConfigRedis()

    val overrideBackends: Boolean = true
    val joinMessage: String = "<yellow>{p} joined {s}"
    val leaveMessage: String = "<yellow>{p} left {s}"
    val deathMessage: String = "<yellow>{m}"

    val wrapperLeft: String = "&e&o"
    val wrapperRight: String = ""
}

@ConfigSerializable
class ConfigRedis {
    val address: String = "0.0.0.0"
    val port: Int = 6379
    val channel: String = "hyacinthhello"
    val user: String? = null
    val pass: String? = null
}