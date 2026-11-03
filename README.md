# gauge-kotlin

[English](README.md) | [日本語](README.ja.md)

> **Unofficial, community-maintained fork.**
> This project is a fork of [getgauge/gauge-java](https://github.com/getgauge/gauge-java). It is not affiliated with, endorsed by, or supported by the Gauge project or ThoughtWorks, Inc.

gauge-kotlin aims to make [Gauge](https://gauge.org/) understand step implementations written in **Kotlin** (`.kt`), on top of the existing Java language plugin.

## Status

**Work in progress. Not ready for use.**

At this point the code is the same as upstream gauge-java.

## Goals

The runtime (executing steps via reflection, hooks, gRPC, classpath handling) is reused from gauge-java as is. Only the parts that read and write source code are added in Kotlin, so that they understand `.kt` files:

- [ ] Kotlin build: compile Kotlin sources in the same Maven module as the existing Java code
- [ ] Verify that Gauge can execute Kotlin step implementations (top-level functions, `object` / `companion object`, default arguments)
- [ ] Static analysis: register `@Step` texts (including aliases) and line numbers found in `.kt` files
- [ ] Cache: reflect edits and deletions of `.kt` files in the step list
- [ ] Stub generation: generate Kotlin stubs for unimplemented steps
- [ ] Refactoring: renaming a step or changing its parameters also rewrites the `.kt` source

The package name `com.thoughtworks.gauge` is kept unchanged, so that existing step code that imports `com.thoughtworks.gauge.Step` keeps working. New Kotlin code lives in sub-packages such as `com.thoughtworks.gauge.kotlin`.

## Build from source

Requirements:

- Java 17 or later
- [Maven](https://maven.apache.org/)
- [Go](https://go.dev/) (for the build script and the launcher)
- [Gauge](https://docs.gauge.org/getting_started/installing-gauge.html) (to install and run the plugin)

```sh
# Run unit tests
mvn test

# Compile
go run build/make.go

# Install the plugin into Gauge
go run build/make.go --install
```

See [README of gauge-java](https://github.com/getgauge/gauge-java#build-from-source) for more build options.

## License

Licensed under the [Apache License, Version 2.0](LICENSE.txt).

- Original work: Copyright ThoughtWorks, Inc.
- Modifications and additions in this fork: Copyright Itsuki Yamada

Third-party dependencies are listed in [notice.md](notice.md).

"Gauge" is used only to describe compatibility. This fork does not use the Gauge logo.
