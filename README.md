# Honeypot Anti-Grief Solution

## Why was Honeypot created?
Honeypot is a customizable anti-grief plugin which allows any placeable block to be used as a trap for players looking to grief. It can be used to catch Xray-ers, deter griefers from breaking unprotected builds, and more.
Honeypot plugins were popularized back in the early days of Minecraft servers but never really took off due to their lack of customization, updates, or difficulty to configure, setup, and/or maintain. This plugin was designed to be simple, lightweight, and easy to use for staff and admins.

## Goals
These are the goals for Honeypot. Not all of these have been achieved yet, but I think I'm on the right track.
* **Lightweight.** I wanted this Honeypot plugin to be lightweight. I'm still working on optimizing the code but so far so good.
* **Easy to update.** The code in this plugin uses really basic Java and Bukkit/Spigot APIs, so it's not too overly complicate to update.
* **Easy to build upon.** I made the Honeypot block manager classes super easy to use and add functionality to. I designed the Command Manager to be extensible, enabling me to add commands and subcommands at any time without too many major updates to the code or base classes. It also has the added benefit of making it easier for other developers to fork and add on to. 

So far we've met those goals, but are working on adding more features to the plugin and continuously optimizing as time goes on.

## What's the current state of development?
This is a hobby project; Nonetheless, I'm hoping this plugin gets some use out of it and will continue to be developed for some time by me.

## To-do
* Add custom actions to honeyblocks (Want to teleport them to the void? Want to charge them a griefing fee? These are all things I want to do).
* Increase speed of database lookups
* Add the ability to whitelist or blacklist exemptions based on permissions
* Add ability to store in different database types (SQLite, H2, etc.)
* Add block filtering to avoid checking every single BlockBreakEvent for a honeypot (This would limit the type of honeypots you can create, however)

## Credits
TerrorByte aka redstonefreak589 is the sole developer of this plugin. He maintains the entire codebase, however any forks, pull-requests, etc. are welcomed!
Google Gson is also used as a "database" of sorts to persistently store Honeypot blocks and players who break them in JSON files and load them to RAM at plugin startup for quicker lookups.
