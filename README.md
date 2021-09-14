# k6-kotlin

> Embedding k6 test-infrastructure into your JVM projects.

## TL;DR
This project wires everything you need to run load tests done by [k6](https://k6.io) from you JVM project, 
e.g., as a jUnit test during you gradle or maven build or triggered programmatically from anywhere in your codebase. 
It automatically spins-up everything you need to run a load test using k6 (like an influxDB, grafana as an UI for your results, as well as k6 itself) via docker.
Therefore, it is mandatory to have a running [docker installation](https://docs.docker.com/engine/install/) on your system.

## Setup
**important: It's mandatory to have a running Docker installation to use this library.**

### Add the dependency to your project:

```kotlin
implementation("io.github.christian-draeger:k6-kotlin:0.1.0")
```

## Usage
Place valid k6 load test scenarios (usually js files) under `resources/k6-tests` in your project.

Start the k6 infrastructure and run all the test scenarios that have been placed  in `resources/k6-tests`:
```kotlin
// info: you could call it from wherever you want, even your production code. it doesn't necessarily gets called inside a test
@Test
fun `run load test with default infra settings`() {
    val defaultK6Infra = k6
    defaultK6Infra.start
}
```

### Result output
#### Console
The result of the k6 runner will be piped to STDOUT (updated in real time):
![terminal-output](./terminal-output.gif)

#### Grafana
Since the setup will spin-up a grafana that is preconfigured to connect to the influxDB where the k6 runner results are stored it is possible to watch the results via grafana during a running k6 load test scenario (by default the current results will be updated every 5 seconds):
![grafana-output](./grafana-output.gif)

### Customize Setup 
It is possible to customize most of the crucial config parameters that are used to spin-up a working k6 test environment.
```kotlin
// info: you could call it from wherever you want, even your production code. it doesn't necessarily gets called inside a test
@Test
internal fun `run load test with custom inbfra settings`() {
    k6 {
        influxDB {
            image = "influxdb" // the docker image that will be used
            version = "1.8.4-alpine" // the docker images version
            dbName = "fancy-name"
            networkAlias = "influx" // service alias inside the docker network
            internalPort = 8086 // port used inside of the docker network
            // external port is dynamically allocated and will be some free port
        }
        grafana {
            image = "grafana/grafana" // the docker image that will be used
            version = "latest" // the docker images version
            internalPort = 3000 // port used inside of the docker network
            networkAlias = "grafana" // service alias inside the docker network
        }
        runner {
            image = "loadimpact/k6" // the docker image that will be used
            version = "0.28.0" // the docker images version
            networkAlias = "k6" // service alias inside the docker network
            resourcePath = "./k6-tests" // location of the test scenarios relative to the src/(main|test)/resources folder
        }
    }
}
```

## Why [k6](https://k6.io)?
k6 is a developer-centric, free and open-source load testing tool built for making performance testing a productive and enjoyable experience.
Using k6, you'll be able to catch performance regression and problems earlier, allowing you to build resilient systems and robust applications.

### Use cases
[k6](https://k6.io) users are typically Developers, QA Engineers, and DevOps. They use k6 for testing the performance of APIs, microservices, and websites. Common k6 use cases are:

#### Load testing
[k6](https://k6.io) is optimized for minimal consumption of system resources. It’s a high-performance tool designed for running tests with high load (spike, stress, soak tests) in pre-production and QA environments.

#### Performance monitoring
[k6](https://k6.io) provides great primitives for performance testing automation. You could run tests with a small amount of load to continuously monitor the performance of your production environment.

## Development
bump dependencies:

    ./gradlew dependencyUpdates

bump gradle version:

    ./gradlew wrapper --gradle-version 7.0.2

run tests with k6 result output visible:

     ./gradlew clean build -i
