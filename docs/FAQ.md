# Frequently Asked Questions (FAQ)

## Multi-Profile System

**Q: What is the Multi-Profile System?**
A: By default, LandClaimPlugin assigns a single claim profile to each player. However, if you enable multi-profile mode (`claiming.multiProfilesEnabled: true` in `config.yml`), players can create multiple independent claim profiles such as a personal base and a team base.

**Q: What is the difference between `claiming.maxProfilesPerPlayer` and `limits.maxMemberships`?**

- `claiming.maxProfilesPerPlayer` limits how many profiles a single player can **own** via `/claim create`.
- `limits.maxMemberships` limits how many different claims a player can **join as a member**.
These two settings are independent, so a player can own up to their owned profile cap while joining other profiles up to `limits.maxMemberships`.

**Q: If I give a player permission to own 100 chunks, can they create 2 profiles with 100 chunks each to bypass the limit?**
A: No. The chunk limit is global across all profiles a player owns. If a player has a limit of 100 chunks and claims 60 in one profile, they can claim 40 across any other owned profiles.

**Q: What happens if `claiming.multiProfilesEnabled` is toggled OFF after players have created multiple profiles?**
A: Turning multi-profile mode off hides secondary profiles because single-profile mode maps players directly to their primary profile UUID. Leave `claiming.multiProfilesEnabled` enabled once players create secondary profiles.

---

## Protection & Limits

**Q: What happens if a player breaks the last block connecting their claims?**
A: If `claiming.requireConnectedClaims` is enabled and a player breaks the block connecting their claims, `/claim auto-unclaim` removes disconnected chunks according to the connection rule.

**Q: Are members restricted from creating their own profiles?**

- In single-profile mode, players can belong to only one claim as owner or member, so claim members cannot create their own profile.
- In multi-profile mode, members can still own separate profiles up to `claiming.maxProfilesPerPlayer`.

