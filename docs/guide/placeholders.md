# Placeholders

LandClaimPlugin supports both **internal placeholders** (used in messages and menus) and **PlaceholderAPI** (for external integrations).

## 1. Internal Placeholders
These placeholders are used within `messages.yml` and GUI configuration files. You can use either **angle brackets** `<>` or **curly braces** `{}` interchangeably.

### Core Variables
| Placeholder | Description |
| :--- | :--- |
| `<owner>` | Name of the claim owner |
| `<player>` | Name of the target player (e.g. invited player) |
| `<name>` | Name of the claim or warp |
| `<count>` | Number of chunks or items |
| `<amount>` | Numerical amount |
| `<limit>` | Max limit for claims, warps, or profiles |
| `<gap>` | Required distance between claims |
| `<time>` | Remaining time (e.g. for temporary PvP) |
| `<seconds>` | Seconds remaining for cooldowns |

### Location & Meta
| Placeholder | Description |
| :--- | :--- |
| `<world>` | Name of the world |
| `<x>` | X coordinate |
| `<z>` | Z coordinate |
| `<id>` | Internal Claim ID |
| `<uuid>` | Player's Unique ID |
| `<mode>` | Active visualization mode |
| `<players>` | Comma-separated list of players |

---

## 2. PlaceholderAPI
To use these in external plugins, ensure you have [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed.

**Identifier:** `landclaim`

### Claim Data (Current Location)
| Placeholder | Description |
| :--- | :--- |
| `%landclaim_owner%` | Name of the claim owner at your location |
| `%landclaim_owner_uuid%` | UUID of the claim owner |
| `%landclaim_name%` | The custom name of the claim |
| `%landclaim_role%` | Your role (**Owner**, **Member**, **Visitor**) |
| `%landclaim_members%` | Number of members in the claim |
| `%landclaim_size%` | Total chunks in the current claim |
| `%landclaim_pvp%` | PvP status (**Enabled**/**Disabled**) |
| `%landclaim_is_claimed%` | Check if chunk is claimed (**Yes**/**No**) |
| `%landclaim_world%` | Current world name |
| `%landclaim_x%`, `%landclaim_z%` | Your current coordinates |

### Player Status
| Placeholder | Description |
| :--- | :--- |
| `%landclaim_profile%` | Name of your active profile |
| `%landclaim_chunks%` | Chunks used in your active profile |
| `%landclaim_limit%` | Your total chunk claim limit |
| `%landclaim_power%` | Current power level (if enabled) |

### Dynamic Fetch
| Placeholder | Description |
| :--- | :--- |
| `%landclaim_message:<key>%` | Fetches any raw string from your `messages.yml` |

---

## Placeholder Format Compatibility
As noted above, all internal placeholders support both bracket styles:
*   `<owner>` is the same as `{owner}`
*   `<limit>` is the same as `{limit}`
*   etc.
