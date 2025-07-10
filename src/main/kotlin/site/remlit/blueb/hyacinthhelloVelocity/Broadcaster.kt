package site.remlit.blueb.hyacinthhelloVelocity

import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.ComponentLike

class Broadcaster {
    companion object {
        val allServers: Collection<RegisteredServer> =
            HyacinthHelloVelocity.server.allServers

        fun broadcastToAll(message: ComponentLike) {
            for (server in allServers) {
                server.sendMessage(message)
            }
        }
        
        fun broadcastToAllBut(exceptionServer: RegisteredServer, message: ComponentLike) {
            for (server in allServers) {
                if (server != exceptionServer)
                    server.sendMessage(message)
            }
        }
    }
}