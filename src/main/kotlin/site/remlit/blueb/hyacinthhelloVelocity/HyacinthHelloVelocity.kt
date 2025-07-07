package site.remlit.blueb.hyacinthhelloVelocity;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull


@Plugin(
    id = "hyacinthhello-velocity",
    name = "HyacinthHello Velocity",
    version = BuildConstants.VERSION,
    authors = ["blueb"]
)
class HyacinthHelloVelocity {
    @Inject
    @Suppress("unused")
    fun HyacinthHelloVelocity(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path) {
        instance = this

        Companion.server = server
        Companion.logger = logger
        Companion.dataDirectory = dataDirectory
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        server.getChannelRegistrar().register(IDENTIFIER);
    }

    @Subscribe
    fun onPlayerJoin(event: ServerConnectedEvent) {
        val playerName = event.player.username
        val serverName = event.server.serverInfo.name
        server.sendMessage(
            MiniMessage.miniMessage().deserialize("<yellow>$playerName joined $serverName")
        )
    }

    @Subscribe
    fun onPluginMessageFromBackend(event: PluginMessageEvent) {
        if (IDENTIFIER != event.identifier)
            return

        event.result = PluginMessageEvent.ForwardResult.handled()

        val data = event.data.toString()
        val split = data.split("::")

        val type = split[0]
        val player = split[1]
        val message = split[2]

        if (type.isBlank() || player.isBlank() || message.isBlank()) {
            logger.warn("Received message, but contents were invalid.")
            return
        }

        val playerSplit = player.split(",")
        val playerUuid = playerSplit[0]
        val playerName = playerSplit[1]

        if (playerUuid.isBlank() || playerName.isBlank()) {
            logger.warn("Received message, but contents of player were invalid.")
            return
        }

        val fetchedPlayer = server.getPlayer(playerUuid).getOrNull()
        val displayName = fetchedPlayer?.username ?: playerName

        if (message == null)
            return

        val converted = LegacyComponentSerializer.legacyAmpersand().deserializeOrNull(message)

        server.sendMessage(
            Component.text("")
        )
    }

    companion object {
        lateinit var instance: HyacinthHelloVelocity

        lateinit var server: ProxyServer
        lateinit var logger: Logger
        lateinit var dataDirectory: Path

        val IDENTIFIER: MinecraftChannelIdentifier? = MinecraftChannelIdentifier.from("hyacinthhello:main")
    }
}
