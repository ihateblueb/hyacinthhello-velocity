package site.remlit.blueb.hyacinthhelloVelocity

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import redis.clients.jedis.JedisPubSub
import kotlin.jvm.optionals.getOrNull

class MessageListener : JedisPubSub() {
    override fun onMessage(channel: String, message: String)
        = try { handle(message) } catch (e: Exception) { HyacinthHelloVelocity.logger.error("${e.message}: ${e.stackTrace}") }

    fun handle(message: String) {
        val split = message.split("::")

        val type = split.getOrNull(0)

        val player = split.getOrNull(1)?.split(",")
        val uuid = player?.getOrNull(0)
        val name = player?.getOrNull(1)

        val server =
            try { HyacinthHelloVelocity.server.getPlayer(name).getOrNull()?.currentServer?.getOrNull()?.server }
            catch (e: Exception) { null }

        println(HyacinthHelloVelocity.config.wrapperLeft + message + HyacinthHelloVelocity.config.wrapperRight)

        fun wrapCustomMessage(message: String): Component =
            LegacyComponentSerializer.legacyAmpersand().deserialize(
                HyacinthHelloVelocity.config.wrapperLeft + message + HyacinthHelloVelocity.config.wrapperRight
            )

        fun handleCustom() {
            val customMessage = split.getOrNull(2)
            val sendCustomMessage = !customMessage.isNullOrBlank() && customMessage != "NULL"

            if (!sendCustomMessage)
                return

            if (HyacinthHelloVelocity.config.overrideBackends || server == null) {
                Broadcaster.broadcastToAll { wrapCustomMessage(customMessage) }
            } else {
                Broadcaster.broadcastToAllBut(server) { wrapCustomMessage(customMessage) }
            }
        }

        fun handleNonDeath(type: String) = when (type) {
            "join", "leave" -> {
                val playerName = HyacinthHelloVelocity.server.getPlayer(uuid).getOrNull()?.username ?: name ?: "Unknown player"

                val serverName = server?.serverInfo?.name ?: "Unknown server"

                val baseMessage = if (type == "join") HyacinthHelloVelocity.config.joinMessage else HyacinthHelloVelocity.config.leaveMessage
                val message = MiniMessage.miniMessage().deserialize(
                    baseMessage
                        .replace("{p}", playerName)
                        .replace("{s}", serverName)
                )

                if (HyacinthHelloVelocity.config.overrideBackends || server == null) {
                    Broadcaster.broadcastToAll { message }
                } else {
                    Broadcaster.broadcastToAllBut(server) { message }
                }

                handleCustom()
            }
            else -> {}
        }

        fun handleVanillaDeath() {
            val forwardedMessage = split.getOrNull(1)

            if (forwardedMessage.isNullOrBlank())
                return

            val message = MiniMessage.miniMessage().deserialize(
                HyacinthHelloVelocity.config.deathMessage
                    .replace("{m}", forwardedMessage)
            )

            if (HyacinthHelloVelocity.config.overrideBackends || server == null) {
                Broadcaster.broadcastToAll { message }
            } else {
                Broadcaster.broadcastToAllBut(server) { message }
            }
        }

        when (type) {
            "join" -> handleNonDeath(type)
            "leave" -> handleNonDeath(type)
            "death" -> handleCustom()
            "vanilla_death" -> handleVanillaDeath()
        }
    }
}