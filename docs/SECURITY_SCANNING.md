# Security Scanning

## Overview

KissRequests uses free, GitHub-native and open-source tooling for security scanning. No accounts, tokens, or paid services are required.

## What Runs Automatically

| Tool | Trigger | Where |
|---|---|---|
| CodeQL | Push to main, PR to main, weekly schedule | GitHub Actions |
| Dependabot | Weekly | GitHub (automatic PRs) |
| CI build | Every push and PR | GitHub Actions |

## CodeQL

CodeQL is a static analysis tool by GitHub that scans Java code for security vulnerabilities.

- **Workflow**: `.github/workflows/codeql.yml`
- **Triggers**: push to `main`, pull request to `main`, weekly schedule (`Monday 06:00 UTC`), manual dispatch.
- **Results**: Visible in the repository's **Security → Code scanning alerts** tab.
- **No secrets required**.

## Dependabot

Dependabot monitors Maven dependencies and GitHub Actions for known vulnerabilities and outdated versions.

- **Config**: `.github/dependabot.yml`
- **Ecosystems**: Maven, GitHub Actions.
- **Schedule**: Weekly.
- **Grouping**: Minor and patch updates are grouped to reduce PR noise.
- **Results**: Dependabot creates pull requests automatically. Alerts appear in **Security → Dependabot alerts**.
- **No secrets required**.

## OWASP Dependency-Check

OWASP Dependency-Check scans project dependencies for known vulnerabilities using the NVD database.

### Local Command

```bash
mvn -Psecurity verify
```

This runs the full build plus dependency vulnerability scanning. The first run downloads the NVD database and may take several minutes.

### Normal Build (No Security Scan)

```bash
mvn -B verify
```

The OWASP scan does **not** run during normal builds. It is explicitly activated via the `security` profile.

### CI Command

To run in CI (if desired):

```bash
mvn -Psecurity verify
```

This is **not** wired into the default CI workflow to keep normal builds fast.

### Reading Reports

Reports are generated in:

```
target/dependency-check-report/
```

- `dependency-check-report.html` — Human-readable report.
- `dependency-check-report.json` — Machine-readable report.

### False Positives

OWASP Dependency-Check may produce false positives, especially for libraries with similar names to vulnerable projects. Review findings carefully before acting.

To suppress a false positive, add a suppression file and configure it in the `security` profile in `pom.xml`. Document the reason for each suppression.

### Build Failure Threshold

The `security` profile is configured with `failBuildOnCVSS` set to 11 (effectively disabled). This means the build will not fail on findings by default. To enforce a threshold, lower this value:

- `failBuildOnCVSS=7` — Fail on high-severity findings.
- `failBuildOnCVSS=4` — Fail on medium-severity and above.

Document any threshold change in this file.

## Dependency Policy

- **Zero production dependencies** in the core library.
- JUnit 5 is a test-scope dependency only.
- Build plugins (Maven, OWASP, GPG) are not shipped in the artifact.
- Do not add production dependencies to satisfy a scanner.

## Secrets Policy

See [docs/SECRET_HYGIENE.md](SECRET_HYGIENE.md) and [docs/MAVEN_CENTRAL.md](MAVEN_CENTRAL.md).

## Snyk (Optional Manual Usage)

Snyk is a commercial dependency vulnerability scanner. It is **not** wired into CI and is **not** required.

If a maintainer wants to use Snyk manually:

1. Install the Snyk CLI.
2. Authenticate with `snyk auth`.
3. Run `snyk test` or `snyk monitor` locally.

This requires a Snyk account and is entirely optional.

## Limitations

- Security scanning does not replace code review.
- CodeQL analyzes Java source code but may not catch all issues.
- OWASP Dependency-Check only scans declared dependencies — it does not detect vulnerabilities in the JDK itself.
- Dependabot only monitors declared dependencies in `pom.xml` and GitHub Actions.
- No tool detects all vulnerabilities. Use judgment.
