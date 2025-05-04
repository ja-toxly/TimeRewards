package me.toxly.timerewards;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimeRewards extends JavaPlugin implements Listener {

    private final Map<UUID, Long> lastRewardTime = new HashMap<>();
    private long rewardInterval;

    @Override
    public void onEnable() {

        int pluginId = 25571;
        new MetricsLite(this, pluginId);

        saveDefaultConfig();
        FileConfiguration config = getConfig();
        rewardInterval = config.getLong("rewardInterval", 3600000);

        getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskTimer(this, new RewardTask(), 18000L, 18000L);

        getLogger().info("RanginAgroda został włączony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RanginAgroda został wyłączony!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        lastRewardTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastRewardTime.remove(event.getPlayer().getUniqueId());
    }

    private class RewardTask implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                long lastTime = lastRewardTime.getOrDefault(uuid, now);
                if ((now - lastTime) >= rewardInterval) {
                    FileConfiguration config = getConfig();
                    boolean rewarded = false;

                    for (int i = 1; i <= 5; i++) {
                        String perm = "rangaboost." + i;
                        if (player.hasPermission(perm)) {
                            String path = "rewards." + perm;
                            if (config.isConfigurationSection(path)) {
                                String command = config.getString(path + ".command", "");
                                String executor = config.getString(path + ".executor", "player").toLowerCase();

                                command = command.replace("%player%", player.getName());

                                if (!command.isEmpty()) {
                                    if (executor.equals("console")) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                    } else {

                                        player.performCommand(command);
                                    }
                                    rewarded = true;

                                    break;
                                }
                            }
                        }
                    }

                    if (rewarded) {
                        lastRewardTime.put(uuid, now);
                    }
                }
            }
        }
    }
}
