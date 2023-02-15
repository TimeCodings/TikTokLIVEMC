package dev.timecoding.tiktokspawn.command.completer;

import com.sun.istack.internal.NotNull;
import dev.timecoding.tiktokspawn.TikTokSpawn;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TikTokLiveCompleter implements TabCompleter {

    private TikTokSpawn plugin;

    public TikTokLiveCompleter(TikTokSpawn plugin){
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabCompleterList = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("tiktoklive") || command.getName().equalsIgnoreCase("ttl")){
            if(args.length == 1){
                if(sender.hasPermission("tiktoklive.reload")){
                    tabCompleterList.add("reload");
                }
                if(sender.hasPermission("tiktoklive.generate")){
                    tabCompleterList.add("generate");
                }
                if(sender.hasPermission("tiktoklive.add.selected")){
                    tabCompleterList.add("addselected");
                }
                if(sender.hasPermission("tiktoklive.remove.selected")){
                    tabCompleterList.add("removeselected");
                }
                if(sender.hasPermission("tiktoklive.help")){
                    tabCompleterList.add("help");
                }
            }else if(args.length == 2){
                if(args[0].equalsIgnoreCase("addselected") && sender.hasPermission("tiktoklive.add.selected")){
                    for(Player allOnline : Bukkit.getOnlinePlayers()){
                        if(!this.plugin.getSelectedPlayers().contains(allOnline)){
                            tabCompleterList.add(allOnline.getName());
                        }
                    }
                }else if(args[0].equalsIgnoreCase("removeselected") && sender.hasPermission("tiktoklive.remove.selected")){
                    for(Player allInList : this.plugin.getSelectedPlayers()){
                        tabCompleterList.add(allInList.getName());
                    }
                }
            }
        }
        return tabCompleterList;
    }
}
