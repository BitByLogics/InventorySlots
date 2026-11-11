package net.bitbylogic.inventoryslots.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static net.bitbylogic.inventoryslots.config.Config.ScreenKind.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir Path directory;
    private Path file;
    private Config config;
    private static final String CHEST = "minecraft:generic_9x6";
    private static final String FURNACE = "minecraft:furnace";

    @BeforeEach
    void setUp() {
        file = directory.resolve("settings.json");
        config = new Config(file);
        config.enabled = true;
    }

    @Test
    void containerPresetHidesCreativeOwnInventoryAndAllPlayerSlots() {
        config.containersOnly();
        assertTrue(show(CONTAINER, CHEST, false, false));
        assertTrue(show(CONTAINER, FURNACE, false, false));
        assertFalse(show(CONTAINER, CHEST, true, false));
        assertFalse(show(CONTAINER, CHEST, true, true));
        assertFalse(show(INVENTORY, "inventory_slots:player", true, false));
        assertFalse(show(CREATIVE, "inventory_slots:creative", false, false));
        assertFalse(config.hotbarNumbers);
        assertFalse(config.widgetNumbers);
        assertFalse(config.hotbarOnly);
    }

    @Test
    void playerRowsAndHotbarCanBeShownIndependentlyOfContainer() {
        config.containerSlots = false;
        config.playerInventoryInContainers = true;
        config.playerHotbarInContainers = false;
        assertFalse(show(CONTAINER, CHEST, false, false));
        assertTrue(show(CONTAINER, CHEST, true, false));
        assertFalse(show(CONTAINER, CHEST, true, true));
        config.playerInventoryInContainers = false;
        config.playerHotbarInContainers = true;
        assertFalse(show(CONTAINER, CHEST, true, false));
        assertTrue(show(CONTAINER, CHEST, true, true));
    }

    @Test
    void ownInventoryAndCreativeAreIndependentOfContainerSettings() {
        config.containersOnly();
        config.inventoryScreen = true;
        assertTrue(show(INVENTORY, "inventory_slots:player", true, true));
        assertFalse(show(CREATIVE, "inventory_slots:creative", true, true));
        config.inventoryScreen = false;
        config.creativeInventory = true;
        assertFalse(show(INVENTORY, "inventory_slots:player", true, false));
        assertTrue(show(CREATIVE, "inventory_slots:creative", false, false));
        assertTrue(show(CREATIVE, "inventory_slots:creative", true, true));
    }

    @Test
    void masterAndLegacyHudOnlySwitchOverrideAllInventoryFilters() {
        config.enabled = false;
        for (var kind : Config.ScreenKind.values()) {
            assertFalse(show(kind, CHEST, false, false));
        }
        config.enabled = true;
        config.hotbarOnly = true;
        for (var kind : Config.ScreenKind.values()) {
            assertFalse(show(kind, CHEST, true, true));
        }
    }

    @Test
    void disabledTypeHidesTheWholeScreenWithoutAffectingOtherTypes() {
        assertFalse(config.toggleMenuType(CHEST));
        assertFalse(show(CONTAINER, CHEST, false, false));
        assertFalse(show(CONTAINER, CHEST, true, false));
        assertFalse(show(CONTAINER, CHEST, true, true));
        assertTrue(show(CONTAINER, FURNACE, false, false));
        assertTrue(config.toggleMenuType(CHEST));
        assertTrue(show(CONTAINER, CHEST, false, false));
    }

    @Test
    void exclusionMatchesExactTitleAndTypeAndCanBeReenabled() {
        assertFalse(config.toggleScreen(CHEST, "Server-Menü"));
        assertFalse(config.shouldShowSlot(CONTAINER, CHEST, "Server-Menü", false, false));
        assertFalse(config.shouldShowSlot(CONTAINER, CHEST, "Server-Menü", true, true));
        assertTrue(config.shouldShowSlot(CONTAINER, CHEST, "Large Chest", false, false));
        assertTrue(config.shouldShowSlot(CONTAINER, FURNACE, "Server-Menü", false, false));
        assertTrue(config.toggleScreen(CHEST, "Server-Menü"));
        assertTrue(config.shouldShowSlot(CONTAINER, CHEST, "Server-Menü", false, false));
    }

    @Test
    void containersPresetPreservesAppearanceAndExplicitExclusions() {
        config.textScale = 0.8f;
        config.textAnchor = Config.TextAnchor.BOTTOM_RIGHT;
        config.disabledMenuTypes.add(FURNACE);
        config.toggleScreen(CHEST, "Menu");
        config.containersOnly();
        assertEquals(0.8f, config.textScale);
        assertEquals(Config.TextAnchor.BOTTOM_RIGHT, config.textAnchor);
        assertFalse(show(CONTAINER, FURNACE, false, false));
        assertFalse(config.shouldShowSlot(CONTAINER, CHEST, "Menu", false, false));
    }

    @Test
    void settingsAndUnicodeRulesSurviveSaveAndReload() throws Exception {
        config.containersOnly();
        config.playerHotbarInContainers = true;
        config.textAnchor = Config.TextAnchor.CENTER_RIGHT;
        config.textColor = 0xFF123456;
        config.textScale = 0.7f;
        config.toggleMenuType("example:machine");
        config.toggleScreen(CHEST, "Menü \"Übersicht\" 🎒");
        config.save();
        Config loaded = new Config(file);
        loaded.load();
        assertTrue(loaded.enabled);
        assertFalse(loaded.inventoryScreen);
        assertFalse(loaded.creativeInventory);
        assertFalse(loaded.playerInventoryInContainers);
        assertTrue(loaded.playerHotbarInContainers);
        assertEquals(config.textAnchor, loaded.textAnchor);
        assertEquals(config.textColor, loaded.textColor);
        assertEquals(config.textScale, loaded.textScale);
        assertEquals(config.disabledMenuTypes, loaded.disabledMenuTypes);
        assertEquals(config.excludedScreens, loaded.excludedScreens);
        assertFalse(Files.readString(file).contains("\"file\""));
        assertFalse(Files.exists(file.resolveSibling("settings.json.tmp")));
    }

    @Test
    void legacyConfigKeepsItsSettingsAndDefaultsToPreviousVisibility() throws Exception {
        Files.writeString(file, """
                {"enabled":true,"textScale":0.75,"textColor":16711680,
                 "textAnchor":"BOTTOM_LEFT","textShadow":false,"hotbarNumbers":true,
                 "hotbarOnly":false,"widgetNumbers":true}
                """);
        Config loaded = new Config(file);
        loaded.load();
        assertEquals(0.75f, loaded.textScale);
        assertEquals(0xFFFF0000, loaded.textColor);
        assertEquals(Config.TextAnchor.BOTTOM_LEFT, loaded.textAnchor);
        assertFalse(loaded.textShadow);
        assertTrue(loaded.hotbarNumbers);
        assertTrue(loaded.widgetNumbers);
        assertTrue(loaded.inventoryScreen);
        assertTrue(loaded.creativeInventory);
        assertTrue(loaded.shouldShowSlot(CONTAINER, CHEST, "Chest", true, false));
        assertTrue(loaded.shouldShowSlot(CONTAINER, CHEST, "Chest", true, true));
    }

    @Test
    void badIndividualValuesDoNotPreventOtherSettingsLoading() throws Exception {
        Files.writeString(file, """
                {"enabled":true,"textScale":"NaN","textAnchor":"INVALID",
                 "textColor":null,"creativeInventory":false,"disabledMenuTypes":{},
                 "playerInventoryInContainers":false,"excludedScreens":[null,{}]}
                """);
        Config loaded = new Config(file);
        assertDoesNotThrow(loaded::load);
        assertTrue(loaded.enabled);
        assertFalse(loaded.creativeInventory);
        assertFalse(loaded.playerInventoryInContainers);
        assertEquals(0.5f, loaded.textScale);
        assertEquals(Config.TextAnchor.TOP_LEFT, loaded.textAnchor);
        assertTrue(loaded.disabledMenuTypes.isEmpty());
        assertTrue(loaded.excludedScreens.isEmpty());
    }

    @Test
    void scalesAreClampedToRenderSafeRange() throws Exception {
        Files.writeString(file, "{\"textScale\":100}");
        config.load();
        assertEquals(1.0f, config.textScale);
        Files.writeString(file, "{\"textScale\":-5}");
        config.load();
        assertEquals(0.1f, config.textScale);
    }

    @Test
    void damagedFileIsPreservedAndDoesNotCrash() throws Exception {
        String damaged = "{this isn't json";
        Files.writeString(file, damaged);
        Config loaded = new Config(file);
        assertDoesNotThrow(loaded::load);
        assertFalse(loaded.enabled);
        assertEquals(damaged, Files.readString(file));
        assertEquals(damaged, Files.readString(file.resolveSibling(file.getFileName() + ".bak")));
    }

    @Test
    void savingAfterDamagedLoadKeepsTheBackup() throws Exception {
        String damaged = "{this isn't json";
        Files.writeString(file, damaged);
        Config loaded = new Config(file);
        loaded.load();
        loaded.save();
        assertEquals(damaged, Files.readString(file.resolveSibling(file.getFileName() + ".bak")));
    }

    @Test
    void excludedScreenIsNotAllowed() {
        config.enabled = true;
        config.toggleScreen(CHEST, "Menu");
        assertFalse(config.isScreenAllowed(CHEST, "Menu"));
        assertTrue(config.isScreenAllowed(CHEST, "Other"));
    }

    @Test
    void resetClearsExclusionsAndRestoresConsistentDefaults() {
        config.containersOnly();
        config.toggleMenuType(CHEST);
        config.toggleScreen(CHEST, "Menu");
        config.textShadow = false;
        config.reset();
        assertTrue(config.enabled);
        assertTrue(config.inventoryScreen);
        assertTrue(config.creativeInventory);
        assertTrue(config.playerInventoryInContainers);
        assertTrue(config.playerHotbarInContainers);
        assertTrue(config.textShadow);
        assertEquals(0xFFFFFFFF, config.textColor);
        assertTrue(config.disabledMenuTypes.isEmpty());
        assertTrue(config.excludedScreens.isEmpty());
    }

    private boolean show(Config.ScreenKind kind, String type, boolean player, boolean hotbar) {
        return config.shouldShowSlot(kind, type, "Chest", player, hotbar);
    }
}
