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
- [ ] Create logical git commits for the changes <!-- id: 8 -->

## Review
- [ ] Centralized checks correctly handle separate permissions or bypass them when disabled.
- [ ] Documentation is complete.
- [ ] Changes are committed.
