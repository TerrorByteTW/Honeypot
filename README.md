# Honeypot Anti-Grief 🍯🚫

### [An important update to Honeypot](https://github.com/TerrorByteTW/Honeypot/issues/209)

<p align="center">
    <a href="https://jitpack.io/#TerrorByteTW/Honeypot">
        <img alt="Release" src="https://jitpack.io/v/TerrorByteTW/Honeypot.svg">
    </a>
    <a href="https://github.com/TerrorByteTW/Honeypot/actions/workflows/gradle.yml">
        <img alt="Java CI with Gradle" src="https://github.com/TerrorByteTW/Honeypot/actions/workflows/gradle.yml/badge.svg?branch=master">
    </a>
</p>


<p align="center">
    <a href="https://modrinth.com/plugin/honeypot">
        <img alt="Modrinth" src="https://img.shields.io/badge/Download%20at-Modrinth-brightgreen?style=for-the-badge&logo=modrinth">
    </a>
    <a href="https://hangar.papermc.io/TerrorByte/Honeypot">
        <img alt="Hangar" src="https://img.shields.io/badge/Download%20At-Hangar-%23f29f22?style=for-the-badge">
    </a>
</p>

![Info Slide 1](https://i.imgur.com/pFi4FMk.png)
![Info Slide 2](https://i.imgur.com/o1jEa1c.png)

***

## Current development status: Hobby Project ✅

Honeypot used to be something I worked on all the time. However, with my life and interests changing, I've decided to convert this to more of a "hobby project" I'll support whenever I want. See [this issue](https://github.com/TerrorByteTW/Honeypot/issues/209) for a more detailed explanation.

If there is an important bug report or security vulnerability found, I'll make time to fix it, but features are slower to develop. I can't guarantee Honeypot will work on the latest version of MC either. I'm open to accepting PRs or additional maintainers.

## Why was Honeypot created?

Honeypot is a customizable anti-grief plugin which allows any placeable block to be used as a trap for players looking
to grief. It can be used to catch Xray-ers, deter griefers from breaking unprotected builds, and more.
Honeypot plugins were popularized back in the early days of Minecraft servers but never really took off due to their
lack of customization, updates, or difficulty to configure, setup, and/or maintain. This plugin was designed to be
simple, lightweight, and easy to use for staff and admins.

**View a demo here (Up to date as of Honeypot v3.5.0!)**

[![Honeypot Demo](https://img.youtube.com/vi/mdFpaOktBuM/0.jpg)](https://www.youtube.com/watch?v=mdFpaOktBuM)

Need support? [Reach out on Discord](http://discord.gg/DpcdgTbPnU)!

## Compiling from source

Prerequisites:

- Java 21
- Gradle 9.3

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
please [see the Wiki](https://terrorbytetw.github.io/Honeypot-Docs/adding-functionality.html)!

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
