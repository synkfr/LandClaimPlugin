package org.ayosynk.landClaimPlugin.managers;

import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.MessagesConfig;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.ayosynk.landClaimPlugin.config.menus.MainMenuConfig;
import org.ayosynk.landClaimPlugin.config.menus.ClaimSettingsConfig;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapConfig;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapInfoConfig;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import org.ayosynk.landClaimPlugin.config.menus.TrustManagementConfig;
import org.ayosynk.landClaimPlugin.config.menus.PlayerTrustPermissionConfig;
import org.ayosynk.landClaimPlugin.config.menus.MemberManagementConfig;
import org.ayosynk.landClaimPlugin.config.menus.PlayerControlPanelConfig;
import org.ayosynk.landClaimPlugin.config.menus.RoleSelectionConfig;
import org.ayosynk.landClaimPlugin.config.menus.WarpManagementConfig;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import org.ayosynk.landClaimPlugin.config.menus.WarpChangeIconConfig;
import org.ayosynk.landClaimPlugin.config.menus.AllyManagementConfig;
import org.ayosynk.landClaimPlugin.config.menus.AllyControlPanelConfig;
import org.ayosynk.landClaimPlugin.config.menus.AllyPremissionsConfig;
import org.bukkit.Color;
import java.io.File;
import java.util.List;

public class ConfigManager {
    private final LandClaimPlugin plugin;

    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;
    private MainMenuConfig mainMenuConfig;
    private ClaimSettingsConfig claimSettingsConfig;
    private ClaimMapConfig claimMapConfig;
    private ClaimMapInfoConfig claimMapInfoConfig;
    private VisitorSettingsConfig visitorSettingsConfig;
    private TrustManagementConfig trustManagementConfig;
    private PlayerTrustPermissionConfig playerTrustPermissionConfig;
    private MemberManagementConfig memberManagementConfig;
    private PlayerControlPanelConfig playerControlPanelConfig;
    private RoleSelectionConfig roleSelectionConfig;
    private WarpManagementConfig warpManagementConfig;
    private WarpControlPanelConfig warpControlPanelConfig;
    private WarpChangeIconConfig warpChangeIconConfig;
    private AllyManagementConfig allyManagementConfig;
    private AllyControlPanelConfig allyControlPanelConfig;
    private AllyPremissionsConfig allyPremissionsConfig;

