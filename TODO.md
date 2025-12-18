# LandClaimPlugin - TODO & Roadmap

## 🐛 Bugs & Issues

### Critical
- [x] ~~**WorldGuard Integration Incomplete** - `isTooCloseToWorldGuardRegion()` in `ClaimManager.java:158-166` returns `false` as placeholder, actual WorldGuard API integration not implemented~~ ✅ Fixed in v1.7
- [x] ~~**Offline Player Lookup Performance** - `TrustManager.java:188-195` iterates through ALL offline players which is extremely slow on large servers~~ ✅ Fixed in v1.7
- [x] ~~**Memory Leak Risk** - `lastChunkMap` and `lastActionBarMap` in `EventListener.java` never cleaned up when players leave~~ ✅ Fixed in v1.6
- [x] ~~**Chat Event Exploit** - `EventListener.java:434-448` `onPlayerChat` can be exploited by typing messages starting with `§7- §e` to trigger trust menu~~ ✅ Fixed in v1.6

### High Priority
- [x] ~~**Auto-Claim/Unclaim State Lost on Restart** - `autoClaimPlayers` and `autoUnclaimPlayers` maps in `CommandHandler.java` are not persisted~~ ✅ Fixed in v1.7
- [x] ~~**Visualization Mode Lost on Restart** - `visualizationModes` map in `VisualizationManager.java` not persisted~~ ✅ Fixed in v1.7
- [ ] **Unstuck Cooldowns Lost on Restart** - `unstuckCooldowns` map in `CommandHandler.java` not persisted (intentional - cooldowns reset on restart)
- [x] ~~**Race Condition in Claim Operations** - No synchronization on `claimedChunks` and `playerClaims` maps in `ClaimManager.java`~~ ✅ Fixed in v1.6 (ConcurrentHashMap)
- [x] ~~**Hardcoded Message** - `CommandHandler.java:317-318` and `518` use hardcoded messages instead of config~~ ✅ Fixed in v1.6

### Medium Priority
- [x] ~~**Typo in messages.yml** - Line 77: "Memeber" should be "Member"~~ ✅ Fixed in v1.6
- [x] ~~**Typo in messages.yml** - Line 88: "Premissions" should be "Permissions"~~ ✅ Fixed in v1.6
- [x] ~~**Missing Null Check** - `GUIListener.java:105` could throw NPE if title parsing fails~~ ✅ Fixed in v1.7
- [ ] **Blocked Commands Case Sensitivity** - Commands are converted to lowercase but comparison may fail with aliases
- [x] ~~**Missing CONTAINER Permission Check** - Container interactions (chests, etc.) don't have separate permission check from INTERACT~~ ✅ Fixed in v1.6

---

## 🔧 Optimizations

### Performance
- [x] ~~**Use ConcurrentHashMap** - Replace HashMap with ConcurrentHashMap for thread-safe operations in managers~~ ✅ Fixed in v1.6
- [x] ~~**Batch Particle Spawning** - `VisualizationManager.java` spawns particles one at a time; batch them for better performance~~ ✅ Fixed in v1.8
- [x] ~~**Lazy Loading for Offline Players** - Cache offline player lookups instead of iterating every time~~ ✅ Fixed in v1.7
- [x] ~~**Chunk Position Caching** - Create a chunk position pool to reduce object creation in hot paths~~ ✅ Fixed in v1.8
- [x] ~~**Action Bar Update Throttling** - `EventListener.java:84` updates every 10 ticks; consider reducing frequency~~ ✅ Fixed in v1.8 (configurable)
- [x] ~~**Edge Calculation Optimization** - Pre-calculate edges on claim/unclaim instead of on-demand in visualization~~ ✅ Fixed in v1.8 (cache invalidation)

### Memory
- [ ] **Weak References for Player Data** - Use WeakHashMap for player-specific caches that should be GC'd when player leaves
- [x] ~~**Limit Visualization Cache Size** - Add max size limit to `mergedEdgesCache` to prevent unbounded growth~~ ✅ Fixed in v1.8
- [x] ~~**Clean Up Player Data on Quit** - Add PlayerQuitEvent listener to clean up all player-specific maps~~ ✅ Fixed in v1.6

### I/O
- [x] ~~**Async File Operations** - Save claims/trust data asynchronously to prevent main thread blocking~~ ✅ Fixed in v1.8
- [x] ~~**Debounce Saves** - Don't save immediately on every trust/permission change; batch saves~~ ✅ Fixed in v1.8
- [ ] **Database Support** - Add MySQL/SQLite support for large servers

---

## ✨ New Features

