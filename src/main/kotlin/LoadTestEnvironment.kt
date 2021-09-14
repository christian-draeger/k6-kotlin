import docker.DatabaseConnection
import docker.grafana
import docker.influxDB
import docker.runner
import org.testcontainers.containers.Network
import org.testcontainers.containers.Network.newNetwork

class LoadTestEnvironment(
    private val config: LoadTestEnvironmentConfig = LoadTestEnvironmentConfig()
) {

    val start: Unit
        get() {
            with(config) {
                influxDB {}
                grafana {}
                runner {}
            }
        }
}

@K6Dsl
data class LoadTestEnvironmentConfig(
    var dockerNetwork: Network = newNetwork(),
    var dbConnection: DatabaseConnection? = null,
    var headless: Boolean = true,
)

@K6Dsl
val k6
    get() = LoadTestEnvironment(LoadTestEnvironmentConfig())

@K6Dsl
fun k6(init: LoadTestEnvironmentConfig.() -> Unit): LoadTestEnvironment =
    with(LoadTestEnvironmentConfig().also(init)) {
        LoadTestEnvironment(this)
    }

@DslMarker
annotation class K6Dsl
