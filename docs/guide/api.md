# Public API

LandClaimPlugin exposes its core managers through the main plugin instance, allowing other plugins to integrate with the claim system.

## Getting the Plugin Instance

```java
// Get the LandClaimPlugin instance from Bukkit
LandClaimPlugin plugin = (LandClaimPlugin) Bukkit.getPluginManager().getPlugin("LandClaimPlugin");
if (plugin == null) {
    // Plugin not installed
    return;
}
```

## Checking if a Chunk is Claimed

```java
ClaimManager claimManager = plugin.getClaimManager();

// From a Bukkit Chunk
Chunk chunk = player.getLocation().getChunk();
ChunkPosition pos = new ChunkPosition(chunk);

boolean isClaimed = claimManager.isChunkClaimed(pos);
```

## Getting Claim Info at a Location

```java
ClaimManager claimManager = plugin.getClaimManager();
ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());

// Get the profile that owns this chunk (null if unclaimed)
ClaimProfile profile = claimManager.getProfileAt(pos);

if (profile != null) {
    String claimName = profile.getName();
    UUID ownerId = profile.getOwnerId();
    int chunkCount = profile.getOwnedChunks().size();
}
```

## Checking Player Permissions in a Claim

```java
ClaimProfile profile = claimManager.getProfileAt(pos);
if (profile != null) {
    UUID playerId = player.getUniqueId();

    // Check if a player has a specific permission flag
    boolean canOpenDoors = PermissionResolver.hasPermission(profile, playerId, "USE_DOORS");
    boolean canUseChests = PermissionResolver.hasPermission(profile, playerId, "USE_CONTAINERS");

    // Get the player's status in this claim
    String status = PermissionResolver.getPlayerStatus(profile, playerId);
    // Returns: "owner", "member", "trusted", "visitor", or "wilderness"
}
```

## Getting a Player's Profile

```java
// Get the profile owned by this player
ClaimProfile profile = claimManager.getProfile(player.getUniqueId());

// Get profile by display name
ClaimProfile namedProfile = claimManager.getProfileByName("MyBase");

// Check if a player can create a new profile
boolean canCreate = claimManager.canCreateProfile(player.getUniqueId());

// Get the player's effective chunk limit (includes permission overrides + bonus)
int limit = claimManager.getClaimLimit(player);
```

## Working with Warps

```java
WarpManager warpManager = plugin.getWarpManager();

// Set a warp
warpManager.setWarp(profileId, "home", player.getLocation(), Material.OAK_DOOR);

// Get a warp
Warp warp = warpManager.getWarp(profileId, "home");
if (warp != null) {
    Location loc = warp.getLocation();
}

// Delete a warp
warpManager.deleteWarp(profileId, "home");

// Get all warps for a profile
Map<String, Warp> warps = warpManager.getWarps(profileId);
```

## Combat Status Check

```java
CombatManager combatManager = plugin.getCombatManager();

// Check if a player is currently in combat
boolean inCombat = combatManager.isInCombat(player);
```

## Available Managers

| Manager | Access Method | Purpose |
|---------|--------------|---------|
| `ClaimManager` | `plugin.getClaimManager()` | Core claim operations (claim, unclaim, profiles) |
| `ConfigManager` | `plugin.getConfigManager()` | Configuration and messages |
| `CacheManager` | `plugin.getCacheManager()` | In-memory caches for profiles and players |
| `WarpManager` | `plugin.getWarpManager()` | Warp CRUD operations |
| `CombatManager` | `plugin.getCombatManager()` | Combat tag detection |
| `VisualizationManager` | `plugin.getVisualizationManager()` | Claim boundary rendering |
| `HookManager` | `plugin.getHookManager()` | Third-party plugin integrations |
| `DatabaseManager` | `plugin.getDatabaseManager()` | Direct database access (DAOs) |
| `RedisManager` | `plugin.getRedisManager()` | Cross-server pub/sub |

## Permission Flags Reference

These are the flags you can check using `PermissionResolver.hasPermission()`:

| Flag | Description |
|------|-------------|
| `USE_DOORS` | Open/close doors |
| `USE_TRAPDOORS` | Open/close trapdoors |
| `USE_FENCE_GATES` | Open/close fence gates |
| `USE_CONTAINERS` | Access chests, barrels, hoppers |
| `USE_WORKSTATIONS` | Crafting tables, anvils, etc. |
| `USE_BEDS` | Sleep in beds |
| `USE_REDSTONE` | Buttons, levers, pressure plates |
| `USE_LECTERNS` | Read lecterns |
| `USE_BELLS` | Ring bells |
| `DAMAGE_ANIMALS` | Harm passive mobs |
| `DAMAGE_MONSTERS` | Harm hostile mobs |
| `BREED_ANIMALS` | Breed animals |
| `SHEAR_ENTITIES` | Shear sheep |
| `TRADE_VILLAGERS` | Trade with villagers |
| `FEED_ANIMALS` | Feed animals |
| `LEASH_ENTITIES` | Use leads |
| `MODIFY_ARMOR_STANDS` | Edit armor stands |
| `MODIFY_ITEM_FRAMES` | Item frame interaction |
| `RIDE_VEHICLES` | Enter vehicles |
| `PLACE_VEHICLES` | Place vehicles |
| `DESTROY_VEHICLES` | Break vehicles |
| `USE_ENDER_PEARLS` | Throw ender pearls |
| `USE_CHORUS_FRUIT` | Eat chorus fruit |
| `PICKUP_ITEMS` | Pick up items |
| `DROP_ITEMS` | Drop items |

## Adding as a Dependency

### Maven

```xml
<dependency>
    <groupId>org.ayosynk</groupId>
    <artifactId>LandClaimPlugin</artifactId>
    <version>2.1.2</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml

```yaml
depend: [LandClaimPlugin]  # Hard dependency
# or
softdepend: [LandClaimPlugin]  # Optional dependency
```

::: tip
Always use `provided` scope — LandClaimPlugin will already be loaded on the server. You don't need to shade it.
:::
