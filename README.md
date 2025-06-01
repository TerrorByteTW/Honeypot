# Honeypot Anti-Grief 🍯🚫

### [An important update to Honeypot](https://github.com/TerrorByteTW/Honeypot/issues/209)

<p align="center">
    <a href="https://jitpack.io/#TerrorByteTW/Honeypot">
        <img alt="Release" src="https://jitpack.io/v/TerrorByteTW/Honeypot.svg">
    </a>
    <a href="https://github.com/TerrorByteTW/Honeypot/actions/workflows/gradle.yml">
        <img alt="Java CI with Gradle" src="https://github.com/TerrorByteTW/Honeypot/actions/workflows/gradle.yml/badge.svg?branch=master">
    </a>
    <a href="https://sonarcloud.io/summary/new_code?id=TerrorByteTW_Honeypot">
        <img alt="Maintainability Rating" src="https://sonarcloud.io/api/project_badges/measure?project=TerrorByteTW_Honeypot&metric=sqale_rating">
    </a>
</p>


<p align="center">
    <a href="https://modrinth.com/plugin/honeypot">
        <img alt="Modrinth" src="https://img.shields.io/badge/Download%20at-Modrinth-brightgreen?style=for-the-badge&logo=modrinth">
    </a>
    <a href="https://polymart.org/resource/honeypot-anti-grief.2756">
        <img alt="Polymart" src="https://img.shields.io/badge/Download%20At-Polymart-%2303a092?style=for-the-badge">
    </a>
    <a href="https://hangar.papermc.io/TerrorByte/Honeypot">
        <img alt="Hangar" src="https://img.shields.io/badge/Download%20At-Hangar-%23f29f22?style=for-the-badge">
    </a>
</p>

![Info Slide 1](https://i.imgur.com/pFi4FMk.png)
![Info Slide 2](https://i.imgur.com/o1jEa1c.png)

***

## Current development status: Paused 🚫

I didn't really want to do this, but I'm pausing Development on Honeypot and will only work it when I have time and actually want to. My very limited freetime is spent working 
on projects I want to work on, and Honeypot just don't fall into this category anymore. I work a full-time job, my personal life is extremely hectic, and I also don't play Minecraft 
anymore. While I'd love to put more time into Honeypot and other projects, I just don't have the ability to do so. 

If there is an important bug report or security vulnerability found, I'll make time to fix it, but features are slower to develop. I can't guarantee Honeypot will work on the latest version of MC either. I'm open to accepting PRs or additional maintainers.

## Why was Honeypot created?

Honeypot is a customizable anti-grief plugin which allows any placeable block to be used as a trap for players looking
to grief. It can be used to catch Xray-ers, deter griefers from breaking unprotected builds, and more.
Honeypot plugins were popularized back in the early days of Minecraft servers but never really took off due to their
lack of customization, updates, or difficulty to configure, setup, and/or maintain. This plugin was designed to be
simple, lightweight, and easy to use for staff and admins.

**View a demo here (*Very* outdated 😅)**

[![Honeypot Demo](https://img.youtube.com/vi/M58d5X3NpP0/0.jpg)](https://www.youtube.com/watch?v=M58d5X3NpP0)

Need support? [Reach out on Discord](http://discord.gg/DpcdgTbPnU)!

## Compiling from source

Prerequisites:

- Java 17 (You may compile with Java 18, but Java 17 is what Minecraft servers 1.18+ are required to use. I'd compile
  for that version since most servers will be using Java 17)
- Gradle 8.0.2+ (I use 8.6 as of writing this, but from testing I know at least 8.0.2+ works)

1. Clone the repository. This will clone the master branch. If you wish to pull development branches, specify that
   accordingly

```bash
$ git clone https://github.com/TerrorByteTW/Honeypot.git
```

2. Install Dependencies, Compile & Package

```bash
$ ./gradlew build
```

## Developers

If you are looking to develop using the Honeypot API,
please [see the Wiki](https://github.com/TerrorByteTW/Honeypot/wiki/Developing-plugins-using-the-Honeypot-API)!

## Credits 🎬

Big thanks to dejvokep for
their [BoostedYAML](https://www.spigotmc.org/threads/%E2%9A%A1-boostedyaml-standalone-yaml-library-with-updater-and-comment-support-much-more-5min-setup-%E2%9A%A1.545585/)
library, and the Spigot community for all their tips and tricks!

Also, a big thanks to SamJakob, who develops [SpiGUI](https://github.com/SamJakob/SpiGUI) which allows me to provide the beautiful GUI's in Honeypot that you see today!

## Note 📒

Honeypot Anti-Grief Solution is not designed to replace plugins such
as [Lands](https://www.spigotmc.org/resources/lands-land-claim-plugin-grief-prevention-protection-gui-management-nations-wars-1-17-support.53313/), [WorldGuard](https://dev.bukkit.org/projects/worldguard), [GriefPrevention](https://www.spigotmc.org/resources/griefprevention.1884/), [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/),
etc. Please make sure you have another protection and rollback plugin to prevent mass griefing. Honeypot is just meant
to keep honest people honest and to catch bad actors without moderators having to constantly monitor players. I highly
recommend CoreProtect and GriefPrevention, but if you have the money to spend, Lands is an awesome plugin as well.

This plugin is currently written for Minecraft 1.18+ and is compiled using Java 17. If you are not running Java 17 on your
server, or using Minecraft 1.17(.x) or earlier, this plugin is not for you.
