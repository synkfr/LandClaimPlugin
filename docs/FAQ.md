# Frequently Asked Questions (FAQ)

## Multi-Profile System

**Q: What is the Multi-Profile System?**
A: By default, LandClaimPlugin assigns a single claim profile to each player. However, if you enable the Multi-Profile System (`multiProfilesEnabled: true` in `config.yml`), players can create multiple independent claim profiles (e.g., a personal base and a team base).

**Q: How do players switch between their profiles?**
A: Players can use the `/claim profiles` command to open an interactive GUI. This GUI displays all the profiles they own, as well as any profiles they are a member of. Clicking on a profile sets it as their "Active Profile". All subsequent claim commands (like `/claim`, `/claim menu`, `/unclaim`) will automatically target the currently Active Profile.

**Q: What is the difference between `maxProfilesPerPlayer` and `maxMemberships`?**
A: 
- `maxProfilesPerPlayer`: This setting strictly limits the number of profiles a single player can **own** (i.e., profiles they created using `/claim create`).
- `maxMemberships`: This setting limits the number of different claims a player can **join as a member**. 
These two settings are completely independent. A player can own their maximum allowed profiles while simultaneously being a member of other players' profiles up to the `maxMemberships` limit.

**Q: If I give a player permission to own 100 chunks, can they create 2 profiles with 100 chunks each to bypass the limit?**
A: No! The claim chunk limit is a **global** limit across all of a player's owned profiles. If a player has a limit of 100 chunks and they claim 60 chunks in Profile A, they can only claim up to 40 chunks in Profile B. The system aggregates chunk counts across all owned profiles to prevent exploits.

**Q: What happens if `multiProfilesEnabled` is toggled OFF after players have already created multiple profiles?**
A: **WARNING:** Toggling this setting OFF after use is dangerous. 
If the system reverts to single-profile mode, it relies on mapping a player's UUID directly to their Profile ID. Secondary profiles created in multi-profile mode use random UUIDs. As a result, players will lose access to all secondary profiles they created. If their primary profile was previously deleted, they may lose access to all of their claims entirely. It is highly recommended to leave the setting ON once it has been used.

---

## Protection & Limits

**Q: What happens if a player breaks the last block connecting their claims?**
A: If `requireConnectedClaims` is enabled and a player breaks the block connecting their claims, the `auto-unclaim` feature (if enabled) will automatically unclaim the newly disconnected chunk to enforce the connection rules.

**Q: Are members restricted from creating their own profiles?**
A: 
- **Single-Profile Mode:** Yes, players can only be an owner OR a member. If they are a member of another claim, they cannot create their own.
- **Multi-Profile Mode:** No, players can create their own profiles even if they are already members of other players' claims (up to the `maxProfilesPerPlayer` limit).

**Q: Does `/claim unstuck` work everywhere?**
A: `/claim unstuck` only works when a player is trapped inside a claim (either their own or someone else's). It uses an asynchronous Breadth-First Search (BFS) algorithm to find the nearest safe wilderness block without teleporting them into walls or lava. It respects a configurable cooldown to prevent abuse during combat.

## Members, Trust, and Bans

**Q: What's the difference between `/claim member kick`, `/claim trust remove`, and `/claim ban`?**
A:
- **`/claim member kick`** removes the player from the claim's member list. They can still enter as a visitor (subject to the visitor flag settings).
- **`/claim trust remove`** removes the player from the trusted list. Same as kick — they fall back to visitor permissions.
- **`/claim ban`** is a hard denial. Banned players cannot enter the claim at all — the `PlayerMoveEvent` handler pushes them back to the previous chunk at the boundary. They also lose every flag check, so even if they somehow teleport in, they can't break blocks, open chests, or interact. If the banned player is online and inside the claim at the moment of the ban, the plugin teleports them out via BFS to the nearest safe wilderness.

Bans persist across server restarts. Use `/claim unban <player>` to remove a ban, and `/claim banlist` to see all current bans.

---

## Geyser / Bedrock Players

**Q: My server has Geyser/Floodgate. Do Bedrock players get a different UI?**
A: Yes. When Geyser 2.x is installed, Bedrock players receive native Bedrock forms instead of the Java-only fallbacks. This affects:
- Anvil-based text input (rename claim, change color, role names) → **CustomForm** with a text field
- `/claim abandon` confirmation → **ModalForm** with Confirm / Cancel buttons
- `/unclaim all` confirmation → **ModalForm**
- `/claim ban` confirmation → **ModalForm**

If Geyser is not installed (or `geyserForms: false` in `config.yml`), Bedrock players fall back to the Java chat-prompt flow. This is controlled by `PluginConfig.geyserForms`.

**Q: My Bedrock player can't see the claim boundary visualization.**
A: The VisualizationManager automatically switches Bedrock players to particle-based visualization instead of display-entity blocks, since Bedrock clients don't render display entities as expected. This happens transparently — no action needed from the server admin.

---

## Wilderness Protection

**Q: How do I make it so players can only build inside their claims? I want a towny / clan-style server.**
A: Set `wildernessProtection.enabled: true` in `config.yml`. When enabled, every block/entity interaction flag is denied in unclaimed chunks. Players can still walk, chat, fight, and use items, but cannot break, place, ignite, use buckets, modify signs, open containers, use doors, ride vehicles, or harm/interact with entities outside of any claim. Admins (`landclaim.admin`) always bypass.

You can exempt specific worlds (e.g. a creative build world) by adding them to `wildernessProtection.exceptionWorlds`. The list takes plain Bukkit world names, not the namespaced key — so `world` and `world_nether`, not `minecraft:overworld`.

**Q: Does wilderness protection affect PvP?**
A: No. The `PvpProtectionListener` has its own logic (claim-by-claim PvP toggle via `/claim pvp`) and wilderness protection does not change it. To disable PvP in the wilderness, use a separate anti-PvP plugin or set up a spawn-protection area via a `blockWorld` entry.

**Q: Can I let specific non-admin players bypass wilderness protection?**
A: Not in the current implementation — only `landclaim.admin` bypasses. If you need a finer-grained bypass (e.g. give builders the right to terraform the wilderness), grant them `landclaim.admin` and rely on a separate permission plugin to scope what they can do with that power.

