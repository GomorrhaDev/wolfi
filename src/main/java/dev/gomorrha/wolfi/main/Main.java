package dev.gomorrha.wolfi.main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    private Map<UUID, Long> timerMap = new HashMap<>();
    private Map<UUID, Wolf> wolfMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Wolfi plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Wolfi plugin disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (timerMap.containsKey(playerId)) {
            long timeDiff = System.currentTimeMillis() - timerMap.get(playerId);
            int seconds = (int) (timeDiff / 1000);
            player.sendMessage(ChatColor.GREEN + "Your wolf " + wolfMap.get(playerId).getName() + " has been alive for " + seconds + " seconds.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (wolfMap.containsKey(playerId)) {
            wolfMap.get(playerId).remove();
            wolfMap.remove(playerId);
            timerMap.remove(playerId);
        }
    }

    @EventHandler
    public void onWolfDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.WOLF) {
            Wolf wolf = (Wolf) event.getEntity();
            UUID ownerId = wolf.getOwner().getUniqueId();

            if (wolfMap.containsKey(ownerId) && wolfMap.get(ownerId).equals(wolf)) {
                Player player = Bukkit.getPlayer(ownerId);
                player.setHealth(0);
            }
        }
    }

    public void spawnWolf(Player player) {
        UUID playerId = player.getUniqueId();

        if (wolfMap.containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "You already have a wolf!");
            return;
        }

        Location spawnLocation = player.getLocation();
        Wolf wolf = (Wolf) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.WOLF);
        wolf.setTamed(true);
        wolf.setOwner(player);
        wolf.setCustomName(ChatColor.GREEN + player.getName() + "'s Wolfi");
        wolf.setCustomNameVisible(true);

        wolfMap.put(playerId, wolf);
        timerMap.put(playerId, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "Your wolf " + wolf.getName() + " has been spawned!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("wolfi")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
                spawnWolf(player);
                return true;
            }

            return false;
        }

        return false;
    }
}