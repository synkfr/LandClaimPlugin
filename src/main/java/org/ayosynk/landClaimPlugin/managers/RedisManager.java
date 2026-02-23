package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisManager {

    private final LandClaimPlugin plugin;
    private JedisPool jedisPool;
    private JedisPubSub pubSub;

    public RedisManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        PluginConfig.RedisConfig config = plugin.getConfigManager().getPluginConfig().redis;
        if (!config.enabled) {
            return;
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);

        if (config.password != null && !config.password.trim().isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, config.host, config.port, 2000, config.password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, config.host, config.port, 2000);
        }

        plugin.getLogger().info("Connecting to Redis...");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            plugin.getLogger().info("Successfully connected to Redis.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to Redis: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        startSubscriber();
    }

    private void startSubscriber() {
        PluginConfig.RedisConfig config = plugin.getConfigManager().getPluginConfig().redis;

        this.pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equals(config.channel)) {
                    handleMessage(message);
                }
            }
        };

        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(pubSub, config.channel);
            } catch (Exception e) {
                if (!jedisPool.isClosed()) {
                    plugin.getLogger().warning("Redis subscriber disconnected. Attempting to restart...");
                    try {
                        Thread.sleep(5000);
                        startSubscriber();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private void handleMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2)
            return;

        String action = parts[0];
        String targetIdStr = parts[1];

        try {
            UUID targetId = UUID.fromString(targetIdStr);
            CacheManager cache = plugin.getCacheManager();

            switch (action) {
                case "INVALIDATE_CLAIM":
                    cache.getClaimCache().invalidate(targetId);
                    break;
                case "INVALIDATE_PLAYER":
                    cache.getPlayerCache().invalidate(targetId);
                    break;
                case "INVALIDATE_ROLE":
                    cache.getRoleCache().invalidate(targetId);
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Ignore invalid UUIDs
        }
    }

    public void publishUpdate(String action, UUID targetId) {
        if (jedisPool == null || jedisPool.isClosed())
            return;

        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String channel = plugin.getConfigManager().getPluginConfig().redis.channel;
                jedis.publish(channel, action + ":" + targetId.toString());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to publish Redis update: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        if (pubSub != null) {
            pubSub.unsubscribe();
        }
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            plugin.getLogger().info("Redis connection closed.");
        }
    }
}
