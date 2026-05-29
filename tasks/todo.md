# Task: Add LuckPerms separate permissions and bypass configuration

## Plan
- [x] Implement config option `useSeparatePremission = false` (default false) inside `PluginConfig` and expose in `ConfigManager` <!-- id: 0 -->
- [x] Centralize permission check helpers `checkPermission` and `checkMenuPermission` inside `GuiHelper` <!-- id: 1 -->
- [x] Apply permission checks to all GUI open entry points (`MainMenuGUI`, `ClaimSettingsGUI`, `MemberManagementGUI`, `RoleManagementGUI`, `TrustManagementGUI`, `VisitorSettingsGUI`, `AllyManagementGUI`, `ClaimMapGUI`, `WarpManagementGUI`, `ProfileSelectorGUI`) <!-- id: 2 -->
- [x] Apply permission checks to all subcommands in `ClaimCommand`, `MemberCommand`, `TrustCommand`, `AllyCommand`, `AbandonCommand`, `UnclaimCommand`, `UnstuckCommand` <!-- id: 3 -->
- [x] Register new permissions in `plugin.yml` <!-- id: 4 -->
- [x] Verify compilation succeeds with Maven <!-- id: 5 -->
- [x] Document all new permissions in `docs/guide/commands.md` <!-- id: 6 -->
- [x] Document the new config option in `docs/CONFIGURATION.md` <!-- id: 7 -->
- [x] Create logical git commits for the changes <!-- id: 8 -->

# Follow-up: Make config more understandable and remove unused things
- [x] Clean up `PluginConfig.java` by removing unused fields (`claimWandItem`, `autoClaimDefault`, `autoUnclaimDefault`) <!-- id: 9 -->
- [x] Add detailed, readable comments/descriptions to all configuration fields in `PluginConfig.java` <!-- id: 10 -->
- [x] Remove unused settings from `docs/CONFIGURATION.md` and update documentation for clarity <!-- id: 11 -->
- [x] Verify compilation succeeds with Maven <!-- id: 12 -->
- [x] Commit the changes <!-- id: 13 -->

# Follow-up: Fix ActionBar (hotbar) not updating on unclaim/abandon/trust/untrust
- [x] Track relationship status and claim status changes in `EventListener` (`lastPlayerStatusMap` and `lastClaimStatusMap`) <!-- id: 14 -->
- [x] Refresh ActionBar if chunk position, claim status, or player status changes <!-- id: 15 -->
- [x] Call `updatePlayerClaimCache` inside `AbandonCommand` <!-- id: 16 -->
- [x] Verify compilation succeeds with Maven <!-- id: 17 -->
- [x] Commit the changes <!-- id: 18 -->

# Follow-up: Fix trust addition in TrustManagementGUI bypassing player consent
- [x] Route GUI trust addition through the standard `sendTrustInvite` invitation flow instead of direct addition <!-- id: 19 -->
- [x] Verify compilation succeeds with Maven <!-- id: 20 -->
- [x] Commit the changes <!-- id: 21 -->

# Follow-up: Add Banned Claim Names system and banned-claim-name.txt
- [x] Add `bannedClaimNamesFile` option inside `PluginConfig.java` <!-- id: 22 -->
- [x] Implement loading and validation logic in `ConfigManager.java` <!-- id: 23 -->
- [x] Check for banned claim names during profile creation and renaming inside `ClaimCommand.java` and `RenameClaimGUI.java` <!-- id: 24 -->
- [x] Add default `banned-claim-name.txt` file inside `src/main/resources` and load/create it dynamically on startup <!-- id: 25 -->
- [x] Document the new config option inside `docs/CONFIGURATION.md` and add the `bannedClaimName` message to `MessagesConfig.java` <!-- id: 26 -->
- [x] Verify compilation succeeds with Maven <!-- id: 27 -->
- [x] Commit the changes <!-- id: 28 -->

# Follow-up: Add Tab-Completion for /claim leave command
- [x] Create `ClaimLeaveSuggestions.java` suggesting claims a player belongs to <!-- id: 29 -->
- [x] Register suggestion provider in `/claim leave` subcommand inside `ClaimCommand.java` <!-- id: 30 -->
- [x] Verify compilation succeeds with Maven <!-- id: 31 -->
- [x] Commit the changes <!-- id: 32 -->

## Review
- [x] Tab-completion for `/claim leave` suggests exact leavable claims.
- [x] Project builds successfully.
- [x] Changes are logically committed and pushed.

# Follow-up: Render custom claim colors in placeholders, action bar, and menus
- [x] Enter Fast mode for implementation & verification <!-- id: 33 -->
- [x] Update `%landclaim_profile%` PAPI placeholder in `LandClaimExpansion.java` to use colored name <!-- id: 34 -->
- [x] Update event listener action bar & enter/leave title owner formatting in `EventListener.java` <!-- id: 35 -->
- [x] Update GUI menu names & owner names in claim GUI menus (MainMenuGUI, ClaimSettingsGUI, etc.) to use colored name/owner <!-- id: 36 -->
- [x] Verify that building compiles successfully via Maven <!-- id: 37 -->
- [ ] Document results in tasks/todo.md and commit changes <!-- id: 38 -->

