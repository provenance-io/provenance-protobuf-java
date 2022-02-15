# Provenance Protobuf Java Library [pb-proto]

# THIS LIBRARY IS DEPRECATED

[Use the protobindings provided in the Provenance repository](https://github.com/provenance-io/provenance/tree/main/protoBindings).

## Artifacts

### For Java

* https://search.maven.org/artifact/io.provenance/proto-java

### For Kotlin

* https://search.maven.org/artifact/io.provenance/proto-kotlin

---

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.provenance.protobuf/pb-proto-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.provenance.protobuf/pb-proto-java)
[![Latest Release][release-badge]][release-latest]
[![License][license-badge]][license-url]

[release-badge]: https://img.shields.io/github/v/tag/provenance-io/provenance-protobuf-java.svg?sort=semver
[release-latest]: https://github.com/provenance-io/provenance-protobuf-java/releases/latest

[license-badge]: https://img.shields.io/github/license/provenance-io/provenance-protobuf-java.svg
[license-url]: https://github.com/provenance-io/provenance-protobuf-java/blob/main/LICENSE

---

Provenance protos transpiled to java for gRPC.

## Installation

### Maven

```xml
<dependency>
  <groupId>io.provenance.protobuf</groupId>
  <artifactId>pb-proto-java</artifactId>
  <version>${version}</version>
</dependency>
```

### Gradle

#### Groovy

In `build.gradle`:

```groovy
implementation 'io.provenance.protobuf:pb-proto-java:${version}'
```

#### Kotlin

In `build.gradle.kts`:

```kotlin
implementation("io.provenance.protobuf:pb-proto-java:${version}")
```