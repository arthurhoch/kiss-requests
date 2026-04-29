# 11 — GitHub Pages

This document defines the GitHub Pages documentation plan for KissRequests.

## Approach

GitHub Pages serves the documentation site from the `docs/` directory on the `main` branch.

Jekyll with a minimal theme renders the markdown files into a static site. No heavy frontend frameworks.

## Configuration

### docs/_config.yml

```yaml
title: KissRequests
description: A tiny, KISS-oriented Java 17+ HTTP library.
theme: jekyll-theme-primer
markdown: kramdown
kramdown:
  input: GFM
baseurl: /kiss-requests
```

**Notes:**
- `jekyll-theme-primer` is a GitHub-native theme. It is automatically available on GitHub Pages without adding it as a dependency.
- `baseurl` should match the repository name for correct link resolution when hosted at `https://arthurhoch.github.io/kiss-requests/`.
- If the repository is published under a custom domain, `baseurl` should be empty and `url` should be set to the custom domain.

## Structure

```
docs/
├── _config.yml
├── index.md              # Entry point
├── PRODUCT_SPEC.md
├── GETTING_STARTED.md
├── API.md
├── EXAMPLES.md
├── ERROR_HANDLING.md
├── CURL_DEBUGGING.md
├── FILE_UPLOAD_DOWNLOAD.md
├── CONFIGURATION.md
├── RELEASE.md
├── MAVEN_CENTRAL.md
├── IMPLEMENTATION_PLAN.md
└── REVIEW_CHECKLIST.md
```

## Entry Point: docs/index.md

The entry point must:

1. Introduce the library.
2. Show a quick example.
3. Link to all documentation pages.
4. Provide Maven coordinates.

## Workflow

The GitHub Pages workflow (`.github/workflows/pages.yml`) deploys the site on push to `main`.

Steps:
1. Checkout the repository.
2. Set up Jekyll (using GitHub Actions Jekyll action or built-in GitHub Pages build).
3. Build the Jekyll site from `docs/`.
4. Deploy to GitHub Pages.

## Consistency

- README.md and docs/index.md must be consistent. README is the quick start; docs is the full documentation.
- API examples must be consistent across README, EXAMPLES.md, and API.md.
- When updating examples, update all locations.

## Limitations

1. No search functionality in v1 (Jekyll does not include search by default).
2. No versioned documentation in v1 (the site always shows the latest).
3. No API docs generated from Javadoc in v1 (the API reference is manual markdown).

## Future Considerations

- Add search (e.g., Algolia DocSearch) if the documentation grows large.
- Add versioned docs if multiple major versions are maintained.
- Generate API docs from Javadoc if the library grows complex.
