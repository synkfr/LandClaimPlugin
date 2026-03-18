# Development Guide

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.8+**
- **Git**

## Building

```bash
# Clone the repository
git clone https://github.com/synkfr/LandClaimPlugin.git
cd LandClaimPlugin

# Build (clean + package with shade)
mvn clean package
```

Output: `target/LandClaimPlugin-2.0.3.jar` (shaded JAR with all dependencies)

## Project Structure

```
LandClaimPlugin/
├── src/main/java/org/ayosynk/landClaimPlugin/
│   ├── commands/          — Command implementations (Cloud framework)
│   ├── config/            — Okaeri config classes
│   ├── db/                — Database layer (DAOs)
│   ├── gui/               — Inventory GUIs
│   │   └── framework/     — Reusable GUI framework
│   ├── hooks/             — Third-party plugin hooks
│   ├── listeners/         — Bukkit event listeners
│   ├── managers/          — Business logic managers
│   ├── models/            — Domain models
│   └── exceptions/        — Custom exceptions
├── src/main/resources/
│   └── plugin.yml         — Bukkit plugin descriptor
├── docs/                  — Documentation
├── pom.xml                — Maven build configuration
└── README.md              — User-facing documentation
```

## Dependencies

| Library | Version | Purpose | Scope |
|---------|---------|---------|-------|
| Paper API | 1.21.4 | Server API | provided |
| Okaeri Configs | 6.1.0-beta.1 | YAML configuration | compile |
| Cloud (incendo) | 2.0.0-beta.10 | Command framework | compile |
| HikariCP | 5.1.0 | Connection pooling | compile |
| Caffeine | 3.1.8 | In-memory caching | compile |
| Jedis | 5.1.0 | Redis client | compile |
| bStats | 3.0.2 | Metrics | compile |
| BlueMap API | 2.7.7 | Map integration | provided |
| Dynmap API | 3.7-beta-6 | Map integration | provided |
| Squaremap API | 1.3.11 | Map integration | provided |
| Pl3xMap | 1.21.5-527 | Map integration | provided |
| WorldGuard | 7.0.9 | Region protection | provided |
| DeluxeCombat API | 1.5.1 | Combat tagging | provided |
| PvPManager | 4.0.4 | Combat tagging | provided |
| EternalCombat API | 2.4.0 | Combat tagging | provided |

## Relocations (Shade)

The following packages are relocated in the shaded JAR to avoid conflicts:

| Original | Shaded |
|----------|--------|
| `org.bstats` | `org.ayosynk.landClaimPlugin.bstats` |
| `eu.okaeri` | `org.ayosynk.landClaimPlugin.lib.okaeri` |
| `com.zaxxer.hikari` | `org.ayosynk.landClaimPlugin.lib.hikari` |
| `com.github.benmanes.caffeine` | `org.ayosynk.landClaimPlugin.lib.caffeine` |
| `redis.clients.jedis` | `org.ayosynk.landClaimPlugin.lib.jedis` |

## Code Conventions

### Naming
- Managers: `*Manager.java` — Business logic coordination
- DAOs: `*Dao.java` (interface) / `SQL*Dao.java` (implementation)
- GUIs: `*GUI.java` — Inventory GUI classes
- Listeners: `*Listener.java` — Bukkit event handlers
- Models: Plain class names — `Claim`, `Role`, `Warp`, etc.

### Async Pattern
All database operations use `CompletableFuture`:
```java
public CompletableFuture<Void> saveProfile(ClaimProfile profile) {
    return CompletableFuture.runAsync(() -> {
        // Database operations here
    });
}
```

### GUI Pattern
GUIs extend `CustomGui` and declare structure via `fillFromStructure()`:
```java
public class MyGUI extends CustomGui {
    public MyGUI(Player player, SomeData data) {
        super(Component.text("Title"), 3);
        // Build GUI...
        fillFromStructure(rows, ingredients);
    }
}
```

### Command Pattern
Commands implement `LandClaimCommand`:
```java
public class MyCommand implements LandClaimCommand {
    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        manager.command(claimBuilder.literal("subcommand")
            .handler(ctx -> { /* ... */ }));
    }
}
```

### Listener Pattern
Protection listeners follow a consistent pattern:
```java
public class MyProtectionListener implements Listener {
    private final ClaimManager claimManager;

    @EventHandler
    public void onSomeEvent(SomeEvent event) {
        ChunkPosition pos = new ChunkPosition(event.getBlock().getChunk());
        ClaimProfile profile = claimManager.getProfileAt(pos);
        if (profile == null) return; // Unclaimed — allow

        if (!PermissionResolver.hasPermission(profile, player.getUniqueId(), "SOME_FLAG")) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("no-permission"));
        }
    }
}
```

## Adding a New Protection

1. Create a listener in `listeners/protections/`
2. Register it in `ListenerManager.registerAll()`
3. Add the permission flag to `ClaimProfile.setupDefaultRoles()` if needed
4. Add the flag to the permission flags documentation
5. Add GUI toggles in `VisitorSettingsGUI` and role editing GUIs

## Adding a New Command

1. Create a class implementing `LandClaimCommand` in `commands/`
2. Instantiate it in `CommandHandler` constructor
3. Add it to the `commands` list for registration

## Adding a New GUI

1. Create the GUI class in `gui/` extending `CustomGui` or `PaginatedGui`
2. Add a config class in `config/menus/` for the menu layout
3. Add eager class preloading in `CommandHandler` constructor
4. Wire navigation from parent GUIs

## Testing

The project does not include unit tests. Manual testing is done on a Paper 1.21+ server:

1. Build the plugin: `mvn clean package`
2. Copy to test server: `cp target/LandClaimPlugin-*.jar /path/to/server/plugins/`
3. Restart server
4. Test commands, GUIs, and protection events

## Common Development Tasks

### Reload config during development
```
/claim admin reload
```

### Debug claims in database
```sql
-- SQLite
SELECT * FROM lc_claim_profiles;
SELECT * FROM lc_claimed_chunks;
SELECT * FROM lc_profile_roles;
```

### View plugin logs
```bash
tail -f logs/latest.log | grep LandClaim
```

## Architecture Decisions

### Why Profile-based instead of Claim-based?
The V1 architecture used individual `Claim` objects with sub-claims. V2 consolidated to a single `ClaimProfile` per player, simplifying permission resolution, ally management, and persistence. The old `Claim` model is kept for backward compatibility during migration.

### Why Clear-and-Reinsert for saves?
Profile saves delete all related rows and re-insert them within a transaction. This is simpler than tracking diffs and ensures consistency. The trade-off is more writes per save, which is acceptable given the profile-centric save frequency.

### Why Cloud for commands?
Cloud provides type-safe command parsing, async execution support, and a clean builder API. The custom `ExecutionCoordinator` allows per-phase thread isolation (parsing on main, execution on worker pool).

### Why Caffeine for caching?
Caffeine provides high-performance concurrent caching with configurable expiry and size limits. It's significantly faster than alternatives for the read-heavy workload of claim lookups.
