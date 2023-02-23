package dev.timecoding.tiktokspawn.socket;

import dev.timecoding.tiktokspawn.TikTokSpawn;
import dev.timecoding.tiktokspawn.data.ConfigHandler;
import dev.timecoding.tiktokspawn.data.GiftDataHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class TikTokSocket {

    private ServerSocket serverSocket;
    private Socket client;
    private PrintWriter outPut;
    private BufferedReader inPut;

    private TikTokSpawn plugin;
    private ConfigHandler configHandler;
    private GiftDataHandler giftDataHandler;
    private boolean connected = false;
    private Integer times = 0;
    private HashMap<String, String> valueList = new HashMap<>();

    public TikTokSocket(TikTokSpawn plugin){
        this.plugin = plugin;
        this.configHandler = this.plugin.getConfigHandler();
        this.giftDataHandler = this.plugin.getGiftDataHandler();
        this.plugin.setCurrentSocket(this);
        if(this.plugin.getConfigHandler().getBoolean("Socket.Legacy")){
            this.listenLegacy();
        }else{
            this.listen();
        }
    }

    public void listenLegacy(){
        if(!isConnected()) {
            connected = true;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    String ip = configHandler.getString("Socket.IP");
                    InetAddress InetAdrr = null;
                    try {
                        InetAdrr = InetAddress.getByName(ip);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        serverSocket = new ServerSocket(configHandler.getInteger("Socket.Port"), 0, InetAdrr);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        client = serverSocket.accept();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (client != null) {
                        try {
                            outPut = new PrintWriter(client.getOutputStream(), true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            inPut = new BufferedReader(new InputStreamReader(client.getInputStream()), 200 * 1024 * 1024);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        int i = 0;
                        while (true) {
                            try {
                                if (!inPut.ready()) break;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            i++;
                            String line = null;
                            try {
                                line = inPut.readLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (line == null) {
                                break;
                            } else if (i == 4 && line.startsWith("value1=")) {
                                decodeString(line);
                                if(!getValues().containsKey("giftId") && getValues().containsKey("content") || getValues().containsKey("giftId") && getValues().get("giftId").equalsIgnoreCase("") && getValues().containsKey("content")){
                                    if(getValues().containsKey("giftId")){
                                        getValues().remove("giftId");
                                    }
                                    getValues().put("giftId", getValues().get("content"));
                                }
                                if (getValues().get("password").equals(configHandler.getString("Socket.Password"))) {
                                    executeActions();
                                }
                            }
                        }
                        reconnectLegacy();
                    }
                }
            });
        }
    }

    private BukkitTask task = null;

    public void listen() {
        if(!isListening()) {
            this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL("https://timecoding.de/tikfinity/reader.php?ip=" + plugin.getPublicIPAddress() + "&port=" + configHandler.getInteger("Socket.Port") + "&password=" + configHandler.getString("Socket.Password") + "");
                        Scanner sc = new Scanner(url.openStream());
                        StringBuffer sb = new StringBuffer();
                        while (sc.hasNext()) {
                            sb.append(sc.next());
                        }
                        String result = sb.toString();
                        result = result.replaceAll("<[^>]*>", "");
                        for (String message : result.split(",")) {
                            System.out.println(message+" |");
                            if (!message.equalsIgnoreCase("")) {
                                decodeString(message);
                                if(!getValues().containsKey("giftId") && getValues().containsKey("content") || getValues().containsKey("giftId") && getValues().get("giftId").equalsIgnoreCase("") && getValues().containsKey("content")){
                                    if(getValues().containsKey("giftId")){
                                        getValues().remove("giftId");
                                    }
                                    getValues().put("giftId", getValues().get("content"));
                                }
                                executeActions();
                            }
                        }
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, this.configHandler.getInteger("Socket.RefreshInTicks"));
        }
    }

    public boolean isListening(){
        return (task != null);
    }

    public void disconnect(){
        if(isListening()){
            task.cancel();
            task = null;
        }
    }

    public void reconnect(){
        disconnect();
        listen();
    }

    public void executeActions(){
        for (String action : getActions()) {
            List<String> giftIdListFromAction = getValidGiftIDs(action);
            for (String giftId : giftIdListFromAction) {
                if (giftId.toString().equals(getValues().get("giftId").toString())) {
                    String path = "Actions." + action + ".";
                    for (Player selected : plugin.getSelectedPlayers()) {
                        executeConfigActions(selected, path, action);
                    }
                }
            }
        }
    }

    private HashMap<String, String> decodeString(String toDecode){
        List<String> valuesAndKeys = new ArrayList<String>(Arrays.asList(toDecode.split("&")));
        HashMap<String, String> decodedList = new HashMap<>();
        for(String valueAndKey : valuesAndKeys){
            String[] splitter = valueAndKey.split("=");
            String value = "";
            if(splitter.length > 1){
                value = splitter[1];
            }
            decodedList.put(splitter[0], value);
        }
        this.valueList = decodedList;
        return decodedList;
    }

    public HashMap<String, String> getValues(){
        return this.valueList;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnectLegacy(){
        if(isConnected()) {
            connected = false;
            if(inPut != null) {
                try {
                    inPut.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(outPut != null) {
                outPut.close();
            }
            if(client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void executeConfigActions(Player player, String path, String action){
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                String userId = getValues().get("userId");
                String username = getValues().get("username");
                String giftId = String.valueOf(getValues().get("giftId"));
                String giftAmount = getValues().get("value2");
                if(configHandler.keyExists(path+"Command")){
                    String command = replacePlaceholders(configHandler.getString(path+"Command"), player, getGiftNameByID(giftId), giftId, getCoinsByGiftID(giftId), giftAmount);
                    if(command.startsWith("/")){
                        command = command.substring(1, command.length());
                    }
                    if(configHandler.keyExists(path+"PerformAsConsole") && configHandler.getBoolean(path+"PerformAsConsole")){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }else{
                        player.performCommand(command);
                    }
                }
                if(configHandler.keyExists(path+"Message")){
                    String message = replacePlaceholders(configHandler.getString(path+"Message"), player, getGiftNameByID(giftId), giftId, getCoinsByGiftID(giftId), giftAmount);
                    player.sendMessage(message);
                }
                if(configHandler.keyExists(path+"Actionbar")){
                    String message = replacePlaceholders(configHandler.getString(path+"Actionbar"), player, getGiftNameByID(giftId), giftId, getCoinsByGiftID(giftId), giftAmount);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                }
                if(configHandler.keyExists(path+"Sound")){
                    player.playSound(player.getLocation(), getSoundByString(configHandler.getString(path+"Sound")), 2, 2);
                }
                if(configHandler.keyExists(path+"TeleportPlayer")){
                    Location modifiedLocation = getModifiedLocation(player.getLocation(), path+"TeleportPlayer");
                    player.teleport(modifiedLocation);
                }
                for(String spawnMobPath : getSpawnMobPathList(action)){
                    EntityType type = getEntityTypeByString(configHandler.getString(spawnMobPath+"Type"), spawnMobPath);
                    if(type != null){
                        Integer entityAmount = 1;
                        if(configHandler.keyExists(spawnMobPath+"Amount")){
                            entityAmount = configHandler.getInteger(spawnMobPath+"Amount");
                        }
                        Location modifiedLocation = player.getLocation();
                        if(configHandler.getBoolean(spawnMobPath+"SpawnDistance.Random.Enabled")){
                            Integer maxRadius = configHandler.getInteger(spawnMobPath+"SpawnDistance.Random.MaxRadius");
                            Random random = new Random();
                            Integer randomX = random.nextInt(maxRadius);
                            Integer randomZ = random.nextInt(maxRadius);
                            modifiedLocation = modifiedLocation.add(randomX, 0, randomZ);
                        }else{
                            modifiedLocation = getModifiedLocation(player.getLocation(), spawnMobPath+"SpawnDistance.");
                        }
                        Entity entity = player.getWorld().spawnEntity(modifiedLocation, type);
                        if(configHandler.keyExists(spawnMobPath+"Name")){
                            entity.setCustomName(replacePlaceholders(configHandler.getString(spawnMobPath+"Name"), player, getGiftNameByID(giftId), giftId, getCoinsByGiftID(giftId), giftAmount));
                        }
                        if(entity instanceof Tameable && configHandler.keyExists(spawnMobPath+"Tamed")) {
                            Tameable animalTamer = ((Tameable) entity);
                            animalTamer.setTamed(configHandler.getBoolean(spawnMobPath+"Tamed"));
                            animalTamer.setOwner(player);
                        }
                    }
                }
            }
        });
    }

    private EntityType getEntityTypeByString(String stringEntityType, String basePath){
        if(configHandler.keyExists(basePath+"RandomMobs") && configHandler.getBoolean(basePath+"RandomMobs.Enabled")) {
            if (configHandler.getBoolean(basePath+"RandomMobs.CompletelyRandom")) {
                Random random = new Random();
                Integer randomNumber = random.nextInt(EntityType.values().length);
                return EntityType.values()[randomNumber];
            } else if (configHandler.keyExists(basePath + "RandomMobs.List")) {
                List<String> randomMobStringList = configHandler.cfg.getStringList(basePath + "RandomMobs.List");
                List<EntityType> randomMobTypeList = new ArrayList<>();
                for (String randomMob : randomMobStringList) {
                    randomMobTypeList.add(EntityType.valueOf(randomMob.toUpperCase()));
                }
                Random random = new Random();
                Integer randomNumber = random.nextInt(randomMobTypeList.size());
                return randomMobTypeList.get(randomNumber);
            }
        }
        return EntityType.valueOf(stringEntityType.toUpperCase());
    }

    private Sound getSoundByString(String soundStringType){
        return Sound.valueOf(soundStringType.toUpperCase());
    }

    private Location getModifiedLocation(Location location, String path){
        String xCoordinateValue = this.configHandler.getString(path+".X");
        String yCoordinateValue = this.configHandler.getString(path+".Y");
        String zCoordinateValue = this.configHandler.getString(path+".Z");
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        if(!xCoordinateValue.equalsIgnoreCase("")){
            if(xCoordinateValue.startsWith("+")){
                x = x+Integer.valueOf(xCoordinateValue.replace("+", ""));
            }else if(xCoordinateValue.startsWith("-")){
                x = x-Integer.valueOf(xCoordinateValue.replace("-", ""));
            }
        }
        if(!yCoordinateValue.equalsIgnoreCase("")){
            if(yCoordinateValue.startsWith("+")){
                y = y+Integer.valueOf(yCoordinateValue.replace("+", ""));
            }else if(yCoordinateValue.startsWith("-")){
                y = y-Integer.valueOf(yCoordinateValue.replace("-", ""));
            }
        }
        if(!zCoordinateValue.equalsIgnoreCase("")){
            if(zCoordinateValue.startsWith("+")){
                z = z+Integer.valueOf(zCoordinateValue.replace("+", ""));
            }else if(xCoordinateValue.startsWith("-")){
                z = z-Integer.valueOf(zCoordinateValue.replace("-", ""));
            }
        }
        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }

    private String replacePlaceholders(String existsString, Player player, String giftName, String giftId, Integer giftCoins, String giftAmount){
        return existsString.replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%player_x%", String.valueOf(player.getLocation().getBlockX())).replace("%player_y%", String.valueOf(player.getLocation().getBlockY())).replace("%player_z%", String.valueOf(player.getLocation().getBlockZ()))
                .replace("%player_yaw%", String.valueOf(player.getLocation().getYaw())).replace("%player_pitch%", String.valueOf(player.getLocation().getPitch()))
                .replace("%gift_name%", notNullReplacer(giftName)).replace("%gift_id%", notNullReplacer(String.valueOf(giftId))).replace("%gift_coins%", notNullReplacer(String.valueOf(giftCoins))).replace("%gift_amount%", notNullReplacer(giftAmount))
                .replace("%gifter_name%", notNullReplacer(getValues().get("username"))).replace("%gifter_id%", notNullReplacer(getValues().get("userId")));
    }

    private String notNullReplacer(String existsString){
        if(existsString != null){
            return existsString;
        }
        return "";
    }

    public List<String> getSpawnMobPathList(String action){
        List<String> mobPaths = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        for(String keys : this.configHandler.getConfig().getValues(true).keySet()){
            String[] splitter = keys.split("\\.");
            if(splitter.length > 3){
                if(splitter[2].equalsIgnoreCase("SpawnMob") && splitter[1].equals(action)){
                    if(!idList.contains(splitter[3])){
                        idList.add(splitter[3]);
                        mobPaths.add("Actions."+action+"."+"SpawnMob."+splitter[3]+".");
                    }
                }
            }
        }
        return mobPaths;
    }

    public Integer getCoinsByGiftID(String giftId){
        String path = getGiftPath(giftId);
        if(path != null){
            return Integer.valueOf(path.split("\\.")[1]);
        }
        return 0;
    }

    private Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

    public List<String> getActions(){
        List<String> actions = new ArrayList<>();
        for(String keys : this.configHandler.getConfig().getValues(true).keySet()){
            if(keys.startsWith("Actions.") && !actions.contains(keys.split("\\.")[1])){
                actions.add(keys.split("\\.")[1]);
            }
        }
        return actions;
    }

    public List<String> getValidGiftIDs(String ids){
        List<String> mixedGiftList = new ArrayList<String>(Arrays.asList(ids.split(", ")));
        List<String> giftIdList = new ArrayList<>();
        for(String mixedValue : mixedGiftList){
            if(isInteger(mixedValue) || !isInteger(mixedValue) && mixedValue.contains("C")){
                String id = mixedValue;
                if(mixedValue.contains("C")){
                    for(String keys : this.giftDataHandler.getConfig().getValues(true).keySet()){
                        String actionBase = "GiftID."+id.toString().replace("C", "")+".";
                        if(keys.startsWith(actionBase)){
                            if(!giftIdList.contains(Integer.valueOf(keys.replace(actionBase, "")))) {
                                giftIdList.add(String.valueOf(Integer.valueOf(keys.replace(actionBase, ""))));
                            }
                        }
                    }
                }else if(giftExists(String.valueOf(Integer.valueOf(id)))){
                    if(!giftIdList.contains(id)) {
                        giftIdList.add(String.valueOf(Integer.valueOf(id)));
                    }
                }
            }else {
                Integer giftIDbyName = getGiftIDbyName(mixedValue);
                if(giftIDbyName != 0){
                    giftIdList.add(String.valueOf(giftIDbyName));
                }else{
                    giftIdList.add(mixedValue);
                }
            }
        }
        return giftIdList;
    }

    public Integer getGiftIDbyName(String name){
        for(String keys : this.giftDataHandler.getConfig().getValues(true).keySet()){
            if(keys.startsWith("GiftID.")){
                String value = this.giftDataHandler.getString(keys);
                if(value.equalsIgnoreCase(name)){
                    return Integer.valueOf(keys.split("\\.")[2]);
                }
            }
        }
        return 0;
    }

    public String getGiftNameByID(String id){
        String path = getGiftPath(id);
        if(path != null) {
            return this.giftDataHandler.getString(path);
        }
        return null;
    }

    public String getGiftPath(String id){
        if(giftExists(id)){
            for(String keys : this.giftDataHandler.getConfig().getValues(true).keySet()){
                if(keys.startsWith("GiftID.") && keys.endsWith("."+String.valueOf(id))){
                    return keys;
                }
            }
        }
        return null;
    }

    public boolean giftExists(String id){
        for(String keys : this.giftDataHandler.getConfig().getValues(true).keySet()){
            if(keys.startsWith("GiftID.") && keys.endsWith("."+String.valueOf(id))){
                return true;
            }
        }
        return false;
    }

    private boolean isInteger(String toProof){
        try {
            Integer.parseInt(toProof);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public void reconnectLegacy(){
        disconnectLegacy();
        Integer tiktokEvents = this.configHandler.getInteger("AntiSpam.TikTokEvents");
        Integer seconds = this.configHandler.getInteger("AntiSpam.MaxDistanceInSecBetweenEveryEvent");
        if(calendar.getTimeInMillis() >= System.currentTimeMillis() || times == 0) {
            if (this.configHandler.getBoolean("AntiSpam.Enabled")) {
                calendar.add(Calendar.SECOND, seconds);
                times++;
                if (times >= tiktokEvents) {
                    times = 0;
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                listenLegacy();
                            }
                        }, 20 * this.configHandler.getInteger("AntiSpam.Actions.DelayInSeconds"));
                } else {
                    listenLegacy();
                }
            } else {
                listenLegacy();
            }
        }else{
            listenLegacy();
        }
    }

}
