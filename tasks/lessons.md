# Lessons Learned

## Development Patterns
- **Verify Method Existence**: Always check if a method exists in the target manager/class before calling it from a GUI or Command.
- **Centralize Logic**: Business logic (like sending invitations, managing pending states) should reside in Managers (e.g., `ClaimManager`), not in Commands or GUIs. This prevents duplication and makes the code easier to maintain and access from multiple entry points.
- **Incremental Verification**: Run `mvn compile` or `mvn clean install` frequently after making changes to catch compilation errors early.

## Multi-Profile System
- **Never use `getProfile(playerId)` for claiming operations.** When multi-profiles are enabled, profile IDs are random UUIDs, NOT the player's UUID. Using `getProfile(playerId)` only finds the legacy first profile. Always use `getActiveProfile(player)` instead.
- **Never compare `profile.getProfileId().equals(player.getUniqueId())` for ownership checks.** Use `profile.isOwner(player.getUniqueId())` which checks the actual `ownerId` field.
- **When abandoning a profile**, pass `profile.getProfileId()` to `abandonProfile()`, not `playerId`. The profile ID and player ID are different in multi-profile mode.

## Config Initialization
- **Always initialize runtime-derived fields in both `loadConfigs()` and `reloadMainConfig()`.** If a field like `blockedWorlds` is only populated in `reloadMainConfig()`, it stays empty until the first reload, making it seem like the config isn't working on initial startup.
- **Okaeri `saveDefaults()` only writes missing keys**, it does NOT re-add values that already exist. If values seem to "re-add themselves", the real issue is likely that the runtime list isn't being populated from the config on startup.

## CLI vs GUI Parity
- **When adding a feature accessible via GUI, always consider adding a CLI shortcut too.** Players should be able to perform common actions both ways. Reuse the same validation logic (name patterns, uniqueness checks, permission checks) across both entry points.

## Map Hooks (BlueMap/Dynmap)
- **Never scan `Bukkit.getOfflinePlayers()` to find profiles.** Use `claimManager.getAllProfiles()` directly. The offline player scan is slow (O(n) over ALL players who ever joined), misses multi-profile data, and depends on cache lookups that may be empty.
- **Never use reflection for classes available at compile time.** If `bluemap-api` is a `provided` dependency, use its classes (`Vector2d`, `Shape.Builder`) directly. Reflection silently fails and produces zero markers with no visible error.
- **Always add debug logging to map hooks.** Log profile/chunk counts and world-matching results so users can diagnose issues from server logs.
- **BlueMap world IDs use `worldName#minecraft:dimension` format** (e.g. `earth preview#minecraft:overworld`). World matching must split on `#` and compare the base name. Also normalize spaces to underscores since BlueMap may sanitize folder names.
- **Classloader isolation with `provided` API transitive dependencies.** When a `provided` API (like `bluemap-api`) uses types from a library (`flow-math`) that the host loads internally but doesn't export: (1) Not bundling → `ClassNotFoundException`, (2) Shading + relocating → `NoSuchMethodError` (type mismatch), (3) Shading without relocation → `LinkageError` (two Class objects from different classloaders). **The only correct fix**: use reflection to load the class from the host's classloader (`Shape.class.getClassLoader().loadClass("com.flowpowered.math.vector.Vector2d")`), cache Constructor/Method handles, and invoke via reflection.
