package org.ayosynk.landClaimPlugin.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateManager {

    private final LandClaimPlugin plugin;
    private volatile String latestVersion = null;
    private volatile String updateUrl = null;
    private volatile boolean updateAvailable = false;

    public UpdateManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfigManager().getPluginConfig().updateChecker.enabled) {
            return;
        }

        String projectId = plugin.getConfigManager().getPluginConfig().updateChecker.modrinthProjectId;
        if (projectId == null || projectId.isBlank()) {
            return;
        }

        FoliaScheduler.runAsync(plugin, () -> {
            try {
                String currentVersion = plugin.getPluginMeta().getVersion();
                String apiUrl = "https://api.modrinth.com/v2/project/" + projectId + "/version";

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("User-Agent", "synkfr/LandClaimPlugin/" + currentVersion)
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonElement jsonElement = JsonParser.parseString(response.body());
                    if (jsonElement.isJsonArray()) {
                        JsonArray versionsArray = jsonElement.getAsJsonArray();
                        if (!versionsArray.isEmpty()) {
                            JsonObject latestObject = versionsArray.get(0).getAsJsonObject();
                            String remoteVersion = latestObject.get("version_number").getAsString();
                            String versionId = latestObject.has("id") ? latestObject.get("id").getAsString() : "";

                            if (isNewerVersion(currentVersion, remoteVersion)) {
                                this.updateAvailable = true;
                                this.latestVersion = remoteVersion;
                                this.updateUrl = "https://modrinth.com/plugin/landclaimplugin/version/" + (versionId.isEmpty() ? remoteVersion : versionId);
                                plugin.getLogger().info("A new version of LandClaimPlugin is available on Modrinth: " + remoteVersion + " (Current: " + currentVersion + ")");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates on Modrinth: " + e.getMessage());
            }
        });
    }

    public void notifyPlayerIfUpdateAvailable(Player player) {
        if (!updateAvailable || !plugin.getConfigManager().getPluginConfig().updateChecker.enabled) {
            return;
        }

        String permission = plugin.getConfigManager().getPluginConfig().updateChecker.notifyPermission;
        if (player.isOp() || (permission != null && !permission.isEmpty() && player.hasPermission(permission))) {
            String currentVersion = plugin.getPluginMeta().getVersion();
            String targetUrl = updateUrl != null ? updateUrl : "https://modrinth.com/plugin/landclaimplugin";

            Component notification = MiniMessage.miniMessage().deserialize(
                    "<gold><bold>[LandClaim]</bold></gold> <yellow>A new version is available on Modrinth!</yellow>\n" +
                    "<gray>Current: <red>" + currentVersion + "</red> ➔ Latest: <green>" + latestVersion + "</green></gray>\n" +
                    "<click:open_url:'" + targetUrl + "'><hover:show_text:'<gray>Click to view release on Modrinth</gray>'><aqua><u>[Click here to download update]</u></aqua></hover></click>"
            );
            player.sendMessage(notification);
        }
    }

    private boolean isNewerVersion(String currentVersion, String remoteVersion) {
        if (currentVersion == null || remoteVersion == null) {
            return false;
        }

        String cleanCurrent = cleanVersion(currentVersion);
        String cleanRemote = cleanVersion(remoteVersion);

        if (cleanCurrent.equalsIgnoreCase(cleanRemote)) {
            return false;
        }

        String[] currentParts = cleanCurrent.split("\\.");
        String[] remoteParts = cleanRemote.split("\\.");

        int length = Math.max(currentParts.length, remoteParts.length);
        for (int i = 0; i < length; i++) {
            int currentPart = 0;
            int remotePart = 0;

            if (i < currentParts.length) {
                try {
                    currentPart = Integer.parseInt(currentParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }

            if (i < remoteParts.length) {
                try {
                    remotePart = Integer.parseInt(remoteParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }

            if (remotePart > currentPart) {
                return true;
            } else if (remotePart < currentPart) {
                return false;
            }
        }

        return false;
    }

    private String cleanVersion(String version) {
        String clean = version.trim();
        if (clean.startsWith("v") || clean.startsWith("V")) {
            clean = clean.substring(1);
        }
        int dashIndex = clean.indexOf('-');
        if (dashIndex != -1) {
            clean = clean.substring(0, dashIndex);
        }
        return clean;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }
}
