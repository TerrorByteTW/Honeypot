# Change notes will now be listed here from here on out as well as in the release notes
## Honeypot 2.6.1
This release is a hotfix for a bug found in Honeypot which affects versions 2.5.x - 2.6.0 and is recommend for all users.
A bug was found which, if abused, could allow players to teleport wherever they wanted without needing permissions. The bug works by abusing an internal command used for the Player History GUI menu. This command was not registered with the server so players were unable to see it in the tab-complete menu, however it *was* used as a placeholder to bypass needing /minecraft:tp permissions for staff in order to teleport them to where a history event took place. However, due to not writing the event listener responsible for handling this command properly, the command bypassed permission checks altogether.

Fix: A new permission, `honeypot.teleport`, was introduced to prevent abuse of this command. Please ensure if you want your staff to be able to teleport to the location of a Honeypot history item, that they have this permission. My apologies for any issues this may have caused.

## Honeypot 2.6.0
### Core

Additions:

* Added InventoryClickEvent. This new event type will trigger Honeypot actions if an inventory is opened & manipulated, instead of just opened.

Changes:

* Config.yml version is now 13 to accomodate new settings
* InventoryClickEvent and PlayerInteractEvent are now mutally exclusive. One will not load if the other is loaded. This is determined by the `use-inventory-click` value in config. A server restart is required for change to take effect if `use-inventory-click` is changed.
* Fixed a bug where custom actions would not work on PlayerInteractEvents. Apparently I forgot to migrate that portion of code to the new Custom Action types introduced in 2.1.0. My mistake!
* PlayerInteractEventListener and InventoryClickEventListener are only registered if necessary, reducing overhead. This means your server and Honeypot should be faster, even if ever so slightly :)

Removals:

* None

Known Bugs:

* None at the moment. If you see any bugs, please be sure to report them!

### API

Additions:

* Added two new events, HoneypotPreInventoryClickEvent and HoneypotInventoryClickEvent.

Removals:

* Removed deprecated Storage Manager constructors. They were removed from the core plugin, but forgotten in the API, which would've caused some nasty runtime issues if used. Sorry Devs! :P
