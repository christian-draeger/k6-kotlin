import org.junit.jupiter.api.Test

class LoadTestEnvironmentTest {
    @Test
    internal fun `run load test with docker compose infra`() {
        LoadTestEnvironment().start()
    }
}
