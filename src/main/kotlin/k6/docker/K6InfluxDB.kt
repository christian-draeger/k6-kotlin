package k6.docker

import K6Dsl
import LoadTestEnvironmentConfig
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class K6InfluxDB(
    private val config: InfluxDbConfig = InfluxDbConfig()
) : GenericContainer<K6InfluxDB>(DockerImageName.parse("${config.image}:${config.version}")) {
    init {
        withNetworkAliases(config.networkAlias)
        withExposedPorts(config.internalPort)
        withEnv("INFLUXDB_DB", config.dbName)
    }

    val connection: DatabaseConnection by lazy {
        DatabaseConnection(
            networkAlias = config.networkAlias,
            internalPort = config.internalPort,
            dbName = config.dbName,
        )
    }
}

data class DatabaseConnection(
    val networkAlias: String,
    val internalPort: Int,
    val dbName: String,
    val dbUrl: String = "http://$networkAlias:$internalPort/$dbName"
)

@K6Dsl
data class InfluxDbConfig(
    var image: String = "influxdb",
    var version: String = "1.8.4",
    var dbName: String = "k6",
    var internalPort: Int = 8086,
    var networkAlias: String = "influx"
)

@K6Dsl
fun LoadTestEnvironmentConfig.influxDB(init: InfluxDbConfig.() -> Unit): K6InfluxDB {
    val config = InfluxDbConfig().also(init)
    return K6InfluxDB(config).apply {
        withNetwork(dockerNetwork)
        start()
        this@influxDB.dbConnection = connection
    }
}
