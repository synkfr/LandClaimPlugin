# Architecture Overview

## High-Level Design

LandClaimPlugin follows a **modular, manager-based architecture** with clear separation of concerns. The plugin entry point (`LandClaimPlugin`) acts as a lightweight orchestrator that initializes and wires together specialized managers.

```
LandClaimPlugin (entry point)
├── ConfigManager         — Configuration loading (Okaeri)
├── DatabaseManager       — Database connection + DAO routing
├── CacheManager          — Caffeine in-memory caches
├── RedisManager          — Cross-server pub/sub sync
├── ClaimManager          — Core claim business logic
├── CombatManager         — Combat tag integration
├── VisualizationManager  — Boundary rendering
├── WarpManager           — Warp point management
├── CommandHandler        — Cloud command framework
├── ListenerManager       — Event listener registration
└── HookManager           — Third-party plugin integrations
```

## Lifecycle

### Startup (`onEnable`)
```
1. bStats metrics
2. V1 legacy migration (pre-config cleanup)
3. ConfigManager init (Okaeri YAML configs)
4. DatabaseManager init (SQLite/MySQL + DAOs + table creation)
5. CacheManager init (Caffeine caches)
6. RedisManager init (optional cross-server sync)
7. V1 → V2 SQL migration
8. Business logic managers (Combat, Claim, Visualization, Warp)
9. CommandHandler (Cloud framework + thread pool)
10. ListenerManager (all Bukkit listeners)
11. HookManager (WorldGuard, BlueMap, Dynmap, etc.)
```

### Shutdown (`onDisable`)
```
1. CommandHandler.shutdown() — drain thread pool
2. WarpManager.save() — persist warp data
3. VisualizationManager.cleanupLocalDisplays() — remove visual effects
4. DatabaseManager.shutdown() — close connections
5. RedisManager.shutdown() — close pub/sub
```

## Package Structure

```
org.ayosynk.landClaimPlugin
├── LandClaimPlugin.java          — Plugin entry point
├── commands/                     — Cloud command implementations
│   ├── CommandHandler.java       — Central command registry
│   ├── LandClaimCommand.java     — Command interface
│   ├── ClaimCommand.java         — /claim base command
│   ├── UnclaimCommand.java       — /unclaim
│   ├── AbandonCommand.java       — /claim abandon
│   ├── AdminCommand.java         — /claim admin
│   ├── MemberCommand.java        — /claim member
│   ├── TrustCommand.java         — /claim trust
│   └── AllyCommand.java          — /claim ally
├── config/                       — Okaeri configuration classes
│   ├── PluginConfig.java         — Main config.yml
│   ├── MessagesConfig.java       — Messages/locale
│   └── menus/                    — GUI menu configs (YAML)
├── db/                           — Data access layer
│   ├── Database.java             — Abstract DB interface
│   ├── DatabaseManager.java      — DB routing + lifecycle
│   ├── SQLiteDatabase.java       — SQLite implementation
│   ├── MySQLDatabase.java        — MySQL/MariaDB implementation
│   ├── ProfileDao.java           — ClaimProfile DAO interface
│   ├── SQLProfileDao.java        — SQL implementation
│   ├── ClaimDao.java             — Legacy Claim DAO interface
│   ├── SQLClaimDao.java          — Legacy SQL implementation
│   ├── PlayerDao.java            — Player data DAO interface
│   ├── SQLPlayerDao.java         — SQL implementation
│   ├── RoleDao.java              — Role DAO interface
│   ├── SQLRoleDao.java           — SQL implementation
│   ├── WarpDao.java              — Warp DAO interface
│   └── SQLWarpDao.java           — SQL implementation
├── gui/                          — Inventory GUI system
│   ├── framework/                — Reusable GUI framework
│   │   ├── CustomGui.java        — Base GUI runtime
│   │   ├── PaginatedGui.java     — Paginated list GUI
│   │   ├── GuiItem.java          — Item wrapper
│   │   ├── ClickAction.java      — Click handler interface
│   │   ├── SlotDefinition.java   — Structure-based slot def
│   │   └── GuiListener.java      — Bukkit event listener
│   ├── GuiHelper.java            — Shared GUI utilities
│   ├── MainMenuGUI.java          — Main claim menu
│   ├── ClaimSettingsGUI.java     — Claim settings
│   ├── ClaimMapGUI.java          — Interactive claim map
│   ├── TrustManagementGUI.java   — Trust player management
│   ├── MemberManagementGUI.java  — Member management
│   ├── AllyManagementGUI.java    — Ally management
│   ├── WarpManagementGUI.java    — Warp management
│   ├── RoleManagementGUI.java    — Role management
│   └── ...                       — 20+ specialized GUIs
├── hooks/                        — Third-party integrations
│   ├── map/                      — Map plugin hooks
│   └── combat/                   — Combat plugin hooks
├── listeners/                    — Bukkit event listeners
│   ├── EventListener.java        — Core claim events
│   ├── ChatInputListener.java    — Chat-based input
│   ├── CommandBlocker.java       — Block commands in claims
│   ├── PlayerJoinListener.java   — Player join handling
│   └── protections/              — Protection listeners
│       ├── BlockProtectionListener.java
│       ├── EntityProtectionListener.java
│       ├── InteractProtectionListener.java
│       ├── ExplosionProtectionListener.java
│       ├── PistonProtectionListener.java
│       ├── PvpProtectionListener.java
│       ├── VehicleProtectionListener.java
│       └── ItemProtectionListener.java
├── managers/                     — Business logic managers
│   ├── ClaimManager.java         — Claim CRUD + validation
│   ├── PermissionResolver.java   — 4-tier permission chain
│   ├── BlockPermissionResolver.java — Block → flag mapping
│   ├── CacheManager.java         — Caffeine cache wrapper
│   ├── ConfigManager.java        — Config lifecycle
│   ├── CombatManager.java        — Combat tag detection
│   ├── VisualizationManager.java — Boundary visualization
│   ├── WarpManager.java          — Warp CRUD
│   ├── RedisManager.java         — Redis pub/sub sync
│   ├── HookManager.java          — Third-party plugin hooks
│   └── ListenerManager.java      — Listener registration
├── models/                       — Domain models
│   ├── ClaimProfile.java         — Central profile (per-player)
│   ├── Claim.java                — Legacy claim model
│   ├── ClaimPlayer.java          — Player preferences
│   ├── Role.java                 — Permission role
│   ├── Warp.java                 — Warp point
│   ├── BlockPermission.java      — Block-permission enum
│   ├── ChunkPosition.java        — Immutable chunk coordinate
│   ├── ChunkSelection.java       — Multi-chunk selection
│   └── Edge.java                 — Chunk edge (for rendering)
└── exceptions/
    └── CombatBlockedException.java
```

