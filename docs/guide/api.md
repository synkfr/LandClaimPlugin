# Public API

LandClaimPlugin provides a comprehensive API for external plugins to integrate with the claim system. The API includes both a direct interface and custom Bukkit events.

## Quick Start

```java
// Get the API instance
LandClaimAPI api = LandClaimAPI.getInstance();

// Check if a chunk is claimed
boolean claimed = api.isLocationClaimed(player.getLocation());

// Get claim info
ClaimProfile profile = api.getClaimAt(player.getLocation());
if (profile != null) {
    player.sendMessage("You are in " + profile.getName());
}
```

## API Interface Methods

### Claim Queries

| Method | Description |
|--------|-------------|
| `isChunkClaimed(world, x, z)` | Check if specific chunk is claimed |
| `isLocationClaimed(location)` | Check if location's chunk is claimed |
| `getClaimAt(location)` | Get ClaimProfile at location (null if wilderness) |
| `getClaimByName(name)` | Find claim by its name |
| `getClaimsByOwner(uuid)` | Get all claims owned by a player |
| `getClaimsByMember(uuid)` | Get all claims where player is a member |
| `getTotalChunksByOwner(uuid)` | Total chunks across all player's claims |

### Permission Checks

| Method | Description |
|--------|-------------|
| `hasPermission(profile, playerId, flag)` | Check if player has specific permission |
| `getPlayerStatus(profile, playerId)` | Get player status: "owner", "member", "trusted", "visitor" |
| `isOwner(profile, playerId)` | Check if player owns the claim |
| `isMember(profile, playerId)` | Check if player is a member |
| `isTrusted(profile, playerId)` | Check if player is trusted |

### Warp Operations

| Method | Description |
|--------|-------------|
| `getWarps(profileId)` | Get all warps in a claim |
| `getWarp(profileId, name)` | Get specific warp by name |

### Combat & Limits

| Method | Description |
|--------|-------------|
| `isInCombat(player)` | Check if player is in combat |
| `getClaimLimit(player)` | Get player's chunk claim limit |
| `canCreateClaim(playerId)` | Check if player can create new claim |

### Admin Operations

| Method | Description |
|--------|-------------|
| `adminClaimChunk(player, location)` | Force-claim chunk (requires admin permission) |
| `adminUnclaimChunk(player, location)` | Force-unclaim chunk (requires admin permission) |
| `addBonusBlocks(playerId, amount)` | Add/subtract bonus claim blocks |
| `getBonusBlocks(playerId)` | Get player's bonus blocks |

---

## Custom Events

LandClaimPlugin fires custom events that other plugins can listen for:

### ClaimCreateEvent

Fired when a new claim is created.

```java
@EventHandler
public void onClaimCreate(ClaimCreateEvent event) {
    System.out.println("Claim created: " + event.getClaimName());
    System.out.println("Owner: " + event.getProfile().getOwnerId());
    System.out.println("Chunk: " + event.getWorld() + " @ " + event.getChunkX() + ", " + event.getChunkZ());
}
```

### ClaimDeleteEvent

Fired when a claim is deleted.

```java
@EventHandler
public void onClaimDelete(ClaimDeleteEvent event) {
    System.out.println("Claim deleted: " + event.getClaimName());
    System.out.println("Reason: " + event.getReason()); // PLAYER_ABANDON, ADMIN_UNCLAIM, etc.
}
```

### PlayerEnterClaimEvent

Fired when a player enters a claimed chunk.

```java
@EventHandler
public void onPlayerEnterClaim(PlayerEnterClaimEvent event) {
    Player player = event.getPlayer();
    ClaimProfile claim = event.getProfile();

    if (event.isFromWilderness()) {
        player.sendMessage("Entering " + claim.getName());
    } else {
        // Player moved from another claim
        player.sendMessage("Entering " + claim.getName() + " from " + event.getFromClaim().getName());
    }
}
```

### PlayerLeaveClaimEvent

Fired when a player leaves a claimed chunk.

```java
@EventHandler
public void onPlayerLeaveClaim(PlayerLeaveClaimEvent event) {
    if (event.isToWilderness()) {
        event.getPlayer().sendMessage("Leaving " + event.getClaimName());
    }
}
```

### Registering Listeners

```java
// In your plugin's onEnable()
getServer().getPluginManager().registerEvents(new MyClaimListener(), this);
```

---

## Permission Flags Reference

These are the flags you can check using `api.hasPermission()`:

| Flag | Description |
|------|-------------|
| `USE_DOORS` | Open/close doors |
| `USE_TRAPDOORS` | Open/close trapdoors |
| `USE_FENCE_GATES` | Open/close fence gates |
| `USE_CONTAINERS` | Access chests, barrels, hoppers |
| `USE_WORKSTATIONS` | Crafting tables, anvils, etc. |
| `USE_BEDS` | Sleep in beds |
| `USE_REDSTONE` | Buttons, levers, pressure plates |
| `BLOCK_BREAK` | Break blocks |
| `BLOCK_PLACE` | Place blocks |
| `BLOCK_IGNITE` | Ignite blocks (fire, flint & steel) |
| `INTERACT_ENTITIES` | Interact with mobs |
| `HARM_ENTITIES` | Damage mobs |
| `MANAGE_MEMBERS` | Add/remove members |
| `MANAGE_ROLES` | Create/edit roles |
| `MANAGE_SETTINGS` | Change claim settings |
| `WARP_MANAGE` | Create/delete warps |
| `CLAIM_LAND` | Claim land on behalf of owner |
| `ADMIN_MENU` | Access admin menu |

---

## Adding as a Dependency

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.synkfr</groupId>
        <artifactId>LandClaimPlugin</artifactId>
        <version>2.1.3</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### plugin.yml

```yaml
depend: [LandClaimPlugin]  # Hard dependency
# or
softdepend: [LandClaimPlugin]  # Optional dependency
```

::: tip
Always use `provided` scope — LandClaimPlugin will already be loaded on the server.
:::

---

## Full API Reference

For the complete method signatures, see the source code:
- API Interface: `src/main/java/org/ayosynk/landClaimPlugin/api/LandClaimAPI.java`
- API Implementation: `src/main/java/org/ayosynk/landClaimPlugin/api/LandClaimAPIImpl.java`
- Events: `src/main/java/org/ayosynk/landClaimPlugin/api/event/`