**Q: Does `/claim unstuck` work everywhere?**
A: `/claim unstuck` only works when a player is trapped inside a claim (either their own or someone else's). It uses an asynchronous Breadth-First Search (BFS) algorithm to find the nearest safe wilderness block without teleporting them into walls or lava. It respects `general.cooldownUnstuck` to prevent abuse during combat.

## Members, Trust, and Bans

**Q: What is the difference between `/claim member kick`, `/claim trust remove`, and `/claim ban`?**

- `/claim member kick` removes the player from the claim member list so they fall back to visitor permissions.
- `/claim trust remove` removes the player from the trusted list so they fall back to visitor permissions.
- `/claim ban` blocks entry at the claim border (`PlayerMoveEvent`), denies all permission flags, and teleports any online banned player currently inside the claim to the nearest safe wilderness via BFS.

Bans persist across server restarts. Use `/claim unban <player>` to remove a ban, and `/claim banlist` to list current bans.

---

## Geyser / Bedrock Players

**Q: My server has Geyser/Floodgate. Do Bedrock players get a different UI?**
A: Yes. When Geyser 2.x is installed and `general.geyserForms` is `true` in `config.yml`, Bedrock players receive native Bedrock forms instead of Java-only prompts.

- Anvil text input (rename claim, owner alias, change color, title text, role name/priority) sends a `CustomForm` text field.
- Abandon, `/unclaim all`, and `/claim ban` confirmations send a `ModalForm` dialog.

If Geyser is missing or `general.geyserForms` is `false`, Bedrock players fall back to the chat-prompt flow.

**Q: My Bedrock player can't see the claim boundary visualization.**
A: `VisualizationManager` switches Bedrock players to particle visualization instead of display entities automatically because Bedrock clients do not render Java display entities reliably.

---

## Wilderness Protection

**Q: How do I make it so players can only build inside their claims? I want a towny / clan-style server.**
A: Set `wildernessProtection.enabled: true` in `config.yml`. When enabled, the flags listed in `wildernessProtection.deniedFlags` are denied in unclaimed chunks. The **default** denied list contains only build / interaction flags (BLOCK_BREAK, BLOCK_PLACE, USE_CONTAINERS, USE_DOORS, etc.). Combat flags (DAMAGE_ANIMALS, DAMAGE_MONSTERS, BREED_ANIMALS, SHEAR_ENTITIES, TRADE_VILLAGERS, FEED_ANIMALS, LEASH_ENTITIES) and PvP are NOT in the default list — players can still hunt and fight mobs in the wilderness. Admins (`landclaim.admin`) always bypass.

You can exempt specific worlds (e.g. a creative build world) by adding them to `wildernessProtection.exceptionWorlds`. The list takes plain Bukkit world names, not the namespaced key — so `world` and `world_nether`, not `minecraft:overworld`.

**Q: I enabled wilderness protection and now players can't even attack animals. How do I let them hunt mobs?**
A: The default `deniedFlags` list deliberately excludes combat flags. If you're seeing a build where animals can't be hit, either you're on an older build (pre-2.5.0) or your config has been customized. Check the `deniedFlags` list — remove `DAMAGE_ANIMALS`, `DAMAGE_MONSTERS`, `BREED_ANIMALS`, `SHEAR_ENTITIES`, `TRADE_VILLAGERS`, `FEED_ANIMALS`, and `LEASH_ENTITIES` to restore the default mob-hunting behavior.

**Q: Does wilderness protection affect PvP?**
A: By default, no — `PVP` is NOT in the default `deniedFlags` list, so players can fight in the wilderness. The `PvpProtectionListener` has its own logic (claim-by-claim PvP toggle via `/claim pvp`) and is independent of wilderness protection. To force PvP off in the wilderness, add `PVP` to `deniedFlags`. To force PvP on in every claim regardless of the per-claim toggle, set `pvp.forceEnabled: true`.

**Q: Can I let specific non-admin players bypass wilderness protection?**
A: Not in the current implementation — only `landclaim.admin` bypasses. If you need a finer-grained bypass (e.g. give builders the right to terraform the wilderness), grant them `landclaim.admin` and rely on a separate permission plugin to scope what they can do with that power.

## PvP & Combat

**Q: How do I make it so PvP is always on and claim owners can't disable it?**
A: Set `pvp.forceEnabled: true` in `config.yml`. This makes `PvpProtectionListener` always allow PvP regardless of the per-claim `pvpEnabled` flag, and `/claim pvp` refuses to toggle (sends the `pvp-force-locked` message). Note that this is a server-wide lock — there's no per-claim or per-player granularity. The per-claim `pvpEnabled` flag and its on-disk state are left untouched; only the listener's decision changes, so toggling the config back to `false` restores normal per-claim behavior immediately.

**Q: How do I stop players from running into claims to escape PvP fights?**
A: Ensure `combatTag.combatTagBypass: true` is set in `config.yml` (enabled by default). When hooked into DeluxeCombat, CombatLogX, PvPManager, or EternalCombat, players actively tagged in combat temporarily lose claim PvP protection until their combat tag expires, preventing safe-zone abuse.

---

## Inactive Claims & Auto-Decay

**Q: How do I automatically remove abandoned claims from inactive players?**
A: Set `decay.enabled: true` in `config.yml`. Configure `decay.inactiveDays: 14` (or any desired threshold) and `decay.checkIntervalHours: 12`. The plugin will periodically scan for players offline longer than that duration and automatically unclaim their land.

**Q: Can I exempt specific players (like VIPs or staff) from claim decay?**
A: Yes! You have two ways:
1. Grant them the `landclaim.decay.exempt` permission node (or whatever you set in `decay.exemptPermission`).
2. Run `/claim admin decay exempt <player> true` from console or in-game. This flag is saved permanently in the database even across restarts. You can view all exempt players with `/claim admin decay list`.

---

## DeluxeMenus & Citizens 2 Integration

**Q: How do I let players purchase extra claim chunks using a GUI shop or an in-game NPC?**
A: Use `/claim admin add chunk <player> <amount>`.
- **With DeluxeMenus:** In your menu's click commands, execute `[console] claim admin add chunk %player_name% 1` along with item removal (`[console] clear %player_name% gold_block 30`) or Vault currency deduction (`[console] eco take %player_name% 5000`).
- **With Citizens 2:** Spawn your NPC (`/npc create "Surveyor" --type player`) and link it to your DeluxeMenus shop (`/npc command add -p "claimshop"`). If you want the NPC to grant chunks directly via console command, use `/npc command add -c "claim admin add chunk <p> 1"`. Both `<p>` (Citizens placeholder) and `@p` (Minecraft selector) are supported.

See the full [Integrations Guide](/guide/integrations) for complete copy-paste configuration examples and command setups.


