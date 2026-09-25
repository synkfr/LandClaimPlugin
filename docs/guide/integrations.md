# Integrations (DeluxeMenus & Citizens 2)

LandClaimPlugin integrates with popular community plugins such as **DeluxeMenus**, **Citizens 2**, **PlaceholderAPI**, and **Vault**. This guide details how to configure interactive GUI shops and NPC traders that allow players to purchase extra claim chunks.

---

## 1. DeluxeMenus Chunk Shop

Using [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/), you can create a custom GUI where players can trade in-game currency or physical items (e.g. Gold Blocks) in exchange for bonus claim chunks.

### How It Works
When a player clicks to purchase:
1. DeluxeMenus validates that the player meets the cost requirement (items in inventory or Vault economy balance).
2. DeluxeMenus deducts the payment.
3. DeluxeMenus executes the console command:
   ```bash
   [console] claim admin add chunk %player_name% 1
   ```
4. LandClaimPlugin immediately updates the player's bonus claim chunks in cache and database.

---

### Example Configuration: `claimshop.yml`

Save this file in `plugins/DeluxeMenus/gui_menus/claimshop.yml` and register it in `config.yml` of DeluxeMenus:

```yaml
menu_title: '&8Buy Land Claim Chunks'
open_command: claimshop
size: 27
items:
  'info_display':
    material: PAPER
    slot: 4
    display_name: '&e&lYour Claim Status'
    lore:
      - '&7Active Profile: &f%landclaim_profile%'
      - '&7Claimed Chunks: &a%landclaim_chunks% &7/ &b%landclaim_limit%'
      - ''
      - '&7Purchase extra chunks below to expand your territory!'

  'buy_chunk_items':
    material: GOLD_BLOCK
    slot: 11
    display_name: '&6&lBuy 1 Extra Chunk &7(Items)'
    lore:
      - '&7Cost: &e30x Gold Block'
      - ''
      - '&7Current Chunks: &a%landclaim_chunks% &7/ &b%landclaim_limit%'
      - ''
      - '&a&lCLICK &fto trade 30 Gold Blocks for 1 chunk!'
    left_click_commands:
      - '[console] clear %player_name% gold_block 30'
      - '[console] claim admin add chunk %player_name% 1'
      - '[message] &aSuccessfully traded 30 Gold Blocks for 1 extra claim chunk!'
      - '[sound] ENTITY_PLAYER_LEVELUP 1 1'
      - '[refresh]'
    left_click_requirement:
      requirements:
        has_gold_blocks:
          type: has item
          material: GOLD_BLOCK
          amount: 30
      deny_commands:
        - '[message] &cYou need 30 Gold Blocks in your inventory to buy this!'
        - '[sound] BLOCK_ANVIL_LAND 1 1'

  'buy_chunk_vault':
    material: EMERALD
    slot: 15
    display_name: '&a&lBuy 1 Extra Chunk &7(Coins)'
    lore:
      - '&7Cost: &6$5,000'
      - ''
      - '&7Current Chunks: &a%landclaim_chunks% &7/ &b%landclaim_limit%'
      - ''
      - '&a&lCLICK &fto purchase 1 chunk with economy!'
    left_click_commands:
      - '[console] eco take %player_name% 5000'
      - '[console] claim admin add chunk %player_name% 1'
      - '[message] &aSuccessfully purchased 1 extra claim chunk for $5,000!'
      - '[sound] ENTITY_PLAYER_LEVELUP 1 1'
      - '[refresh]'
    left_click_requirement:
      requirements:
        has_balance:
          type: '>='
          input: '%vault_eco_balance%'
          output: '5000'
      deny_commands:
        - '[message] &cYou do not have enough money ($5,000 required)!'
        - '[sound] BLOCK_ANVIL_LAND 1 1'
```

After creating or modifying the menu, reload DeluxeMenus:
```bash
/dm reload
```

---

## 2. Citizens 2 NPC Setup

With [Citizens 2](https://www.spigotmc.org/resources/citizens.13812/), you can place an in-game NPC (such as an architect, surveyor, or real estate agent) that players can interact with to purchase claim chunks.

### Method A: Connect NPC to DeluxeMenus (Recommended)

Connecting the NPC to DeluxeMenus provides the best user experience because it gives players a visual interface showing prices, their current chunk count, and transaction confirmation.

1. **Spawn and position the NPC:**
   ```bash
   /npc create "Territory Surveyor" --type player
   ```
2. **Select the NPC:**
   ```bash
   /npc select
   ```
3. **Set the NPC skin (optional):**
   ```bash
   /npc skin Architect
   ```
4. **Make the NPC look at nearby players:**
   ```bash
   /npc lookclose
   ```
5. **Attach the DeluxeMenus open command:**
   ```bash
   /npc command add -p "claimshop"
   ```
   > `-p` means the command is executed **by the player** who clicks the NPC. When right-clicked, it executes `/claimshop` and opens the menu.

---

### Method B: Direct NPC Console Command

If you want the NPC to directly grant a chunk without opening a GUI:

1. **Select your NPC:**
   ```bash
   /npc select
   ```
2. **Attach the console command:**
   ```bash
   /npc command add -c "claim admin add chunk <p> 1"
   ```
   > **Note on Placeholders:**
   > - `<p>` is Citizens' built-in placeholder for the clicking player's name.
   > - In LandClaimPlugin **v3.2.2+**, `@p` (Minecraft's nearest-player entity selector) is also fully supported! Both `<p>` and `@p` work reliably.

::: tip Economy Protection
If you run direct console commands via Citizens, use a command wrapper plugin (such as [CommandNPC](https://www.spigotmc.org/resources/commandnpc.10533/) or Citizens' built-in price flags) to ensure players are charged before the command is dispatched.
:::

---

## 3. Command Syntax & Entity Selectors

Starting in **v3.2.2**, `/claim admin add chunk` includes flexible syntax parsing:

| Command Syntax | Target | Chunks Added | Example |
|---|---|---|---|
| `/claim admin add chunk <player> <amount>` | Targeted Player | `<amount>` | `/claim admin add chunk Steve 5` |
| `/claim admin add chunk <amount> <player>` | Targeted Player | `<amount>` | `/claim admin add chunk 5 Steve` |
| `/claim admin add chunk @p <amount>` | Nearest Player | `<amount>` | `/claim admin add chunk @p 1` |
| `/claim admin add chunk @s <amount>` | Executing Sender | `<amount>` | `/claim admin add chunk @s 10` |
| `/claim admin add chunk <amount>` | Self (In-Game Admin) | `<amount>` | `/claim admin add chunk 3` |
| `/claim admin add chunk <player>` | Targeted Player | `1` (Default) | `/claim admin add chunk @p` |

### Supported Selectors
- `@p` — Nearest player (works from Console, Command Blocks, and in-game)
- `@s` — The entity executing the command
- `@r` — Random online player
- `@a` — All online players (each player receives the bonus chunk amount)
- `<UUID>` — Direct Mojang UUID
- `<Username>` — Online or cached offline player

All selector lookups are evaluated synchronously on the main thread for Folia/Paper safety, and chunk persistence is processed asynchronously in the background.
