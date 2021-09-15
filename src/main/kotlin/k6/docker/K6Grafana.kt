package k6.docker

import K6Dsl
import LoadTestEnvironmentConfig
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.logging.Level

class K6Grafana(
    databaseConnection: DatabaseConnection,
    private val config: GrafanaConfig = GrafanaConfig(),
) : GenericContainer<K6Grafana>(DockerImageName.parse("${config.image}:${config.version}")) {
    init {
        withNetworkAliases(config.networkAlias)
        withExposedPorts(config.internalPort)
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
        addDashboards()
    }

    val externalPort: Int by lazy { getMappedPort(config.internalPort) }
    val url: String by lazy { "http://${containerIpAddress}:${externalPort}/d/k6-kotlin/load-testing-results" }

    fun openInBrowser() {
        WebDriverManager.chromedriver().setup()
        with(ChromeDriver(chromeOptions())) {
            manage().window().maximize()
            get(url)
        }
    }

    private fun chromeOptions() = ChromeOptions()
        .addArguments("--disable-gpu")
        .addArguments("--dns-prefetch-disable")
        .addArguments("disable-infobars")
        .addArguments("--disable-dev-shm-usage")
        .merge(capabilities())

    private fun capabilities(): DesiredCapabilities {
        val capabilities = DesiredCapabilities()
        val logPrefs = LoggingPreferences().apply { enable(LogType.BROWSER, Level.ALL) }

        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs)
        capabilities.isJavascriptEnabled = true

        return capabilities
    }

    private fun addDashboards() {
        mapOf(
            "/grafana/dashboards.yaml" to "/etc/grafana/provisioning/dashboards/dashboards.yaml",
            "/grafana/datasources.yaml" to "/etc/grafana/provisioning/datasources/datasources.yaml",
            "/grafana/dashboards" to "/var/lib/grafana/dashboards/"
        ).forEach {
            withClasspathResourceMapping(it.key, it.value, BindMode.READ_ONLY)
        }
    }

}

@K6Dsl
data class GrafanaConfig(
    var image: String = "grafana/grafana",
    var version: String = "latest",
    var internalPort: Int = 3000,
    var networkAlias: String = "grafana",
    var autoOpen: Boolean = false
)

@K6Dsl
fun LoadTestEnvironmentConfig.grafana(init: GrafanaConfig.() -> Unit) {
    val config = GrafanaConfig().also(init)
    dbConnection?.let { db ->
        K6Grafana(
            databaseConnection = db,
            config = config
        ).apply {
            withNetwork(dockerNetwork)
            start()
        }.also {
            println("""
                ###############################################################
                ###                                                         ###
                ### Want to see live test results via grafana? see:         ###
                ### ${it.url} ###
                ###                                                         ###
                ###############################################################
            """.trimIndent())
            if (config.autoOpen) {
                it.openInBrowser()
            }
        }
    }
}
