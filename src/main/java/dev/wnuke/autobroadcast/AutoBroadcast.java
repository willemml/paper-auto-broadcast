package dev.wnuke.autobroadcast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AutoBroadcast extends JavaPlugin {

    public static final String VERSION = "1.0.0";
    public static final String MESSAGESJSON = "messages.json";
    public static Map<String, String[]> messages = new HashMap<>();
    public static ArrayList<AutoBroadcaster> broadcasters = new ArrayList<>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        this.getCommand("autobc").setExecutor(new AutoBroadcastCommand());
        try {
            messages = gson.fromJson(new FileReader(MESSAGESJSON), new TypeToken<Map<String, String[]>>(){}.getType());
            loadBroadcasters();
        } catch (FileNotFoundException e) {
            try {
                writeMessagesJson();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                getLogger().warning("Failed to create messages json.");
                onDisable();
                return;
            }
        }
        getLogger().info("Loaded Auto Broadcast version " + VERSION + " by wnuke.");
    }

    public void loadBroadcasters() {
        broadcasters.clear();
        for (Map.Entry<String, String[]> message : messages.entrySet()) {
            if (NumberUtils.isNumber(message.getValue()[1])) {
                broadcasters.add(new AutoBroadcaster(Integer.parseInt(message.getValue()[1]), message.getValue()[0]));
            } else {
                getLogger().warning("Message " + message.getKey() + " has an invalid delay.");
            }
        }
    }

    public void writeMessagesJson() throws IOException {
        FileWriter fw = new FileWriter(MESSAGESJSON);
        gson.toJson(messages, fw);
        fw.flush();
        fw.close();
    }

    @Override
    public void onDisable() {
        broadcasters.clear();
        getLogger().info("Disabled Auto Broadcast version " + VERSION + " by wnuke.");
    }

    private class AutoBroadcaster extends BukkitRunnable implements dev.wnuke.autobroadcast.AutoBroadcaster {
        private long startTime = 0;
        private int delay = 0;
        private String message = "";
        public AutoBroadcaster(int newDelay, String newMessage) {
            delay = newDelay;
            message = newMessage;
        }

        @Override
        public void run() {
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            if (startTime + (delay * 1000 * 60) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
                startTime = System.currentTimeMillis();
                getServer().broadcastMessage(message);
            }
        }
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
                            for (Map.Entry<String, String[]> message : messages.entrySet()) {
                                sender.sendMessage("  - " + message.getKey() + ": \"" + message.getValue()[0] + "\" with delay " + message.getValue()[1]);
                            }
                        } else {
                            sender.sendMessage(MISSINGPERMISSION);
                        }
                        break;
                    case "add":
                        if (sender.hasPermission("autobc.add")) {
                            if (args.length == 4) {
                                if (NumberUtils.isNumber(args[3])) {
                                    try {
                                        messages.put(args[1], new String[]{args[2], args[3]});
                                        writeMessagesJson();
                                        loadBroadcasters();
                                        sender.sendMessage("Added message " + args[1] + " with contents \"" + args[2] + "\" and a delay of " + args[3]);
                                    } catch (IOException e) {
                                        sender.sendMessage("Failed to add message, could not write file.");
                                        e.printStackTrace();
                                    }
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
                            if (args.length > 1) {
                                if (messages.containsKey(args[1])) {
                                    messages.remove(args[1]);
                                    try {
                                        writeMessagesJson();
                                    } catch (IOException e) {
                                        sender.sendMessage("Failed to remove message, could not write file.");
                                        e.printStackTrace();
                                    }
                                } else {
                                    sender.sendMessage("Could not find message " + args[1]);
                                }
                            } else {
                                sender.sendMessage("Invalid arguments, correct syntax is /autobc del <messageName>");
                            }
                        } else {
                            sender.sendMessage(MISSINGPERMISSION);
                        }
                        break;
                    case "reload":
                        try {
                            messages = gson.fromJson(new FileReader(MESSAGESJSON), new TypeToken<Map<String, String[]>>(){}.getType());
                            loadBroadcasters();
                            sender.sendMessage("Successfully reloaded messages list");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            sender.sendMessage("Failed to reload messages list");
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
