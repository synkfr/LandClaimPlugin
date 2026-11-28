package org.ayosynk.landClaimPlugin.managers;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.MessagesConfig;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.ayosynk.landClaimPlugin.config.menus.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ConfigManager {
    public static final String CURRENT_CONFIG_VERSION = "3.5.0";
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final LandClaimPlugin plugin;

    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;
    private MainMenuConfig mainMenuConfig;
    private ClaimSettingsConfig claimSettingsConfig;
    private VisitorSettingsConfig visitorSettingsConfig;
    private TrustManagementConfig trustManagementConfig;
    private PlayerTrustPermissionConfig playerTrustPermissionConfig;
    private MemberManagementConfig memberManagementConfig;
    private PlayerControlPanelConfig playerControlPanelConfig;
    private RoleSelectionConfig roleSelectionConfig;
    private WarpManagementConfig warpManagementConfig;
    private WarpControlPanelConfig warpControlPanelConfig;
    private WarpChangeIconConfig warpChangeIconConfig;
    private PublicWarpsConfig publicWarpsConfig;
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
    private final Set<String> bannedClaimNames = ConcurrentHashMap.newKeySet();
    private final Set<String> defaultVisitorFlags = ConcurrentHashMap.newKeySet();
    private final Map<String, Supplier<String>> messageIndex = new HashMap<>();

    public ConfigManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private <T extends OkaeriConfig> T loadConfig(Class<T> type, String relativePath) {
        return eu.okaeri.configs.ConfigManager.create(type, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), relativePath));
            it.saveDefaults();
            it.load(true);
        });
    }

    private void loadConfigs() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        boolean upgradedFromLegacy = handleVersionUpgradeBackup(configFile);
        String migratedPrefix = migrateLegacyPluginConfig(configFile);

        this.pluginConfig = loadConfig(PluginConfig.class, "config.yml");
        if (upgradedFromLegacy || !CURRENT_CONFIG_VERSION.equals(pluginConfig.general.configVersion)) {
            pluginConfig.general.configVersion = CURRENT_CONFIG_VERSION;
            pluginConfig.save();
        }

        List<String> configErrors = pluginConfig.validateConfig();
        if (!configErrors.isEmpty()) {
            plugin.getLogger().warning("Configuration validation found " + configErrors.size() + " issue(s):");
            for (String error : configErrors) {
                plugin.getLogger().warning("  - " + error);
            }
            plugin.getLogger().warning("Please fix the configuration errors in config.yml.");
        }

        String messagesPath = "locales/messages_" + pluginConfig.general.language + ".yml";
        File messagesFile = new File(plugin.getDataFolder(), messagesPath);
        migrateLegacyMessagesConfig(messagesFile, migratedPrefix);
        if (upgradedFromLegacy) {
            migrateRemainingLocales(messagesFile, migratedPrefix);
        }

        this.messagesConfig = loadConfig(MessagesConfig.class, messagesPath);
        rebuildMessageIndex();

        this.mainMenuConfig = loadConfig(MainMenuConfig.class, "menus/mainmenu.yml");
        this.profileSelectorConfig = loadConfig(ProfileSelectorConfig.class, "menus/profile-selector.yml");
        this.claimSettingsConfig = loadConfig(ClaimSettingsConfig.class, "menus/ClaimSettings.yml");
        this.visitorSettingsConfig = loadConfig(VisitorSettingsConfig.class, "menus/VisitorSettings.yml");
        ensureVisitorAdminFlags();

        this.trustManagementConfig = loadConfig(TrustManagementConfig.class, "menus/TrustManagement.yml");
        this.playerTrustPermissionConfig = loadConfig(PlayerTrustPermissionConfig.class, "menus/PlayerTrustPermission.yml");
        this.memberManagementConfig = loadConfig(MemberManagementConfig.class, "menus/MemberManagement.yml");
        this.playerControlPanelConfig = loadConfig(PlayerControlPanelConfig.class, "menus/PlayerControlPanel.yml");
        this.roleSelectionConfig = loadConfig(RoleSelectionConfig.class, "menus/RoleSelection.yml");
        this.warpManagementConfig = loadConfig(WarpManagementConfig.class, "menus/WarpManagement.yml");
        this.warpControlPanelConfig = loadConfig(WarpControlPanelConfig.class, "menus/WarpControlPanel.yml");
        this.warpChangeIconConfig = loadConfig(WarpChangeIconConfig.class, "menus/WarpChangeIcon.yml");
        this.publicWarpsConfig = loadConfig(PublicWarpsConfig.class, "menus/PublicWarps.yml");
        this.allyManagementConfig = loadConfig(AllyManagementConfig.class, "menus/AllyManagement.yml");
        this.allyControlPanelConfig = loadConfig(AllyControlPanelConfig.class, "menus/AllyControlPanel.yml");
        this.allyPremissionsConfig = loadConfig(AllyPremissionsConfig.class, "menus/AllyPremissions.yml");
        this.roleManagementConfig = loadConfig(RoleManagementConfig.class, "menus/RoleManagement.yml");
        this.roleSetupConfig = loadConfig(RoleSetupConfig.class, "menus/RoleSetup.yml");
        this.titleSettingsConfig = loadConfig(TitleSettingsConfig.class, "menus/TitleSettings.yml");
        this.renameClaimConfig = loadConfig(RenameClaimConfig.class, "menus/RenameClaim.yml");
        this.changeClaimColorConfig = loadConfig(ChangeClaimColorConfig.class, "menus/ChangeClaimColor.yml");
        this.roleEditConfig = loadConfig(RoleEditConfig.class, "menus/RoleEdit.yml");
        this.onlinePlayerSelectorConfig = loadConfig(OnlinePlayerSelectorConfig.class, "menus/OnlinePlayerSelector.yml");

        blockedCommands = pluginConfig.claiming.blockCmd.stream().map(String::toLowerCase).toList();
        blockedWorlds = pluginConfig.claiming.blockWorld.stream().map(String::toLowerCase).toList();
        loadBannedWords();
        loadDefaultVisitorFlags();
    }

    private void ensureVisitorAdminFlags() {
        boolean changed = false;
        if (!visitorSettingsConfig.flags.containsKey("CLAIM_LAND")) {
            visitorSettingsConfig.flags.put("CLAIM_LAND", new VisitorSettingsConfig.ItemConfig("GOLDEN_SHOVEL", "<color:#00E262>Claim Land", List.of("<color:#A0A0A0>Allow claiming land on behalf of the owner.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("ADMIN_MENU")) {
            visitorSettingsConfig.flags.put("ADMIN_MENU", new VisitorSettingsConfig.ItemConfig("COMMAND_BLOCK", "<color:#E2B700>Admin Menu Access", List.of("<color:#A0A0A0>Allow members to open /claim menu.", "<color:#A0A0A0>Does not permit abandoning the claim.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_SETTINGS")) {
            visitorSettingsConfig.flags.put("MANAGE_SETTINGS", new VisitorSettingsConfig.ItemConfig("COMPARATOR", "<color:#00E262>Manage Settings", List.of("<color:#A0A0A0>Allow changing claim toggles, color, and name.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_MEMBERS")) {
            visitorSettingsConfig.flags.put("MANAGE_MEMBERS", new VisitorSettingsConfig.ItemConfig("PLAYER_HEAD", "<color:#00E262>Manage Members", List.of("<color:#A0A0A0>Allow inviting and managing members or trust.")));
            changed = true;
        }
        if (!visitorSettingsConfig.flags.containsKey("MANAGE_ROLES")) {
            visitorSettingsConfig.flags.put("MANAGE_ROLES", new VisitorSettingsConfig.ItemConfig("WRITABLE_BOOK", "<color:#00E262>Manage Roles", List.of("<color:#A0A0A0>Allow creating, editing, and deleting roles.")));
            changed = true;
        }
        if (changed) {
            visitorSettingsConfig.save();
        }
    }

    public void reloadMainConfig() {
        org.ayosynk.landClaimPlugin.gui.GuiHelper.clearCache();
        pluginConfig.load();
        messagesConfig.load();
        rebuildMessageIndex();

        mainMenuConfig.load();
        claimSettingsConfig.load();
        visitorSettingsConfig.load();
        trustManagementConfig.load();
        playerTrustPermissionConfig.load();
        memberManagementConfig.load();
        playerControlPanelConfig.load();
        roleSelectionConfig.load();
        warpManagementConfig.load();
        warpControlPanelConfig.load();
        warpChangeIconConfig.load();
        publicWarpsConfig.load();
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
        profileSelectorConfig.load();

        blockedCommands = pluginConfig.claiming.blockCmd.stream().map(String::toLowerCase).toList();
        blockedWorlds = pluginConfig.claiming.blockWorld.stream().map(String::toLowerCase).toList();
        loadBannedWords();
        loadDefaultVisitorFlags();
    }

    public PluginConfig getPluginConfig() { return pluginConfig; }
    public MessagesConfig getMessagesConfig() { return messagesConfig; }
    public MainMenuConfig getMainMenuConfig() { return mainMenuConfig; }
    public ClaimSettingsConfig getClaimSettingsConfig() { return claimSettingsConfig; }
    public VisitorSettingsConfig getVisitorSettingsConfig() { return visitorSettingsConfig; }
    public TrustManagementConfig getTrustManagementConfig() { return trustManagementConfig; }
    public PlayerTrustPermissionConfig getPlayerTrustPermissionConfig() { return playerTrustPermissionConfig; }
    public MemberManagementConfig getMemberManagementConfig() { return memberManagementConfig; }
    public PlayerControlPanelConfig getPlayerControlPanelConfig() { return playerControlPanelConfig; }
    public RoleSelectionConfig getRoleSelectionConfig() { return roleSelectionConfig; }
    public WarpManagementConfig getWarpManagementConfig() { return warpManagementConfig; }
    public WarpControlPanelConfig getWarpControlPanelConfig() { return warpControlPanelConfig; }
    public WarpChangeIconConfig getWarpChangeIconConfig() { return warpChangeIconConfig; }
    public PublicWarpsConfig getPublicWarpsConfig() { return publicWarpsConfig; }
    public AllyManagementConfig getAllyManagementConfig() { return allyManagementConfig; }
    public AllyControlPanelConfig getAllyControlPanelConfig() { return allyControlPanelConfig; }
    public AllyPremissionsConfig getAllyPremissionsConfig() { return allyPremissionsConfig; }
    public RoleManagementConfig getRoleManagementConfig() { return roleManagementConfig; }
    public RoleSetupConfig getRoleSetupConfig() { return roleSetupConfig; }
    public TitleSettingsConfig getTitleSettingsConfig() { return titleSettingsConfig; }
    public RenameClaimConfig getRenameClaimConfig() { return renameClaimConfig; }
    public ChangeClaimColorConfig getChangeClaimColorConfig() { return changeClaimColorConfig; }
    public ProfileSelectorConfig getProfileSelectorConfig() { return profileSelectorConfig; }
    public RoleEditConfig getRoleEditConfig() { return roleEditConfig; }
    public OnlinePlayerSelectorConfig getOnlinePlayerSelectorConfig() { return onlinePlayerSelectorConfig; }

    public int getMaxMemberships() { return pluginConfig.limits.maxMemberships; }
    public boolean requireConnectedClaims() { return pluginConfig.claiming.requireConnectedClaims; }
    public boolean allowDiagonalConnections() { return pluginConfig.claiming.allowDiagonalConnections; }
    public boolean isWorldBlocked(String worldName) { return blockedWorlds.contains(worldName.toLowerCase()); }
    public List<String> getBlockedCommands() { return blockedCommands; }
    public List<String> getBlockedWorlds() { return blockedWorlds; }
    public boolean isMultiProfilesEnabled() { return pluginConfig.claiming.multiProfilesEnabled; }
    public int getMaxProfilesPerPlayer() { return pluginConfig.claiming.maxProfilesPerPlayer; }
    public int getUnstuckCooldown() { return pluginConfig.general.cooldownUnstuck; }
    public int getWorldGuardGap() { return pluginConfig.claiming.worldguardGap; }
    public int getMinClaimGap() { return pluginConfig.claiming.minClaimGap; }
    public boolean useSeparatePremission() { return pluginConfig.permissions.useSeparatePermission; }
    public String getDefaultVisualizationMode() { return "OFF"; }
    public boolean isActionBarEnabled() { return pluginConfig.display.actionbarEnabled; }
    public int getActionBarUpdateInterval() { return pluginConfig.display.actionbarUpdateInterval; }
    public boolean isClaimChatNotificationsEnabled() { return pluginConfig.display.claimChatNotifications; }

    public boolean isVisitorSettingsLocked() {
        return pluginConfig.visitorSettings != null && pluginConfig.visitorSettings.locked;
    }

    public boolean hasDefaultVisitorFlag(String flag) {
        return flag != null && defaultVisitorFlags.contains(flag.toUpperCase());
    }

    public Set<String> getDefaultVisitorFlags() {
        return Collections.unmodifiableSet(defaultVisitorFlags);
    }

    private void loadDefaultVisitorFlags() {
        defaultVisitorFlags.clear();
        if (pluginConfig.visitorSettings != null && pluginConfig.visitorSettings.defaultFlags != null) {
            for (String flag : pluginConfig.visitorSettings.defaultFlags) {
                if (flag != null && !flag.isBlank()) {
                    defaultVisitorFlags.add(flag.trim().toUpperCase());
                }
            }
        }
    }

    // --- MiniMessage formatting & indexed lookup ---

    private void rebuildMessageIndex() {
        messageIndex.clear();
        indexConfigFields("", messagesConfig);
        // Semantic aliases that differ from raw field names
        aliasKey("no-pending-invitations", "members.noPendingInvite");
        aliasKey("no-pending-invites", "members.noPendingInvite");
        aliasKey("visibility-mode-changed", "claiming.visualizationModeChanged");
        aliasKey("member-list-header", "members.listHeader");
        aliasKey("member-list-entry", "members.listEntry");
        aliasKey("member-list-empty", "members.listEmpty");
        aliasKey("trust-list-header", "trust.listHeader");
        aliasKey("trust-list-entry", "trust.listEntry");
        aliasKey("trust-list-empty", "trust.listEmpty");
        aliasKey("banlist-empty", "bans.listEmpty");
        aliasKey("banlist-header", "bans.listHeader");
        aliasKey("banlist-entry", "bans.listEntry");
    }

    private void aliasKey(String alias, String targetKey) {
        Supplier<String> target = messageIndex.get(targetKey.toLowerCase(Locale.ROOT));
        if (target != null) {
            messageIndex.putIfAbsent(alias.toLowerCase(Locale.ROOT), target);
        }
    }

    private void indexConfigFields(String prefix, Object instance) {
        for (Field field : instance.getClass().getFields()) {
            try {
                if (OkaeriConfig.class.isAssignableFrom(field.getType())) {
                    Object child = field.get(instance);
                    if (child != null) {
                        String nextPrefix = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
                        indexConfigFields(nextPrefix, child);
                    }
                } else if (field.getType() == String.class) {
                    String fieldName = field.getName();
                    Supplier<String> reader = () -> {
                        try {
                            return (String) field.get(instance);
                        } catch (IllegalAccessException e) {
                            return "";
                        }
                    };
                    String dotted = (prefix.isEmpty() ? fieldName : prefix + "." + fieldName).toLowerCase(Locale.ROOT);
                    String dottedKebab = (prefix.isEmpty() ? toKebab(fieldName) : toKebab(prefix) + "." + toKebab(fieldName)).toLowerCase(Locale.ROOT);
                    messageIndex.put(dotted, reader);
                    messageIndex.put(dottedKebab, reader);
                    // Short field-name & flat kebab-case compatibility for callers
                    messageIndex.putIfAbsent(fieldName.toLowerCase(Locale.ROOT), reader);
                    messageIndex.putIfAbsent(toKebab(fieldName).toLowerCase(Locale.ROOT), reader);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static String toKebab(String camel) {
        StringBuilder sb = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('-').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String applyPlaceholders(String template, String... replacements) {
        if (template == null) return "";
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            String placeholder = replacements[i];
            String value = replacements[i + 1] != null ? replacements[i + 1] : "";
            template = template.replace(placeholder, value);
            if (placeholder.startsWith("<") && placeholder.endsWith(">")) {
                template = template.replace("{" + placeholder.substring(1, placeholder.length() - 1) + "}", value);
            } else if (placeholder.startsWith("{") && placeholder.endsWith("}")) {
                template = template.replace("<" + placeholder.substring(1, placeholder.length() - 1) + ">", value);
            }
        }
        return template;
    }

    /** Prefixed MiniMessage Component preserving click, hover, and 24-bit hex tags. */
    public Component getMessage(String key, String... replacements) {
        String template = applyPlaceholders(getRawMessageString(key), replacements);
        String prefix = messagesConfig.prefix != null ? messagesConfig.prefix : "";
        return MM.deserialize(prefix + template);
    }

    /** Unprefixed MiniMessage Component for GUI titles, lore, actionbars, and custom links. */
    public Component getRawComponent(String key, String... replacements) {
        String template = applyPlaceholders(getRawMessageString(key), replacements);
        return MM.deserialize(template);
    }

    /** Unprefixed serialized string for PlaceholderAPI and Bedrock form text. */
    public String getRawMessage(String key, String... replacements) {
        return LegacyComponentSerializer.legacySection().serialize(getRawComponent(key, replacements));
    }

    /** Raw template string for actionbars and GUI formatting that supply extra placeholders. */
    public String getRawTemplate(String key, String... replacements) {
        return applyPlaceholders(getRawMessageString(key), replacements);
    }

    public String getActionBarMessage(String key) {
        return getRawMessageString(key);
    }

    private String getRawMessageString(String key) {
        if (key == null) return "";
        Supplier<String> reader = messageIndex.get(key.toLowerCase(Locale.ROOT));
        if (reader != null) {
            String value = reader.get();
            return value != null ? value : "";
        }
        return "<color:#E20000>Message not found: " + key;
    }

    // --- Upgrade backup & legacy flat-to-grouped YAML migration ---

    @SuppressWarnings("unchecked")
    private boolean handleVersionUpgradeBackup(File configFile) {
        if (!configFile.exists()) return false;
        try {
            String content = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            Object loaded = new Yaml().load(content);
            if (loaded instanceof Map<?, ?> raw && raw.get("general") instanceof Map<?, ?> general) {
                Object ver = general.get("configVersion");
                if (CURRENT_CONFIG_VERSION.equals(String.valueOf(ver))) {
                    return false;
                }
            }

            copyBackupFile(configFile, new File(plugin.getDataFolder(), "config.yml.bk"));
            backupDirectoryYmlFiles(new File(plugin.getDataFolder(), "locales"), false);
            backupDirectoryYmlFiles(new File(plugin.getDataFolder(), "menus"), true);
            plugin.getLogger().info("Upgrading configuration to " + CURRENT_CONFIG_VERSION
                    + ": created .bk backups (config.yml.bk, locales/*.yml.bk, menus/*.yml.bk) and refreshed menu defaults.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create pre-upgrade backups: " + e.getMessage());
            return false;
        }
    }

    private void backupDirectoryYmlFiles(File dir, boolean deleteOriginalAfterBackup) throws IOException {
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            copyBackupFile(file, new File(dir, file.getName() + ".bk"));
            if (deleteOriginalAfterBackup) {
                file.delete();
            }
        }
    }

    private void copyBackupFile(File source, File backup) throws IOException {
        Files.copy(source.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void migrateRemainingLocales(File activeMessagesFile, String migratedPrefix) {
        File localesDir = new File(plugin.getDataFolder(), "locales");
        if (!localesDir.isDirectory()) return;
        File[] files = localesDir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            if (!file.equals(activeMessagesFile)) {
                migrateLegacyMessagesConfig(file, migratedPrefix);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String migrateLegacyPluginConfig(File file) {
        if (!file.exists()) return null;
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(content);
            if (!(loaded instanceof Map<?, ?> raw)) return null;
            Map<String, Object> root = new LinkedHashMap<>((Map<String, Object>) raw);

            String migratedPrefix = null;
            if (root.containsKey("prefix") && root.get("prefix") instanceof String s) {
                migratedPrefix = s;
                root.remove("prefix");
            }

            boolean dirty = migratedPrefix != null;
            dirty |= moveToGroup(root, "general", "language", "bannedClaimNamesFile", "cooldownUnstuck", "geyserForms");
            dirty |= moveToGroup(root, "claiming", "requireConnectedClaims", "allowDiagonalConnections",
                    "worldguardGap", "minClaimGap", "multiProfilesEnabled", "maxProfilesPerPlayer",
                    "blockWorld", "blockCmd");
            dirty |= moveToGroup(root, "limits", "chunkClaimLimit", "maxBonusClaimBlocks",
                    "maxTotalClaimLimit", "maxMemberships", "maxClaimMembers", "maxWarps");
            dirty |= moveToGroup(root, "display", "actionbarEnabled", "actionbarUpdateInterval", "claimChatNotifications");
            dirty |= moveToGroup(root, "maps", "dynmap", "bluemap", "squaremap", "pl3xmap");

            if (root.containsKey("useSeparatePremission")) {
                Object val = root.remove("useSeparatePremission");
                Map<String, Object> perms = getOrCreateMap(root, "permissions");
                perms.putIfAbsent("useSeparatePermission", val);
                dirty = true;
            }

            if (dirty) {
                writeYaml(file, root);
            }
            return migratedPrefix;
        } catch (Exception e) {
            plugin.getLogger().warning("Could not migrate legacy config.yml keys: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void migrateLegacyMessagesConfig(File file, String prefixFromMainConfig) {
        if (!file.exists()) {
            if (prefixFromMainConfig != null) {
                try {
                    Files.createDirectories(file.getParentFile().toPath());
                    Map<String, Object> seed = new LinkedHashMap<>();
                    seed.put("prefix", prefixFromMainConfig);
                    writeYaml(file, seed);
                } catch (IOException ignored) {
                }
            }
            return;
        }
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(content);
            if (!(loaded instanceof Map<?, ?> raw)) return;
            Map<String, Object> root = new LinkedHashMap<>((Map<String, Object>) raw);

            boolean dirty = false;
            if (prefixFromMainConfig != null && !root.containsKey("prefix")) {
                root.put("prefix", prefixFromMainConfig);
                dirty = true;
            }

            dirty |= moveToGroup(root, "general", "accessDenied", "noPermission", "wildernessProtected", "playerNotFound", "playerNotOnline", "invalidCommand", "inCombat", "reloaded");
            dirty |= moveToGroup(root, "claiming", "chunkClaimed", "chunkUnclaimed", "claimRadiusSuccess", "unclaimRadiusSuccess", "invalidRadius", "worldBlocked", "alreadyClaimed", "claimLimitReached", "notConnected", "tooCloseToWorldguard", "tooCloseToOtherClaim", "notInClaim", "notOwner", "autoClaimEnabled", "autoClaimDisabled", "autoUnclaimEnabled", "autoUnclaimDisabled", "invalidVisualizationMode", "visualizationModeChanged", "claimVisibilityToggled", "claimInfoOwner");
            dirty |= moveToGroup(root, "profile", "noProfile", "noProfileFound", "cannotClaimAsMember", "profileCreated", "profileChanged", "profileLimitReached", "multiProfilesDisabled", "alreadyHasProfile", "profileAbandoned", "unclaimAllConfirm", "claimRenamed", "claimNameInvalid", "bannedClaimName", "nameAlreadyInUse", "claimColorChanged", "claimColorInvalid");
            dirty |= moveToGroup(root, "members", "cannotInviteSelf", "alreadyMember", "alreadyInClaim", "targetAlreadyInClaim", "memberInvited", "memberKicked", "youWereKicked", "notAMember", "inviteExpired", "inviteReceived", "inviteAccepted", "inviteDenied", "inviteWasDenied", "memberJoined", "claimLeft", "notInThatClaim", "memberLeftClaim");
            dirty |= moveRenamedKey(root, "noPendingInvites", "members", "noPendingInvite");
            dirty |= moveRenamedKey(root, "memberListHeader", "members", "listHeader");
            dirty |= moveRenamedKey(root, "memberListEntry", "members", "listEntry");
            dirty |= moveRenamedKey(root, "memberListEmpty", "members", "listEmpty");
            dirty |= moveToGroup(root, "trust", "cannotTrustSelf", "alreadyTrusted", "notTrusted", "trustInvited", "trustInviteReceived", "trustInviteAccepted", "trustInviteDenied", "trustInviteWasDenied", "noPendingTrustInvite", "trustAdded", "trustRemoved");
            dirty |= moveRenamedKey(root, "trustListHeader", "trust", "listHeader");
            dirty |= moveRenamedKey(root, "trustListEntry", "trust", "listEntry");
            dirty |= moveRenamedKey(root, "trustListEmpty", "trust", "listEmpty");
            dirty |= moveToGroup(root, "allies", "claimNotFound", "cannotAllySelf", "alreadyAllied", "notAllied", "allyInviteSent", "allyInviteReceived", "noAllyInviteFrom", "specifyAllyName", "allyAccepted", "allyAcceptedTarget", "allyDenied", "allyRemoved");
            dirty |= moveToGroup(root, "bans", "cannotBanSelf", "alreadyBanned", "notBanned", "playerBanned", "bannedFromClaim", "playerUnbanned", "unbannedByOwner", "bannedTeleportedOut", "bannedNoEscape");
            dirty |= moveRenamedKey(root, "banlistEmpty", "bans", "listEmpty");
            dirty |= moveRenamedKey(root, "banlistHeader", "bans", "listHeader");
            dirty |= moveRenamedKey(root, "banlistEntry", "bans", "listEntry");
            dirty |= moveToGroup(root, "warps", "notInOwnClaim", "warpSet", "warpDeleted", "warpTeleport", "warpNotFound", "warpLimitReached", "warpLocationUpdated", "warpIconUpdated", "warpMadePublic", "warpMadePrivate", "publicwarpsNoWarps", "publicwarpsTeleported");
            dirty |= moveToGroup(root, "protection", "bucketDenied", "commandBlocked", "visitorSettingsLocked", "pvpDenied", "pvpEnabled", "pvpEnabledTemp", "pvpDisabled", "pvpForceLocked", "tntEnabled", "tntDisabled", "mobSpawningEnabled", "mobSpawningDisabled", "unstuckCooldown", "unstuckSuccess", "unstuckNotInClaim", "unstuckSearching", "unstuckFailed");
            dirty |= moveToGroup(root, "titles", "titleUpdated", "titleCancelled");
            dirty |= moveToGroup(root, "chatNotifications", "claimEnterChat", "claimLeaveChat", "wildernessEnterChat", "claimNotifyEnabled", "claimNotifyDisabled");
            dirty |= moveToGroup(root, "actionbar", "actionbarWilderness", "actionbarOwnedByYou", "actionbarOwnedByOther", "actionbarTrusted", "actionbarAdmin", "actionbarBanned");
            dirty |= moveToGroup(root, "economy", "limitPurchasesDisabled", "maxBonusClaimBlocksReached", "maxTotalClaimLimitReached");
            dirty |= moveToGroup(root, "admin", "adminChunkClaimed", "adminAlreadyClaimed", "adminBypassedUnclaim", "adminClaimInfoOwnedBy", "adminClaimInfoId", "adminEditingProfile", "adminAddChunkSuccess", "adminSetChunkSuccess", "adminRemoveChunkSuccess", "adminResetChunkSuccess", "adminDecayRunStarted", "adminDecayRunFinished", "adminDecayExemptSet");
            dirty |= moveToGroup(root, "gui", "renameCancelled");
            dirty |= moveToGroup(root, "bedrock", "geyserChatPrompt", "geyserChatCancelled", "geyserFormSent", "geyserFormCancelled", "geyserConfirmTitle", "geyserAbandonContent", "geyserUnclaimAllContent", "geyserBanContent");

            // Drop dead legacy keys no longer backed by code
            for (String dead : List.of("adminTrustAdded", "adminTrustRemoved", "memberInviteSent", "allyAdded",
                    "claimInfoManage", "wandGiven", "wandPos1", "wandPos2", "accessDeniedInteract",
                    "autoUnclaimed", "harmEntityDenied", "roleRemoved", "roleAssigned", "claimListTotal",
                    "claimListWorld", "claimListEntry", "claimInfoTrusted", "memberInviteReceived",
                    "inviteAcceptedOwner", "inviteAcceptedTarget", "inviteDeniedTarget", "renamePrompt",
                    "colorHexPrompt", "visibilityModeChanged", "titleEnterPrompt", "titleLeavePrompt",
                    "adminDecayDisabled")) {
                dirty |= (root.remove(dead) != null);
            }

            if (dirty) {
                writeYaml(file, root);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not migrate legacy messages config keys: " + e.getMessage());
        }
    }

    private boolean moveToGroup(Map<String, Object> root, String group, String... keys) {
        boolean moved = false;
        for (String key : keys) {
            if (root.containsKey(key)) {
                Object value = root.remove(key);
                getOrCreateMap(root, group).putIfAbsent(key, value);
                moved = true;
            }
        }
        return moved;
    }

    private boolean moveRenamedKey(Map<String, Object> root, String oldKey, String group, String newKey) {
        if (!root.containsKey(oldKey)) return false;
        Object value = root.remove(oldKey);
        getOrCreateMap(root, group).putIfAbsent(newKey, value);
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateMap(Map<String, Object> root, String key) {
        Object existing = root.get(key);
        if (existing instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        Map<String, Object> created = new LinkedHashMap<>();
        root.put(key, created);
        return created;
    }

    private void writeYaml(File file, Map<String, Object> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        String out = new Yaml(options).dump(data);
        Files.writeString(file.toPath(), out, StandardCharsets.UTF_8);
    }

    public void loadBannedWords() {
        bannedClaimNames.clear();
        String fileName = pluginConfig.general.bannedClaimNamesFile;
        if (fileName == null || fileName.isEmpty()) {
            fileName = "banned-claim-name.txt";
        }
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                Files.write(file.toPath(), List.of(
                        "# LandClaimPlugin - Banned Claim Names",
                        "# Words or phrases (one per line) rejected in claim names.",
                        "# Empty lines and lines starting with '#' are ignored.",
                        "",
                        "slur1",
                        "slur2",
                        "offensiveword1",
                        "offensiveword2",
                        "badword"
                ));
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create default " + fileName + ": " + e.getMessage());
            }
        }

        if (file.exists()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        bannedClaimNames.add(line.toLowerCase());
                    }
                }
                plugin.getLogger().info("Loaded " + bannedClaimNames.size() + " banned claim name words from " + fileName);
            } catch (IOException e) {
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
