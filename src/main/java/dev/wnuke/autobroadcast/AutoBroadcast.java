package dev.wnuke.autobroadcast;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoBroadcast extends JavaPlugin {

    public static final String VERSION = "1.0.0";
    public static List<Map<?, ?>> messages;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("autobc").setExecutor(new AutoBroadcastCommand());
        messages = this.getConfig().getMapList("messages");
        this.getConfig().set("messages", messages);
        this.saveConfig();
        getLogger().info("Loaded Auto Broadcast version " + VERSION + " by wnuke.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloaded Auto Broadcast version " + VERSION + " by wnuke.");
    }

    private class AutoBroadcastCommand implements CommandExecutor {
        private static final String MISSINGPERMISSION = "You do not have permission to use that command.";

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "list":
                        if (sender.hasPermission("autobc.list")) {
                            sender.sendMessage("List of automatic messages:");
                            for (Map<?, ?> messageMap : messages) {
                                for (Map.Entry<?, ?> message : messageMap.entrySet()) {
                                    sender.sendMessage("  - " + message.getKey() + ": \"" + message.getValue().toString() + "\"");
                                }
                            }
                        } else {
                            sender.sendMessage(MISSINGPERMISSION);
                        }
                        break;
                    case "add":
                        if (sender.hasPermission("autobc.add")) {
                            if (args.length == 4) {
                                if (NumberUtils.isNumber(args[3])) {
                                    Map<String, String[]> newMessage = new HashMap<>();
                                    newMessage.put(args[1], new String[]{args[2], args[3]});
                                    messages.add(newMessage);
                                    getConfig().set("messages", messages);
                                    saveConfig();
                                    sender.sendMessage("Added message " + args[1] + " with contents \"" + args[2] + "\" and a delay of " + args[3]);
                                } else {
                                    sender.sendMessage("Invalid argument " + args[3] + ", please make sure this is a number.");
                                }
                            } else {
                                sender.sendMessage("Invalid arguments, correct syntax is /autobc add <messageName> <message> <delay>");
                            }
                        } else {
                            sender.sendMessage(MISSINGPERMISSION);
                        }
                        break;
                    case "del":
                        if (sender.hasPermission("autobc.del")) {
                            if (args.length == 2) {
                                messages.remove(messages.indexOf(args[1]));
                                getConfig().set("messages", messages);
                                saveConfig();
                                sender.sendMessage("Removed message " + args[1]);
                            } else {
                                sender.sendMessage("Invalid arguments, correct syntax is /autobc del <messageName>");
                            }
                        } else {
                            sender.sendMessage(MISSINGPERMISSION);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Invalid argument: " + args[0]);
                }
            }
            return true;
        }
    }
}
