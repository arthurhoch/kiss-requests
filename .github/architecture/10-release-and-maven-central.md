# 10 — Release and Maven Central

This document defines the release process and Maven Central publishing plan for KissRequests.

## Maven Coordinates

- **groupId**: `io.github.arthurhoch`
- **artifactId**: `kiss-requests`
- **Version scheme**: Semantic versioning (MAJOR.MINOR.PATCH)
- **Initial release version**: `0.1.0`

## Sonatype Central Publisher Portal

KissRequests uses the [Sonatype Central Publisher Portal](https://central.sonatype.org/publish/publish-portal/) for publishing to Maven Central.

### Prerequisites

1. Sonatype Central Portal account.
2. Namespace verified for `io.github.arthurhoch`.
3. GPG key pair for signing.
4. Central Portal token (username/password).

### Required Secrets (GitHub Actions)

| Secret | Description |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal token username. |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal token password. |
| `GPG_PRIVATE_KEY` | ASCII-armored GPG private key. |
| `GPG_PASSPHRASE` | Passphrase for the GPG private key. |

### Setup Steps

1. Create a Sonatype Central Portal account at https://central.sonatype.com.
2. Verify the `io.github.arthurhoch` namespace.
3. Generate a GPG key pair: `gpg --full-generate-key`.
4. Export the private key: `gpg --armor --export-secret-keys KEY_ID`.
5. Export the public key to a keyserver: `gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID`.
6. Generate a Central Portal token in the account settings.
7. Add the four secrets to the GitHub repository settings.

## Maven Configuration

### Source JAR

The `maven-source-plugin` creates a source JAR automatically:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-source-plugin</artifactId>
    <executions>
        <execution>
            <id>attach-sources</id>
            <goals>
                <goal>jar-no-fork</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Javadoc JAR

The `maven-javadoc-plugin` creates a Javadoc JAR automatically:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <executions>
        <execution>
            <id>attach-javadocs</id>
            <goals>
                <goal>jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### GPG Signing

GPG signing is configured under the `release` profile:

```xml
<profile>
    <id>release</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

### Central Publishing Plugin

The `central-publishing-maven-plugin` publishes to Maven Central under the `release` profile:

```xml
<profile>
    <id>release</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.sonatype.central</groupId>
                <artifactId>central-publishing-maven-plugin</artifactId>
                <extensions>true</extensions>
                <configuration>
                    <publishingServerId>central</publishingServerId>
                    <autoPublish>true</autoPublish>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

### Maven settings.xml

The GitHub Actions workflow must configure `settings.xml` with the Central Portal credentials:

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>${env.MAVEN_CENTRAL_USERNAME}</username>
            <password>${env.MAVEN_CENTRAL_PASSWORD}</password>
        </server>
    </servers>
</settings>
```

## Release Process

### Pre-Release Checklist

1. All tests pass: `mvn -B verify`.
2. CHANGELOG.md updated with the release version.
3. Documentation updated for all public behavior changes.
4. Version in pom.xml is the release version (`0.1.0` for the first release).
5. Commit the release-ready state.
6. Tag the commit: `git tag v0.1.0`.
7. Push the tag: `git push origin v0.1.0`.

### Release Workflow

The GitHub Actions release workflow (`.github/workflows/release-maven-central.yml`) is triggered by tags matching `v*`.

Steps:
1. Checkout the repository.
2. Set up Java 17.
3. Cache Maven dependencies.
4. Run `mvn -B verify` (tests must pass).
5. Import GPG key.
6. Run `mvn -B deploy -P release` (builds source/javadoc jars, signs, publishes).
7. The Central Publisher Portal receives and publishes the artifacts.

### Post-Release

1. Verify the artifact appears on Maven Central (may take a few minutes to hours).
2. Update the version in pom.xml to the next `-SNAPSHOT` version.
3. Commit the snapshot version bump.

## Semantic Versioning

- **MAJOR**: Breaking public API changes.
- **MINOR**: New features, backward-compatible.
- **PATCH**: Bug fixes, backward-compatible.

## Changelog

- CHANGELOG.md follows [Keep a Changelog](https://keepachangelog.com/).
- Every release must have a changelog entry.
- Entries are grouped: Added, Changed, Deprecated, Removed, Fixed, Security.

## Dry-Run / Review Mode

Before the first actual publish:

1. Run `mvn -B verify -P release` locally to verify GPG signing and source/javadoc jar generation.
2. Do not deploy until all secrets and metadata are confirmed.
3. The release workflow documents the manual setup required before publishing.

## Status

**Release configuration is present.** Publishing still requires external setup:

- [ ] Sonatype Central Portal account created.
- [ ] Namespace `io.github.arthurhoch` verified.
- [ ] GPG key pair generated and public key uploaded to a keyserver.
- [ ] GitHub secrets configured: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.
- [ ] First dry-run verified locally.
