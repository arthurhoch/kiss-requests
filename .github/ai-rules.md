# AI Behavior Rules

This file contains strict rules for AI agents working on this repository.

These rules are non-negotiable. Violating them is a bug in the agent's behavior.

## Mandatory Reading

Before implementing any code, an AI agent must read:

1. `AGENTS.md` (root)
2. `docs/PRODUCT_SPEC.md`
3. `.github/architecture/index.md`

## Do Not

1. **Do not invent features outside v1 scope.** If it is not listed in the v1 scope in `AGENTS.md`, do not implement it.
2. **Do not add dependencies unless explicitly approved by a human.** Zero production dependencies is a core constraint. JUnit 5 for tests is the only exception.
3. **Do not create a framework.** This is a library. No annotations, no IoC, no plugin system, no extension points.
4. **Do not over-engineer.** When in doubt, choose the simpler solution. Simple records over complex hierarchies. Explicit names over clever abstractions.
5. **Do not remove public API without documenting why.** If a public method or class is removed, the removal must be documented in CHANGELOG.md and the relevant architecture docs.
6. **Do not silently change behavior.** If behavior changes, update docs, tests, and CHANGELOG.md.
7. **Do not skip tests.** Every public method must have at least one test.
8. **Do not skip documentation.** Every public behavior change must be reflected in docs and examples.
9. **Do not break Java 17 compatibility.** The library must compile and run on Java 17 without any flags or preview features.
10. **Do not add JSON or XML serialization.** These are v1 non-goals.
11. **Do not add framework integrations.** No Spring, Quarkus, or similar.
12. **Do not commit unless explicitly asked.** Do not push unless explicitly asked.
13. **Do not add comments to code unless explicitly asked.** Code should be self-documenting through clear naming.

## Always

1. **Always prefer simple records and classes** over complex hierarchies.
2. **Always prefer explicit names** over clever abstractions.
3. **Always preserve Java 17 compatibility.**
4. **Always update documentation** when public behavior changes.
5. **Always update tests** when behavior changes.
6. **Always update CHANGELOG.md** for user-facing changes.
7. **Always run tests after making changes** to verify nothing is broken.
8. **Always report what you changed and what remains.**
9. **Always choose the simpler solution** when in doubt.

## Change Protocol

1. Read the relevant architecture documents for the area you are changing.
2. Make changes incrementally.
3. Run tests after every change.
4. Update documentation for every public behavior change.
5. Update examples for every public API change.
6. Report what you changed and what remains.
