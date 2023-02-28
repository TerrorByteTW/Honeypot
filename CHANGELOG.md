# Change notes will now be listed here from here on out as well as in the release notes
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
