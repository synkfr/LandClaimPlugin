package org.ayosynk.landClaimPlugin.managers;

import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.ayosynk.landClaimPlugin.config.menus.RoleManagementConfig;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import org.ayosynk.landClaimPlugin.config.menus.RoleEditConfig;
import org.ayosynk.landClaimPlugin.config.menus.TitleSettingsConfig;
import org.ayosynk.landClaimPlugin.config.menus.OnlinePlayerSelectorConfig;
import org.ayosynk.landClaimPlugin.config.menus.ProfileSelectorConfig;
import org.ayosynk.landClaimPlugin.config.menus.RenameClaimConfig;
import org.ayosynk.landClaimPlugin.config.menus.ChangeClaimColorConfig;
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
    private RoleManagementConfig roleManagementConfig;
    private RoleSetupConfig roleSetupConfig;
    private TitleSettingsConfig titleSettingsConfig;
    private RenameClaimConfig renameClaimConfig;
    private ChangeClaimColorConfig changeClaimColorConfig;
    private RoleEditConfig roleEditConfig;
    private OnlinePlayerSelectorConfig onlinePlayerSelectorConfig;
    private ProfileSelectorConfig profileSelectorConfig;

    private List<String> blockedCommands = List.of();
    private List<String> blockedWorlds = List.of();
    private final java.util.Set<String> bannedClaimNames = new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet(Boolean.TRUE);

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
        
        // Validate configuration
        java.util.List<String> configErrors = pluginConfig.validateConfig();
        if (!configErrors.isEmpty()) {
            plugin.getLogger().warning("Configuration validation found " + configErrors.size() + " issue(s):");
            for (String error : configErrors) {
                plugin.getLogger().warning("  - " + error);
            }
            plugin.getLogger().warning("Please fix the configuration errors in config.yml.");
        }

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

        this.profileSelectorConfig = eu.okaeri.configs.ConfigManager.create(ProfileSelectorConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/profile-selector.yml"));
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

        // Ensure new flags exist to fix backward compatibility for servers updating from older versions
        boolean changed = false;
        if (!visitorSettingsConfig.flags.containsKey("CLAIM_LAND")) {
            visitorSettingsConfig.flags.put("CLAIM_LAND", new VisitorSettingsConfig.ItemConfig("GOLDEN_SHOVEL", "<yellow>Claim Land", java.util.List.of("<gray>Allow claiming land on behalf of owner.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("ADMIN_MENU")) {
            visitorSettingsConfig.flags.put("ADMIN_MENU", new VisitorSettingsConfig.ItemConfig("COMMAND_BLOCK", "<gold>Admin Menu Access", java.util.List.of("<gray>Allow members to open the /claim menu.", "<gray>Does not allow abandoning the claim.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_SETTINGS")) {
            visitorSettingsConfig.flags.put("MANAGE_SETTINGS", new VisitorSettingsConfig.ItemConfig("COMPARATOR", "<yellow>Manage Settings", java.util.List.of("<gray>Allow changing claim settings (PvP, Color, Toggles).")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_MEMBERS")) {
            visitorSettingsConfig.flags.put("MANAGE_MEMBERS", new VisitorSettingsConfig.ItemConfig("PLAYER_HEAD", "<yellow>Manage Members", java.util.List.of("<gray>Allow adding/removing members and trusted players.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_ROLES")) {
            visitorSettingsConfig.flags.put("MANAGE_ROLES", new VisitorSettingsConfig.ItemConfig("WRITABLE_BOOK", "<yellow>Manage Roles", java.util.List.of("<gray>Allow creating, editing, and deleting roles.")));
            changed = true;
        }

        if (changed) {
            visitorSettingsConfig.save();
        }

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

        this.roleManagementConfig = eu.okaeri.configs.ConfigManager.create(RoleManagementConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/RoleManagement.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.roleSetupConfig = eu.okaeri.configs.ConfigManager.create(RoleSetupConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/RoleSetup.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.titleSettingsConfig = eu.okaeri.configs.ConfigManager.create(TitleSettingsConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/TitleSettings.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.renameClaimConfig = eu.okaeri.configs.ConfigManager.create(RenameClaimConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/RenameClaim.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.changeClaimColorConfig = eu.okaeri.configs.ConfigManager.create(ChangeClaimColorConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/ChangeClaimColor.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.roleEditConfig = eu.okaeri.configs.ConfigManager.create(RoleEditConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/RoleEdit.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.onlinePlayerSelectorConfig = eu.okaeri.configs.ConfigManager.create(OnlinePlayerSelectorConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "menus/OnlinePlayerSelector.yml"));
            it.saveDefaults();
            it.load(true);
        });

        // Populate blocked lists on initial load (not just on reload)
        blockedCommands = pluginConfig.blockCmd.stream().map(String::toLowerCase).toList();
        blockedWorlds = pluginConfig.blockWorld.stream().map(String::toLowerCase).toList();
        loadBannedWords();
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
        roleManagementConfig.load();
        roleSetupConfig.load();
        titleSettingsConfig.load();
        renameClaimConfig.load();
        changeClaimColorConfig.load();
        roleEditConfig.load();
        onlinePlayerSelectorConfig.load();

        blockedCommands = pluginConfig.blockCmd.stream().map(String::toLowerCase).toList();
        blockedWorlds = pluginConfig.blockWorld.stream().map(String::toLowerCase).toList();
        loadBannedWords();
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

    public RoleManagementConfig getRoleManagementConfig() {
        return roleManagementConfig;
    }

    public RoleSetupConfig getRoleSetupConfig() {
        return roleSetupConfig;
    }

    public TitleSettingsConfig getTitleSettingsConfig() {
        return titleSettingsConfig;
    }

    public RenameClaimConfig getRenameClaimConfig() {
        return renameClaimConfig;
    }

    public int getMaxMemberships() {
        return pluginConfig.maxMemberships;
    }

    public ChangeClaimColorConfig getChangeClaimColorConfig() {
        return changeClaimColorConfig;
    }

    public ProfileSelectorConfig getProfileSelectorConfig() {
        return profileSelectorConfig;
    }

    public RoleEditConfig getRoleEditConfig() {
        return roleEditConfig;
    }

    public OnlinePlayerSelectorConfig getOnlinePlayerSelectorConfig() {
        return onlinePlayerSelectorConfig;
    }

    // --- Legacy wrapper methods to bridge until other classes are transformed ---

    public boolean requireConnectedClaims() {
        return pluginConfig.requireConnectedClaims;
    }

    public boolean allowDiagonalConnections() {
        return pluginConfig.allowDiagonalConnections;
    }

    public boolean isWorldBlocked(String worldName) {
        return blockedWorlds.contains(worldName.toLowerCase());
    }

    public List<String> getBlockedCommands() {
        return blockedCommands;
    }

    public List<String> getBlockedWorlds() {
        return blockedWorlds;
    }

    public boolean isMultiProfilesEnabled() {
        return pluginConfig.multiProfilesEnabled;
    }

    public int getMaxProfilesPerPlayer() {
        return pluginConfig.maxProfilesPerPlayer;
    }

    public int getUnstuckCooldown() {
        return pluginConfig.cooldownUnstuck;
    }

    @Deprecated // TODO: Implement per-profile visualization colors
    public Color getVisualizationColor(String type) {
        return Color.LIME;
    }

    @Deprecated // TODO: Implement configurable particle spacing
    public double getParticleSpacing() {
        return 0.5;
    }

    @Deprecated // TODO: Implement configurable visualization update interval
    public int getVisualizationUpdateInterval() {
        return 20;
    }

    public int getWorldGuardGap() {
        return pluginConfig.worldguardGap;
    }

    public int getMinClaimGap() {
        return pluginConfig.minClaimGap;
    }

    public boolean useSeparatePremission() {
        return pluginConfig.useSeparatePremission;
    }

    @Deprecated // TODO: Implement auto-save logging configuration
    public boolean logAutoSaveMessage() {
        return true;
    }

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
            String placeholder = replacements[i];
            String value = replacements[i + 1];
            template = template.replace(placeholder, value);
            
            // Support alternate bracket style if placeholder starts with < or {
            if (placeholder.startsWith("<") && placeholder.endsWith(">")) {
                String alt = "{" + placeholder.substring(1, placeholder.length() - 1) + "}";
                template = template.replace(alt, value);
            } else if (placeholder.startsWith("{") && placeholder.endsWith("}")) {
                String alt = "<" + placeholder.substring(1, placeholder.length() - 1) + ">";
                template = template.replace(alt, value);
            }
        }
        Component comp = MiniMessage.miniMessage().deserialize(pluginConfig.prefix + template);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getRawMessage(String key, String... replacements) {
        String template = getRawMessageString(key);
        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = replacements[i];
            String value = replacements[i + 1];
            template = template.replace(placeholder, value);

            // Support alternate bracket style
            if (placeholder.startsWith("<") && placeholder.endsWith(">")) {
                String alt = "{" + placeholder.substring(1, placeholder.length() - 1) + "}";
                template = template.replace(alt, value);
            } else if (placeholder.startsWith("{") && placeholder.endsWith("}")) {
                String alt = "<" + placeholder.substring(1, placeholder.length() - 1) + ">";
                template = template.replace(alt, value);
            }
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

    public void loadBannedWords() {
        bannedClaimNames.clear();
        String fileName = pluginConfig.bannedClaimNamesFile;
        if (fileName == null || fileName.isEmpty()) {
            fileName = "banned-claim-name.txt";
        }
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                java.nio.file.Files.write(file.toPath(), java.util.List.of(
                    "# LandClaimPlugin - Banned Claim Names",
                    "# Add words or phrases (one per line) that cannot be used in claim profile names.",
                    "# Any claim name containing these words (case-insensitive) will be rejected.",
                    "# Empty lines and lines starting with '#' are ignored.",
                    "",
                    "slur1",
                    "slur2",
                    "offensiveword1",
                    "offensiveword2",
                    "badword"
                ));
            } catch (java.io.IOException e) {
                plugin.getLogger().severe("Failed to create default " + fileName + ": " + e.getMessage());
            }
        }

        if (file.exists()) {
            try {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    bannedClaimNames.add(line.toLowerCase());
                }
                plugin.getLogger().info("Loaded " + bannedClaimNames.size() + " banned claim name words from " + fileName);
            } catch (java.io.IOException e) {
                plugin.getLogger().severe("Failed to read " + fileName + ": " + e.getMessage());
            }
        }
    }

    public boolean isBannedClaimName(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        for (String banned : bannedClaimNames) {
            if (lower.contains(banned)) {
                return true;
            }
        }
        return false;
    }
}