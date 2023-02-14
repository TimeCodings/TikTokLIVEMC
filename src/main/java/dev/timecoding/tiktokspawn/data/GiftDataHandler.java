package dev.timecoding.tiktokspawn.data;

import dev.timecoding.tiktokspawn.TikTokSpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiftDataHandler {

    private TikTokSpawn plugin;

    public GiftDataHandler(TikTokSpawn plugin) {
        this.plugin = plugin;
    }

    private File f = null;
    public YamlConfiguration cfg = null;

    private boolean retry = false;

    private String newconfigversion = "1.0";

    public void init() {
        f = new File(plugin.getDataFolder(), "giftlist.yml");
        if(!f.exists()){
            plugin.saveResource("giftlist.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(f);
        cfg.options().copyDefaults(true);
        checkForUpdate();
    }

    public String getPluginVersion() {
        return plugin.getDescription().getVersion();
    }

    public void save() {
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNewestConfigVersion() {
        return this.newconfigversion;
    }

    public boolean configUpdateAvailable() {
        if (!getNewestConfigVersion().equalsIgnoreCase(getString("version")))
            return true;
        return false;
    }

    public void reload() {
        f = new File(plugin.getDataFolder(), "giftlist.yml");
        cfg = YamlConfiguration.loadConfiguration(f);
    }

    public Integer getItemSlot(String key){
        return getInteger("Item."+key+".Slot");
    }

    public void checkForUpdate() {
        if (configUpdateAvailable()) {
            final Map<String, Object> quicksave = getConfig().getValues(true);
            File file = new File("plugins//TikTokLive", "giftlist.yml");
            if (file.exists()) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "A newer version of the giftlist.yml was found! (" + getNewestConfigVersion() + ") Updating the Giftlist...");
                Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, new Runnable() {

                    public void run() {
                        plugin.saveResource("giftlist.yml", true);
                        reload();
                        for (String save : quicksave.keySet()) {
                            if (keyExists(save) && quicksave.get(save) != null && !save.equalsIgnoreCase("version")) {
                                getConfig().set(save, quicksave.get(save));
                            }
                        }
                        save();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "The giftlist.yml got updated!");
                    }
                }, 50);
            }
        }
    }


    private List<String> getStringList(String key, String tag){
        if(keyExists(key)){
            List<String> list = getConfig().getStringList(key);
            list.replaceAll(msg -> msg.replace("&", "§").replace("%tag%", tag));
            return list;
        }
        return new ArrayList<>();
    }

    public Material getMaterialByString(String material){
        for(Material mats : Material.values()){
            if(mats.name().toLowerCase().equalsIgnoreCase(material.toLowerCase())){
                return Material.valueOf(material);
            }
        }
        return Material.GRASS_BLOCK;
    }

    public Enchantment getEnchantmentByString(String enchant){
        for(Enchantment ench : Enchantment.values()){
            if(ench.toString().toLowerCase().equalsIgnoreCase(enchant.toLowerCase())){
                return Enchantment.getByName(enchant);
            }
        }
        return Enchantment.ARROW_DAMAGE;
    }

    public YamlConfiguration getConfig(){
        return cfg;
    }

    public void setString(String key, String value){
        cfg.set(key, value);
        save();
    }

    public Integer getInteger(String key){
        if(keyExists(key)){
            return cfg.getInt(key);
        }
        return 1;
    }

    public String getString(String key){
        if(keyExists(key)){
            return ChatColor.translateAlternateColorCodes('&', cfg.getString(key));
        }
        return "";
    }

    public Boolean getBoolean(String key){
        if(keyExists(key)){
            return cfg.getBoolean(key);
        }
        return false;
    }

    public boolean keyExists(String key){
        if(cfg.get(key) != null){
            return true;
        }
        return false;
    }
}
