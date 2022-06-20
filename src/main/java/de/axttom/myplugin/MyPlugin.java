package de.axttom.myplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class MyPlugin extends JavaPlugin implements Listener, CommandExecutor {
    private final HashMap<UUID, Boolean> damage = new HashMap<>();
    private final HashMap<UUID, Boolean> vanish = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("fly")) {
            final String prefix =
                    ChatColor.GRAY + "[" +
                    ChatColor.WHITE + "Fly" +
                    ChatColor.GRAY + "] ";

            Player player = null;
            if (args.length == 0) {
                if (sender instanceof Player) player = (Player)sender;
                else sender.sendMessage(prefix + ChatColor.RED + "Only players can fly");
            }
            else if (args.length == 1) {
                player = getServer().getPlayer(args[0]);
                if (player == null) sender.sendMessage(prefix + ChatColor.RED + "Player not found");
            }
            if (player == null) return true;

            player.setAllowFlight(!player.getAllowFlight());

            if (player.getAllowFlight()) player.sendMessage(Component.text(
                     prefix + ChatColor.GREEN + "enabled"
            ));
            else player.sendMessage(Component.text(
                    prefix + ChatColor.RED + "disabled"
            ));
        }

        if (command.getName().equalsIgnoreCase("flyspeed")) {
            final String prefix =
                    ChatColor.GRAY + "[" +
                    ChatColor.WHITE + "Fly" +
                    ChatColor.GRAY + "] " + ChatColor.WHITE;

            Player player = null;
            if (args.length == 1) {
                if (sender instanceof Player) player = (Player)sender;
                else sender.sendMessage(prefix + ChatColor.RED + "Only players can have a fly speed");
            }
            else if (args.length == 2) {
                player = getServer().getPlayer(args[1]);
                if (player == null) sender.sendMessage(prefix + ChatColor.RED + "Player not found");
            }
            int speed = Integer.parseInt(args[0]);
            if (player == null || speed <= 0) return true;
            if (speed > 10) {
                sender.sendMessage(prefix + ChatColor.RED + " cant be bigger than 10");
                return true;
            }

            player.setFlySpeed((float)speed/10);

            player.sendMessage(Component.text(
                    prefix + "speed set to " + speed
            ));
        }

        if (sender instanceof Player player) {
            if (command.getName().equalsIgnoreCase("damage")) {
                damage.put(player.getUniqueId(), !damage.getOrDefault(player.getUniqueId(), false));
                if (damage.get(player.getUniqueId())) player.sendMessage(Component.text(
                        ChatColor.GRAY + "[" +
                                ChatColor.WHITE + "Damage" +
                                ChatColor.GRAY + "] " +
                                ChatColor.GREEN + "enabled"
                ));
                else player.sendMessage(Component.text(
                        ChatColor.GRAY + "[" +
                                ChatColor.WHITE + "Damage" +
                                ChatColor.GRAY + "] " +
                                ChatColor.RED + "disabled"
                ));
            }

            if (command.getName().equalsIgnoreCase("invsee")) {
                if (args.length == 0) return true;
                Player other = getServer().getPlayer(args[0]);
                if (other != null) player.openInventory(other.getInventory());
            }

            if (command.getName().equalsIgnoreCase("vanish")) {
                vanish.put(player.getUniqueId(), !vanish.getOrDefault(player.getUniqueId(), false));
                if (vanish.get(player.getUniqueId())) {
                    player.sendMessage(Component.text(
                            ChatColor.GRAY + "[" +
                                    ChatColor.WHITE + "Vanish" +
                                    ChatColor.GRAY + "] " +
                                    ChatColor.GREEN + "enabled"
                    ));
                    getServer().getOnlinePlayers().forEach((other) -> {
                        other.hidePlayer(this, player);
                    });
                    if (args.length == 1 && args[0].equalsIgnoreCase("true")) {
                        getServer().broadcast(Component.text(
                                ChatColor.GRAY + "[" +
                                        ChatColor.RED + "-" +
                                        ChatColor.GRAY + "] " +
                                        ChatColor.WHITE + player.getName()
                        ));
                    }
                }
                else {
                    player.sendMessage(Component.text(
                            ChatColor.GRAY + "[" +
                                    ChatColor.WHITE + "Vanish" +
                                    ChatColor.GRAY + "] " +
                                    ChatColor.RED + "disabled"
                    ));
                    getServer().getOnlinePlayers().forEach((other) -> {
                        other.showPlayer(this, player);
                    });
                    if (args.length == 1 && args[0].equalsIgnoreCase("true")) {
                        getServer().broadcast(Component.text(
                                ChatColor.GRAY + "[" +
                                        ChatColor.GREEN + "+" +
                                        ChatColor.GRAY + "] " +
                                        ChatColor.WHITE + player.getName()
                        ));
                    }
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getServer().getOnlinePlayers().forEach((other) -> {
            Player player = event.getPlayer();
            if (vanish.get(other.getUniqueId()))
                player.hidePlayer(this, other);
        });
        event.joinMessage(Component.text(
                ChatColor.GRAY + "[" +
                        ChatColor.GREEN + "+" +
                        ChatColor.GRAY + "] " +
                        ChatColor.WHITE + event.getPlayer().getName()
        ));
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (vanish.get(event.getPlayer().getUniqueId())) vanish.remove(event.getPlayer().getUniqueId());
        event.quitMessage(Component.text(
                ChatColor.GRAY + "[" +
                        ChatColor.RED + "-" +
                        ChatColor.GRAY + "] " +
                        ChatColor.WHITE + event.getPlayer().getName()
        ));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            damage.forEach((uuid, on) -> {
                if (on) {
                    Player player = getServer().getPlayer(uuid);
                    if (player != null) {
                        player.sendMessage(Component.text(
                                player.getName() + " was damaged by " + event.getCause().name() +
                                        " (" + (double)((int)event.getFinalDamage())/2 + ")"
                        ));
                    }
                }
            });
        }
    }
}
