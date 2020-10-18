package docker

import influxVersion
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class K6InfluxDB(
    version: String = influxVersion,
    dbName: String = "k6",
    val internalPort: Int = 8086,
    val networkAlias: String = "influx"

) : GenericContainer<K6InfluxDB>(DockerImageName.parse("influxdb:$version")) {
    init {
        withNetworkAliases(networkAlias)
        withExposedPorts(internalPort)
        withEnv("INFLUXDB_DB", dbName)
    }

    val connection: InfluxConnection by lazy {
        InfluxConnection(
            networkAlias = networkAlias,
            internalPort = internalPort,
            dbName = dbName,
        )
    }
}

data class InfluxConnection(
    val networkAlias: String,
    val internalPort: Int,
    val dbName: String,
    val dbUrl: String = "http://$networkAlias:$internalPort/$dbName"
)

