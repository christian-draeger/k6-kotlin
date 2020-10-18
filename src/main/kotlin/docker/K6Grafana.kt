package docker

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class K6Grafana(
    influxConnection: InfluxConnection,
    private val internalPort: Int = 3000,
) : GenericContainer<K6Grafana>(DockerImageName.parse("grafana/grafana:latest")) {
    init {
        withNetworkAliases("grafana")
        withExposedPorts(internalPort)
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
                "INFLUX_HOST" to influxConnection.networkAlias,
                "INFLUX_PORT" to "${influxConnection.internalPort}",
            )
        )
    }

    val externalPort: Int by lazy { getMappedPort(internalPort) }
    val url: String by lazy { "http://${containerIpAddress}:${externalPort}/d/k6-kotlin/load-testing-results" }
}
