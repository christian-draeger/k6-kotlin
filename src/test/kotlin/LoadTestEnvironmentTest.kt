import docker.grafana
import docker.influxDB
import docker.runner
import org.junit.jupiter.api.Test

class LoadTestEnvironmentTest {

    @Test
    internal fun `run load test with default infra settings`() {
        val defaultK6Infra = k6
        defaultK6Infra.start
    }

    @Test
    internal fun `run load test with custom infra settings`() {
        k6 {
            influxDB {
                image = "influxdb"
                version = "1.8.4-alpine"
                dbName = "fancy-name"
                networkAlias = "my-influx"
                internalPort = 8086
            }
            grafana {
                image = "grafana/grafana"
                version = "latest"
                internalPort = 3000
                networkAlias = "my-grafana"
            }
            runner {
                image = "loadimpact/k6"
                version = "0.28.0"
                networkAlias = "my-k6"
                resourcePath = "./k6-tests"
            }
        }
    }
}
