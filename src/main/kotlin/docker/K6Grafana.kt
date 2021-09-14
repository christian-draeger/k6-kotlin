package docker

import K6Dsl
import LoadTestEnvironmentConfig
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class K6Grafana(
    databaseConnection: DatabaseConnection,
    private val config: GrafanaConfig = GrafanaConfig(),
) : GenericContainer<K6Grafana>(DockerImageName.parse("${config.image}:${config.version}")) {
    init {
        withNetworkAliases(config.networkAlias)
        withExposedPorts(config.internalPort)
        withClasspathResourceMapping(
            "./grafana/dashboards.yaml",
            "/etc/grafana/provisioning/dashboards/dashboards.yaml",
            BindMode.READ_ONLY
        )
        withClasspathResourceMapping(
            "./grafana/datasources.yaml",
            "/etc/grafana/provisioning/datasources/datasources.yaml",
            BindMode.READ_ONLY
        )
        withClasspathResourceMapping(
            "./grafana/dashboards",
            "/var/lib/grafana/dashboards/",
            BindMode.READ_ONLY
        )
        withEnv(
            mutableMapOf(
                "GF_AUTH_DISABLE_LOGIN_FORM" to "true",
                "GF_AUTH_ANONYMOUS_ENABLED" to "true",
                "GF_AUTH_ANONYMOUS_ORG_ROLE" to "Admin",
                "INFLUX_HOST" to databaseConnection.networkAlias,
                "INFLUX_PORT" to "${databaseConnection.internalPort}",
                "INFLUX_DB" to databaseConnection.dbName,
            )
        )
    }

    val externalPort: Int by lazy { getMappedPort(config.internalPort) }
    val url: String by lazy { "http://${containerIpAddress}:${externalPort}/d/k6-kotlin/load-testing-results" }
}

@K6Dsl
data class GrafanaConfig(
    var image: String = "grafana/grafana",
    var version: String = "latest",
    var internalPort: Int = 3000,
    var networkAlias: String = "grafana",
)

@K6Dsl
fun LoadTestEnvironmentConfig.grafana(init: GrafanaConfig.() -> Unit) {
    val config = GrafanaConfig().also(init)
    println("###### grafana config: $config")
    dbConnection?.let { db ->
        K6Grafana(
            databaseConnection = db,
            config = config
        ).apply {
            withNetwork(dockerNetwork)
            start()
        }.also { println("grafana.url: ${it.url}") }
    }
}
