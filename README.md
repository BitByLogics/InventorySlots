<div align="center">

# InventorySlots

InventorySlots is a client-side Fabric mod that adds customizable text overlays to inventory slots. It is primarily designed as a utility for developers and configurators to visualize slot indices and layout positioning in-game.

[![Issues](https://img.shields.io/github/issues-raw/BitByLogics/InventorySlots)](https://github.com/BitByLogics/InventorySlots/issues)
[![Stars](https://img.shields.io/github/stars/BitByLogics/InventorySlots)](https://github.com/BitByLogics/InventorySlots/stargazers)
<a href="https://modrinth.com/mod/inventory-slots">

<img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/available/modrinth_vector.svg" height="35"></a>

| Double Chest | Anvil |
|:---:|:---:|
| <img src="https://i.imgur.com/E3LJtgy.png" height="180" /> | <img src="https://i.imgur.com/AHIk2zc.png" height="180" /> |
| **Brewing Stand** | **Enchanting Table** |
| <img src="https://i.imgur.com/YMHXdrq.png" height="180" /> | <img src="https://i.imgur.com/rE5V1rF.png" height="180" /> |

</div>

---

### Overview
The mod provides a real-time overlay on any container GUI. Settings can be adjusted through an in-game menu with an immediate preview of changes.

* **Rendering:** Adjust text scale (0.25 to 2.0), hex colors, and toggle text shadows.
* **Positioning:** Supports multiple anchor points (Top-left, Center, Bottom-right, etc.).
* **Persistence:** All settings are saved to a local config file and persist across sessions.
* **Client-side:** No server-side installation required.

---

### Configuration
Press **END** to open the settings, including from an open inventory. The settings have four tabs:

* **Visibility:** Master switch, a **Containers only** preset, and separate switches for container slots, your own inventory, creative inventory, the player rows and hotbar beneath containers, HUD hotbar numbers and widget numbers.
* **Appearance:** Text scale, color, anchor and shadow, with a live preview.
* **Inventories:** Turn slot numbers on or off for a whole inventory type (chests, furnaces, workstations, modded inventories).
* **Excluded menus:** Exclude a specific menu title, useful for server menus that look like chests.

Filters hide labels without renumbering: numbers are always the real slot IDs, starting at zero.

---

### Keybinds
* **HOME:** Toggle overlays
* **END:** Open configuration menu

Extra actions (container slots, player rows, HUD numbers, widget numbers, current menu title, current inventory type) start unbound. Assign them under Options > Controls > Key Binds > Inventory Slots. They also work while an inventory is open.

---

### Compatibility
This branch targets the latest Minecraft version (Java 25) with Fabric Loader and [Fabric API](https://modrinth.com/mod/fabric-api). Older Minecraft versions live on their own branches (`1.21.11`, `26.1.2`, `26.2`, `26.3`).

---

No more resourcepacks or googling.
