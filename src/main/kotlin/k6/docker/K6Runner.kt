package k6.docker

import K6Dsl
import LoadTestEnvironmentConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.output.WaitingConsumer
import org.testcontainers.utility.DockerImageName

inline val <reified T> T.logger: Logger
    get() = LoggerFactory.getLogger(T::class.java)

class K6Runner(
    val databaseConnection: DatabaseConnection,
    private val config: K6RunnerConfig = K6RunnerConfig()
) : GenericContainer<K6Runner>(DockerImageName.parse("${config.image}:${config.version}")) {
    init {
        withNetworkAliases(config.networkAlias)
        withClasspathResourceMapping(config.resourcePath, "/sources/", BindMode.READ_ONLY)
        withCommand("run", "/sources/load.test.js")
    }

    fun followOutputUntilTestFinished() {
        val waitingConsumer = WaitingConsumer()
        followOutput(Slf4jLogConsumer(logger).andThen(waitingConsumer))
        waitingConsumer.waitUntil { it.utf8String.contains("data_received......") }
    }
}

@K6Dsl
data class K6RunnerConfig(
    var image: String = "loadimpact/k6",
    var version: String = "0.28.0",
    var networkAlias: String = "k6",
    var resourcePath: String = "./k6-tests",
)

@K6Dsl
fun LoadTestEnvironmentConfig.runner(init: K6RunnerConfig.() -> Unit) {
    val config = K6RunnerConfig().also(init)
    dbConnection?.let { db ->
        K6Runner(
            databaseConnection = db,
            config = config
        ).apply {
            withNetwork(dockerNetwork)
            withEnv("K6_OUT", "influxdb=${databaseConnection.dbUrl}")
            start()
        }.followOutputUntilTestFinished()
    }
}
