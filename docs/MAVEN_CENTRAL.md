# Maven Central Publishing

**Status: Release configuration is present.** Publishing still requires external Central Portal setup, GPG signing setup, and GitHub secrets.

## Overview

KissRequests is published to Maven Central via the [Sonatype Central Publisher Portal](https://central.sonatype.org/publish/publish-portal/).

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.arthurhoch</groupId>
    <artifactId>kiss-requests</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Required GitHub Secrets

The following secrets must be configured in the GitHub repository settings before publishing:

| Secret | Description | How to Obtain |
|---|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal token username | Generate at https://central.sonatype.com → Account → Tokens |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal token password | Same as above |
| `GPG_PRIVATE_KEY` | ASCII-armored GPG private key | `gpg --armor --export-secret-keys KEY_ID` |
| `GPG_PASSPHRASE` | Passphrase for the GPG key | Set during GPG key generation |

## Setup Steps

### 1. Create Sonatype Central Portal Account

1. Go to https://central.sonatype.com.
2. Create an account.
3. Verify the namespace `io.github.arthurhoch`.
   - This typically requires proof of ownership of the GitHub account `arthurhoch`.

### 2. Generate GPG Key

```bash
gpg --full-generate-key
# Choose RSA, 4096 bits, no expiration
# Name: Arthur Hoch
# Email: (your email)

# Get the key ID
gpg --list-keys --keyid-format long

# Export the private key (this goes into GPG_PRIVATE_KEY)
gpg --armor --export-secret-keys KEY_ID

# Upload the public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### 3. Generate Central Portal Token

1. Go to https://central.sonatype.com → Account → Tokens.
2. Generate a new token.
3. Save the username and password (these go into `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`).

### 4. Configure GitHub Secrets

1. Go to the GitHub repository → Settings → Secrets and variables → Actions.
2. Add the four secrets listed above.

## Maven Configuration

### pom.xml Requirements

The `pom.xml` must include all metadata required by Maven Central:

- `name`
- `description`
- `url`
- `licenses`
- `developers`
- `scm`

### Plugins

| Plugin | Purpose |
|---|---|
| `maven-source-plugin` | Generates the source JAR. |
| `maven-javadoc-plugin` | Generates the Javadoc JAR. |
| `maven-gpg-plugin` | Signs the artifacts. Only in `release` profile. |
| `central-publishing-maven-plugin` | Publishes to Central Portal. Only in `release` profile. |

### settings.xml

The release workflow generates a `settings.xml` with the Central Portal credentials:

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

The `<id>central</id>` must match the `<publishingServerId>` in the `central-publishing-maven-plugin` configuration.

## Dry-Run

Before the first actual publish, verify locally:

```bash
# Build and test
mvn -B verify

# Build with release profile (signing will fail without GPG key, but verify the rest)
mvn -B verify -P release
```

## Verification

After publishing, verify:

1. Check the Central Portal at https://central.sonatype.com for the published artifact.
2. Search for `io.github.arthurhoch:kiss-requests` on [Maven Central](https://search.maven.org/).
3. Note: it may take a few minutes to hours for the artifact to appear.

## Current Status

- [ ] Sonatype Central Portal account created.
- [ ] Namespace `io.github.arthurhoch` verified.
- [ ] GPG key pair generated and public key uploaded.
- [ ] GitHub secrets configured.
- [ ] First dry-run verified locally.
- [ ] First release published.
