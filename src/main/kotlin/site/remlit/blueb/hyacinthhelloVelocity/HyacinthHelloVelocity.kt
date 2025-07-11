package site.remlit.blueb.hyacinthhelloVelocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.thread


@Plugin(
    id = "hyacinthhello-velocity",
    name = "HyacinthHello Velocity",
    version = BuildConstants.VERSION,
    authors = ["blueb"]
)
class HyacinthHelloVelocity {
    @Inject
    @Suppress("unused", "FunctionName")
    fun HyacinthHelloVelocity(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path, metricsFactory: Metrics.Factory) {
        instance = this

        Companion.server = server
        Companion.logger = logger
        Companion.dataDirectory = dataDirectory
        Companion.metricsFactory = metricsFactory

        File(dataDirectory.toAbsolutePath().toString()).mkdirs()
    }

    @Subscribe
    @Suppress("unused")
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        metricsFactory.make(this, 26458)

        val path = dataDirectory.resolve("config.yml")
        val file = File(path.toAbsolutePath().toString())
        if (!file.exists()) file.createNewFile()

        val loader = YamlConfigurationLoader.builder()
            .path(path)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()

        val config = loader.load().get<Config>()
        if (config == null) throw Exception("Config could not be loaded")
        Companion.config = config

        val jedisConfig = JedisPoolConfig()

        pool = if (!config.redis.pass.isNullOrBlank()) {
            JedisPool(jedisConfig, config.redis.address, config.redis.port, 0, config.redis.pass?.ifBlank { null }, config.redis.ssl)
        } else JedisPool(jedisConfig, config.redis.address, config.redis.port, 0, config.redis.ssl)

        thread(name = "HyacinthHello Velocity Subscriber") {
            pool.resource.use { jedis -> jedis.subscribe(MessageListener(), config.redis.channel) }
        }
    }

    @Subscribe
    fun onShutdown(event: ProxyShutdownEvent) {
        pool.destroy()
    }

    companion object {
        lateinit var instance: HyacinthHelloVelocity

        lateinit var server: ProxyServer
        lateinit var logger: Logger
        lateinit var dataDirectory: Path
        lateinit var metricsFactory: Metrics.Factory

        lateinit var config: Config

        lateinit var pool: JedisPool
    }
}
