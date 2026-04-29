# Secret Hygiene

## Rules

1. **Never commit Maven Central tokens.** Use GitHub Actions secrets for `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`.
2. **Never commit GPG private keys.** Use the `GPG_PRIVATE_KEY` GitHub Actions secret. See [docs/MAVEN_CENTRAL.md](MAVEN_CENTRAL.md).
3. **Never commit API keys, passwords, or credentials** of any kind.
4. **Use GitHub Actions secrets** for all sensitive values in workflows.
5. **If a secret is accidentally committed**, rotate it immediately and audit access.

## Expected Release Secrets

Documented in [docs/MAVEN_CENTRAL.md](MAVEN_CENTRAL.md):

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`

## `.toCurl()` and Secret Exposure

KissRequests' `.toCurl()` method intentionally **does not mask secrets** in v1. This means Authorization headers, tokens, and sensitive body content appear in plain text in the curl output.

This is a **conscious KISS design decision**, not a security feature. The purpose of `toCurl()` is debugging, and masking would reduce its usefulness.

**Applications consuming this library are responsible for:**

- Not logging `toCurl()` output in production if it contains sensitive data.
- Not storing curl output in files or monitoring systems that lack access controls.
- Using appropriate log-level filtering for debug output.

## GitHub Secret Scanning

GitHub provides automatic secret scanning for public repositories. Ensure it is enabled in the repository settings under **Security → Code security and analysis**.

## Pre-Commit Checklist

Before committing:

- [ ] No tokens or keys in the diff.
- [ ] No `.env` files committed.
- [ ] No hardcoded passwords or credentials.
- [ ] GitHub Actions workflows reference secrets, not plaintext values.
