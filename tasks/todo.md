# LandClaimPlugin Update Plan

## 1. Convert Chat Input to AnvilGUI
- [x] Investigate Paper 1.21.4 `MenuType.ANVIL` to create virtual anvil GUIs.
- [x] Create `AnvilInputGUI` utility class that opens an anvil, places a paper item with the prompt, and listens for the result when the player clicks the output slot.
- [x] Replace `ChatInputListener` usages in `TitleToggleGUI` and `RenameClaimGUI` with the new `AnvilInputGUI`.
- [x] Remove `ChatInputListener.java` entirely as it will no longer be needed.

## 2. Claim Land for Owner (Delegated Claiming)
- [x] Add new flag `CLAIM_LAND` to the default Member role setup and available flags (e.g., in `BlockPermission` or role management).
- [x] Update `ClaimCommand.java` `/claim` logic:
  - If a player doesn't have a profile, check if they are a member of exactly one profile (or the profile of the chunk adjacent to them, or prompt them if multiple).
  - Check if their assigned role in that profile has the `CLAIM_LAND` flag.
  - If they possess the flag, claim the chunk for the profile owner instead of denying them or creating a new profile.

## 3. Trust System Improvements (Invite & Verification)
- [x] Replace direct `/claim trust add <player>` with `/claim trust invite <player>`.
- [x] Add `/claim trust accept <owner>` and `/claim trust deny <owner>` commands.
- [x] Add a `pendingTrustInvites` map in `ClaimManager` (structured similarly to `pendingAllyInvites` or `pendingMemberInvites`).
- [x] Implement Admin Commands in `AdminCommand.java`:
  - `/claim admin trust list <owner>`: Outputs a list of all players trusted by `<owner>`.
  - `/claim admin trust who <player>`: Outputs a list of all owners who have trusted `<player>`.
