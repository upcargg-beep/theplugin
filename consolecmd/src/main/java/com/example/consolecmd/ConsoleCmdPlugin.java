package com.example.consolecmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ConsoleCmdPlugin extends JavaPlugin implements Listener {

    // The prefix players type in chat before their command
    private static final String TRIGGER = "#console ";

    // Feature can be toggled on/off in-game with /consoletoggle
    private boolean enabled = true;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ConsoleCmd enabled. Players with 'consolecmd.use' can type "
                + TRIGGER + "<command> in chat.");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        if (!message.startsWith(TRIGGER)) {
            return; // not our trigger, ignore — normal chat continues as usual
        }

        // Always cancel so the raw "#console ..." text never shows in public chat
        event.setCancelled(true);

        if (!enabled) {
            event.getPlayer().sendMessage(ChatColor.RED + "Console commands are currently disabled.");
            return;
        }

        if (!event.getPlayer().hasPermission("consolecmd.use")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return;
        }

        String command = message.substring(TRIGGER.length()).trim();
        if (command.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Usage: " + TRIGGER + "<command>  (e.g. " + TRIGGER + "give " + event.getPlayer().getName() + " diamond 1)");
            return;
        }

        final String playerName = event.getPlayer().getName();

        // Run on the main server thread — chat events fire async
        Bukkit.getScheduler().runTask(this, () -> {
            getLogger().info(playerName + " triggered console command: /" + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });

        event.getPlayer().sendMessage(ChatColor.GRAY + "[ConsoleCmd] Ran: " + ChatColor.YELLOW + "/" + command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("consoletoggle")) {
            if (args.length != 1 || !(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
                sender.sendMessage(ChatColor.RED + "Usage: /consoletoggle <on|off>");
                return true;
            }
            enabled = args[0].equalsIgnoreCase("on");
            sender.sendMessage(ChatColor.GREEN + "ConsoleCmd is now " + (enabled ? "ON" : "OFF") + ".");
            return true;
        }
        return false;
    }
}
