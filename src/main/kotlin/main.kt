import docker.K6Grafana
import docker.K6InfluxDB
import docker.K6Runner
import org.testcontainers.containers.Network
import org.testcontainers.containers.Network.newNetwork

const val influxVersion = "latest"
const val k6Version = "0.28.0"

fun main() {
    LoadTestEnvironment().start()
}

class LoadTestEnvironment {

    fun start() {
        val k6DockerNetwork: Network = newNetwork()
        val influx = K6InfluxDB().apply {
            withNetwork(k6DockerNetwork)
            start()
        }
        val grafana = K6Grafana(influx.connection).apply {
            withNetwork(k6DockerNetwork)
            withEnv(
                mutableMapOf(
                    "INFLUX_HOST" to influx.networkAlias,
                    "INFLUX_PORT" to "${influx.internalPort}",
                )
            )
            start()
        }
        println("grafana.url: ${grafana.url}")

        K6Runner().apply {
            withNetwork(k6DockerNetwork)
            withEnv("K6_OUT", "influxdb=${influx.connection.dbUrl}")
            start()
        }.followOutputUntilTestFinished()

    }
}
