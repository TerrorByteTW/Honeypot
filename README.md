# Honeypot Anti-Grief Solution 🍯🚫 

## What is Honeypot?
Honeypot Anti-Grief Solution is a Minecraft Spigot server plugin which is designed to catch bad actors and punish them in the act. The goal is to catch them before damage is done, especially if staff doesn't realize it's happening or are unable to intervene. Its job is to minimize damage and make less of a mess for staff to clean up later on. 

## Why was Honeypot created?
Honeypot is a customizable anti-grief plugin which allows any placeable block to be used as a trap for players looking to grief. It can be used to catch Xray-ers, deter griefers from breaking unprotected builds, and more.
Honeypot plugins were popularized back in the early days of Minecraft servers but never really took off due to their lack of customization, updates, or difficulty to configure, setup, and/or maintain. This plugin was designed to be simple, lightweight, and easy to use for staff and admins.

**View a demo here**

[![Honeypot Demo](https://img.youtube.com/vi/Ff2Ju91CgxA/0.jpg)](https://www.youtube.com/watch?v=Ff2Ju91CgxA)

## Goals
These are the goals for Honeypot. Not all of these have been achieved yet, but I think I'm on the right track.
* **👀 Privacy focused.** This plugin collects no data or statistics.
* **💪 Lightweight.** I wanted this Honeypot plugin to be lightweight. I'm still working on optimizing the code but so far so good.
* **😄 Easy to update.** The code in this plugin uses really basic Java and Bukkit/Spigot APIs, so it's not too overly complicate to update.
* **🔨 Easy to build upon.** I made the Honeypot block manager classes super easy to use and add functionality to. I designed the Command Manager to be extensible, enabling me to add commands and subcommands at any time without too many major updates to the code or base classes. It also has the added benefit of making it easier for other developers to fork and add on to. 

So far we've met those goals, but are working on adding more features to the plugin and continuously optimizing as time goes on.

## Servers using Honeypot 💻
If your server is using Honeypot, let me know! I'd love to add it to this list.

## What's the current state of development? ▶
This is a hobby project; Nonetheless, I'm hoping this plugin gets some use out of it and will continue to be developed for some time by me. Development is done at the pace in which I have time to do so, but it is currently **in progress** ▶

## To-do 📝
* Add custom actions to honeyblocks *(Researching)*
* Add block filtering to avoid checking every single BlockBreakEvent for a honeypot (This would limit the type of honeypots you can create, however) *(On Hold)*

## Credits 🎬
TerrorByte aka redstonefreak589 is the sole developer of this plugin. He maintains the entire codebase, however any forks, pull-requests, etc. are welcomed!
Google Gson is also used as a "database" of sorts to persistently store Honeypot blocks and players who break them in JSON files and load them to RAM at plugin startup for quicker lookups.

## Note 📒
Honeypot Anti-Grief Solution is not designed to replace land protection plugins such as [Lands](https://www.spigotmc.org/resources/lands-land-claim-plugin-grief-prevention-protection-gui-management-nations-wars-1-17-support.53313/), [WorldGuard](https://dev.bukkit.org/projects/worldguard), [GriefPrevention](https://www.spigotmc.org/resources/griefprevention.1884/), [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/), etc. Please make sure you have another protection and rollback plugin to prevent mass griefing. Honeypot is just meant to keep honest people honest and to catch bad actors without moderators having to constantly monitor players. I actually recommend CoreProtect and the paid Lands plugin, as I used them in production servers and they are fantastic! Honeypot is a perfect plugin to compliment them both.
