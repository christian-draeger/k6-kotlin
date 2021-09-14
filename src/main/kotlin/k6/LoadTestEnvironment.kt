import k6.docker.DatabaseConnection
import k6.docker.grafana
import k6.docker.influxDB
import k6.docker.runner
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
