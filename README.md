![Night Vision Banner](https://cdn.modrinth.com/data/cached_images/53bc8fe7c0c7944f4446f187d5068b7e3e9a249b.png)

# 🌙 Night Vision — Simple, Persistent Night Vision Plugin

A no-setup, ultra-lightweight Minecraft plugin that lets players toggle Night Vision with a single command.

---

## 🚀 Features

- 🔘 Toggle Night Vision using `/nv`
- 🔓 No permissions needed — accessible to all players
- 💀 Effect persists after death
- 🥛 Not removed by milk
- 🔁 Remains active after logout/login
- 🧼 No potion particles for clean visuals
- ⚙️ Configurable messages via `config.yml`

---

## 📥 Installation

1. Download the latest release from [GitHub Releases](https://github.com/synkfr/nvplugin/releases)
2. Drop `NightVision.jar` into your server's `plugins/` folder
3. Restart your Minecraft server

> ✔️ Compatible with Spigot, Paper, Purpur, etc.  
> ☕ Requires Java 8 or newer

---

## 🔧 Configuration (`config.yml`)

```yaml
# Toggle chat messages (set to "" to disable)
messages:
  enabled: "&a&lNight Vision Enabled"
  disabled: "&c&lNight Vision Disabled"

# Toggle title messages (set to "" to disable)
title_enabled: "&7Night Vision &aON"
title_disabled: "&7Night Vision &cOFF"
