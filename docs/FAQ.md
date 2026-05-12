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