    public ConfigManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        this.pluginConfig = eu.okaeri.configs.ConfigManager.create(PluginConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "config.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.messagesConfig = eu.okaeri.configs.ConfigManager.create(MessagesConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "locales/messages_" + pluginConfig.language + ".yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.mainMenuConfig = eu.okaeri.configs.ConfigManager.create(MainMenuConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/mainmenu.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.claimSettingsConfig = eu.okaeri.configs.ConfigManager.create(ClaimSettingsConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/ClaimSettings.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.claimMapConfig = eu.okaeri.configs.ConfigManager.create(ClaimMapConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/ClaimMap.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.claimMapInfoConfig = eu.okaeri.configs.ConfigManager.create(ClaimMapInfoConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/ClaimMapInfo.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.visitorSettingsConfig = eu.okaeri.configs.ConfigManager.create(VisitorSettingsConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/VisitorSettings.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.trustManagementConfig = eu.okaeri.configs.ConfigManager.create(TrustManagementConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/TrustManagement.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.playerTrustPermissionConfig = eu.okaeri.configs.ConfigManager.create(PlayerTrustPermissionConfig.class,
                (it) -> {
                    it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
                    it.withBindFile(new File(plugin.getDataFolder(), "menus/PlayerTrustPermission.yml"));
                    it.saveDefaults();
                    it.load(true);
                });

        this.memberManagementConfig = eu.okaeri.configs.ConfigManager.create(MemberManagementConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/MemberManagement.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.playerControlPanelConfig = eu.okaeri.configs.ConfigManager.create(PlayerControlPanelConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/PlayerControlPanel.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.roleSelectionConfig = eu.okaeri.configs.ConfigManager.create(RoleSelectionConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/RoleSelection.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.warpManagementConfig = eu.okaeri.configs.ConfigManager.create(WarpManagementConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/WarpManagement.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.warpControlPanelConfig = eu.okaeri.configs.ConfigManager.create(WarpControlPanelConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/WarpControlPanel.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.warpChangeIconConfig = eu.okaeri.configs.ConfigManager.create(WarpChangeIconConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/WarpChangeIcon.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.allyManagementConfig = eu.okaeri.configs.ConfigManager.create(AllyManagementConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/AllyManagement.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.allyControlPanelConfig = eu.okaeri.configs.ConfigManager.create(AllyControlPanelConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/AllyControlPanel.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.allyPremissionsConfig = eu.okaeri.configs.ConfigManager.create(AllyPremissionsConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/AllyPremissions.yml"));
            it.saveDefaults();
            it.load(true);
        });
    }

    public void reloadMainConfig() {
        pluginConfig.load();
        messagesConfig.load();
        mainMenuConfig.load();
        claimSettingsConfig.load();
        claimMapConfig.load();
        claimMapInfoConfig.load();
        visitorSettingsConfig.load();
        trustManagementConfig.load();
        playerTrustPermissionConfig.load();
        memberManagementConfig.load();
        playerControlPanelConfig.load();
        roleSelectionConfig.load();
        warpManagementConfig.load();
        warpControlPanelConfig.load();
        warpChangeIconConfig.load();
        allyManagementConfig.load();
        allyControlPanelConfig.load();
        allyPremissionsConfig.load();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public MainMenuConfig getMainMenuConfig() {
        return mainMenuConfig;
    }

    public ClaimSettingsConfig getClaimSettingsConfig() {
        return claimSettingsConfig;
    }

    public ClaimMapConfig getClaimMapConfig() {
        return claimMapConfig;
    }

    public ClaimMapInfoConfig getClaimMapInfoConfig() {
        return claimMapInfoConfig;
    }

    public VisitorSettingsConfig getVisitorSettingsConfig() {
        return visitorSettingsConfig;
    }

    public TrustManagementConfig getTrustManagementConfig() {
        return trustManagementConfig;
    }

    public PlayerTrustPermissionConfig getPlayerTrustPermissionConfig() {
        return playerTrustPermissionConfig;
    }

    public MemberManagementConfig getMemberManagementConfig() {
        return memberManagementConfig;
    }

    public PlayerControlPanelConfig getPlayerControlPanelConfig() {
        return playerControlPanelConfig;
    }

    public RoleSelectionConfig getRoleSelectionConfig() {
        return roleSelectionConfig;
    }

    public WarpManagementConfig getWarpManagementConfig() {
        return warpManagementConfig;
    }

    public WarpControlPanelConfig getWarpControlPanelConfig() {
        return warpControlPanelConfig;
    }

    public WarpChangeIconConfig getWarpChangeIconConfig() {
        return warpChangeIconConfig;
    }

    public AllyManagementConfig getAllyManagementConfig() {
        return allyManagementConfig;
    }

    public AllyControlPanelConfig getAllyControlPanelConfig() {
        return allyControlPanelConfig;
    }

    public AllyPremissionsConfig getAllyPremissionsConfig() {
        return allyPremissionsConfig;
    }

    // --- Legacy wrapper methods to bridge until other classes are transformed ---

    public boolean requireConnectedClaims() {
        return pluginConfig.requireConnectedClaims;
    }

    public boolean allowDiagonalConnections() {
        return pluginConfig.allowDiagonalConnections;
    }

    public boolean isWorldBlocked(String worldName) {
        return pluginConfig.blockWorld.contains(worldName);
    }

    public List<String> getBlockedCommands() {
        return pluginConfig.blockCmd;
    }

    public int getUnstuckCooldown() {
        return pluginConfig.cooldownUnstuck;
    }

    public Color getVisualizationColor(String type) {
        return Color.LIME;
    } // Temporary stub for v2 border

    public double getParticleSpacing() {
        return 0.5;
    } // Temporary stub

    public int getVisualizationUpdateInterval() {
        return 20;
    } // Temporary stub

    public int getWorldGuardGap() {
        return pluginConfig.worldguardGap;
    }

    public int getMinClaimGap() {
        return pluginConfig.minClaimGap;
    }

    public boolean logAutoSaveMessage() {
        return true;
    } // Temporary

    public String getDefaultVisualizationMode() {
        return "OFF";
    }

    public int getActionBarUpdateInterval() {
        return pluginConfig.actionbarUpdateInterval;
    }

    // --- MiniMessage formatting ---

    public String getMessage(String key, String... replacements) {
        String template = getRawMessageString(key);
        for (int i = 0; i < replacements.length; i += 2) {
            template = template.replace(replacements[i], replacements[i + 1]);
        }
        Component comp = MiniMessage.miniMessage().deserialize(pluginConfig.prefix + template);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getRawMessage(String key, String... replacements) {
        String template = getRawMessageString(key);
        for (int i = 0; i < replacements.length; i += 2) {
            template = template.replace(replacements[i], replacements[i + 1]);
        }
        Component comp = MiniMessage.miniMessage().deserialize(template);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getActionBarMessage(String key) {
        return getRawMessageString(key);
    }

    private String getRawMessageString(String key) {
        try {
            // Convert kebab-case to camelCase if needed
            String camelCaseKey = key;
            if (key.contains("-")) {
                StringBuilder sb = new StringBuilder();
                boolean nextUpper = false;
                for (char c : key.toCharArray()) {
                    if (c == '-') {
                        nextUpper = true;
                    } else if (nextUpper) {
                        sb.append(Character.toUpperCase(c));
                        nextUpper = false;
                    } else {
                        sb.append(c);
                    }
                }
                camelCaseKey = sb.toString();
            }

            var field = messagesConfig.getClass().getField(camelCaseKey);
            return (String) field.get(messagesConfig);
        } catch (Exception e) {
            return "<red>Message not found: " + key;
        }
    }
}