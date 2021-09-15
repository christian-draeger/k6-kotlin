package k6

import k6
import k6.docker.grafana
import k6.docker.influxDB
import k6.docker.runner
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LoadTestEnvironmentTest {

    @Test
    @Disabled
    internal fun `run load test with default infra settings`() {
        val defaultK6Infra = k6
        defaultK6Infra.start
    }

    @Test
    @Disabled
    internal fun `run load test with custom infra settings`() {
        k6 {
            influxDB {
                image = "influxdb"
                version = "1.8.4-alpine"
                // TODO: fix db name override
                // dbName = "fancy-name"
                networkAlias = "my-influx"
                internalPort = 8086
            }
            grafana {
                image = "grafana/grafana"
                version = "latest"
                internalPort = 3000
                networkAlias = "my-grafana"
                // autoOpen = true
            }
            runner {
                image = "loadimpact/k6"
                version = "0.28.0"
                networkAlias = "my-k6"
                resourcePath = "k6-tests"
            }
        }
    }
}
