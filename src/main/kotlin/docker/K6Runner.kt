package docker

import k6Version
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.output.WaitingConsumer
import org.testcontainers.utility.DockerImageName

inline val <reified T> T.logger: Logger
    get() = LoggerFactory.getLogger(T::class.java)

class K6Runner(
    version: String = k6Version,
    networkAlias: String = "k6"
) : GenericContainer<K6Runner>(DockerImageName.parse("loadimpact/k6:$version")) {
    init {
        withNetworkAliases(networkAlias)
        withClasspathResourceMapping("./tests", "/sources/", BindMode.READ_ONLY)
        withCommand("run", "/sources/load.test.js")
    }

    fun followOutputUntilTestFinished() {
        val waitingConsumer = WaitingConsumer()
        followOutput(Slf4jLogConsumer(logger).andThen(waitingConsumer))
        waitingConsumer.waitUntil { it.utf8String.contains("data_received......") }
    }
}
