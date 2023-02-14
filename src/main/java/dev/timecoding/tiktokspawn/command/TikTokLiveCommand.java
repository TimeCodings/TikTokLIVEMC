package dev.timecoding.tiktokspawn.command;

import dev.timecoding.tiktokspawn.TikTokSpawn;
import dev.timecoding.tiktokspawn.socket.TikTokSocket;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TikTokLiveCommand implements CommandExecutor {

    private TikTokSpawn plugin;

    public TikTokLiveCommand(TikTokSpawn plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("tiktoklive.reload")){
                sender.sendMessage("§aThe plugin was successfully reloaded!");
                this.plugin.getConfigHandler().reload();
                this.plugin.getGiftDataHandler().reload();
                if(this.plugin.getConfigHandler().getBoolean("Enabled")) {
                    this.plugin.getCurrentSocket().disconnectLegacy();
                    this.plugin.getCurrentSocket().disconnect();
                    this.plugin.setCurrentSocket(new TikTokSocket(this.plugin));
                }else{
                    Bukkit.getConsoleSender().sendMessage("§cThe plugin got disabled, because someone disabled the plugin in the config.yml!");
                    Bukkit.getPluginManager().disablePlugin(this.plugin);
                }
            }else if(args[0].equalsIgnoreCase("generate") && sender.hasPermission("tiktoklive.generate")){
                sender.sendMessage("§aFirstly, thanks for installing my plugin! §eTo register this plugin in TikFinity, create a new action under \"Actions & Events\" in which you activate \"Webhooks\" and insert the following link: ");
                if(this.plugin.getConfigHandler().getBoolean("Socket.Legacy")){
                    sender.sendMessage("§fhttps://timecoding.de/tikfinity/legacy.php?ip="+this.plugin.getConfigHandler().getString("Socket.IP")+"&port="+this.plugin.getConfigHandler().getString("Socket.Port")+"&password="+this.plugin.getConfigHandler().getString("Socket.Password"));
                }else{
                    sender.sendMessage("§fhttps://timecoding.de/tikfinity/connector.php?ip="+this.plugin.getConfigHandler().getString("Socket.IP")+"&port="+this.plugin.getConfigHandler().getString("Socket.Port")+"&password="+this.plugin.getConfigHandler().getString("Socket.Password"));
                }
            }else if(sender.hasPermission("tiktoklive.help")){
                sender.sendMessage("");
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§c/tiktoklive reload §f- §eReloads the Configuration-Files and restarts the socket");
                sender.sendMessage("§c/tiktoklive generate §f- §eGenerate the basic URL which is needed to connect this plugin to TikFinity");
                sender.sendMessage("§c/tiktoklive setselected/removeselected §f- §eAdd/Remove a player to/from the selected player-list");
                sender.sendMessage("§c/tiktoklive help §f- §eOpens the help-menu");
                sender.sendMessage("");
            }else{
                sender.sendMessage("§cYou do not have the permission to use this command!");
            }
        }else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("addselected") && sender.hasPermission("tiktoklive.add.selected")){
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if(targetPlayer != null){
                    if(!this.plugin.getSelectedPlayers().contains(targetPlayer)){
                        sender.sendMessage("§aThe player §e"+targetPlayer.getName()+" §awas successfully added to the selected persons!");
                        this.plugin.getSelectedPlayers().add(targetPlayer);
                    }else{
                        sender.sendMessage("§cThis player is already selected!");
                    }
                }else{
                    sender.sendMessage("§cThis player isn't online right now!");
                }
            }else if(args[0].equalsIgnoreCase("removeselected") && sender.hasPermission("tiktoklive.remove.selected")){
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if(this.plugin.getSelectedPlayers().contains(targetPlayer)){
                        sender.sendMessage("§aThe player §e"+targetPlayer.getName()+" §awas successfully §cremoved §afrom the selected persons!");
                        this.plugin.getSelectedPlayers().remove(targetPlayer);
                    }else{
                        sender.sendMessage("§cThis player is isn't selected right now!");
                    }
            }else if(sender.hasPermission("tiktoklive.help")){
                sender.sendMessage("");
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§c/tiktoklive reload §f- §eReloads the Configuration-Files and restarts the socket");
                sender.sendMessage("§c/tiktoklive generate §f- §eGenerate the basic URL which is needed to connect this plugin to TikFinity");
                sender.sendMessage("§c/tiktoklive setselected/removeselected §f- §eAdd/Remove a player to/from the selected player-list");
                sender.sendMessage("§c/tiktoklive help §f- §eOpens the help-menu");
                sender.sendMessage("");
            }else{
                sender.sendMessage("§cYou do not have the permission to use this command!");
            }
        }else if(sender.hasPermission("tiktoklive.help")){
                sender.sendMessage("");
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§c/tiktoklive reload §f- §eReloads the Configuration-Files and restarts the socket");
                sender.sendMessage("§c/tiktoklive generate §f- §eGenerate the basic URL which is needed to connect this plugin to TikFinity");
                sender.sendMessage("§c/tiktoklive setselected/removeselected §f- §eAdd/Remove a player to/from the selected player-list");
                sender.sendMessage("§c/tiktoklive help §f- §eOpens the help-menu");
                sender.sendMessage("");
        }else{
            sender.sendMessage("§cYou do not have the permission to use this command!");
        }

        return false;
    }
}
