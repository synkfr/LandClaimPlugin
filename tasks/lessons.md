# Lessons Learned

## Development Patterns
- **Verify Method Existence**: Always check if a method exists in the target manager/class before calling it from a GUI or Command.
- **Centralize Logic**: Business logic (like sending invitations, managing pending states) should reside in Managers (e.g., `ClaimManager`), not in Commands or GUIs. This prevents duplication and makes the code easier to maintain and access from multiple entry points.
- **Incremental Verification**: Run `mvn compile` or `mvn clean install` frequently after making changes to catch compilation errors early.
