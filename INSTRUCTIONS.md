# instructions.md

Read this file before making any changes.

## Core Principles

1. Keep the codebase as simple as possible. Remove unnecessary abstractions, duplication, and dead code.
2. Implement the root-cause fix. Do not add workarounds, hacks, or temporary patches.
3. Preserve existing functionality unless explicitly instructed otherwise.
4. Prioritize correctness over speed of implementation.
5. Keep implementations maintainable and production-ready.

## Code Quality

1. Write clean, readable, and consistent code.
2. Follow the existing project structure and coding style.
3. Avoid unnecessary dependencies.
4. Never leave unused variables, imports, or functions.
5. Never add comments unless explicitly requested.

## Reliability

1. Handle all errors explicitly.
2. Review logs before making changes and resolve the underlying issue.
3. Consider thread safety, race conditions, deadlocks, and resource leaks where applicable.
4. Validate inputs and handle edge cases.
5. Avoid introducing breaking changes.

## Security

1. Never expose secrets, tokens, API keys, or credentials.
2. Validate and sanitize all external input.
3. Apply the principle of least privilege.
4. Prevent common vulnerabilities such as injection, path traversal, insecure deserialization, and authentication or authorization flaws.

## Performance

1. Avoid unnecessary allocations, queries, and computations.
2. Optimize only when there is measurable benefit.
3. Prefer simple and efficient algorithms.

## Before Finishing

1. Ensure the project builds successfully.
2. Resolve all compiler errors and relevant warnings.
3. Verify that the requested change works as intended.
4. Do not modify unrelated code.
