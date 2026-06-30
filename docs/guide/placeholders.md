# Placeholders

LandClaimPlugin supports both **internal placeholders** (used in messages and menus) and **PlaceholderAPI** (for external integrations).

## 1. Internal Placeholders
These placeholders are used within `messages.yml` and GUI configuration files. 

::: warning Context Specific
Internal placeholders are **not global**. They only work in specific messages where that data is available. For example, `<time>` only works in PvP-related messages.
:::

### Common Variables
| Placeholder | Description | Context |
| :--- | :--- | :--- |
| `<owner>` | Name of the claim owner | Most claim-related messages |
| `<player>` | Name of the target player | Invites, admin commands |
| `<name>` | Name of the claim or warp | Warp/Rename messages |
| `<count>` | Number of chunks/items | Bulk actions |
| `<limit>` | Max limit | Limit reached messages |

### Message-Specific Variables
| Placeholder | Description | Found In |
| :--- | :--- | :--- |
| `<world>`, `<x>`, `<z>` | Location coordinates | Wand, Claim List |
| `<id>` | Internal Claim ID | Admin Claim Info |
| `<uuid>` | Player's Unique ID | Admin Claim Info |
| `<time>`, `<seconds>` | Remaining time/cooldown | PvP, Unstuck |
| `<gap>` | Required distance | Gap protection messages |
| `<players>` | List of trusted players | Claim Info |

---

## 2. PlaceholderAPI (Global)
Unlike internal placeholders, these can be used **anywhere** on your server (Tab, Scoreboard, etc.) via [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

**Identifier:** `landclaim`

### Claim Data (Current Location)
These return data based on where the player is currently standing.

| Placeholder | Description |
| :--- | :--- |
| `%landclaim_owner%` | Name of the claim owner |
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
These return data about the player's own profile and limits.

| Placeholder | Description |
| :--- | :--- |
| `%landclaim_profile%` | Name of your active profile |
| `%landclaim_chunks%` | Chunks used in your active profile |
| `%landclaim_limit%` | Your total chunk claim limit |

### Utility
| Placeholder | Description |
| :--- | :--- |
| `%landclaim_message:<key>%` | Fetches any raw string from `messages.yml` |

---

## Placeholder Format Compatibility
All internal placeholders support both bracket styles interchangeably in configuration files:
*   `<owner>` is the same as `{owner}`
*   `<limit>` is the same as `{limit}`

---

## 3. Claim Limits, Slots and Economy (Global)

These placeholders return data about claim limits, custom roles, members, warps, and the cost of buying additional slots.

**Identifier:** `claimplugin`

| Placeholder | Description |
| :--- | :--- |
| `%claimplugin_claims_current%` | Number of chunks claimed by the player |
| `%claimplugin_claims_max%` | Maximum chunk limit for the player |
| `%claimplugin_cost_next_claim%` | Cost of the next chunk |
| `%claimplugin_roles_current%` | Number of custom roles in active profile |
| `%claimplugin_roles_max%` | Maximum custom roles limit for active profile |
| `%claimplugin_cost_next_role_slot%` | Cost to buy an additional custom role slot |
| `%claimplugin_members_current%` | Number of members in active profile |
| `%claimplugin_members_max%` | Maximum members limit for active profile |
| `%claimplugin_cost_next_member_slot%` | Cost to buy an additional member slot |
| `%claimplugin_warps_current%` | Number of warps in active profile |
| `%claimplugin_warps_max%` | Maximum warps limit for active profile |
| `%claimplugin_cost_next_warp_slot%` | Cost to buy an additional warp slot |
