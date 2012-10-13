package com.ammaraskar.bukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandItem extends JavaPlugin implements Listener, CommandExecutor {
    
    static String DEFAULT_MESSAGE = ChatColor.RED + "You need to be holding %item% to use that command";
    Map<String, String> commands = new HashMap<String, String>(); // Maps command -> command message
    
    public void onEnable() {
        saveDefaultConfig();

        getCommand("commanditem").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        
        loadConfig(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().substring(1, event.getMessage().length()).split(" ");

        if (args.length > 0) {
            if ((getConfig().contains("commands." + args[0])) && (event.getPlayer().getItemInHand().getTypeId() != getConfig().getInt("commands." + args[0]))) {
                if (commands.get(args[0]) != null) {
                    event.getPlayer().sendMessage(formatString(commands.get(args[0]), getConfig().getInt(new StringBuilder("commands.") + args[0])));
                } else {
                    event.getPlayer().sendMessage(formatString(DEFAULT_MESSAGE, getConfig().getInt(new StringBuilder("commands.") + args[0])));
                }
                event.setCancelled(true);
            }
        }
    }
    
    public String formatString (String string, int id) {
        return ChatColor.translateAlternateColorCodes('&', string.replace("%item%", Material.getMaterial(id).toString().toLowerCase().replace("_", " ")));
    }
    
    public void loadConfig(CommandSender sender) {
        for (String key : getConfig().getKeys(true)) {
            if (key.startsWith("commands.")) {
                if (Material.getMaterial(getConfig().getInt(key)) == null || key.length() < 9) {
                    if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                        sender.sendMessage("Ignoring invalid id or key at '" + key + "' value of '" + getConfig().get(key) + "'");
                    }
                    getLogger().warning("Ignoring invalid id or key at '" + key + "' value of '" + getConfig().get(key) + "'");
                    continue;
                }
                String command = key.substring(9);
                if (getConfig().getString("messages." + command) != null) { 
                    commands.put(command, getConfig().getString("messages." + command));
                } else {
                    commands.put(command, null);
                }
                continue;
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((args.length == 1) && (args[0].equalsIgnoreCase("reload") && sender.hasPermission("commanditem.admin"))) {
            this.reloadConfig();
            this.loadConfig(sender);
            sender.sendMessage(ChatColor.AQUA + "Reloaded!");
            return true;
        }
        return false;
    }
}