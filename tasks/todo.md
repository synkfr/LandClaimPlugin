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

## Review
- [x] Unused configuration parameters are removed cleanly from source code and docs.
- [x] Detailed Okaeri comments are added to all configuration fields.
- [x] Project compiles and builds successfully.
- [x] Changes are logically committed.