## Core Concepts

### ClaimProfile (Central Data Model)

Each player owns exactly **one** `ClaimProfile`. This profile contains:
- **Owned chunks** — Set of `ChunkPosition` claimed by the owner
- **Roles** — Custom permission roles (Member, CoOwner, user-defined)
- **Members** — Player → Role assignments
- **Trusted players** — Per-player permission flag overrides
- **Allies** — Allied claim profiles with configurable inter-claim permissions
- **Warps** — Named teleport points within claimed chunks
- **Visitor flags** — Base permission layer for non-members
- **Settings** — Claim color, visualization mode, entry/exit titles

### Permission Chain (4-Tier Priority)

```
Owner → Role → Trusted → Ally → Visitor
```

The first matching tier **decides** — no merging occurs. See `PermissionResolver.java:21`.

| Tier | Source | Example |
|------|--------|---------|
| Owner | `profile.isOwner(playerId)` | Always `true` |
| Role | `profile.getMemberRole(playerId)` → `Role.hasFlag()` | Member, CoOwner, Custom |
| Trusted | `profile.isTrusted(playerId)` → per-player flags | Individual overrides |
| Ally | `profile.hasAlly(allyProfile)` → ally flags | Inter-claim access |
| Visitor | `profile.hasVisitorFlag(flag)` | Default for everyone else |

### Claim Validation Flow

When a player claims a chunk (`ClaimManager.claimChunk()`):

```
1. World blocked check
2. Already claimed check
3. Member/trusted elsewhere check
4. Profile creation (auto)
5. Claim limit check
6. Connected claims check (if enabled)
7. WorldGuard gap check
8. Min claim gap check
9. Add chunk → save to cache → save to DB → sync Redis
```

### Command Architecture

Commands use the **Cloud framework** with a custom `ExecutionCoordinator`:

- **Parsing**: Runs on calling thread (lightweight tokenization)
- **Suggestions**: Runs on calling thread (prevents deadlocks)
- **Execution**: Runs on dedicated 4-thread daemon pool (heavy I/O off main)

All commands extend `LandClaimCommand` interface and register via `CommandHandler`.

### GUI Framework

Custom inventory-based GUI framework (`gui/framework/`):

- `CustomGui` — Base class implementing `InventoryHolder` for reliable event routing
- `PaginatedGui` — Extends CustomGui with page-based navigation
- `SlotDefinition` — Maps structure characters to items + click handlers
- `GuiListener` — Single Bukkit listener routing clicks to correct GUI instance
- Thread-safe: `open()` schedules to main thread if called async

## Data Flow

```
Player Action
    ↓
Command (Cloud framework, async thread pool)
    ↓
ClaimManager (business logic + validation)
    ↓
CacheManager (Caffeine in-memory) → DatabaseManager (async DB writes)
    ↓                              → RedisManager (pub/sub to other servers)
HookManager (refresh map hooks)
```

## Threading Model

| Thread | Purpose |
|--------|---------|
| Main (server) | Bukkit events, inventory opens, scheduler |
| LandClaim-Command-Worker (×4) | Command execution, DB reads, GUI building |
| ForkJoinPool (async) | DAO operations (`CompletableFuture.runAsync`) |
| Redis subscriber (×1) | Pub/sub message handling |
