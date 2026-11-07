<div align="center">

## Inventory Slots

**InventorySlots** is a client-side Fabric mod that lets players customize and preview inventory slot visuals with adjustable text overlays. Featuring scalable rendering, anchor point control, shadow options, and in-game previews, it's perfect for development/configuration!

## InventorySlots

![Issues](https://img.shields.io/github/issues-raw/BitByLogics/InventorySlots)
[![Stars](https://img.shields.io/github/stars/BitByLogics/InventorySlots)](https://github.com/BitByLogics/InventorySlots/stargazers)

<a href="#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/supported/fabric_46h.png" height="35"></a>
<a href="#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/available/modrinth_vector.svg" height="35" href="https://modrinth.com/mod/inventory-slots"></a>

</div>

## ✨ Features

- Toggle inventory slot overlays client-side, including while an inventory is open
- One-click **Containers only** preset
- Separate visibility for containers, your own inventory, creative tabs, player rows in containers and the player hotbar in containers
- Enable or disable each registered inventory type (chests, furnaces, workstations, modded inventories, and more)
- Exclude specific menu titles without disabling every chest; useful for server menus
- Customize scale, color, anchor point, and shadow
- In-game config GUI
- Real-time preview and freely assignable keyboard/mouse shortcuts
- Persistent config saved between sessions
- English and German settings

---

## 📷 Preview

<div align="center">

<table>
  <tr>
    <td align="center">
      <img src="https://i.imgur.com/E3LJtgy.png" height="180" /><br>
      <em>Double Chest Slots</em>
    </td>
    <td align="center">
      <img src="https://i.imgur.com/AHIk2zc.png" height="180" /><br>
      <em>Anvil Slots</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://i.imgur.com/YMHXdrq.png" height="180" /><br>
      <em>Brewing Stand Slots</em>
    </td>
    <td align="center">
      <img src="https://i.imgur.com/rE5V1rF.png" height="180" /><br>
      <em>Enchanting Table Slots</em>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://i.imgur.com/3qoqJcX.png" height="320" /><br>
      <em>Configuration screen with live preview</em>
    </td>
  </tr>
</table>

</div>

---

## 🛠 Configuration

Press **END** to open the settings, including from an open inventory. The settings have four tabs:

- **Visibility:** master switch, the **Containers only** preset, and independent switches for container slots, your survival inventory, creative inventory, player rows beneath containers, the player hotbar beneath containers, HUD hotbar numbers, widget numbers, and the legacy HUD-only mode.
- **Appearance:** scale (0.1–1.0), color palette, text anchor, shadow, and a live preview.
- **Inventories:** enable/disable entire inventory types. Registered modded types appear automatically. Unregistered custom types appear when you open settings from that inventory. A type disabled here hides all its slot numbers, including player slots.
- **Excluded menus:** open settings from a container and click its title to exclude or allow it. Remove saved exclusions here as well. Rules match the exact visible title and inventory type, across all servers; different titles remain independent. Dynamic menu titles need separate rules. Minecraft does not tell a client mod whether a chest-shaped interface is a physical chest or a server menu, so title rules provide explicit control.

For numbers **only in the upper container**, select **Containers only**. This enables chest/furnace/etc. slots and hides the player inventory below, the survival and creative inventory screens, HUD numbers, and widget numbers. Appearance and explicit exclusions are preserved. Existing installations retain their previous settings until you select the preset.

Filters hide labels without renumbering the remaining slots: numbers still identify the actual menu slot IDs, starting at zero. HUD hotbar numbers remain 0–8.

The config file is located at:

```
.minecraft/config/inventory-slots-config.json
```

---

## 🔧 Keybinds

| Action       | Default Key |
|--------------|-------------|
| Toggle Slots | `HOME`      |
| Open Options | `END`       |

Additional, initially **unbound** actions toggle container slots, player rows in containers, the player hotbar in containers, your own inventory screen, creative inventory, HUD numbers, widget numbers, the current menu title, and the current inventory type.

Assign keys or mouse buttons under **Options → Controls → Key Binds → Inventory Slots**, or use the **Key bindings** button in this mod's settings. Inventory shortcuts work while a container is open. Focused search/text fields keep their normal input. A short action-bar message confirms changes; changes are saved immediately. The master switch must be on, and other filters still apply when you enable an individual option.

The two current-menu shortcuts require an open inventory. They toggle an exclusion rule; allowing a rule again does not override other disabled visibility settings.

---

## 📦 Installation

1. Download the latest version from [Modrinth](https://modrinth.com/mod/inventory-slots)
2. Install [Fabric Loader](https://fabricmc.net/use/)
3. Add InventorySlots to your `mods/` folder
4. Launch Minecraft

---

## 🧩 Compatibility

- This branch: Minecraft **26.3**, Java **25**
- Loader: **Fabric**
- Required Mods: [Fabric API](https://modrinth.com/mod/fabric-api)

---

## Development and verification

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClientGameTest
```

The build runs configuration, migration and visibility unit tests. Client game tests launch an isolated Minecraft instance to check real chest/furnace/player/creative slots, inventory hotkeys, the configuration tabs, and returning to an inventory. Screenshots are written to `build/run/clientGameTest/screenshots`. Test code is not included in the release JAR.

The installable file is `build/libs/inventoryslots-2.2.0+26.3.jar` (the `-sources.jar` is for development).

---

## 📜 License

This project is licensed under the MIT License.  
See the [LICENSE](./LICENSE) file for details.

---

## 👍 Support & Feedback

Need help? Found a bug?  
Feel free to [open an issue](https://github.com/VaultedMC/InventorySlots/issues)!

---
