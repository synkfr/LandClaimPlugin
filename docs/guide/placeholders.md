# Placeholders

LandClaimPlugin supports **PlaceholderAPI** for use in tablists, scoreboards, chat, and more. 

## Integration
To use these placeholders, ensure you have [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed on your server.

The plugin automatically registers its expansion with the identifier: `landclaim`

## Claim Placeholders
These placeholders return information about the claim at the **player's current location**.

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%landclaim_owner%` | Name or alias of the claim owner | `AyoSynk` |
| `%landclaim_owner_uuid%` | UUID of the claim owner | `f84c...` |
| `%landclaim_name%` | The custom name of the claim | `Base Alpha` |
| `%landclaim_is_claimed%` | Whether the current chunk is claimed | `Yes` / `No` |
| `%landclaim_pvp%` | Whether PvP is currently enabled | `Enabled` / `Disabled` |

## Player Placeholders
These placeholders return information about the **player's own status and profile**.

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%landclaim_profile%` | Name of player's active profile | `My Primary Claim` |
| `%landclaim_chunks%` | Chunks used in active profile | `12` |
| `%landclaim_chunks_used%` | Alias for `%landclaim_chunks%` | `12` |
| `%landclaim_limit%` | Player's total chunk claim limit | `50` |
| `%landclaim_chunks_max%` | Alias for `%landclaim_limit%` | `50` |

## Advanced Placeholders
You can dynamically fetch any raw message string from your `messages.yml` configuration.

| Placeholder | Description |
| :--- | :--- |
| `%landclaim_message:<key>%` | Fetches the raw message for the specified key. |

**Example:** `%landclaim_message:alreadyClaimed%`

---

## Placeholder Format Compatibility
The plugin is designed to be flexible with placeholder formats. When configuring your `messages.yml` or menu files, you can use either **angle brackets** or **curly braces** for the plugin's internal placeholders.

### Supported Formats
Both of these are treated identically by the plugin:
*   `<owner>` or `{owner}`
*   `<gap>` or `{gap}`
*   `<limit>` or `{limit}`
*   `<time>` or `{time}`

This ensures compatibility with various message styles and prevents configuration errors if you prefer one format over the other.
