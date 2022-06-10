package me.terrorbyte.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.events.PlayerConversationListener;
import me.terrorbyte.honeypot.gui.GUI;
import me.terrorbyte.honeypot.gui.GUIMenu;
import me.terrorbyte.honeypot.gui.button.GUIButton;
import me.terrorbyte.honeypot.gui.item.GUIItemBuilder;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockObject;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import net.md_5.bungee.api.ChatColor;

public class HoneypotGUI extends HoneypotSubCommand{

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		if (!(p.hasPermission("honeypot.gui"))){
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			return;
		}

		GUIMenu mainMenu = Honeypot.gui.create("Honeypot Main Menu", 1);
		GUIItemBuilder createItem;
		GUIItemBuilder removeItem;
		GUIItemBuilder listItem;
		GUIItemBuilder locateItem;

		createItem = new GUIItemBuilder(Material.GREEN_CONCRETE);
		createItem.name("Create a Honeypot");

		removeItem = new GUIItemBuilder(Material.DIAMOND_PICKAXE);
		removeItem.name("Remove a Honeypot");

		listItem = new GUIItemBuilder(Material.BOOK);
		listItem.name("List all Honeypots");

		locateItem = new GUIItemBuilder(Material.SPYGLASS);
		locateItem.name("Locate nearby Honeypots");

		GUIButton createButton = new GUIButton(
				createItem.build()).withListener((InventoryClickEvent event) -> {
					try {
						createHoneypotInventory(p, args);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

		GUIButton removeButton = new GUIButton(
				// TODO: - Add support for near and all removals, as well as singular.
				removeItem.build()).withListener((InventoryClickEvent event) -> {
					Block block;
					if (!(p.hasPermission("honeypot.remove"))) {
						p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
						return;
					}

					if (event.getWhoClicked().getTargetBlockExact(5) != null) {
						block = ((Player) event.getWhoClicked()).getTargetBlockExact(5);
					} else {
						event.getWhoClicked().closeInventory();
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
						return;
					}

					if (block == null) {
						event.getWhoClicked().closeInventory();
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
						return;
					}

					event.getWhoClicked().closeInventory();
					if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
						HoneypotBlockStorageManager.deleteBlock(block);
						p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));
					} else {
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
					}

				});

