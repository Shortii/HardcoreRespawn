package me.Noogs.HardcoreRespawn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.world.item.Item;

public class Main extends JavaPlugin implements Listener {

	public Inventory inv;
	Location chestLocation;
	// item array to be checked
	ArrayList<Material> items = new ArrayList<Material>() {
		{
			add(Material.NETHER_STAR);
			add(Material.TOTEM_OF_UNDYING);
		}
	};

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		createInv();
	}

	@Override
	public void onDisable() {

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// RESPAWN COMMAND
		if (label.equalsIgnoreCase("respawn")) {
			// CONSOLE
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cConsole cannot run this command!"));
			}
			// PLAYER
			else {
				Player player = (Player) sender;
				player.getOpenInventory().getTopInventory().clear();
				updateInv();
				if (inv.getViewers().size() > 0) {
					player.sendMessage("Someone is currently using the respawn menu");
				} else {
					player.openInventory(inv);
				}

			}
		}
		return false;
	}

	@EventHandler()
	public void onClick(InventoryClickEvent event) {
		boolean shift = false;
		if (!event.getInventory().equals(inv)) {
			return;
		}
		if (event.getCurrentItem() == null) {
			return;
		}
		if (event.getCurrentItem().getItemMeta() == null) {
			return;
		}
		if (event.getCurrentItem().getItemMeta().getDisplayName() == null) {
			return;
		}
		if (event.isShiftClick()) {
			shift = true;
			event.setCancelled(true);
		}
		event.setCancelled(true);

		Player player = (Player) event.getWhoClicked();

		if (event.getSlot() == 26 && !shift) {
			player.closeInventory();
		}

		Player respawnPlayer = null;
		if (event.getCurrentItem().getType() == Material.PLAYER_HEAD && !shift) {
			respawnPlayer = Bukkit.getPlayer(event.getCurrentItem().getItemMeta().getDisplayName());
		} else {
			return;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			String current_Player = p.getName().toString();
			String respawn_Player = respawnPlayer.getName().toString();

			if (current_Player == respawn_Player) {
				// check for items here
				if (hasItems(player, items)) {
					removeItems(player, items, 1);
					respawnPlayer.teleport(player);
					respawnPlayer.setGameMode(GameMode.SURVIVAL);
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
							"&a&l" + respawn_Player + " has been revived by " + player.getName().toString()));
					player.closeInventory();
				}
			}
		}
		return;
	}

	public void createInv() {
		inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&a&lRespawn Menu"));
		updateInv();
	}

	public void updateInv() {
		inv.clear();
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		ItemStack item = new ItemStack(Material.DIRT);
		SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();

		int index = 0;
		// loop through server player list
		for (Player player : Bukkit.getOnlinePlayers()) {
			// check if player is spectator
			if (player.getGameMode() == GameMode.SPECTATOR) {
				// individual player
				skullMeta.setDisplayName(player.getName());
				skullMeta.setOwningPlayer(Bukkit.getPlayer(player.getName()));
				head.setItemMeta(skullMeta);

				skullMeta.setDisplayName(player.getName());
				lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to respawn " + player.getName()));
				skullMeta.setLore(lore);
				head.setItemMeta(skullMeta);
				inv.setItem(index, head);
				lore.clear();

				index++;
			}
		}
		
		// cost of respawn
		item.setType(Material.NETHER_STAR);
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Amount needed to respawn: 1 " ));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(24, item);
		lore.clear();
		
		item.setType(Material.TOTEM_OF_UNDYING);
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Amount needed to respawn: 1 " ));
		meta.setLore(lore);
		item.setItemMeta(meta);
		lore.clear();
		inv.setItem(23, item);
		
		// how to use menu note
		item.setType(Material.PAPER);
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lHow to use the menu "));
		lore.add(ChatColor.translateAlternateColorCodes(
				'&', "&7Click on a player head to respawn them." ));
		lore.add(ChatColor.translateAlternateColorCodes(
				'&', "&7The cost to respawn is listed on the right of this note." ));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(21, item);

		// close button
		item.setType(Material.BARRIER);
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lClose Menu"));
		lore.clear();
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(26, item);
	}

	public boolean hasItems(Player p, ArrayList<Material> items) {
		int amount = 0;
		for (int i = 0; i < items.size(); i++) {
			if (p.getInventory().contains(items.get(i))) {
				amount++;
			}
		}

		if (amount == items.size()) {
			return true;
		}

		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lMissing items to respawn"));
		return false;
	}

	public void removeItems(Player p, ArrayList<Material> items, int amount) {
		for (int i = 0; i < items.size(); i++) {
			if (amount <= 0) {
				return;
			}
			
			int size = p.getInventory().getSize();
			for (int slot = 0; slot < size; slot++) {
				ItemStack invSlot = p.getInventory().getItem(slot);
				if (invSlot == null)
					continue;
				if (items.get(i) == invSlot.getType()) {
					int newAmount = invSlot.getAmount() - amount;
					if (newAmount >= 0) {
						invSlot.setAmount(newAmount);
						break;
					}
				}
			}
		}
	}
}