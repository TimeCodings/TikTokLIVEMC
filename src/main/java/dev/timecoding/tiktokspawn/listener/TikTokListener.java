package dev.timecoding.tiktokspawn.listener;

import dev.timecoding.tiktokspawn.TikTokSpawn;
import dev.timecoding.tiktokspawn.data.ConfigHandler;
import dev.timecoding.tiktokspawn.data.GiftDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TikTokListener implements Listener {

    private TikTokSpawn plugin;
    private List<Player> selectedPlayers = new ArrayList<>();
    private ConfigHandler configHandler;
    private GiftDataHandler giftDataHandler;

    public TikTokListener(TikTokSpawn plugin){
        this.plugin = plugin;
        this.selectedPlayers = this.plugin.getSelectedPlayers();
        this.configHandler = this.plugin.getConfigHandler();
        this.giftDataHandler = this.plugin.getGiftDataHandler();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(configHandler.getBoolean("Player.AllOnline") || configHandler.getBoolean("Player.FirstWhichJoins") && selectedPlayers.size() == 0){
            selectedPlayers.add(player);
        }else if(!configHandler.getBoolean("Player.AllOnline") && !configHandler.getBoolean("Player.FirstWhichJoins") && player.getName().equalsIgnoreCase(configHandler.getString("Player.OrName"))){
            selectedPlayers.add(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(selectedPlayers.contains(player)){
            selectedPlayers.remove(player);
        }
    }

}