		GUIButton listButton = new GUIButton(
				listItem.build()).withListener((InventoryClickEvent event) -> {
					try {
						allBlocksInventory(p, args);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		GUIButton locateButton = new GUIButton(locateItem.build())
				.withListener((InventoryClickEvent event) -> {
					event.getWhoClicked().closeInventory();
					if (!(p.hasPermission("honeypot.locate"))) {
						p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
						return;
					}

					// Set a 5 block search radius
					final double radius = 5d;
					final double xCoord = p.getLocation().getX();
					final double yCoord = p.getLocation().getY();
					final double zCoord = p.getLocation().getZ();
					boolean potFound = false;

					// For every x value within 5 blocks
					for (double x = xCoord - radius; x < xCoord + radius; x++) {
						// For every y value within 5 blocks
						for (double y = yCoord - radius; y < yCoord + radius; y++) {
							// For every z value within 5 blocks
							for (double z = zCoord - radius; z < zCoord + radius; z++) {

								// Check the block at coords x,y,z to see if it's a Honeypot
								final Block b = new Location(p.getWorld(), x, y, z).getBlock();

								// If it is a honeypot do this
								if (HoneypotBlockStorageManager.isHoneypotBlock(b)) {
									potFound = true;

									// Create a dumb, invisible, invulnerable, block-sized glowing slime and spawn
									// it inside the block
									Slime slime = (Slime) Objects
											.requireNonNull(Bukkit.getWorld(b.getWorld().getName()))
											.spawnEntity(b.getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
									slime.setSize(2);
									slime.setAI(false);
									slime.setGlowing(true);
									slime.setInvulnerable(true);
									slime.setHealth(4.0);
									slime.setInvisible(true);

									// After 5 seconds, remove the slime. Setting its health to 0 causes the death
									// animation, removing it just makes it go away. Poof!
									new BukkitRunnable() {

										@Override
										public void run() {
											slime.remove();
										}
									}.runTaskLater(Honeypot.getPlugin(), 20 * 5);
								}
							}
						}
					}
					
					if (potFound) {
						p.sendMessage(CommandFeedback.sendCommandFeedback("foundpot"));
					} else {
						p.sendMessage(CommandFeedback.sendCommandFeedback("nopotfound"));
					}
				});


		mainMenu.setButton(2, createButton);
		mainMenu.setButton(3, removeButton);
		mainMenu.setButton(5, listButton);
		mainMenu.setButton(6, locateButton);
		p.openInventory(mainMenu.getInventory());
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	private void allBlocksInventory(Player p, String[] args) throws IOException {
		GUIMenu allBlocksGUI = Honeypot.gui.create("Honeypots {currentPage}/{maxPage}", 3);
		
		for (HoneypotBlockObject honeypotBlock : HoneypotBlockStorageManager.getAllHoneypots()) {
			GUIItemBuilder item;


			if (Honeypot.guiConfig.getBoolean("display-button-as-honeypot")) {
				item = new GUIItemBuilder(honeypotBlock.getBlock().getType());
				item.lore("Click to teleport to Honeypot");
				item.name("Honeypot: " + honeypotBlock.getCoordinates());
			} else {
				item = new GUIItemBuilder(Material.getMaterial(Honeypot.guiConfig.getString("default-gui-button")));
				item.lore("Click to teleport to Honeypot");
				item.name("Honeypot: " + honeypotBlock.getCoordinates());
			}

			GUIButton button = new GUIButton(
				item.build()
			).withListener((InventoryClickEvent event) -> {
				event.getWhoClicked().sendMessage(ChatColor.ITALIC.toString() + ChatColor.GRAY.toString() + "Whoosh!");
				event.getWhoClicked().teleport(honeypotBlock.getLocation().add(0.5, 1, 0.5));
				event.getWhoClicked().closeInventory();
			});

			allBlocksGUI.addButton(button);
			
		}

		p.openInventory(allBlocksGUI.getInventory());
	}

	private void createHoneypotInventory(Player p, String[] args) throws IOException {
		if (!(p.hasPermission("honeypot.create"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			return;
		}

		GUIMenu createHoneypotGUI = Honeypot.gui.create("Create Honeypot", 1);

		GUIItemBuilder kickItem;
		GUIItemBuilder banItem;
		GUIItemBuilder warnItem;
		GUIItemBuilder notifyItem;
		GUIItemBuilder nothingItem;
		GUIItemBuilder customItem;

		kickItem = new GUIItemBuilder(Material.YELLOW_CONCRETE);
		kickItem.name("Kick");
		kickItem.lore("Click to create a 'kick' action");

		banItem = new GUIItemBuilder(Material.RED_CONCRETE);
		banItem.name("Ban");
		banItem.lore("Click to create a 'ban' action");

		warnItem = new GUIItemBuilder(Material.GREEN_CONCRETE);
		warnItem.name("Warn");
		warnItem.lore("Click to create a 'warn' action");

		notifyItem = new GUIItemBuilder(Material.OAK_SIGN);
		notifyItem.name("Notify");
		notifyItem.lore("Click to create a 'notify' action");

		nothingItem = new GUIItemBuilder(Material.BARRIER);
		nothingItem.name("Nothing");
		nothingItem.lore("Click to create a 'nothing' action");

		customItem = new GUIItemBuilder(Material.WRITTEN_BOOK);
		customItem.name("Custom Item");
		customItem.lore("Click to create a custom action (If enabled)");

		GUIButton kickButton = new GUIButton(
				kickItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "kick");
				});

		GUIButton banButton = new GUIButton(
				banItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "ban");
				});

		GUIButton warnButton = new GUIButton(
				warnItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "warn");
				});

		GUIButton notifyButton = new GUIButton(
				notifyItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "notify");
				});

		GUIButton nothingButton = new GUIButton(
				nothingItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "nothing");
				});

		GUIButton customButton = new GUIButton(
				customItem.build()).withListener((InventoryClickEvent event) -> {
					createHoneypotFromGUI(event, "custom");
				});

		createHoneypotGUI.setButton(1, kickButton);
		createHoneypotGUI.setButton(2, banButton);
		createHoneypotGUI.setButton(3, warnButton);
		createHoneypotGUI.setButton(5, notifyButton);
		createHoneypotGUI.setButton(6, nothingButton);
		createHoneypotGUI.setButton(7, customButton);
		p.openInventory(createHoneypotGUI.getInventory());

	}

	@SuppressWarnings("unchecked")
	private static void createHoneypotFromGUI(InventoryClickEvent event, String action){
		Block block;
			
		//Get block the player is looking at
		if(event.getWhoClicked().getTargetBlockExact(5) != null){
			 block = ((Player) event.getWhoClicked()).getTargetBlockExact(5);
		} else {
			event.getWhoClicked().closeInventory();
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
			return;
		}

		if (block == null){
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
			return;
		}

		if(Honeypot.config.getBoolean("filters.blocks") || Honeypot.config.getBoolean("filters.inventories")) {
			List<String> allowedBlocks = (List<String>) Honeypot.config.getList("allowed-blocks");
			List<String> allowedInventories = (List<String>) Honeypot.config.getList("allowed-inventories");
			boolean allowed = false;

			if (Honeypot.config.getBoolean("filters.blocks")){
				for (String blockType : allowedBlocks) {
					if (block.getType().name().equals(blockType)){
						allowed = true;
						break;
					}
				}
			}

			if (Honeypot.config.getBoolean("filters.inventories")){
				for (String blockType : allowedInventories) {
					if (block.getType().name().equals(blockType)){
						allowed = true;
						break;
					}
				}
			}

			if (!allowed){
				event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("againstfilter"));
				return;
			}
		}

		event.getWhoClicked().closeInventory();
		if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));
			
			//If it does not have a honeypot tag or the honeypot tag does not equal 1, create one
		} else {
			if(action.equalsIgnoreCase("custom")){
				if (Honeypot.config.getBoolean("enable-custom-actions")){
					if (!event.getWhoClicked().hasPermission("honeypot.custom")){
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
					} else {
						((Player) event.getWhoClicked()).sendTitle(ChatColor.AQUA + "Enter action", "Enter your custom action command (WITHOUT THE /) in chat. Type cancel to exit", 10, 60, 10);
						ConversationFactory cf = new ConversationFactory(Honeypot.getPlugin());
						Conversation conv = cf.withFirstPrompt(new PlayerConversationListener(block)).withLocalEcho(false).withEscapeSequence("cancel").addConversationAbandonedListener(new PlayerConversationListener(block)).withTimeout(10).buildConversation((Player) event.getWhoClicked());
						conv.begin();
					}
				} else {
					event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("customactionsdisabled"));
				}
			} else {
				HoneypotBlockStorageManager.createBlock(event.getWhoClicked().getTargetBlockExact(5), action);
				event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("success", true));
			}
		}
	}
	
}
