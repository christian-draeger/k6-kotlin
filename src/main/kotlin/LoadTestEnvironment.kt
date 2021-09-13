import docker.K6Grafana
import docker.K6InfluxDB
import docker.K6Runner
import org.testcontainers.containers.Network
import org.testcontainers.containers.Network.newNetwork

const val influxVersion = "1.8.4-alpine"
const val k6Version = "0.28.0"

class LoadTestEnvironment {

    fun start() {
        val k6DockerNetwork: Network = newNetwork()
        val influx = K6InfluxDB().apply {
            withNetwork(k6DockerNetwork)
            start()
        }
        val grafana = K6Grafana(influx.connection).apply {
            withNetwork(k6DockerNetwork)
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