### Claim Management
- [ ] **Claim Naming** - Allow players to name their claims (e.g., "Home Base", "Farm")
- [ ] **Claim Descriptions** - Add optional descriptions to claims
- [ ] **Claim Flags** - Per-claim flags (PvP, mob spawning, fire spread, etc.)
- [ ] **Claim Expiration** - Auto-unclaim after X days of inactivity
- [ ] **Claim Transfer** - Transfer claim ownership to another player
- [ ] **Claim Rent/Sell** - Economy integration for renting/selling claims
- [ ] **Claim Subdivisions** - Create sub-claims within larger claims with different permissions
- [ ] **Claim Groups** - Group multiple claims together for easier management
- [ ] **Claim Templates** - Save and apply permission templates

### Trust System
- [ ] **Trust Levels** - Multiple trust levels (Visitor, Member, Co-Owner, etc.)
- [ ] **Time-Limited Trust** - Trust that expires after a set duration
- [ ] **Trust All Claims vs Specific Claims** - Option to trust for specific claims only
- [ ] **Trust Requests** - Players can request trust from claim owners
- [ ] **Trust Notifications** - Notify owners when trusted players enter/leave claims

### Protection
- [ ] **Block-Specific Protection** - Protect specific block types (spawners, beacons)
- [ ] **Entity Protection** - Protect item frames, armor stands, paintings
- [ ] **Redstone Protection** - Prevent redstone manipulation from outside claims
- [ ] **Piston Protection** - Prevent pistons from pushing blocks into/out of claims
- [ ] **Fluid Flow Protection** - Prevent water/lava from flowing into claims
- [ ] **Chorus Fruit/Ender Pearl Protection** - Prevent teleportation into claims
- [ ] **Elytra Flight Restriction** - Option to restrict elytra in claims
- [ ] **Vehicle Protection** - Protect boats and minecarts

### Admin Features
- [ ] **Admin Bypass Mode** - Toggle admin bypass on/off
- [ ] **Claim Inspection Tool** - Wand to inspect claim info by clicking
- [ ] **Claim Statistics** - View server-wide claim statistics
- [ ] **Claim Limits by Group** - Different limits for different permission groups
- [ ] **Force Claim** - Admin ability to claim for other players
- [ ] **Claim Backup/Restore** - Backup and restore claim data
- [ ] **Audit Log** - Log all claim actions for review

### Economy Integration
- [ ] **Claim Cost** - Charge money to claim chunks
- [ ] **Claim Tax** - Recurring cost to maintain claims
- [ ] **Refund on Unclaim** - Partial refund when unclaiming
- [ ] **Vault Integration** - Support for Vault economy

### Map Integration
- [ ] **Dynmap Integration** - Show claims on Dynmap
- [ ] **BlueMap Integration** - Show claims on BlueMap
- [ ] **Pl3xMap Integration** - Show claims on Pl3xMap

---

## 🎨 GUI Improvements

### Trust List GUI (`TrustListGUI.java`)
- [ ] **Pagination** - Add page navigation for more than 54 trusted players
- [ ] **Search/Filter** - Search bar to find specific trusted players
- [ ] **Sort Options** - Sort by name, date added, online status
- [ ] **Online Status Indicator** - Show green/red indicator for online/offline
- [ ] **Remove Button** - Quick remove button without opening sub-menu
- [ ] **Add Player Button** - Button to add new trusted player via anvil input
- [ ] **Glass Pane Borders** - Add decorative borders
- [ ] **Info Item** - Show total trusted count and claim info

### Trust Menu GUI (`TrustMenuGUI.java`)
- [ ] **Better Layout** - Use 27-slot inventory with organized sections
- [ ] **Permission Descriptions** - Lore explaining what each permission does
- [ ] **Toggle All Button** - Enable/disable all permissions at once
- [ ] **Remove Trust Button** - Quick remove from this menu
- [ ] **Copy Permissions** - Copy permissions from another trusted player
- [ ] **Permission Presets** - Quick presets (Full Access, Build Only, View Only)

### Visitor Menu GUI (`VisitorMenuGUI.java`)
- [ ] **Same improvements as Trust Menu GUI**
- [ ] **Warning for Dangerous Permissions** - Warn before enabling BUILD for visitors

### New GUIs
- [ ] **Main Claim Menu** - Central hub for all claim management
- [ ] **Claim List GUI** - View all your claims with teleport options
- [ ] **Claim Settings GUI** - Per-claim settings and flags
- [ ] **Nearby Claims GUI** - Show claims near your location
- [ ] **Claim Map GUI** - Visual map of your claims using map items
- [ ] **Admin Panel GUI** - Admin-only management interface
- [ ] **Help/Tutorial GUI** - Interactive help system

