# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 0.1.x | Yes |

## Reporting a Vulnerability

If you discover a security vulnerability in KissRequests, please report it responsibly:

- **Email**: arthurhoch@users.noreply.github.com
- **GitHub**: Open a private security advisory via the repository's Security tab.

Do not disclose security vulnerabilities publicly until the maintainers have had a reasonable time to respond and fix the issue.

## Dependency Policy

KissRequests has **zero production dependencies** by design. The only compile-scope dependency is the JDK itself. Test dependencies (JUnit 5) and build plugins do not ship in the published artifact.

This means the attack surface for transitive dependency vulnerabilities is minimal by architecture.

## Scope

This policy applies to the KissRequests library source code, build configuration, and release artifacts published to Maven Central.

## Response Time

We aim to acknowledge security reports within 7 days and provide a fix or mitigation within 30 days, depending on severity.
