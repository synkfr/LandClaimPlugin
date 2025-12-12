# LandClaimPlugin - Update History

## Version 1.7

### 🐛 Bug Fixes
- **Fixed null pointer exception**: Added null safety checks in GUIListener title parsing
- **Fixed offline player lookup performance**: Replaced slow iteration with Bukkit's cached lookup

### ✨ New Features
- **Player Data Persistence**: Auto-claim/unclaim states and visualization modes now persist across server restarts
- **WorldGuard Integration**: Full WorldGuard API integration - claims now properly respect WorldGuard region gaps
- **New playerdata.yml**: Stores player-specific settings (auto-claim, visualization modes)

### 🔧 Optimizations
- **Cached player lookups**: GUIListener and TrustManager now use efficient cached lookups instead of iterating all offline players

### 📋 Technical Changes
- Config version bumped to 6
- Plugin version bumped to 1.7
- Improved README.md with clean, minimal documentation
- Added WorldGuard dependency (optional, soft-depend)
- Added `loadPlayerData()` and `saveAllPlayerData()` methods to CommandHandler and VisualizationManager

---

## Version 1.6

### 🐛 Bug Fixes
- **Fixed memory leak**: Player data maps now cleaned up on player quit (EventListener, CommandHandler, VisualizationManager)
- **Fixed chat exploit vulnerability**: Removed vulnerable `onPlayerChat` handler that could be exploited
- **Fixed typos in messages.yml**: "Memeber" → "Member", "Premissions" → "Permissions"
- **Fixed hardcoded messages**: CommandHandler now uses config for all messages
- **Fixed soft-depend syntax**: Changed `soft-depend: WorldGuard\`` to `soft-depend: [WorldGuard]` in plugin.yml
- **Added CONTAINER permission**: Separate permission check for chests, furnaces, barrels, etc.

### 🔧 Optimizations
- **Thread Safety**: Replaced HashMap with ConcurrentHashMap in ClaimManager, TrustManager, and VisualizationManager
- **Memory Management**: Added comprehensive player data cleanup on quit via PlayerJoinListener
- **Separated Block Types**: Container blocks and interactable blocks now have separate permission checks

### ✨ New Features
- **Command Aliases**: Added `/c` as alias for `/claim` and `/uc` as alias for `/unclaim`
- **Improved Tab Completion**: Now suggests online players for trust/untrust/member commands
- **CONTAINER Permission**: New permission type for accessing chests, furnaces, hoppers, etc.

### 🎨 GUI Improvements
- **TrustListGUI Overhaul**:
  - 54-slot inventory with glass pane borders
  - Online/offline status indicator (green/red) for trusted players
  - Info item showing total trusted count
  - Better organized layout with player heads in center area

- **TrustMenuGUI Overhaul**:
  - 27-slot inventory with glass pane borders
  - Player head with online status at top
  - Permission-specific icons (Bricks for BUILD, Lever for INTERACT, Chest for CONTAINER, Ender Pearl for TELEPORT)
  - Permission descriptions in lore
  - Clear enabled/disabled status indicators

- **VisitorMenuGUI Overhaul**:
  - 27-slot inventory with glass pane borders
  - Info item explaining visitor permissions
  - Warning for dangerous permissions (BUILD)
  - Same permission icons and descriptions as TrustMenuGUI

### 📋 Technical Changes
- Config version bumped to 5
- Plugin version bumped to 1.6
- Added `cleanupPlayer()` methods to EventListener, CommandHandler, and VisualizationManager
- Extended PlayerJoinListener to handle PlayerQuitEvent

---

## Version 1.5

- Initial tracked version
- Basic claim system
- Trust management
- Visualization system
- Admin commands
- WorldGuard placeholder support

---

*For full changelog, see commit history*