### GUI Framework
- [ ] **Custom Inventory Holder** - Use custom InventoryHolder for better GUI detection
- [ ] **GUI Animation** - Add subtle animations (item glow, etc.)
- [ ] **Sound Effects** - Add click sounds for better feedback
- [ ] **Confirmation Dialogs** - Confirm destructive actions
- [ ] **Input Handling** - Anvil/Sign input for text entry

---

## 🎯 Quality of Life (QoL)

### Commands
- [ ] **Command Aliases** - Add aliases like `/c` for `/claim`, `/uc` for `/unclaim`
- [ ] **Tab Completion Improvements** - Better tab completion with player suggestions
- [ ] **Claim Here Shortcut** - `/claim here` as alias for `/claim`
- [ ] **Quick Trust** - `/trust <player>` shortcut for `/claim trust <player>`
- [ ] **Undo Command** - Undo last claim/unclaim action
- [ ] **Claim Count Command** - `/claim count` to see remaining claims

### Notifications
- [ ] **Entry/Exit Messages** - Customizable messages when entering/leaving claims
- [ ] **Title/Subtitle Support** - Show claim info as title instead of action bar
- [ ] **Boss Bar Support** - Option to show claim info in boss bar
- [ ] **Sound on Claim** - Play sound when claiming/unclaiming
- [ ] **Particle Effects on Claim** - Visual feedback when claiming

### Visualization
- [ ] **Different Colors for Different Claims** - Own claims vs trusted vs others
- [ ] **3D Visualization** - Show vertical boundaries too
- [ ] **Corner Markers** - Place visible markers at claim corners
- [ ] **Glowing Border** - Use glowing effect for boundaries
- [ ] **Visualization Height Options** - Show at feet, eye level, or ground level
- [ ] **Claim Size Display** - Show dimensions when visualizing

### Usability
- [ ] **First Claim Tutorial** - Guide new players through first claim
- [ ] **Claim Stick/Tool** - Special item for claim management
- [ ] **Right-Click to Claim** - Alternative to commands
- [ ] **Shift-Click Actions** - Quick actions with shift-click
- [ ] **Hover Text in Chat** - Clickable/hoverable messages
- [ ] **Claim Minimap** - Show nearby claims in chat as ASCII art

### Configuration
- [ ] **Per-World Settings** - Different settings for different worlds
- [ ] **Permission-Based Limits** - More granular permission nodes
- [ ] **Configurable Cooldowns** - All cooldowns in config
- [ ] **Message Placeholders** - PlaceholderAPI support
- [ ] **Disable Individual Features** - Toggle each protection type

---

## 📋 Code Quality & Maintenance

### Refactoring
- [ ] **Extract Constants** - Move magic numbers to constants
- [ ] **Builder Pattern for GUIs** - Create GUI builder for cleaner code
- [ ] **Event Priority** - Set appropriate event priorities
- [ ] **Null Safety** - Add `@Nullable`/`@NotNull` annotations
- [ ] **Logging Improvements** - Add debug logging levels

### Documentation
- [ ] **JavaDoc Comments** - Add documentation to all public methods
- [ ] **Wiki Pages** - Create GitHub wiki with setup guide
- [ ] **API Documentation** - Document public API for developers
- [ ] **Changelog** - Maintain detailed changelog

### Testing
- [ ] **Unit Tests** - Add unit tests for managers
- [ ] **Integration Tests** - Test with mock Bukkit server
- [ ] **Performance Benchmarks** - Benchmark critical operations

### API
- [ ] **Developer API** - Expose API for other plugins
- [ ] **Custom Events** - Fire events for claim/unclaim/trust actions
- [ ] **API Maven Repository** - Publish API to Maven Central

---

## 🔄 Version Compatibility

- [ ] **1.20.x Support** - Ensure compatibility with latest Minecraft
- [ ] **1.19.x Support** - Maintain backward compatibility
- [ ] **Paper/Spigot/Purpur** - Test on all major server software
- [ ] **Folia Support** - Add support for Folia (multi-threaded)

---

## 📊 Priority Matrix

| Priority | Category | Items |
|----------|----------|-------|
| 🔴 Critical | Bugs | WorldGuard integration, Memory leaks, Chat exploit |
| 🟠 High | Performance | Async saves, ConcurrentHashMap, Player data cleanup |
| 🟡 Medium | Features | Claim naming, Trust levels, Economy integration |
| 🟢 Low | QoL | Aliases, Sounds, Animations |

---

## 📝 Notes

- All GUI improvements should maintain backward compatibility with existing data
- Performance optimizations should be measured before and after
- New features should be toggleable in config
- Consider creating a separate API module for developer integration

---

*Last Updated: December 2024*
