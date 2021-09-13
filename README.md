# k6-kotlin

> Embedding k6 test-infrastructure into your JVM projects.

## TL;DR
This project wires everything you need to run load tests done by [k6](https://k6.io) from you JVM project, 
e.g., as a jUnit test during you gradle or maven build or triggered programmatically from anywhere in your codebase. 
It automatically spins-up everything you need to run a load test using k6 (like an influxDB, grafana as an UI for your results, as well as k6 itself) via docker.
Therefore, it is mandatory to have a running [docker installation](https://docs.docker.com/engine/install/) on your system.

## How to use
tbd

## Why [k6](https://k6.io)?
k6 is a developer-centric, free and open-source load testing tool built for making performance testing a productive and enjoyable experience.
Using k6, you'll be able to catch performance regression and problems earlier, allowing you to build resilient systems and robust applications.

### Use cases
[k6](https://k6.io) users are typically Developers, QA Engineers, and DevOps. They use k6 for testing the performance of APIs, microservices, and websites. Common k6 use cases are:

#### Load testing
[k6](https://k6.io) is optimized for minimal consumption of system resources. It’s a high-performance tool designed for running tests with high load (spike, stress, soak tests) in pre-production and QA environments.

#### Performance monitoring
[k6](https://k6.io) provides great primitives for performance testing automation. You could run tests with a small amount of load to continuously monitor the performance of your production environment.

