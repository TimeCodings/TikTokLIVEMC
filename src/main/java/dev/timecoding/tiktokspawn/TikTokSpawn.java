package dev.timecoding.tiktokspawn;

import dev.timecoding.tiktokspawn.api.Metrics;
import dev.timecoding.tiktokspawn.command.TikTokLiveCommand;
import dev.timecoding.tiktokspawn.command.completer.TikTokLiveCompleter;
import dev.timecoding.tiktokspawn.data.ConfigHandler;
import dev.timecoding.tiktokspawn.data.GiftDataHandler;
import dev.timecoding.tiktokspawn.listener.TikTokListener;
import dev.timecoding.tiktokspawn.socket.TikTokSocket;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class TikTokSpawn extends JavaPlugin {

    private ConfigHandler configHandler;
    private GiftDataHandler giftDataHandler;
    private TikTokSocket currentSocket;
    private List<Player> selectedPlayers = new ArrayList<>();
    private Metrics metrics;
    private ConsoleCommandSender consoleCommandSender = this.getServer().getConsoleSender();

    @Override
    public void onEnable() {
        this.configHandler = new ConfigHandler(this);
        this.configHandler.init();
        if(this.configHandler.getBoolean("Enabled")) {
        if(this.configHandler.getString("Socket.IP").equalsIgnoreCase("127.0.0.1")){
            this.configHandler.setString("Socket.IP", getPublicIPAddress());
        }
        this.giftDataHandler = new GiftDataHandler(this);
        this.giftDataHandler.init();
        this.consoleCommandSender.sendMessage("§dTikTok§fLive §av"+this.getDescription().getVersion()+" §egot §aenabled!");
        TikTokSocket socket = new TikTokSocket(this);
        this.currentSocket = socket;
        PluginCommand tikTokCommand = getCommand("tiktoklive");
        tikTokCommand.setExecutor(new TikTokLiveCommand(this));
        tikTokCommand.setTabCompleter(new TikTokLiveCompleter(this));
        getServer().getPluginManager().registerEvents(new TikTokListener(this), this);
        if(configHandler.getBoolean("Player.AllOnline") || configHandler.getBoolean("Player.FirstWhichJoins") && selectedPlayers.size() == 0){
            for(Player allOnline : Bukkit.getOnlinePlayers()){
                selectedPlayers.add(allOnline);
            }
        }else if(!configHandler.getBoolean("Player.AllOnline") && !configHandler.getBoolean("Player.FirstWhichJoins") && Bukkit.getOnlinePlayers().size() != 0 && Bukkit.getOnlinePlayers().stream().toList().get(0).getName().equalsIgnoreCase(configHandler.getString("Player.OrName"))){
            selectedPlayers.add(Bukkit.getOnlinePlayers().stream().toList().get(0));
        }
        if(this.configHandler.getBoolean("bStats")){
            this.metrics = new Metrics(this,17647);
        }
        }else{
            consoleCommandSender.sendMessage("§cThe plugin got disabled, because someone disabled the plugin in the config.yml!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        this.consoleCommandSender.sendMessage("§dTikTok§fLive §av"+this.getDescription().getVersion()+" §egot §cdisabled!");
        if(this.currentSocket != null){
            this.currentSocket.disconnect();
        }
    }

    private String getPublicIPAddress(){
        try {
            URL amazonIpServer = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(amazonIpServer.openStream()));
            String ip = in.readLine();
            return ip;
        } catch (MalformedURLException e) {
            return "127.0.0.1";
        } catch (IOException e) {
            return "127.0.0.1";
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public GiftDataHandler getGiftDataHandler() {
        return giftDataHandler;
    }

    public List<Player> getSelectedPlayers() {
        return selectedPlayers;
    }

    public TikTokSocket getCurrentSocket() {
        return currentSocket;
    }

    public void setCurrentSocket(TikTokSocket currentSocket) {
        this.currentSocket = currentSocket;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
}
