package org.ayosynk.landClaimPlugin.utils;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ConfigUpdater {
    public static void updateConfig(LandClaimPlugin plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
                return;
            }

            YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
            int currentVersion = currentConfig.getInt("config-version", 0);

            InputStream defaultStream = plugin.getResource("config.yml");
            if (defaultStream == null) return;

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            int defaultVersion = defaultConfig.getInt("config-version", 1);

            if (currentVersion < defaultVersion) {
                plugin.getLogger().info("Updating config from v" + currentVersion + " to v" + defaultVersion);

                File backup = new File(plugin.getDataFolder(), "config_old_v" + currentVersion + ".yml");
                Files.copy(configFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Backed up old config to " + backup.getName());

                Set<String> currentKeys = currentConfig.getKeys(true);
                for (String key : defaultConfig.getKeys(true)) {
                    if (!currentKeys.contains(key)) {
                        currentConfig.set(key, defaultConfig.get(key));
                    }
                }

                currentConfig.set("config-version", defaultVersion);

                currentConfig.save(configFile);
                plugin.getLogger().info("Config updated to version " + defaultVersion);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to update config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}