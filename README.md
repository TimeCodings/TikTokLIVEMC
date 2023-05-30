![TikTok_Live_Banner](https://user-images.githubusercontent.com/94994775/218834285-8d809227-ac19-452d-9972-d0e38e5232b6.png)

**Hey, thanks for viewing this page!** This plugin was a wish by @seb1211! Because there's actually no other plugin (for free) on the market which is able handle TikTok Donations in Minecraft, I decided to create this one for all of you for free :coffee:

Please note that this plugin is in BETA phase, means there may be few-many bugs in the plugin. If you find any, feel free to open a ticket on my discord server: https://discord.tikmc.de/

**Whats TikTokLive?**
TikTok Live allows you to connect your TikFinity account to Minecraft to perform various actions at various events (ATTENTION: currently only donations) in Minecraft!

**Features:**
- Change selected players
- Change Socket IP/Port/Password
- Add change gift-actions (for example commands, messages, actionbars, teleportations, ...)
- AntiSpam

**How to install:**
**1.** Click on this webpage on Download Now and open your Downloads folder
**2.** Open your Minecraft-Server-Folder and put the plugin from your Downloads in your plugins folder of your Minecraft-Server
**3.** Start/Restart your server
**4.** After your server got started the plugin should get activated. Type /plugins in your Console to see if the plugin is working (In the List which get printed out you should see a plugin named "TikTokLive").
(**5.** Open your TikTokLive folder which can be found in the plugins folder of you Minecraft-Server and open the config.yml. There you can change all settings how you want them)
**6.** Login/Register at TikFinity and click on "Actions & Events" at the left bar
**7.** Type /tiktoklive generate as a player with OP-Permissions or console. Now a link should be displayed. Click on the link and click on the button "Copy to clipboard"
**8.** On the opened TikFinity website click on "Create new Action", type in a name (it isn't important which is the name) and scroll down to "Trigger Webhook". Enable this setting with one click on the box and paste in the copied link (from Minecraft) in the left textbox.
**9.** On the webpage scroll down to "Save" and Click the Button
**10.** You can now define "Events" and configure them to execute a Minecraft-Gift-Action!'
**11.** You're ready! All you need to do is changing the gifts/actions in the config.yml to your preferences. If you need help feel free to ask on my discord: [https://discord.gg/8QWmU4ebCC](https://discord.tikmc.de/)

**Coming Soon / TODO:**
- A GUI for a easier setup
- Edit Code
- Fix Bugs
- Full Support for the Minecraft-Versions between 1.8.8 - 1.19.3
- Your Wishes (Write me your Ideas: https://discord.tikmc.de/)

**Commands and Permissions:**
/tiktoklive reload - Reloads the Configuration-Files and restarts the socket - Permission: tiktoklive.reload
/tiktoklive generate - Generates the basic URL which is needed to connect this plugin to TikFinity - Permission: tiktoklive.generate
/tiktoklive addselected/removeselected <Player> - Add/Remove a player to/from the selected player-list - Permission: tiktoklive.add.selected / tiktoklive.remove.selected
/tiktoklive help - Opens the help-menu - Permission: tiktoklive.help

**API:**
Coming Soon
